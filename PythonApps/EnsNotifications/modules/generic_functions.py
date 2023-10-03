# Third party libs
from datetime import timedelta
import re
from datetime import datetime
from office365.runtime.auth.user_credential import UserCredential
from office365.sharepoint.client_context import ClientContext
from office365.sharepoint.files.file import File
# Custom libs
from modules.config import config_object
from modules.url_config import url_configuration
from modules.mongo_connection import database_con
from modules.logger import main_logger
from modules.regex import regex_list


def read_credials(comment_char='#', sep='='):
    credential_file_location = config_object['SHAREPOINT']['credential_file_path']
    properties = {}
    with open(credential_file_location, "rt") as f:
        for line in f:
            var_l = line.strip()
            if var_l and not var_l.startswith(comment_char):
                key_value = var_l.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip()
                if value != '':
                    properties[key] = value
    # Check required properties
    for each_property in ['username', 'password']:
        if each_property not in properties.keys():
            main_logger.warning(f'{each_property} is missing in {credential_file_location} property file, using existing regex.xlsx')
            properties[each_property] = ''

    return properties


def clean_string(raw_string):
    """
    Method to remove line breaks from string. If the string starts or ends with a line break, just remove it. Line breaks in the middle of a string
    will be replaced with a space. In case this results in consecutive spaces then replace these with one space
    """
    replace_pattern = '[\r\n]'
    result = re.sub(replace_pattern, ' ', raw_string.strip())

    return re.sub('\s{2,}', ' ', result)


def check_notice(find_query):
    query_cursor = database_con.get_collection('workItem').find_one(find_query)
    if query_cursor:
        return True
    else:
        return False


def convert_date(raw_date, exchange):
    converted_date = datetime.strptime(raw_date, url_configuration[exchange]['dateformat'])

    return converted_date


def get_offset_date():
    date_offset = int(config_object['PROCESSING']['lookback_period'])
    date_test = datetime.now() - timedelta(days=date_offset)

    return date_test


def evaluate_regex(exchange, subject):
    in_scope = False

    # In case the subject can't be translated the notication is processed as valid to be manually evaluated
    if subject == 'Unable to translate subject':
        return True

    cleansed_subject = clean_string(subject)

    exchange_regex = regex_list[exchange]

    for index, each_regex in exchange_regex.iterrows():
        compiled_regex = re.compile(each_regex['Positive regex'], re.IGNORECASE)
        if compiled_regex.search(cleansed_subject):
            return True

    return in_scope


def update_regex_excel():
    """
    Static method to get the latest modified version of the regex.xlsx from Sharepoint, if it's more recent than the current version.
    """
    main_logger.debug('Checking for new regex Excel version...')
    sharepoint_config = config_object['SHAREPOINT']
    credentials = read_credials()
    date_string = open('resources/last_modified_date.txt', 'r').read()
    last_modified_date = datetime.strptime(date_string, '%Y-%m-%d %H:%M:%S')

    ctx = ClientContext(sharepoint_config['sharepoint_url']).with_credentials(
        UserCredential(credentials['username'], credentials['password']))
    web = ctx.web
    ctx.load(web)
    try:
        ctx.execute_query()
        file = ctx.web.get_file_by_server_relative_url(sharepoint_config['excel_url'])
        ctx.load(file)
        ctx.execute_query()
        file_last_modified = datetime.strptime(file.time_last_modified, '%Y-%m-%dT%H:%M:%SZ')

        if file_last_modified > last_modified_date:
            response = File.open_binary(ctx, sharepoint_config['excel_url'])
            response.raise_for_status()
            with open(f'{sharepoint_config["download_location"]}/regex.xlsx', 'wb') as download_file:
                download_file.write(response.content)
                with open(f'{sharepoint_config["download_location"]}/last_modified_date.txt', 'w') as lst_mod_date_file:
                    lst_mod_date_file.write(file_last_modified.strftime('%Y-%m-%d %H:%M:%S'))
                lst_mod_date_file.close()
                main_logger.info('Downloaded copy of regex Excel from Sharepoint')
        else:
            main_logger.debug(f'No new version of the regex Excel found, compared to {date_string}')
    except Exception as ex:
        main_logger.warning(f'Unable to query Sharepoint due to: "{str(ex)}".\n.....Using existing local copy of regex Excel.')
        pass
