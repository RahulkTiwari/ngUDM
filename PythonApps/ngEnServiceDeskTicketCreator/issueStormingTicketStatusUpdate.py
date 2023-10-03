# Third party libraries
import logging
from requests.auth import HTTPBasicAuth
import requests
import json
from pathlib import Path
import datetime
# Custom libraries
from libraries.objects.WorkItemsStorm import WorkItemStormCache
from libraries.service_desk_logger import Logger
from libraries.caches.config import config_object
from libraries.caches.database_connection import database_connection


def query_jira_status(jira_id):

    config = config_object['JIRA']

    url = f'{config["JIRA_URL"]}/rest/api/2/issue/{jira_id}'
    auth = HTTPBasicAuth(config['JIRA_USERNAME'], config['JIRA_TOKEN'])

    # Build the request
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }

    response = requests.request(
        'GET',
        url,
        headers=headers,
        auth=auth
    )

    response_json = json.loads(response.text)

    if str(response.status_code).startswith('2'):
        Logger.logger.info(f'JIRA ID QUERIED: {jira_id}. Response code from Service Desk REST API {response.status_code}, '
                           f'Ticket status: {response_json["fields"]["customfield_10030"]["currentStatus"]["status"]}.')
        return response_json['fields']['customfield_10030']['currentStatus']['status']
    else:
        Logger.logger.warning(f'JIRA ID QUERIED FAILED TO BE RETURNED: {jira_id}.Response code from Service Desk REST API {response.status_code}, '
                              f'Response text from Service Desk REST API {response.text}.')
        return 'Unknown'


def inactivate_work_item(ticket_id):

    col = database_connection['workItem']

    js_data_unique = {
        'workItemId.RDU.value': ticket_id
    }
    js_data = {
        'workItemStatus.RDU.value.normalizedValue': 'I',
        'updDate.RDU.value': datetime.datetime.now(),
        'updUser.RDU.value': 'EN Workflow',
    }

    col.update_one(js_data_unique, {'$set': js_data})


def inactivate_work_item_ref(storm_key):

    col = database_connection['enData']

    js_data_unique = {
        'enWorkItemReference.stormKey.RDU.value': storm_key
    }
    js_data = {
        'enWorkItemReference.$.workItemStatus.RDU.value.normalizedValue': 'I'
    }

    col.update_many(js_data_unique, {'$set': js_data})


def update_log_handler():

    log_config = config_object['LOG']

    log_dir = log_config['log_file_path']

    try:
        Path(log_dir).mkdir(parents=True, exist_ok=False)
    except FileExistsError:
        pass

    log_date = (str(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')).replace(' ', '')).replace(':', '')
    log_file = f'{log_dir}/log_status_update_{log_date}.log'
    handler = logging.FileHandler(log_file)
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    handler.setFormatter(formatter)
    Logger.logger.setLevel(getattr(logging, log_config['log_level'].upper()))
    Logger.logger.addHandler(handler)

    Logger.logger.info(f'Logs will be written to file {log_file} with log level {log_config["log_level"].upper()}.')


def main():
    update_log_handler()

    Logger.logger.info('Processing started for inactivating EN workItemIds')

    work_item_docs = WorkItemStormCache()

    for each_work_item in work_item_docs.work_item_objects:
        jira_item = query_jira_status(each_work_item.work_item_id)
        status = jira_item.upper()
        if status == 'CLOSED':
            inactivate_work_item(each_work_item.work_item_id)
            inactivate_work_item_ref(each_work_item.storm_key)
            Logger.logger.info(f'Inactivated workItemId: {each_work_item.work_item_id}, with stormKey: {each_work_item.storm_key}')


if __name__ == '__main__':

    try:
        main()
    except Exception as ex:
        Logger.logger.fatal(ex, exc_info=True)

    Logger.logger.info('Run for inactivating EN workItemIds completed succesfully.')
