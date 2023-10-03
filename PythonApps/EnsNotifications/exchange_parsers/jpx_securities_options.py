# Third party libs
from datetime import datetime
from requests_html import HTMLSession
from bs4 import BeautifulSoup
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url

exchange = 'jpx_securities_options'


def get_notice_list():
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    current_year_url = url_configuration[exchange]['url']

    # Since the url contains the year we might need to look back to previous year's url in case of start of new year (January)
    this_month = datetime.now().month

    url_list = [current_year_url]

    if this_month == 1:
        url_list.append('https://www.jpx.co.jp/english/derivatives/products/individual/securities-options/05-archives-01.html')

    notice_list = []

    for each_url in url_list:

        page = session.get(each_url, headers=headers)

        page_content = page.text

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find_all('tr')

        notice_list.extend(notice_table_entries)

    return notice_list


def set_values(notice_table_row):
    columns = notice_table_row.find_all('td')
    notice = Notice(exchange)

    if not columns:
        if notice_table_row.find_all('th'):
            notice.raise_warning = False
            return notice
        else:
            return notice

    url_obj = Url(columns[3].a['href'])
    source_unique_id = f'{url_obj.all_elements[-1]}/{url_obj.document_root}'

    notice.exchangeReferenceId = source_unique_id
    notice.noticeSourceUniqueId = source_unique_id
    notice.eventInitialUrl = f'https://www.jpx.co.jp{columns[3].a["href"]}'
    notice.eventSubject = f'{columns[1].text} {columns[2].text}'
    notice.eventPublishDate = convert_date(columns[0].text.strip(), exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
