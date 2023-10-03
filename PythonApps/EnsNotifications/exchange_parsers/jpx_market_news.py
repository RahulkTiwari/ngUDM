# Third party libs
import json
import requests
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'jpx_market_news'


def get_exchange_ref_id(url, update_date):
    pattern = '.*\d{8}-\d{1,4}'
    elem_list = list(filter(None, url.replace('.html', '').split('/')))
    match = re.search(pattern, elem_list[-1])
    if match:
        # if url ends on e.g. 20230106-01 then take last 2 elements of url including /, excluding '.html'
        ref_id = f'{elem_list[-2]}/{elem_list[-1]}'
    else:
        # else we expect some string, and we use the last 2 elements together with today's date
        ref_id = f'{"/".join(elem_list[-2:])}|{update_date}'

    return ref_id


def get_notice_list():
    notice_list = []

    root_url = url_configuration[exchange]['url']

    # Lookback arbitrarily 3 response jsons. Increase number in case further lookback required.
    for each_number in list(range(0, 2)):

        url = f'{root_url}/news_ym_0{each_number}.json'
        raw_data = requests.get(
            url,
            headers={
                "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
            }
        )

        try:
            for each_row in json.loads(raw_data.text):
                if each_row['kind'] == 'マーケットニュース':
                    notice_list.append(each_row)
        except json.decoder.JSONDecodeError:
            pass

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)

    updated_date = f'{notice_table_row["updated_date"]["year"]}/{notice_table_row["updated_date"]["month"]}/{notice_table_row["updated_date"]["day"]}'
    exchange_reference_id = get_exchange_ref_id(notice_table_row['url'], updated_date)

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.jpx.co.jp{notice_table_row["url"]}'
    notice.eventSubject = notice_table_row['title']
    notice.eventPublishDate = convert_date(updated_date, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
