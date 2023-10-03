# Third party libs
import json
import requests
from requests.auth import HTTPBasicAuth
# Custom libs
from modules.config import config_object
from modules.logger import main_logger
from modules.data_sources import datasources_cache



def set_jira_json(security):

    service_desk_config = config_object['SERVICE_DESK']
    reason_summary_map = {
        'No record found': 'Unable to find corresponding record for',
        None: ''
    }

    sd_json = {
        'fields': {
            'project': {
                'key': service_desk_config['JIRA_PROJECT_KEY']
            },
            'summary': f'{security.mic_code}: {reason_summary_map[security.skeletal_reason]} {security.lookup_attr} {security.lookup_attr_val}',
            'issuetype': {
                'name': service_desk_config['JIRA_ISSUE_TYPE']
            },
            'description': f'{security.source}: {datasources_cache[security.source].description}\n\n{security.mic_code}: {reason_summary_map[security.skeletal_reason]} {security.lookup_attr} {security.lookup_attr_val}',
            'priority': {'name': 'High'},
            'assignee': {'accountId': datasources_cache[security.source].exchange_owner},
            'reporter': {'accountId': service_desk_config['JIRA_REPORTER']},
            'customfield_10251': {'accountId': datasources_cache[security.source].exchange_owner},  # Primary exchange owner
            'customfield_10031': [{'accountId': user} for user in datasources_cache[security.source].ops_analysts],
            'customfield_10127': security.mic_code,
            'customfield_10135': {'value': 'NG_EDS'},
            'customfield_10132': {'value': 'Enrichment'},
            'customfield_10133': {'value': 'Security Lookup Failure'},
            'customfield_10116': 'Exchange Data to trdse Lookup Failure',
            'customfield_10123': security.lookup_attr,
            'customfield_10121': security.lookup_attr_val,
            'customfield_10125': f'{security.source}: {datasources_cache[security.source].description}'
        }
    }

    return sd_json


def create_jira(security):
    """
    Static function to create a Jira for Operation to resolve anomalies
    Args:
        security: Information on the security. Usually just the id type and id value
    """

    main_logger.debug('Posting request to service desk...')
    service_desk_config = config_object['SERVICE_DESK']
    payload = json.dumps(set_jira_json(security))

    response = requests.request(
       'POST',
       f'{service_desk_config["JIRA_URL"]}/rest/api/2/issue',
       data=payload,
       headers={
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
       auth=HTTPBasicAuth(service_desk_config['JIRA_USERNAME'], service_desk_config['JIRA_TOKEN'])
    )

    if not str(response.status_code).startswith('2'):
        main_logger.error(f'Failed to create tickets due to: {response.reason}')
        return False
    else:
        main_logger.info(f'Created Jira {json.loads(response.text)["key"]}')
        return True
