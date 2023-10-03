# Third party libs
import json
import requests
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url

exchange = 'jpx_news_release'


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
                if each_row['kind'] == 'JPXからのお知らせ':
                    notice_list.append(each_row)
        except json.decoder.JSONDecodeError:
            pass

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)

    # For Reference id take the last 2 elements of the url root, stripping html suffix
    url_obj = Url(notice_table_row['url'])
    exchange_reference_id = f'{url_obj.root.split("/")[-1]}/{url_obj.document_root}'
    updated_date = f'{notice_table_row["updated_date"]["year"]}/{notice_table_row["updated_date"]["month"]}/{notice_table_row["updated_date"]["day"]}'

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.jpx.co.jp{notice_table_row["url"]}'
    notice.eventSubject = notice_table_row['title']
    notice.eventPublishDate = convert_date(updated_date, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
