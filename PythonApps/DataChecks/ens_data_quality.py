from datetime import datetime, timedelta
import argparse
from bson.json_util import dumps
import json
from pymongo import MongoClient
import logging
from pathlib import Path

# globals
valid_values = ["TBA", "TBC"]
valid_error_codes = [104, 105]
allowed_error_code_fields = ["eventEffectiveDate"]


class Logger:

    log_dir = str(Path.home()) + '\\Documents\\DrDOutput\\logs'

    try:
        Path(log_dir).mkdir(parents=True, exist_ok=False)
    except FileExistsError:
        pass

    log_date = (str(datetime.now().strftime('%Y-%m-%d %H:%M:%S')).replace(" ", "")).replace(":", "")
    log_file = log_dir + "\\ens_validation_log_" + log_date + ".log"
    logger = logging.getLogger(__name__)
    handler = logging.FileHandler(log_file)
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.addHandler(logging.StreamHandler())
    logger.setLevel(logging.WARN)
    logger.info("Logger has been initialized")


def read_properties(properties_file_name, sep='=', comment_char='#'):
    """
    Function to read the database connection properties
    :param properties_file_name: filename which holds the properties
    :param sep: the separator used by the properties file
    :param comment_char: indicator of comments in the properties file
    :return: dict containing the database properties
    """
    props = {}
    with open(properties_file_name, "rt") as f:
        for line in f:
            var_l = line.strip()
            if var_l and not var_l.startswith(comment_char):
                key_value = var_l.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip().strip('"')
                if value != "":
                    props[key] = value
    return props


def set_up_mongo():
    database_credentials = read_properties('database_prod.properties')
    client = MongoClient(host=database_credentials['host'],
                         port=int(database_credentials['port']),
                         authSource=database_credentials['authSource'],
                         authMechanism=database_credentials['authMechanism'],
                         username=database_credentials['username'],
                         password=database_credentials['password'])
    return client[database_credentials['authSource']]


def get_value_by_key(attribute, event_json):
    possible_value_syntax = {
        'en_data_feed_domain': ['FEED', 'value', 'val'],
        'en_data_feed_date': ['FEED', 'value', '$date'],
        'en_data_feed_number': ['FEED', 'value', '$numberDecimal'],
        'en_data_feed': ['FEED', 'value'],
        'en_data_rdu': ['RDU', 'value'],
        'en_data_rdu_date': ['RDU', 'value', '$date'],
        'en_data_rdu_number': ['RDU', 'value', '$numberDecimal'],
        'en_data_rdu_normalized': ['RDU', 'value', 'normalizedValue'],
        'en_data_enriched': ['ENRICHED', 'value'],
    }

    for each_possible_syntax in possible_value_syntax:
        try:
            attribute_value = event_json[attribute]
            for each_sub_path in possible_value_syntax[each_possible_syntax]:
                attribute_value = attribute_value[each_sub_path]
            # If it's not a string or integer assume it's a list and take first element
            return attribute_value if (isinstance(attribute_value, str) or isinstance(attribute_value, int)) else attribute_value[0]
        except (KeyError, TypeError):
            pass

    Logger.logger.warning(f"No attribute value fetched for event {event_json['eventSourceUniqueId']['FEED']['value']}, "
                          f"{attribute} raw string {str(event_json[attribute])}")


# Validation rules
def mandatory_field_check(event):
    mandatory_fields = ['eventSourceUniqueId', 'exchangeSourceName', 'eventPublishDate', 'eventInsertDate', 'eventType', 'eventSubject',
                        'eventSummaryText', 'eventInitialUrl', 'exchangeReferenceCounter', 'events']
    field_validation = {
        'validation_result': True,
        'missing_fields': []
    }

    for each_field in mandatory_fields:
        try:
            get_value_by_key(each_field, event)
        except KeyError:
            field_validation['missing_fields'].append(each_field)
            field_validation['validation_result'] = False

    return field_validation


def field_value_check(event):
    field_validation = {
        'validation_result': True,
        'error_code_fields': []
    }

    for each_field in event:
        if isinstance(event[each_field], dict):
            # Check for invalid error codes
            try:
                if event[each_field]['FEED']['errorCode'] not in valid_error_codes and each_field not in allowed_error_code_fields:
                    field_validation['validation_result'] = False
                    field_validation['error_code_fields'].append(each_field)
            except KeyError:
                # Check for null values
                try:
                    if get_value_by_key(each_field, event) is None or get_value_by_key(each_field, event) == "":
                        field_validation['validation_result'] = False
                        field_validation['error_code_fields'].append(each_field)
                    else:
                        pass
                except KeyError:
                    field_validation['validation_result'] = False
                    field_validation['error_code_fields'].append(each_field)

        elif isinstance(event[each_field], list):
            for each_index, field in enumerate(event[each_field]):
                for each_item in event[each_field][each_index]:
                    try:
                        if event[each_field][each_index][each_item]['FEED']['errorCode'] not in valid_error_codes:
                            field_validation['validation_result'] = False
                            field_validation['error_code_fields'].append(f"{each_field}[{str(each_index)}].{each_item}")
                    except (KeyError, TypeError):
                        # pass

                        # Check for null values
                        try:
                            if event[each_field][each_index][each_item]['FEED']['value'] is None or \
                                    event[each_field][each_index][each_item]['FEED']['value'] == "":
                                field_validation['validation_result'] = False
                                field_validation['error_code_fields'].append(each_field)
                            else:
                                pass
                        except KeyError:
                            field_validation['validation_result'] = False
                            field_validation['error_code_fields'].append(each_field)

    return field_validation


