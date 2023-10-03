import argparse
from libs.get_data_from_ng import get_ops_users, get_en_data_source_codes, get_storm_keys, get_normalized_map, get_en_data, get_instrument_type_codes
from libs.atlassian_rest_api_calls import jira_rest_create_call, update_jira_status, update_jira_attributes
from libs.mongo_connect import set_up_mongo
from libs.data_for_api_calls import create_servicedesk_new_ticket_data, generate_issue_upd_data, update_servicedesk_new_ticket_hash_data
from libs.conversions import domain_normalized_value, domain_feed_value, date_feed_field_value, date_field_value, string_hash, string_field_value, \
    eff_date_field_value, string_field_no_spaces_max_255_value, string_field_within_nested_array_no_spaces_max_255_value, url_string_field_value, \
    lookup_exchange_source_name_domain, lookup_normalized_parent_instrument_type_name
from libs.update_ng_data import update_storm_keys, update_en_data_storm_key
from libs.en_logger import Logger
import logging
from libs.en_static import EnStatic
from libs.read_config import configuration
from pathlib import Path
import datetime


def update_log_handler():

    log_config = configuration["LOG"]

    log_dir = log_config['log_file_path']

    try:
        Path(log_dir).mkdir(parents=True, exist_ok=False)
    except FileExistsError:
        pass

    log_date = (str(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')).replace(" ", "")).replace(":", "")
    log_file = log_dir + "/log_" + log_date + ".log"
    handler = logging.FileHandler(log_file)
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    handler.setFormatter(formatter)
    Logger.logger.setLevel(getattr(logging, log_config['log_level'].upper()))
    Logger.logger.addHandler(handler)

    Logger.logger.info(f'Logs will be written to file {log_file} with log level {log_config["log_level"].upper()}.')


def calculate_storm_key(exchange_source_name, feed_effective_date_value, instrument_type, en_subject, document):
    if exchange_source_name == 'CMEG_GLOBEX':
        storm_key = "|".join([
            str(domain_feed_value(document, "eventType")),
            str(feed_effective_date_value),
            str(instrument_type),
            str(en_subject),
            str(exchange_source_name)
        ])
    else:
        # Calculating the default stormKey using:
        # normalized instrumentTypeCode,
        # feed eventType (to prevent new tickets from eventTypes not found initially in dvDomainMap)
        # feed eventEffectiveDate or errorCode (to prevent new ticket from OPS locks),
        # exchangeReferenceId & exchangeSourceName
        storm_key = "|".join([
            str(domain_feed_value(document, "eventType")),
            str(feed_effective_date_value),
            str(instrument_type),
            str(string_field_value(document, "exchangeReferenceId")),
            str(exchange_source_name)
        ])

    return storm_key


def create_service_desk_ticket(database, en_data, storm_keys_dict):
    # TODO mv: is the function intended to create a ticket or to gather all the info necessary for another function to create ticket, looking at the
    #  return variable name?
    #  Also, the function is rather big and therefore complex to understand what's being done and why
    # TODO JR: The function gathers all the information and creates and updates tickets by calling other functions. This is the main procedure from
    #  the script. What returns is the new and updated entries for updating issueStorming which happens in one go at the end of the script.

    # Initializaing collection for new service desk tickets field values
    grouped_en_data = {}
    # Initializaing collection for new documents and update to existing documents in issueStorming
    new_stormed_keys = {}

    # Get domains and dvDomainMaps (static data)
    en_domain = get_en_data_source_codes(database)
    ops_users_domain = get_ops_users(database)
    ins_type_dv_map = get_normalized_map(database, "assetClassifications")
    ins_type_domain = get_instrument_type_codes(database)
    event_type_dv_map = get_normalized_map(database, "eventTypes")

    for each_cursor in en_data:
        # Checking if it is a manually imported notification by checking on a mandatory non editable field with OPS lock via UUI
        manual_exchange_notification = False
        try:
            manual_exchange_notification = each_cursor['exchangeSourceName']['RDU']['value']['normalizedValue']
        except KeyError:
            pass

        # Deriving input vaulues required for stormKey
        instrument_type_code = domain_normalized_value(each_cursor, "instrumentTypeCode", ins_type_dv_map)
        instrument_type = lookup_normalized_parent_instrument_type_name(instrument_type_code, ins_type_domain)

        exch_source = domain_feed_value(each_cursor, "exchangeSourceName")
        norm_event_type = domain_normalized_value(each_cursor, "eventType", event_type_dv_map)
        feed_effective_date_value = date_feed_field_value(each_cursor, "eventEffectiveDate")
        # eventSubject calculation
        try:
            en_raw_subject = string_field_value(each_cursor, "eventSubject")
        except KeyError:
            # To be safe as eventSubject is not defined as mandatory
            en_raw_subject = "eventSubject not available"
        # Special handling for eventSubject in CMEG_GLOBEX which is required for the stormKey and hash calculation
        if exch_source == 'CMEG_GLOBEX':
            for string_to_strip_out in EnStatic.CMEG_GLOBEX_STRINGS_TO_STRIP_OUT:
                if en_raw_subject.startswith(string_to_strip_out):
                    en_raw_subject = en_raw_subject[len(string_to_strip_out):]
        else:
            pass
        # Replacing line break by pipe as Service Desk does not allow line breaks for the summary field
        event_subject = en_raw_subject.replace("\n", "|")
        # Calculating stormKey
        storm_key = calculate_storm_key(exch_source, feed_effective_date_value, instrument_type, event_subject, each_cursor)
        # Updating stormKey in enData document
        update_en_data_storm_key(database, string_field_value(each_cursor, "eventSourceUniqueId"), storm_key, manual_exchange_notification)

        # Deriving additional input vaulues required for hash, if hash changes we will update the related fields in the existing ticket and set the
        # status the ticket status to re-open
        effective_date_value = eff_date_field_value(each_cursor, "eventEffectiveDate")
        # Calculating the hash using eventSubject and eventEffective date (using OPS lock value if available)
        value_to_hash = "eventSubject:" + event_subject + " effectiveDate:" + str(effective_date_value)
        # Calling hash function for strings to prevent different value in each run
        hash_key = string_hash(value_to_hash)

        if storm_key in storm_keys_dict:
            # Storming to existing stormKey
            # Adding jira ID from issueStorming to grouped_en_data[storm_key]
            # TODO mv: what's happening here? Does this imply not all storm keys have a Ticket? Isn't a Jira always created or is this for making
            #  robust?
            # TODO JR: Yes, see comments from below
            try:
                # issueStorming document should always have a ticketId but if a new stormKey (storm_keys_dict[storm_key]) has been created
                # in this run then it will not be the case
                ticket_id = storm_keys_dict[storm_key]["ticketId"]
                try:
                    # if grouped_en_data[storm_key] already exists, it should already have the same ticket_id, therefore the below is superflous
                    # but does not hurt
                    grouped_en_data[storm_key]["ticket_id"] = ticket_id
                except KeyError:
                    # if grouped_en_data[storm_key] does not exist yet, we create it and we add the ticketId from issueStorming as we will need it
                    # later for updating the existing ticket
                    grouped_en_data[storm_key] = {}
                    grouped_en_data[storm_key]["ticket_id"] = ticket_id
            except KeyError:
                # In case it is a storm key that has been created in this run, and therefore there is no ticket_id nor need for it as we will be
                # creating a new ticket and not updating existing ones
                pass

            # Cheking if critical fields (included in hash) have changed. Critical field updates require service desk ticket values to be updated
            # and status to be RE-OPEN
            if hash_key == storm_keys_dict[storm_key]["hash"]:
                # No changes in critical field values, adding only the discrepant field values (e.g. exchangePrefix) to existing ticket
                try:
                    discrepant_values_dict = generate_dict_entries_with_discrepant_value(each_cursor, grouped_en_data[storm_key])
                    grouped_en_data[storm_key] = {**grouped_en_data[storm_key], **discrepant_values_dict}
                except KeyError:
                    grouped_en_data[storm_key] = {}
                    discrepant_values_dict = generate_dict_entries_with_discrepant_value(each_cursor, grouped_en_data[storm_key])
                    grouped_en_data[storm_key] = discrepant_values_dict
            else:
                # Changes in critical field values
                Logger.logger.info(f'Hash value has changed for Stormkey: {storm_key}, new hash input value is: {value_to_hash}.')
                # Updating the hash value in dictionary
                storm_keys_dict[storm_key]["hash"] = hash_key
                # Storing the new in a separate dictionary to update issueStorming
                new_stormed_keys[storm_key] = {}
                new_stormed_keys[storm_key]["hash"] = hash_key
                # Creating dictionary with the values to be used to create the service desk ticket
                new_critical_values_dict = generate_service_desk_ticket_update_dict(each_cursor, effective_date_value, event_subject, instrument_type,
                                                                                    norm_event_type, en_domain)
                try:
                    discrepant_values_dict = generate_dict_entries_with_discrepant_value(each_cursor, grouped_en_data[storm_key])
                    grouped_en_data[storm_key] = {**grouped_en_data[storm_key], **discrepant_values_dict, **new_critical_values_dict}
                except KeyError:
                    grouped_en_data[storm_key] = {}
                    discrepant_values_dict = generate_dict_entries_with_discrepant_value(each_cursor, grouped_en_data[storm_key])
                    grouped_en_data[storm_key] = {**discrepant_values_dict, **new_critical_values_dict}

                # Populate ticket_type as "Hash_updated" if ticket_type is not already set (as "New" or "Hash_updated")
                try:
                    grouped_en_data[storm_key]["ticket_type"]
                except KeyError:
                    grouped_en_data[storm_key]["ticket_type"] = "Hash_updated"
        else:
            # New stormKey, creating new ticket
            storm_keys_dict[storm_key] = {}
            storm_keys_dict[storm_key]["hash"] = hash_key
            new_stormed_keys[storm_key] = {}
            new_stormed_keys[storm_key]["hash"] = hash_key
            grouped_en_data[storm_key] = generate_new_service_desk_ticket_dict(each_cursor, effective_date_value, event_subject, instrument_type,
                                                                               norm_event_type, storm_key, en_domain, ops_users_domain)
            discrepant_values_dict = generate_dict_entries_with_discrepant_value(each_cursor, grouped_en_data[storm_key])
            grouped_en_data[storm_key] = {**grouped_en_data[storm_key], **discrepant_values_dict}

            Logger.logger.debug(f'Added storm key {storm_key} to cached list.')

    # Once all the data for all the service desk tickets to create and update is consolidated together, we iterate through the dictionary to
    # create, update and/or re/open tickets
    for each_stormed_event in grouped_en_data:
        Logger.logger.debug(f'Processing storm key: {each_stormed_event}')
        try:
            ticket_type = grouped_en_data[each_stormed_event]["ticket_type"]
            if ticket_type == "New":
                jira_data = create_servicedesk_new_ticket_data(grouped_en_data[each_stormed_event])
                new_stormed_keys[each_stormed_event]["jira"] = jira_rest_create_call(jira_data)
            elif ticket_type == "Hash_updated":
                # For updating existing ticket for discrepant field (with changes in hash/critical fields)
                jira_data = update_servicedesk_new_ticket_hash_data(grouped_en_data[each_stormed_event])
                update_jira_attributes(jira_data, grouped_en_data[each_stormed_event]["ticket_id"])
                # Reopen  jira
                update_jira_status(grouped_en_data[each_stormed_event]["ticket_id"])
                Logger.logger.debug(f"Updated hash for issue {grouped_en_data[each_stormed_event]['ticket_id']}, ticket has been re-opened")
        except KeyError:
            # For updating existing ticket for discrepant fields (no changes in hash/critical fields)
            jira_data = generate_issue_upd_data(grouped_en_data[each_stormed_event])
            update_jira_attributes(jira_data, grouped_en_data[each_stormed_event]["ticket_id"])
            Logger.logger.debug(f"Updated issue {grouped_en_data[each_stormed_event]['ticket_id']}")

    # Returning the documents to be creates and/or updated in issueStorming
    return new_stormed_keys


def generate_service_desk_ticket_update_dict(each_cursor, effective_date_value, event_subject, instrument_type, event_type, en_domain):
    # Function that calculates the input fields required in case we need to update an existing ticket (except the discrepant field values)
    desk_ticket_dict = {
        "exchange_group_name": lookup_exchange_source_name_domain(domain_feed_value(each_cursor, "exchangeSourceName"), "exchangeGroupName",
                                                                  en_domain),
        "norm_instrument_type": instrument_type,
        "norm_event_type": event_type,
        "event_subject": event_subject
    }
    desk_ticket_dict = {**desk_ticket_dict, **add_effective_date_to_dict(effective_date_value)}

    return desk_ticket_dict


def generate_new_service_desk_ticket_dict(each_cursor, effective_date_value, event_subject, instrument_type, event_type, storm_key, en_domain,
                                          ops_users_domain):
    # Function that calculates the input fields required in case we need to create a new ticket (except the discrepant field values)

    config = configuration["JIRA"]

    # Precalculating list of OPS analysts for a given exchangeSourceName
    # TODO mv: create dedicated function and use same function for exchange owner where the function is a paramter
    # TODO JR: probably is possible but be aware that 'owner_list' is a list and 'owner' is a single value. Both have as values a 'dictionary' e.g.
    #  {"accountId": "557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1"}. The lookup column in enDataSourceCodes is also different, one column is a single
    #  value and the other is a coma separated list. As agreed, we will leave this for now as it is.
    try:
        exch_owner_string_list = lookup_exchange_source_name_domain(domain_feed_value(each_cursor,"exchangeSourceName"), "allOpsAnalysts", en_domain)
        exch_owner_list = exch_owner_string_list.split(",")
        owner_account_id_list = []
        for owner in exch_owner_list:
            # In case OPS enters by mistake extra comas and blanks spaces
            owner = owner.lower().strip()
            if owner != "":
                owner_dic = {"accountId": ops_users_domain[owner]["accountId"]}
                owner_account_id_list.append(owner_dic)
    except KeyError:
        # Defaulting to fallback exchange owner in case something goes wrong
        owner_account_id_list = []
        owner_account_id_list.append({"accountId": config["JIRA_FALL_BACK_EXCH_OWNER"]})
        Logger.logger.warning(f"The notification with stormKey: {storm_key} either has an exchangeSourceName not found in enDataSourceCodes or the "
                              f"exchange owner is not found in opsServiceDeskUsers, aading general fallback exchange owner: "
                              f"{config['JIRA_FALL_BACK_EXCH_OWNER']} as Request participants")

    # Precalculating exchange owner for a given exchangeSourceName
    try:
        exch_owner = ops_users_domain[lookup_exchange_source_name_domain(domain_feed_value(each_cursor,"exchangeSourceName"), "exchangeOwner",
                                                                         en_domain)]["accountId"]
    except KeyError:
        # Defaulting to fallback exchange owner in case something goes wrong
        exch_owner = config["JIRA_FALL_BACK_EXCH_OWNER"]
        Logger.logger.warning(f"The notification with stormKey: {storm_key} either has an exchangeSourceName not found in enDataSourceCodes or the "
                              f"exchange owner is not found in opsServiceDeskUsers, assigning ticket to general fallback exchange owner: "
                              f"{config['JIRA_FALL_BACK_EXCH_OWNER']}")

    # Fields that should always have values available either from DB or from the value retreival function fallback
    desk_ticket_dict = {
        "ticket_type": "New",
        "storm_key": storm_key,
        "exchange_reference_id": string_field_value(each_cursor, "exchangeReferenceId"),
        "event_initial_url": url_string_field_value(each_cursor, "eventInitialUrl"),
        "norm_event_type": event_type,
        "exchange_group_name": lookup_exchange_source_name_domain(domain_feed_value(each_cursor, "exchangeSourceName"), "exchangeGroupName",
                                                                  en_domain),
        "exchange_source_name": domain_feed_value(each_cursor, "exchangeSourceName"),
        "norm_instrument_type": instrument_type,
        "exchange_owner": exch_owner,
        "exchange_owners": owner_account_id_list,
        "publish_date": date_field_value(each_cursor, "eventPublishDate"),
        "insert_date": date_field_value(each_cursor, "eventInsertDate"),
        "event_subject": event_subject
    }

    # Fields which may not be available
    # Populating eventSummaryText
    try:
        desk_ticket_dict["event_summary"] = string_field_value(each_cursor, "eventSummaryText")
    except KeyError:
        desk_ticket_dict["event_summary"] = "eventSummaryText not available"

    desk_ticket_dict = {**desk_ticket_dict, **add_effective_date_to_dict(effective_date_value)}

    return desk_ticket_dict


def add_effective_date_to_dict(effective_date_value):
    eff_date_dict = {}
    # Populating from eventEffectiveDate either service desk effective_date or effective_date_error_code
    effective_date_error_code = False
    effective_date = False
    if type(effective_date_value) == int:
        effective_date_error_code = effective_date_value
    else:
        effective_date = effective_date_value

    if effective_date:
        eff_date_dict["effective_date"] = effective_date
    elif effective_date_error_code:
        eff_date_dict["effective_date_error_code"] = effective_date_error_code
    else:
        eff_date_dict["effective_date_error_code"] = 103

    return eff_date_dict


def generate_dict_entries_with_discrepant_value(each_cursor, dictionary):
    # Calculating the field values that change from one stormed notification to another (e.g. exchangePrefix)

    for key, row in EnStatic.FLAT_FIELDS_TO_APPEND.items():
        value = string_field_no_spaces_max_255_value(each_cursor, row)
        try:
            dictionary[key].append(value)
        except KeyError:
            dictionary[key] = []
            dictionary[key].append(value)

    for key, row in EnStatic.NESTED_ARRAY_FIELDS_TO_APPEND.items():
        try:
            number_of_rows = len(each_cursor[row.split(".")[0]])
            for entry in range(number_of_rows):
                try:
                    dictionary[key].append(string_field_within_nested_array_no_spaces_max_255_value(each_cursor, row, entry))
                except KeyError:
                    dictionary[key] = []
                    dictionary[key].append(string_field_within_nested_array_no_spaces_max_255_value(each_cursor, row, entry))
        except KeyError:
            pass

    return dictionary


if __name__ == '__main__':

    update_log_handler()

    Logger.logger.info("Processing started for creating and updating EN workItems")

    # Reading the input arguments
    parser = argparse.ArgumentParser()
    # TODO mv: Given the documentation "All notifications inserted in the last 24 hours " shouldn't a time argument be provided?
    # TODO JR: I will correct the documentation, it should be inserted within the current day. Note also that OPS would like to run the tool
    #  multiple times during the day (e.g. every hour)
    parser.add_argument('--run_datetime',
                        help='Provide the date and time in which the run is executed (YYYYMMDD HH:MM format)',
                        required=True)
    parser.add_argument('--mode',
                        help='Provide the execution mode: init (mode for one off initialization), daily (mode for daily executions)',
                        choices=['init', 'daily'],
                        required=True)
    parser.add_argument('--hour_offset',
                        help='Provide number of hours to offset from the run datetime to query for new notifications inserted (based on insDate) '
                             'and/or updated (based on updDate) since the last run. If omitted, default value used the one defined in config.ini '
                             '(2 hours). If a high value is used, processing time will be higher but output will be the same.',
                        required=False)
    parser.add_argument('--day_offset',
                        help='Provide number of whole days to offset from the run date to query for new notifications inserted '
                             '(based on eventInsertDate) since the last run. If omitted, default value used the one defined in config.ini (1 day). '
                             'If a high value is used, processing time will be higher but output will be the same.',
                        required=False)

    try:
        args = parser.parse_args()
    except Exception as error:
        Logger.logger.fatal("Invalid or no arguments provided: " + str(error))
        exit("Exiting program for invalid input")

    date_time = args.run_datetime

    config = configuration["OFFSET_DEFAULT"]

    if args.hour_offset:
        hour_offset = int(args.hour_offset)
    else:
        hour_offset = int(config["HOUR_OFFSET"])

    if args.day_offset:
        day_offset = int(args.day_offset)
    else:
        day_offset = int(config["DAY_OFFSET"])

    mode = args.mode

    Logger.logger.info(f"Running the script in mode: {mode} with days offset: {day_offset} and hours offset: {hour_offset}.")

    db = set_up_mongo()

    # Get enData documents
    ens_data = get_en_data(db, date_time, mode, hour_offset, day_offset)

    # Get issueStorming documents
    storm_dic = get_storm_keys(db)

    # TODO mv: encapsulate main function in try, same as we do for other application. This way the traceback is printed in case of unexpected errors.

    # TODO mv: read the database in the create_service_desk_ticket function. It's not used in main, so no need to provide it as parameter
    # Creating service desk tickets, updating stormKey in enData, and getting the new and updated issueStorming data
    try:
        new_and_updated_keys = create_service_desk_ticket(db, ens_data, storm_dic)
        if new_and_updated_keys:
            update_storm_keys(db, new_and_updated_keys)
            Logger.logger.info("workItems collection updated as input data changed. Run completed succesfully.")
        else:
            Logger.logger.info("workItems collection was not updated as input data did not change. Run completed succesfully.")
    except Exception as e:
        Logger.logger.error(e, exc_info=True)



