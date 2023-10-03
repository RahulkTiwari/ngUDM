from requests.auth import HTTPBasicAuth
import requests
import json
from libs.en_logger import Logger
from libs.read_config import configuration


def jira_rest_create_call(data):

    config = configuration["JIRA"]

    url = config["JIRA_URL"] + '/rest/api/2/issue'
    auth = HTTPBasicAuth(config["JIRA_USERNAME"], config["JIRA_TOKEN"])

    # Build the request
    headers = {
        'Content-Type': 'application/json',
        "Accept": "application/json"
    }

    response = requests.request(
        "POST",
        url,
        data=json.dumps(data),
        headers=headers,
        auth=auth
    )

    response_json = json.loads(response.text)

    if str(response.status_code).startswith('2'):
        # Ticket sucessfully created with the original data
        Logger.logger.info(f'JIRA CREATED for stormKey: {str(data["fields"]["customfield_10285"])}. Response code from Service Desk REST API '
                           f'{response.status_code}. Response text from Service Desk REST API {response.text}.')
        return response_json['key']
    elif "customfield_10251" in response_json["errors"]:
        # If API response indicates error associated to exchangeOwner/assignee, overwrite exchangeOwner/assigne by fallback one and retry
        data["fields"]["assignee"] = {"accountId": config["JIRA_FALL_BACK_EXCH_OWNER"]}
        data["fields"]["customfield_10251"] = {"accountId": config["JIRA_FALL_BACK_EXCH_OWNER"]}
        response = requests.request(
            "POST",
            url,
            data=json.dumps(data),
            headers=headers,
            auth=auth
        )

        response_json = json.loads(response.text)

        if str(response.status_code).startswith('2'):
            # If retry is sucessful
            Logger.logger.warning(f'JIRA CREATED for stormKey: {str(data["fields"]["customfield_10285"])} with fallback assignee. Response code from '
                                  f'Service Desk REST API {response.status_code}. Response text from Service Desk REST API {response.text}.')
            return response_json['key']
        else:
            # If retry is NOT sucessful
            # Logger.logger.warning(f'JIRA FAILED TO BE CREATED for stormKey with fallback assignee: {str(data["fields"][""customfield_10285"])}. '
            #                       f'Response code from Service Desk REST API {response.status_code}, Response errors from Service Desk REST API '
            #                       f'{json.dumps(response_json["errors"])}.')
            Logger.logger.warning(f'JIRA FAILED TO BE CREATED for stormKey with fallback assignee: {str(data["fields"]["customfield_10285"])}. '
                                  f'Response code from Service Desk REST API {response.status_code}, Response errors from Service Desk REST API '
                                  f'{response.text}.')
            return "No ticket number assigned"
    else:
        # If API response indicates an error NOT associated to exchangeOwner/assignee
        # Logger.logger.warning(f'JIRA FAILED TO BE CREATED for stormKey: {str(data["fields"][""customfield_10285"])}. '
        #                       f'Response code from Service Desk REST API {response.status_code}, Response errors from Service Desk REST API '
        #                       f'{json.dumps(response_json["errors"])}.')
        Logger.logger.warning(f'JIRA FAILED TO BE CREATED for stormKey: {str(data["fields"]["customfield_10285"])}. '
                              f'Response test  from Service Desk REST API {response.status_code}, Response errors from Service Desk REST API '
                              f'{response.text}.')
        return "No ticket number assigned"


def query_jira_status(jira_id):

    config = configuration["JIRA"]

    url = config["JIRA_URL"] + '/rest/api/2/issue/' + jira_id
    auth = HTTPBasicAuth(config["JIRA_USERNAME"], config["JIRA_TOKEN"])

    # Build the request
    headers = {
        'Content-Type': 'application/json',
        "Accept": "application/json"
    }

    response = requests.request(
        "GET",
        url,
        headers=headers,
        auth=auth
    )

    response_json = json.loads(response.text)

    if str(response.status_code).startswith('2'):
        Logger.logger.info(f'JIRA ID QUERIED: {jira_id}. Response code from Service Desk REST API {response.status_code}, '
                           f'Ticket status: {response_json["fields"]["customfield_10030"]["currentStatus"]["status"]}.')
        return response_json["fields"]["customfield_10030"]["currentStatus"]["status"]
    else:
        # Logger.logger.warning(f'JIRA ID QUERIED FAILED TO BE RETURNED: {jira_id}.Response code from Service Desk REST API {response.status_code}, '
        #                       f'Response errors from Service Desk REST API {json.dumps(response_json["errorMessages"])}.')
        Logger.logger.warning(f'JIRA ID QUERIED FAILED TO BE RETURNED: {jira_id}.Response code from Service Desk REST API {response.status_code}, '
                              f'Response text from Service Desk REST API {response.text}.')
        return "Unknown"


def update_jira_status(jira_id):

    config = configuration["JIRA"]

    url = config["JIRA_URL"] + '/rest/api/2/issue/' + jira_id + "/transitions"
    auth = HTTPBasicAuth(config["JIRA_USERNAME"], config["JIRA_TOKEN"])

    data = {"update": {
                "comment": [
                    {
                        "add": {
                            "body": "Ticket has been reopened by SYSTEM."
                        }
                    }
                ]
            },
            "transition": {
                "id": "261"
            }}

    json_data = json.dumps(data)

    # Build the request
    headers = {
        'Content-Type': 'application/json',
        "Accept": "application/json"
    }

    response = requests.request(
        "POST",
        url,
        data=json_data,
        headers=headers,
        auth=auth
    )

    if str(response.status_code).startswith('2'):
        Logger.logger.info(f'JIRA ID {jira_id} STATUS UPDATED TO RE-OPEN: Response code from Service Desk REST API {response.status_code}, '
                           f'Response text from Service Desk REST API {response.text}.')
    else:
        # response_json = json.loads(response.text)
        # Logger.logger.warning(f'JIRA ID {jira_id} STATUS UPDATED FAILED TO RE-OPEN: Response code from Service Desk REST API {response.status_code}, '
        #                       f'Response errors from Service Desk REST API {json.dumps(response_json["errorMessages"])}.')
        Logger.logger.warning(f'JIRA ID {jira_id} STATUS UPDATED FAILED TO RE-OPEN: Response code from Service Desk REST API {response.status_code}, '
                              f'Response text from Service Desk REST API {response.text}.')


def update_jira_attributes(data, jira_id):

    config = configuration["JIRA"]

    url = config["JIRA_URL"] + '/rest/api/2/issue/' + jira_id
    auth = HTTPBasicAuth(config["JIRA_USERNAME"], config["JIRA_TOKEN"])

    # Build the request
    headers = {
        'Content-Type': 'application/json',
        "Accept": "application/json"
    }

    response = requests.request(
        "PUT",
        url,
        data=json.dumps(data),
        headers=headers,
        auth=auth
    )

    if str(response.status_code).startswith('2'):
        Logger.logger.debug(f'JIRA ID {jira_id} UPDATED: Response code from Service Desk REST API {response.status_code}, '
                            f'Response text from Service Desk REST API {response.text}.')
    else:
        # response_json = json.loads(response.text)
        # Logger.logger.warning(f'JIRA ID {jira_id} FAILED TO BE UPDATED: Response code from Service Desk REST API {response.status_code}, '
        #                       f'Response errors from Service Desk REST API {json.dumps(response_json["errorMessages"])}.')
        Logger.logger.warning(f'JIRA ID {jira_id} FAILED TO BE UPDATED: Response code from Service Desk REST API {response.status_code}, '
                              f'Response errors from Service Desk REST API {response.text}.')
