# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url

exchange = 'omip_publications'


def get_notice_list():
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    url = url_configuration[exchange]['url']

    page = session.get(url, headers=headers)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    notice_table_entries = soup.find_all('div', class_='item-content')

    return notice_table_entries


def set_values(notice_table_row):
    notice = Notice(exchange)

    exchange_reference_id = Url(notice_table_row.find('a')['href']).document_root
    publish_date = notice_table_row.find('div', class_='category-date').text.split('|')[-1].strip()

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.omip.pt{notice_table_row.find("a")["href"]}'
    notice.eventSubject = notice_table_row.find('a').text.strip()
    notice.eventPublishDate = convert_date(publish_date, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
