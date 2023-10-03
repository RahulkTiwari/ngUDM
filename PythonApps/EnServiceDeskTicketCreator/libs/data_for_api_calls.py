import json
import validators
from libs.read_config import configuration
from libs.en_logger import Logger


def create_servicedesk_new_ticket_data(event):

    config = configuration["JIRA"]

    json_data = {
        "fields": {
            "project": {
                "key": config["JIRA_PROJECT_KEY"]
            },
            "summary": (event["exchange_group_name"] + ":" + event["norm_instrument_type"] + ":" + event["norm_event_type"] + ":" +
                        event["event_subject"])[:255],
            "issuetype": {
                "name": config["JIRA_ISSUE_TYPE"]
            },
            "description": event["event_summary"],
            # Request Type default to EN (247)
            "customfield_10030": "247",
            # Assignee
            "assignee": {"accountId": str(event["exchange_owner"])},
            # Reporter
            "reporter": {"accountId": "5c8a4fd2677d763daafa57c7"},
            # Exchange owner
            "customfield_10251": {"accountId": str(event["exchange_owner"])},
            # Exchange owners
            "customfield_10031": event["exchange_owners"],
            "customfield_10242": event["publish_date"],
            "customfield_10243": event["insert_date"],
            "customfield_10283": event["event_initial_url"],
            "customfield_10282": event["exchange_reference_id"],
            "customfield_10274": event["exchange_group_name"],
            "customfield_10275": event["exchange_source_name"],
            "customfield_10285": event["storm_key"]
        }
    }



    if event["norm_instrument_type"] in ['Future', 'Option', 'Strategy', 'errorCode101']:
        json_data["fields"]["customfield_10279"] = {"value": event["norm_instrument_type"]}
    else:
        Logger.logger.warning(f"The notification with stormKey: {event['storm_key']} has an invalid instrumentType value: "
                              f"{event['norm_instrument_type']}")

    try:
        # Event Effective Date Error Code
        json_data["fields"]["customfield_10273"] = str(event["effective_date_error_code"])
    except KeyError:
        # Event Effective Date
        json_data["fields"]["customfield_10238"] = event["effective_date"]
        # Due Date
        json_data["fields"]["customfield_10237"] = event["effective_date"]

    try:
        json_data["fields"]["customfield_10259"] = event["exchange_prefixes"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10261"] = event["product_names"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10262"] = event["underlying_names"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10263"] = event["underlying_tickers"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10260"] = event["previous_references"]
    except KeyError:
        pass

    return json_data


def generate_issue_upd_data(event):
    json_data = {}
    json_data["fields"] = {}

    try:
        json_data["fields"]["customfield_10259"] = event["exchange_prefixes"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10261"] = event["product_names"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10262"] = event["underlying_names"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10263"] = event["underlying_tickers"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10260"] = event["previous_references"]
    except KeyError:
        pass

    return json_data


def update_servicedesk_new_ticket_hash_data(event):

    try:
        event["effective_date_error_code"]
        json_data = {
            "fields": {
                "customfield_10273": event["effective_date_error_code"],
                "summary": (event["exchange_group_name"] + ":" + event["norm_instrument_type"] + ":" + event["norm_event_type"] + ":" +
                            event["event_subject"])[:255]
            }
        }
    except KeyError:
        json_data = {
            "fields": {
                "customfield_10238": event["effective_date"],
                "customfield_10237": event["effective_date"],
                "summary": (event["exchange_group_name"] + ":" + event["norm_instrument_type"] + ":" + event["norm_event_type"] + ":" +
                            event["event_subject"])[:255]
            }
        }

    try:
        json_data["fields"]["customfield_10259"] = event["exchange_prefixes"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10261"] = event["product_names"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10262"] = event["underlying_names"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10263"] = event["underlying_tickers"]
    except KeyError:
        pass

    try:
        json_data["fields"]["customfield_10260"] = event["previous_references"]
    except KeyError:
        pass

    return json_data
