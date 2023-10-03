# Third party libs
from datetime import datetime
from requests_html import HTMLSession
from bs4 import BeautifulSoup
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'dgcx_notices'


def get_notice_list():
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    root_url = url_configuration[exchange]['url']

    # Since the url contains the year we might need to look back to previous year's url in case of start of new year (January)
    this_year = datetime.now().year
    this_month = datetime.now().month

    years = [this_year]

    if this_month == 1:
        years.append(this_year - 1)

    notice_list = []

    for each_year in years:
        url = f'{root_url}/{each_year}'

        page = session.get(url, headers=headers)

        page_content = page.text

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find_all('tr', class_=re.compile('ccm-block-document-library-row-[ab]'))

        notice_list.extend(notice_table_entries)

    return notice_list


def set_values(notice_table_row):
    columns = notice_table_row.find_all('td')
    notice = Notice(exchange)

    notice.exchangeReferenceId = str(columns[0].text)
    notice.noticeSourceUniqueId = str(columns[0].text)
    notice.eventInitialUrl = columns[2].a['href']
    notice.eventSubject = columns[2].a.text
    notice.eventPublishDate = convert_date(columns[1].text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
