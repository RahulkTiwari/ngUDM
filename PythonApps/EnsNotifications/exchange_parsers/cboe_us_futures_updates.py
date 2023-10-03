# Third party libs
import json
import requests
from datetime import datetime
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'cboe_us_futures_updates'


def get_notice_list():
    notice_list = []
    this_year = datetime.now().year
    year_list = [this_year]

    if datetime.now().month == 12:
        year_list.append(this_year - 1)

    for each_year in year_list:
        raw_data = requests.get(
            f'{url_configuration[exchange]["url"]}/{each_year}',
            headers={
                "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
            }
        )
        notice_list.extend(json.loads(raw_data.text)['alerts'])

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)

    notice.exchangeReferenceId = str(notice_table_row[url_configuration[exchange]['exchangeReferenceId']])
    notice.noticeSourceUniqueId = str(notice_table_row[url_configuration[exchange]['noticeSourceUniqueId']])
    notice.eventInitialUrl = notice_table_row[url_configuration[exchange]["eventInitialUrl"]]
    notice.eventSubject = notice_table_row[url_configuration[exchange]['eventSubject']]
    notice.eventPublishDate = convert_date(notice_table_row[url_configuration[exchange]['eventPublishDate']].rsplit('-', 1)[0], exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
