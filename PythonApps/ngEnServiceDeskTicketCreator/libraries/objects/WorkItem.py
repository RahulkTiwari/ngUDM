# Third party libraries
import requests
from requests.auth import HTTPBasicAuth
import json
import re
# Custom libraries
from libraries.generic_functions import get_string_value, get_date_value, get_domain_value, get_value_from_array
from libraries.static_vars import GLOBAX_REMOVE_STRINGS, EXCHANGES_WO_REF_ID, ERROR_CODES, VALID_INSTRUMENT_TYPES
from libraries.caches.domain_cache import asset_classification_cache, event_type_cache
from libraries.caches.datasources_cache import exchange_data
from libraries.caches.database_connection import database_connection
from libraries.caches.config import config_object
from libraries.service_desk_logger import Logger
from libraries.objects.Exchange import Exchange, get_ops_user

# Set globals to this module
CONFIG = config_object['JIRA']
HEADERS = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
}
AUTH = HTTPBasicAuth(CONFIG['JIRA_USERNAME'], CONFIG['JIRA_TOKEN'])
BASE_URL = f'{CONFIG["JIRA_URL"]}/rest/api/2/issue'


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


def get_exchange_data_by_code(exchange_code):
    if exchange_code in exchange_data:
        return exchange_data.exchanges[exchange_data.index - 1]
    else:
        jira_config = config_object['JIRA']

        default_exchange = Exchange()
        default_exchange.exchange_group_name = 'default'
        default_exchange.exchange_owner = get_ops_user(jira_config['JIRA_FALLBACK_OWNER_EMAIL'])
        default_exchange.ops_analysts = [get_ops_user(jira_config['JIRA_FALLBACK_OWNER_EMAIL'])]

        return default_exchange


def cleansed_globex_subject(globex_subject):
    cleansed_subject = globex_subject
    for string_to_strip_out in GLOBAX_REMOVE_STRINGS:
        cleansed_subject = cleansed_subject.replace(string_to_strip_out, '')

    return cleansed_subject


def string_hash(string):
    hash_value = 0
    for character in string:
        hash_value = (hash_value*281 ^ ord(character)*997) & 0xFFFFFFFF

    return str(hash_value)


def clean_subject(raw_subject):
    """
    Method to:
    - replace line breaks with pipes (OCC scenario)
    - removes the subject prefix strings (CMEG Globex scenario)
    :param raw_subject: input string
    :return: cleansed subject
    :rtype: str
    """
    return cleansed_globex_subject(raw_subject.replace('\n', '|'))