def isin_check_digit(isin):
    try:
        check_digit = isin[11]
    except IndexError:
        return False

    _alphabet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    isin = isin.strip().upper()
    number = ''.join(str(_alphabet.index(n)) for n in isin[:11])
    number = ''.join(str((2, 1)[i % 2] * int(n)) for i, n in enumerate(reversed(number)))
    calculated_check_digit = str((10 - sum(int(n) for n in number)) % 10)

    return calculated_check_digit == check_digit


def isin_check(event):
    result = {
        'passed': True,
        'attribute': None
    }

    context = {
        'underlyings': 'isin',
        'newUnderlyings': 'newIsin'
    }

    for each_context in context:
        try:
            for index, each_isin in enumerate(event[each_context]):
                isin = get_value_by_key(context[each_context], event[each_context][index])
                if len(isin) != 12 and isin not in valid_values and not isin_check_digit(isin):
                    result['passed'] = False
                    result['attribute'] = f"{each_context}.{context[each_context]}"
                if not isin[:2].isalpha():
                    result['passed'] = False
                    result['attribute'] = f"{each_context}.{context[each_context]}"
        except (KeyError, TypeError):
            pass

    return result


def cusip_check_digit(cusip):
    valid_check_digit = True

    try:
        check_digit = int(cusip[8])
    except IndexError:
        return False

    cusip_chars = [char for char in cusip.upper()][:8]

    the_sum = 0
    value = 0
    for index, char in enumerate(cusip_chars):
        if char.isnumeric():
            value = int(char)
        elif char.isalpha():
            ordeal_position = ord(char) - ord('A') + 1
            value = ordeal_position + 9
        elif char == "*":
            value = 36
        elif char == "@":
            value = 37
        elif char == "#":
            value = 38
        if (index + 1) % 2 == 0:
            value = value * 2

        the_sum += int(value / 10) + (value % 10)

    calculated_check_digit = (10 - (the_sum % 10)) % 10

    if check_digit != calculated_check_digit:
        valid_check_digit = False

    return valid_check_digit


def cusip_check(event):
    result = {
        'passed': True,
        'attribute': None
    }

    context = {
        'underlyings': 'cusip',
        'newUnderlyings': 'newCusip'
    }

    for each_context in context:
        try:
            for index, each_cusip in enumerate(event[each_context]):
                cusip = get_value_by_key(context[each_context], event[each_context][index])
                if len(cusip) != 9 and cusip not in valid_values and not cusip_check_digit(cusip):
                    result['passed'] = False
                    result['attribute'] = f"{each_context}.{context[each_context]}"
        except (KeyError, TypeError):
            pass

    return result


def uniq_check(event):
    result = {
        'passed': True,
        'attribute': None
    }

    unique_id = f"{get_value_by_key('exchangeReferenceId', event)}_{str(get_value_by_key('exchangeReferenceCounter', event))}_" \
                f"{get_value_by_key('exchangeSourceName', event)}"

    if unique_id not in exchange_ref_ids:
        exchange_ref_ids.append(unique_id)
    else:
        result['passed'] = False

    return result


def effective_date_check(event):
    result = {
        'passed': True,
        'attribute': None
    }

    date_dict = {
        'eventEffectiveDate': datetime.now() + timedelta(days=100),
        'eventPublishDate': None,
        'eventInsertDate': None
    }

    for each_date in date_dict.keys():
        try:
            date_dict[each_date] = datetime.strptime(get_value_by_key(each_date, event), '%Y-%m-%dT%H:%M:%SZ')
        except (KeyError, TypeError):
            Logger.logger.info(f"No date available for {each_date}. Falling back to {date_dict[each_date]}")

    if date_dict['eventEffectiveDate'] < date_dict['eventPublishDate']:
        result['passed'] = False
        result['attribute'] = 'eventPublishDate'
    elif date_dict['eventEffectiveDate'] < date_dict['eventInsertDate']:
        result['passed'] = False
        result['attribute'] = 'eventInsertDate'

    return result


