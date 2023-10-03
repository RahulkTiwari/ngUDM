# Third party libs
from bs4 import BeautifulSoup
from requests_html import HTMLSession
from datetime import datetime
import re
# Custom libs
from objects.notice import Notice
from objects.url import Url
from modules.url_config import url_configuration

exchange = 'mgex_news_rel'


def get_exchange_reference_id(href_input):
    substring = Url(href_input).document_root

    return substring


def get_date(string_value):
    today = datetime.now()

    date_pattern = '(0|1)?\d{1}-(0|1|2|3)?\d{1}-(0|1|2)\d'

    compiled_pattern = re.compile(date_pattern)
    match = compiled_pattern.search(string_value)
    if match:
        start_pos = match.span()[0]
        end_pos = match.span()[1]
        converted_string = datetime.strptime(match.string[start_pos:end_pos], url_configuration[exchange]['dateformat'])

        return converted_string

    return today


def get_notice_list():

    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }

    url = url_configuration[exchange]['url']
    page = session.get(url, headers=headers)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    raw_notice_table = soup.find_all('li')

    return raw_notice_table


def set_values(notice_table_row):
    notice = Notice(exchange)

    exchange_ref_id = get_exchange_reference_id(notice_table_row.a['href'])

    notice.exchangeReferenceId = exchange_ref_id
    notice.noticeSourceUniqueId = exchange_ref_id
    notice.eventInitialUrl = f'https://www.mgex.com/{notice_table_row.a["href"]}'
    notice.eventSubject = f'{notice_table_row.a.text}'
    notice.eventPublishDate = get_date(notice_table_row.text)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