class WorkItem:

    def __init__(self, en_data_notice):
        # Work item platform attributes
        self.issue_status = 'OPEN'
        self.issue_type = 'EN NOTIFICATION'
        self.event_source_unique_id = get_string_value(en_data_notice, 'eventSourceUniqueId')
        self.work_item_id = None
        self.work_item_type = 'New'
        # Notification attributes
        self.insert_date = get_date_value(en_data_notice, 'eventInsertDate') \
            if get_date_value(en_data_notice, 'eventInsertDate') not in ERROR_CODES else '9999-12-31'
        self.publish_date = get_date_value(en_data_notice, 'eventPublishDate') \
            if get_date_value(en_data_notice, 'eventPublishDate') not in ERROR_CODES else '9999-12-31'
        self.event_effective_date = get_date_value(en_data_notice, 'eventEffectiveDate')
        self.event_initial_url = get_string_value(en_data_notice, 'eventInitialUrl', trim=None).replace(' ', '%20')
        self.event_subject = clean_subject(get_string_value(en_data_notice, 'eventSubject'))
        self.event_summary = get_string_value(en_data_notice, 'eventSummaryText', trim=None) \
            if get_string_value(en_data_notice, 'eventSummaryText') != 'default' else ''
        self.event_type = get_domain_value(en_data_notice, 'eventType')
        self.exchange_group_name = get_exchange_data_by_code(get_domain_value(en_data_notice, 'exchangeSourceName')).exchange_group_name
        self.exchange_prefix = re.sub(r'(\s|\n)+', '_', get_string_value(en_data_notice, 'exchangePrefix'))
        self.exchange_reference_id = get_string_value(en_data_notice, 'exchangeReferenceId', trim=None)
        self.exchange_source_name = get_domain_value(en_data_notice, 'exchangeSourceName')
        self.norm_event_type = get_domain_value(en_data_notice, 'eventType', event_type_cache)
        self.norm_instrument_type = get_domain_value(en_data_notice, 'instrumentTypeCode', asset_classification_cache) if \
            get_domain_value(en_data_notice, 'instrumentTypeCode', asset_classification_cache) in VALID_INSTRUMENT_TYPES else 'errorCode101'
        self.previous_references = get_value_from_array(en_data_notice, 'eventPreviousReferenceIds.exchangePreviousReferenceId')
        self.product_name = re.sub(r'(\s|\n)+', '_', get_string_value(en_data_notice, 'productName'))
        self.underlying_names = get_value_from_array(en_data_notice, 'underlyings.nameLong')
        self.underlying_tickers = get_value_from_array(en_data_notice, 'underlyings.exchangeTicker')
        # Operational attributes
        self.exchange_owner = get_exchange_data_by_code(self.exchange_source_name).exchange_owner
        self.exchange_owners = [{'accountId': d['account_id']} for d in get_exchange_data_by_code(self.exchange_source_name).ops_analysts]
        self.hash_update = False
        self.previous_storm_hash = None
        self.storm_key = self.get_storm_key(en_data_notice)
        self.storm_hash = string_hash(f'eventSubject:{self.event_subject} effectiveDate:{str(self.event_effective_date)}')
        # Jsons
        self.create_work_item_json = None
        self.update_work_item_json = None
        self.work_item_transition_json = None

    def __str__(self):
        return self.event_source_unique_id

    def make_service_desk_request(self, request_type='update'):

        request_params_by_type_map = {
            'update': {
                'url': f'{BASE_URL}/{self.work_item_id}',
                'api_method': 'PUT',
                'request_body': self.update_work_item_json,
                'log': 'update'
            },
            'transition': {
                'url': f'{BASE_URL}/{self.work_item_id}/transitions',
                'api_method': 'POST',
                'request_body': self.work_item_transition_json,
                'log': 'update'
            },
            'create': {
                'url': BASE_URL,
                'api_method': 'POST',
                'request_body': self.create_work_item_json,
                'log': 'create'
            }
        }

        response = requests.request(
            request_params_by_type_map[request_type]['api_method'],
            request_params_by_type_map[request_type]['url'],
            data=request_params_by_type_map[request_type]['request_body'],
            headers=HEADERS,
            auth=AUTH
        )

        if not str(response.status_code).startswith('2'):
            Logger.logger.warning(f'Failed to {request_params_by_type_map[request_type]["log"]} Jira {self.work_item_id} '
                                  f'due to: {response.reason}. Error: {get_information_from_bad_api_request(response)}')
        else:
            try:
                # Create includes the key in the response
                item_id = json.loads(response.text)["key"]
            except (KeyError, json.decoder.JSONDecodeError):
                # Update doesn't return the key, hence taking from the input work item
                item_id = self.work_item_id
            Logger.logger.info(f'Jira with id {item_id} has been {request_params_by_type_map[request_type]["log"]}d.')

        return response

    def create(self):

        self.set_create_work_item_json()
        api_response = self.make_service_desk_request(request_type='create')
        if str(api_response.status_code).startswith('2'):
            self.work_item_id = json.loads(api_response.text)['key']
        # If request fails due to an error on the exchange owner then retry request with the default owner.
        elif 'customfield_10251' in get_information_from_bad_api_request(api_response) or \
                'customfield_10031' in get_information_from_bad_api_request(api_response):
            Logger.logger.warning(f'Attempting request with fallback assignee for storm key {self.storm_key}.')
            create_work_item_json_real = json.loads(self.create_work_item_json)
            create_work_item_json_real['fields']['assignee'] = {'accountId': CONFIG['JIRA_FALL_BACK_EXCH_OWNER']}
            create_work_item_json_real['fields']['customfield_10251'] = {'accountId': CONFIG['JIRA_FALL_BACK_EXCH_OWNER']}
            create_work_item_json_real['fields']['customfield_10031'] = [{'accountId': CONFIG['JIRA_FALL_BACK_EXCH_OWNER']}]
            self.create_work_item_json = json.dumps(create_work_item_json_real)
            api_response = self.make_service_desk_request(request_type='create')
            self.work_item_id = json.loads(api_response.text)['key'] if str(api_response.status_code).startswith('2') else None
        if self.work_item_id is not None:
            self.update_endata()

    def update(self):

        self.set_update_work_item_json()
        _api_response = self.make_service_desk_request(request_type='update')
        if self.hash_update:
            self.set_work_item_transition_json()
            self.make_service_desk_request(request_type='transition')
        self.update_endata()

    def get_storm_key(self, notice):

        # In absence of ref id for CMEG GLOBEX use subject in the storm key. For other cases the ref id
        ref_id_or_subject = get_string_value(notice, 'exchangeReferenceId', trim=None) if \
            get_domain_value(notice, 'exchangeSourceName', lock_level=['FEED', 'RDU']) not in EXCHANGES_WO_REF_ID else \
            cleansed_globex_subject(get_string_value(notice, 'eventSubject', lock_level=['FEED', 'RDU']))

        self.storm_key = '|'.join([
            get_domain_value(notice, 'eventType', lock_level=['FEED', 'RDU']).split('|')[0],
            str(get_date_value(notice, 'eventEffectiveDate', lock_level=['FEED', 'RDU'])),
            self.norm_instrument_type,
            ref_id_or_subject,
            get_domain_value(notice, 'exchangeSourceName', lock_level=['FEED', 'RDU'])
        ])

        return self.storm_key

    def set_create_work_item_json(self):

        json_work_item_data = {
            'fields': {
                'project': {
                    'key': CONFIG['JIRA_PROJECT_KEY']
                },
                'summary': ':'.join([
                    self.exchange_group_name,
                    self.norm_instrument_type,
                    self.norm_event_type,
                    self.event_subject.replace('\n', '|')
                ])
                [:255],
                'issuetype': {
                    'name': CONFIG['JIRA_ISSUE_TYPE']
                },
                'description': self.event_summary,
                'customfield_10030': '247',  # Request Type default to EN (247)
                'assignee': {'accountId': self.exchange_owner['account_id']},
                'reporter': {'accountId': CONFIG['JIRA_REPORTER']},
                'customfield_10251': {'accountId': self.exchange_owner['account_id']},  # Primary exchange owner
                'customfield_10031': self.exchange_owners,  # Additional exchange owners
                'customfield_10242': self.publish_date,
                'customfield_10243': self.insert_date,
                'customfield_10282': self.exchange_reference_id,
                'customfield_10274': self.exchange_group_name,
                'customfield_10275': self.exchange_source_name,
                'customfield_10285': self.storm_key,
                'customfield_10279': {'value': self.norm_instrument_type}
            }
        }

        optional_element_map = {
            'customfield_10259': [self.exchange_prefix] if self.exchange_prefix != 'default' else '',
            'customfield_10261': [self.product_name] if self.product_name != 'default' else '',
            'customfield_10262': self.underlying_names,
            'customfield_10263': self.underlying_tickers,
            'customfield_10260': self.previous_references,
            'customfield_10283': self.event_initial_url,
            'customfield_10238': self.event_effective_date if self.event_effective_date not in ERROR_CODES else '',  # Effective date
            'customfield_10273': str(self.event_effective_date) if self.event_effective_date in ERROR_CODES else ''  # Event effective date error code
        }

        for each_optional_element in optional_element_map:
            if len(str(optional_element_map[each_optional_element])) > 0:
                json_work_item_data['fields'][each_optional_element] = optional_element_map[each_optional_element]

        self.create_work_item_json = json.dumps(json_work_item_data)

        Logger.logger.debug(f'Created init json {self.create_work_item_json}')

        return self.create_work_item_json

    def set_update_work_item_json(self):

        # Create a space delimited string. This will be stored as a list of strings in the Jira fields in case the datatype is List_string
        optional_element_map = {
            'customfield_10259': [{'add': self.exchange_prefix} if self.exchange_prefix != 'default' else ''],
            'customfield_10261': [{'add': self.product_name} if self.product_name != 'default' else ''],
            'customfield_10262': [{'add': ul_name} for ul_name in self.underlying_names] if len(self.underlying_names) > 0 else [''],
            'customfield_10263': [{'add': ul_ticker} for ul_ticker in self.underlying_tickers] if len(self.underlying_tickers) > 0 else [''],
            'customfield_10260': [{'add': ref_id} for ref_id in self.previous_references] if len(self.previous_references) > 0 else ['']
        }

        fields_map = {
            'summary': ':'.join([
                self.exchange_group_name,
                self.norm_instrument_type,
                self.norm_event_type,
                self.event_subject.replace('\n', '|')
            ])
            [:255],
            'customfield_10238': self.event_effective_date if self.event_effective_date not in ERROR_CODES else '',  # Effective date
            'customfield_10273': str(self.event_effective_date) if self.event_effective_date in ERROR_CODES else ''  # Effective data error code
        }

        json_work_item_data = {
            'update': {},
            'fields': {}
        }

        for each_optional_element in optional_element_map:
            if optional_element_map[each_optional_element][0] != '':
                json_work_item_data['update'][each_optional_element] = optional_element_map[each_optional_element]

        for each_field in fields_map:
            if fields_map[each_field] != '':
                json_work_item_data['fields'][each_field] = fields_map[each_field]

        self.update_work_item_json = json.dumps(json_work_item_data)

        Logger.logger.debug(f'Created update json {self.update_work_item_json}')

        return self.update_work_item_json

    def set_work_item_transition_json(self):

        changed_hash_json = {
            'update': {
                'comment': [{'add': {'body': 'Ticket has been reopened by SYSTEM.'}}]
            },
            'transition': {'id': '261'}
        }

        self.work_item_transition_json = json.dumps(changed_hash_json)

        Logger.logger.debug(f'Created transition json {self.work_item_transition_json}')

        return self.work_item_transition_json

    def update_endata(self):
        collection = database_connection['enData']

        mongo_query = {
            '$and': [
                {'eventSourceUniqueId.FEED.value': self.event_source_unique_id},
                {
                    '$or': [
                        {'eventStatus.RDU.value.normalizedValue': 'A'},
                        {'eventStatus.ENRICHED.value.normalizedValue': 'A'}
                    ]
                }
            ]
        }

        mongo_update_data = {
            'workItemType': {'RDU': {'value': {'normalizedValue': 'EN Workflow Item'}}},
            'stormKey': {'RDU': {'value': self.storm_key}},
            'workItemStatus': {'RDU': {'value': {'normalizedValue': 'A'}}}
        }

        collection.update_one(mongo_query, {'$addToSet': {'enWorkItemReference': mongo_update_data}})
