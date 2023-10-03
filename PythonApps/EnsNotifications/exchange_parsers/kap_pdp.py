# Third party libs
import json
import requests
from datetime import datetime
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'kap_pdp'


def get_date(raw_date):
    now = datetime.now().strftime('%d.%m.%Y')
    converted_date = raw_date.strip().replace('Today', now)

    return converted_date


def get_notice_list():
    raw_data = requests.get(
        url_configuration[exchange]['url'],
        headers={
            "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
        }
    )

    notice_table_entries = json.loads(raw_data.text)

    return notice_table_entries


def set_values(notice_table_row):
    notice = Notice(exchange)

    subject = notice_table_row['basic']['title']

    notice.exchangeReferenceId = str(notice_table_row['basic']['disclosureIndex'])
    notice.noticeSourceUniqueId = str(notice_table_row['basic']['disclosureIndex'])
    notice.eventInitialUrl = f'https://www.kap.org.tr/en/Bildirim/{str(notice_table_row["basic"]["disclosureIndex"])}'
    notice.eventSubject = subject
    notice.eventPublishDate = convert_date(get_date(notice_table_row['basic']['publishDate']), exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
