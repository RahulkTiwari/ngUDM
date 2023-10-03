# Third party libs
from bs4 import BeautifulSoup
from requests_html import HTMLSession
# Custom libs
from modules.generic_functions import convert_date
from objects.notice import Notice
from objects.url import Url
from modules.url_config import url_configuration

exchange = 'nzx_announcements'


def get_notice_list():

    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }

    url = url_configuration[exchange]['url']
    page = session.get(url, headers=headers)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    notice_table = soup.find('table', class_='table-to-list announcements-table').tbody

    notice_list = notice_table.find_all('tr')

    return notice_list


def set_values(notice_table_row):
    supported_prefixes = ['FBU', 'KIWF', 'SPK']
    supported_notice_types = ['MKTUPDTE', 'GENERAL', 'CORPACT']

    notice = Notice(exchange)
    notice_type = notice_table_row.find('td', {'data-title': 'Type'}).span.text
    notice_prefix = notice_table_row.find('td', {'data-title': 'Company Code'}).span.a.text

    if notice_prefix in supported_prefixes and notice_type in supported_notice_types:

        exchange_reference_id = Url(notice_table_row.find('td', {'data-title': 'Title'}).span.a['href']).last_element

        notice.exchangeReferenceId = str(exchange_reference_id)
        notice.noticeSourceUniqueId = str(exchange_reference_id)
        notice.eventInitialUrl = 'https://www.nzx.com' + notice_table_row.find('td', {'data-title': 'Title'}).span.a['href']
        notice.eventSubject = notice_table_row.find('td', {'data-title': 'Title'}).span.a.text
        notice.eventPublishDate = convert_date(notice_table_row.find('td', {'data-title': 'Date'}).span.text, exchange)
        notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
        notice.valid_notice = True if notice.noticeSourceUniqueId else False
    else:
        notice.raise_warning = False

    return notice
