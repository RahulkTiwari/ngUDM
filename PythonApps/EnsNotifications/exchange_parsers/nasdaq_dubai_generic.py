"""
Generic implementation for all Nasdaq Dubai exchanges. Called for each of the exchanges.
"""

# Third party libs
import json
import requests
import re
from datetime import datetime, timedelta
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice


def get_exchange_ref_id(headline):
    match = re.search('\d{2,3}\/[2][1-9]', headline)
    start_pos = match.span()[0]
    end_pos = match.span()[1]

    return headline[start_pos:end_pos]


def get_nasdaq_notice_json(exchange):

    type_map = {
        'nasdaq_dubai_circulars': 'Circular',
        'nasdaq_dubai_notices': 'Notice'
    }

    from_date = (datetime.now() - timedelta(weeks=8)).strftime('%Y-%m-%d')
    to_date = (datetime.now() + timedelta(days=3)).strftime('%Y-%m-%d')

    raw_data = requests.get(
        url_configuration[exchange]['url'],
        headers={
            'User-Agent': 'Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36'
        },
        params={
            'from': from_date,
            'to': to_date,
            'announcement_type': type_map[exchange],
            'issuer': 'Nasdaq+Dubai'
        }
    )

    try:
        notice_table_entries = json.loads(raw_data.text)['root']
    except json.JSONDecodeError:
        notice_table_entries = json.loads(raw_data.text[1:])['root']

    return notice_table_entries


def set_nasdaq_notice_values(notice_table_row, exchange):
    notice = Notice(exchange)

    exchange_reference_id = get_exchange_ref_id(notice_table_row[url_configuration[exchange]['exchangeReferenceId']])

    notice.exchangeReferenceId = str(exchange_reference_id)
    notice.noticeSourceUniqueId = str(exchange_reference_id)
    notice.eventInitialUrl = f'https://www.nasdaqdubai.com/trading/disclosures/{notice_table_row[url_configuration[exchange]["eventInitialUrl"]]}'
    notice.eventSubject = notice_table_row[url_configuration[exchange]['eventSubject']]
    notice.eventPublishDate = convert_date(notice_table_row[url_configuration[exchange]['eventPublishDate']], exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
