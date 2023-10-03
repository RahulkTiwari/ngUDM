# Third party libs
import json
import requests
from datetime import datetime
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from modules.logger import main_logger

exchange = 'cboe_euro_derivates'


def get_notice_list():
    # Since the url contains the year we might need to look back to previous year's url in case of start of new year (January)
    this_year = datetime.now().year
    this_month = datetime.now().month

    years = [this_year]

    if this_month == 1:
        years.append(this_year - 1)

    notice_list = []

    for each_year in years:

        raw_data = requests.get(
            f'{url_configuration[exchange]["url"]}/{each_year}',
            headers={
                "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
            }
        )

        notice_table_entries = json.loads(raw_data.text)['alerts']
        notice_list.extend(notice_table_entries)

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)

    try:
        notice.exchangeReferenceId = str(notice_table_row[url_configuration[exchange]['exchangeReferenceId']])
        notice.noticeSourceUniqueId = str(notice_table_row[url_configuration[exchange]['noticeSourceUniqueId']])
        notice.eventInitialUrl = notice_table_row[url_configuration[exchange]["eventInitialUrl"]]
        notice.eventSubject = notice_table_row[url_configuration[exchange]['eventSubject']]
        notice.eventPublishDate = convert_date(notice_table_row[url_configuration[exchange]['eventPublishDate']], exchange)
        notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
        notice.valid_notice = True if notice.noticeSourceUniqueId else False
    except KeyError:
        main_logger.warning(f'Missing items to create notice for {exchange}, '
                            f'{str(notice_table_row[url_configuration[exchange]["exchangeReferenceId"]])}')

    return notice
