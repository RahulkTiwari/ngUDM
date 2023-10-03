# Third party libs
from datetime import datetime
from requests_html import HTMLSession
from bs4 import BeautifulSoup
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'jse_market_notices'


def get_notice_number(href_string):
    pattern = r'[\s\r\n]*Notice Number:[\s\r\n]*'

    notice_number = re.sub(pattern, '', href_string)

    return notice_number


def get_notice_list():
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    current_year_url = url_configuration[exchange]['url']

    # Since the url contains the year we might need to look back to previous year's url in case of start of new year (January)
    this_month = datetime.now().month
    this_year = datetime.now().year

    url_list = [current_year_url]

    if this_month == 1:
        url_list.append(f'https://clientportal.jse.co.za/communication/jse-market-notices-{this_year - 1}')

    notice_list = []

    for each_url in url_list:

        page = session.get(each_url, headers=headers)

        page_content = page.text

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find_all('table', class_='table')

        notice_list.extend(notice_table_entries)

    return notice_list


def set_values(notice_table_row):
    columns = notice_table_row.find_all('td')
    notice = Notice(exchange)

    if not columns:
        return notice

    publish_date = convert_date(columns[2].text, exchange)
    notice_number = get_notice_number(columns[0].a.text)

    notice.exchangeReferenceId = f'{notice_number}/{publish_date.year}'
    notice.noticeSourceUniqueId = f'{notice_number}/{publish_date.year}'
    notice.eventInitialUrl = columns[0].a['href']
    notice.eventSubject = columns[4].text
    notice.eventPublishDate = publish_date
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
