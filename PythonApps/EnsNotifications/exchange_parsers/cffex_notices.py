# Third party libs
from bs4 import BeautifulSoup
from requests_html import HTMLSession
# Custom libs
from modules.generic_functions import convert_date
from objects.notice import Notice
from modules.url_config import url_configuration

exchange = 'cffex_notices'


def get_notice_list():

    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }

    url = url_configuration[exchange]['url']
    page = session.get(url, headers=headers)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    notice_table = soup.find('div', class_='contentBox fl mobile-list')

    notice_list = notice_table.find_all('div', class_='to_detail')

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)

    exchange_reference_id = notice_table_row.find('div', class_='news_content')['onclick'].split('/')[-1].replace('.html\'', '')
    notice_url_idx = notice_table_row.find('div', class_='news_content')['onclick'].find('/en_new/')

    notice.exchangeReferenceId = f'{exchange_reference_id}'
    notice.noticeSourceUniqueId = f'{exchange_reference_id}'
    notice.eventInitialUrl = f'http://www.cffex.com.cn/{notice_table_row.find("div", class_="news_content")["onclick"][notice_url_idx:-1]}'
    notice.eventSubject = f'{notice_table_row.find("div", class_="news_content").text}'
    notice.eventPublishDate = convert_date(notice_table_row.find('div', class_='news_time').text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
