"""
Generic implementation for all Shanghai exchanges. Called for each of the exchanges.
"""

# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url


def get_shanghai_notice_list(exchange):
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }


    url = url_configuration[exchange]['url']

    page = session.get(url, headers=headers)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    notice_table = soup.find('ul', {'id':'all', 'class': 'sqs_serv_con'})
    notice_table_entries = notice_table.find_all('li')

    return notice_table_entries


def set_shanghai_values(notice_table_row, exchange):
    exchange_root_url_map = {
         'shfe_circularnews_circular': 'shfe',
         'shfe_circularnews_news': 'shfe',
         'ine_circularnews_circular': 'ine',
         'ine_circularnews_news': 'ine'
    }

    root_url = exchange_root_url_map[exchange]

    notice = Notice(exchange)

    exchange_reference_id = Url(notice_table_row.a['href']).document_root

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.{root_url}.com.cn{notice_table_row.a["href"]}'
    notice.eventSubject = notice_table_row.a.text.strip()
    notice.eventPublishDate = convert_date(notice_table_row.span.text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
