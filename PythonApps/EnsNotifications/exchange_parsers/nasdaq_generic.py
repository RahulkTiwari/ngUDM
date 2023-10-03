"""
Generic implementation for all Nasdaq exchanges. Called for each of the exchanges.
"""

# Third party libs
import json
import requests
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url


def get_ref_id(url):
    url_elems = Url(url)
    ref_id = url_elems.parameters['id']
    return ref_id


def get_nasdaq_notice_json(exchange):
    raw_data = requests.get(
        url_configuration[exchange]['url'],
        headers={
            "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
        }
    )

    data = raw_data.text.replace('handleResponse(', '')[:-2]

    notice_table_entries = json.loads(data)['results']['item']

    return notice_table_entries


def set_nasdaq_notice_values(notice_table_row, exchange):
    exchange_map = {
        'nasdaq_stockholm': 'NASDAQ OMX NORDIC',
        'nasdaq_copenhagen': 'NASDAQ OMX NORDIC',
        'nasdaq_helsinki': 'NASDAQ OMX NORDIC',
        'nasdaq_it': 'NASDAQ_OMX_NORDIC_IT_NOTICES',
        'nasdaq_omx': 'NASDAQ_OMX_NORDIC_NEWS_NOTICES'
    }

    notice = Notice(exchange)

    notice.exchangeReferenceId = get_ref_id(notice_table_row[url_configuration[exchange]['eventInitialUrl']])
    notice.noticeSourceUniqueId = get_ref_id(notice_table_row[url_configuration[exchange]['eventInitialUrl']])
    notice.eventInitialUrl = notice_table_row[url_configuration[exchange]['eventInitialUrl']]
    notice.eventSubject = notice_table_row[url_configuration[exchange]['eventSubject']]
    notice.eventPublishDate = convert_date(notice_table_row[url_configuration[exchange]['eventPublishDate']], exchange)
    notice.stormKey = f'{exchange_map[exchange]}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
