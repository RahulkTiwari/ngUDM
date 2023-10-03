# Third party libs
import importlib
from pymongo import ReplaceOne
import json
import requests
from requests.auth import HTTPBasicAuth
# Custom libs
from modules.logger import main_logger
from modules.data_sources import datasources_cache
from modules.config import config_object
from modules.generic_functions import get_offset_date, evaluate_regex, check_notice, clean_string
from modules.mongo_connection import database_con
from modules import run_args as ra


def get_information_from_bad_api_request(reply):
    if reply.status_code == 400:
        try:
            # in case of new tickets
            reply_error = json.loads(reply.text)["errors"]
        except KeyError:
            # in case of updates to existing tickets
            reply_error = json.loads(reply.text)["errorMessages"]
    elif reply.status_code == 401:
        reply_error = json.loads(reply.text)["errorMessages"]
    elif reply.status_code == 404:
        try:
            # in case of new tickets
            reply_error = json.loads(reply.text)["message"]
        except KeyError:
            # in case of updates to existing tickets
            reply_error = json.loads(reply.text)["errorMessages"]
    else:
        reply_error = ""

    return f'status_code: {reply.status_code}, errors: {reply_error}'


def create_service_desk_ticket(notice_list):
    main_logger.debug('Posting request to service desk...')
    service_desk_config = config_object['SERVICE_DESK']
    payload = json.dumps({
        'issueUpdates': notice_list
    })

    response = requests.request(
       'POST',
       f'{service_desk_config["JIRA_URL"]}/rest/api/2/issue/bulk',
       data=payload,
       headers={
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
       auth=HTTPBasicAuth(service_desk_config['JIRA_USERNAME'], service_desk_config['JIRA_TOKEN'])
    )

    if not str(response.status_code).startswith('2'):
        main_logger.warning(f'Failed to create tickets due to: {response.reason}. Error: {get_information_from_bad_api_request(response)}')
        return False
    else:
        main_logger.info('Service desk tickets successfully created')
        return True


def set_service_desk_json(notification):
    service_desk_config = config_object['SERVICE_DESK']

    sd_json = {
        'update': {},
        'fields': {
            'project': {
                'key': service_desk_config['JIRA_PROJECT_KEY']
            },
            'summary': clean_string(notification.eventSubject)[:255],
            'issuetype': {
                'name': service_desk_config['JIRA_ISSUE_TYPE']
            },
            'description': f'{notification.eventSubject} \n\n{notification.eventInitialUrl}',
            'customfield_10030': '247',  # Request Type default to EN (247)
            'assignee': {'accountId': datasources_cache[notification.exchangeSourceName].exchange_owner},
            'reporter': {'accountId': service_desk_config['JIRA_REPORTER']},
            'customfield_10251': {'accountId': datasources_cache[notification.exchangeSourceName].exchange_owner},  # Primary exchange owner
            'customfield_10031': [{'accountId': user} for user in datasources_cache[notification.exchangeSourceName].ops_analysts],
            'customfield_10242': notification.eventPublishDate.strftime('%Y-%m-%d'),
            'customfield_10243': notification.insDate.strftime('%Y-%m-%d'),
            'customfield_10282': notification.exchangeReferenceId,
            'customfield_10283': notification.eventInitialUrl,
            'customfield_10274': datasources_cache[notification.exchangeSourceName].exchange_group_name,
            'customfield_10275': notification.exchangeSourceName
        }
    }

    return sd_json


def parser_wrapper(exchange_obj):

    bulk_update = []
    service_desk_bulk_insert = []
    notice_batch_cache = []
    batch_count = 0
    batch_size = 10
    as_of_date = get_offset_date()

    exchange_module = importlib.import_module(f'exchange_parsers.{exchange_obj.exchange}')
    for each_notice in exchange_obj.notice_table:
        try:
            notice = exchange_module.set_values(each_notice)
        except Exception as ex:
            main_logger.error(f'Failed to process notice due to {ex.__str__()}')
            continue

        # Break and continue in case the table row doesn't contain valid data
        # TODO: make more robust
        if not notice.valid_notice:
            if notice.raise_warning and notice.eventPublishDate >= as_of_date:
                main_logger.warning(f'Failed to process notice:\n {each_notice}')
            continue

        main_logger.debug(f'Evaluating notice {notice.exchangeReferenceId}')

        match_regex = evaluate_regex(exchange_obj.exchange, notice.eventSubject)

        if notice.eventPublishDate >= as_of_date and match_regex:

            find_query = {'stormKey.RDU.value': notice.stormKey}

            already_processed = check_notice(find_query)
            if already_processed or notice.noticeSourceUniqueId in notice_batch_cache:
                main_logger.debug(f'Capturing {notice.exchangeReferenceId} has already been processed')
                continue

            main_logger.info(f'Processing notice {notice.exchangeReferenceId}')
            notice_batch_cache.append(notice.noticeSourceUniqueId)

            mongo_update = ReplaceOne(find_query, notice.notice_to_json(), upsert=True)
            bulk_update.append(mongo_update)
            service_desk_json = set_service_desk_json(notice)
            service_desk_bulk_insert.append(service_desk_json)
            batch_count += 1

            if batch_count >= batch_size:
                success_request = False
                if ra.create_jiras:
                    success_request = create_service_desk_ticket(service_desk_bulk_insert)
                if success_request or not ra.create_jiras:
                    database_con.get_collection('workItem').bulk_write(bulk_update)
                batch_count = 0
                service_desk_bulk_insert = []
                bulk_update = []
                notice_batch_cache = []
        else:
            main_logger.debug(f'Skipping notice {notice.exchangeReferenceId}, {clean_string(notice.eventSubject)}')

    if len(bulk_update) > 0:
        success_request = False
        if ra.create_jiras:
            success_request = create_service_desk_ticket(service_desk_bulk_insert)
        if success_request or not ra.create_jiras:
            database_con.get_collection('workItem').bulk_write(bulk_update)