def validate_event(event):
    validation_outcome = {
        'validation_passed': True,
        'failed_validation': []
    }

    event_in_json = json.loads(event)

    # Validation whether all the mandatory fields are in the record
    manditory_field_validation = mandatory_field_check(event_in_json)
    if manditory_field_validation['validation_result'] is False:
        validation_outcome['validation_passed'] = False
        validation_outcome['failed_validation'].append(f"Mandotory field(s) for {get_value_by_key('eventSourceUniqueId', event_in_json)} are "
                                                       f"missing {str(manditory_field_validation['missing_fields'])}")

    # Validate that there are no errorCodes as field value
    field_values = field_value_check(event_in_json)
    if field_values['validation_result'] is False:
        validation_outcome['validation_passed'] = False
        validation_outcome['failed_validation'].append(f"Error code found for {get_value_by_key('eventSourceUniqueId', event_in_json)} in "
                                                       f"field(s) {str(field_values['error_code_fields'])}")

    # Validate if the isin is valid
    isin_validation = isin_check(event_in_json)
    if isin_validation['passed'] is False:
        validation_outcome['validation_passed'] = False
        validation_outcome['failed_validation'].append(f"{isin_validation['attribute']} validation failed for "
                                                       f"{get_value_by_key('eventSourceUniqueId', event_in_json)}")

    # Validate if the cusip is valid
    cusip_validation = cusip_check(event_in_json)
    if cusip_validation['passed'] is False:
        validation_outcome['validation_passed'] = False
        validation_outcome['failed_validation'].append(f"{cusip_validation['attribute']} validation failed for "
                                                       f"{get_value_by_key('eventSourceUniqueId', event_in_json)}")

    # Validate uniqueness of exchange reference id plus counter
    uniq_ref_id_validation = uniq_check(event_in_json)
    if uniq_ref_id_validation['passed'] is False:
        validation_outcome['validation_passed'] = False
        validation_outcome['failed_validation'].append(
            f"Duplicate record found for exchangeReferenceId {get_value_by_key('exchangeReferenceId', event_in_json)}"
            f" and exchangeReferenceCounter {str(get_value_by_key('exchangeReferenceCounter', event_in_json))} for "
            f"exchangeSourceName {get_value_by_key('exchangeSourceName', event_in_json)}")

    # Validate that the eventPublishDate and eventInsertDate is greater than the eventEffectiveDate
    effective_date_validation = effective_date_check(event_in_json)
    if effective_date_validation['passed'] is False:
        validation_outcome['validation_passed'] = False
        validation_outcome['failed_validation'].append(
            f"Effective date for {get_value_by_key('exchangeReferenceId', event_in_json)} ({get_value_by_key('eventEffectiveDate', event_in_json)}) "
            f"is earlier than the {effective_date_validation['attribute']} "
            f"({get_value_by_key(effective_date_validation['attribute'], event_in_json)})")

    return validation_outcome


def main(query_date):

    # Global to track the exchangeReferenceIds to be able to detect duplicates
    global exchange_ref_ids

    db = set_up_mongo()
    collection = db['enData']
    ignored_fields = {
        "_id": 0,
        "insDate": 0,
        "insUser": 0,
        "updDate": 0,
        "updUser": 0,
        "eventStatus": 0,
        "audit": 0,
        "_class": 0,
        "version": 0
    }

    query = [{"$match": {"$and":
                             [{"eventSourceUniqueId.FEED.value": {"$exists": True}},
                              {"eventInsertDate.FEED.value": {"$gte": datetime.strptime(query_date, '%Y%m%d')}},
                              {"eventStatus.RDU.value.normalizedValue": {"$ne": "I"}}
                              ]}},
             {"$project": ignored_fields}]
    query_result = collection.aggregate(query, allowDiskUse=True)

    validation_result = {}

    try:
        for each_cursor in query_result:
            Logger.logger.info(f"Validating record {get_value_by_key('exchangeReferenceId', each_cursor)}")
            # Validating the record
            validation_result[get_value_by_key('exchangeReferenceId', each_cursor)] = validate_event(dumps(each_cursor))

            # In case the validation failed, the cause of the failure is logged
            if validation_result[get_value_by_key('exchangeReferenceId', each_cursor)]['validation_passed'] is False:
                for each_failure in validation_result[get_value_by_key('exchangeReferenceId', each_cursor)]['failed_validation']:
                    Logger.logger.warning(each_failure)
            else:
                Logger.logger.info("Validation passed.")
    except Exception as e:
        Logger.logger.error(e, exc_info=True)


if __name__ == '__main__':

    exchange_ref_ids = []

    try:
        parser = argparse.ArgumentParser()
        parser.add_argument(
            '--as_off_date',
            help='Provide the start date using field eventInsertDate (format yyyymmdd) for events which need to be validated. Default is 19700101',
            required=False,
            default='19700101'
        )
        parser.add_argument(
            '--info_log',
            help='If set to "True" the log will also output info messages. If set to "False" (default) only warnings will be printed',
            required=False,
            choices=['True', 'False'],
            default='False'
        )
        args = parser.parse_args()

        if args.info_log == 'False':
            Logger.logger.setLevel(logging.WARN)
        else:
            Logger.logger.setLevel(logging.INFO)

        main(args.as_off_date)
    except Exception as e:
        Logger.logger.error(e, exc_info=True)
