# Third party libs
from bs4 import BeautifulSoup
from requests_html import HTMLSession
# Custom libs
from modules.generic_functions import convert_date
from objects.notice import Notice
from modules.url_config import url_configuration

exchange = 'tmx_advisory_notices'


def get_notice_list():

    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }

    url = url_configuration[exchange]['url']
    page = session.get(url, headers=headers)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    try:
        notice_table = soup.find('table', id='tnotices').tbody
        notice_list = notice_table.find_all('tr')
    except AttributeError:
        notice_list = []

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)
    notice_columns = notice_table_row.find_all('td')
    notice.exchangeReferenceId = notice_columns[2].text
    notice.noticeSourceUniqueId = notice_columns[2].text
    notice.eventInitialUrl = f'https://m-x.ca/en/resources/notices/advisory-notices/{notice_columns[3].a["href"]}'
    notice.eventSubject = notice_columns[3].text.strip()
    notice.eventPublishDate = convert_date(notice_columns[1].text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
