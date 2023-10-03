# Third party libs
import json
import requests
import time
# Custom libs
from modules.generic_functions import convert_date
from modules.logger import main_logger
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'nse'


def get_notice_list():

    retries = 3

    for each_try in range(retries):
        try:
            raw_data = requests.get(
                url_configuration[exchange]['url'],
                headers={
                    "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) "
                                  "AppleWebKit/537.36 (KHTML, like Gecko) "
                                  "Chrome/81.0.4044.141 Safari/537.36"
                }
            )
            notice_table_entries = json.loads(raw_data.text)['data']
            break

        except json.decoder.JSONDecodeError:
            if each_try < retries - 1:
                main_logger.info(f'Request response code {raw_data.status_code} and text {raw_data.reason}')
                main_logger.info(f'Failed to parse json for {exchange} Retry number {each_try + 1}')
                time.sleep(5)
            else:
                main_logger.info(f'Request response code {raw_data.status_code} and text {raw_data.reason}')
                main_logger.info(f'Failed final to parse json for {exchange} Retry number {each_try + 1}')
                raise json.decoder.JSONDecodeError

    return notice_table_entries


def set_values(notice_table_row):

    in_scope_circ_departments = ['Commodity Derivatives', 'Currency Derivatives', 'Futures & Options']

    notice = Notice(exchange)

    if notice_table_row['circDepartment'] in in_scope_circ_departments:

        try:
            exchange_reference_id = notice_table_row['fileDept'] + notice_table_row['circNumber']
        except TypeError:
            return notice

        notice.exchangeReferenceId = exchange_reference_id
        notice.noticeSourceUniqueId = exchange_reference_id
        notice.eventInitialUrl = notice_table_row[url_configuration[exchange]['eventInitialUrl']]
        notice.eventSubject = notice_table_row[url_configuration[exchange]['eventSubject']]
        notice.eventPublishDate = convert_date(notice_table_row[url_configuration[exchange]['eventPublishDate']], exchange)
        notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
        notice.valid_notice = True if notice.noticeSourceUniqueId else False
    else:
        notice.raise_warning = False

    return notice
