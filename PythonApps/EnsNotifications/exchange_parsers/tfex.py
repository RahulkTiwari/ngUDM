# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url

exchange = 'tfex'


def get_ref_id(url):
    url_elems = Url(url)
    ref_id = url_elems.parameters['newsId']

    return ref_id


def get_notice_list():

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch()
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table = soup.find('div', class_='inner-content').tbody
        notice_table_entries = notice_table.find_all('tr')
        browser.close()
        playwright.stop()

    except Exception as e:
        main_logger.info(f'Error when scraping {exchange}\n\n')
        main_logger.info(e, exc_info=True)
        browser.close()
        playwright.stop()
        pass

    return notice_table_entries


def set_values(notice_table_row):
    column_values = notice_table_row.find_all('td')
    notice = Notice(exchange)

    exchange_reference_id = get_ref_id(column_values[5].a['href'])

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.tfex.co.th{column_values[5].a["href"]}'
    notice.eventSubject = column_values[4].text.strip()
    notice.eventPublishDate = convert_date(column_values[0].text.strip(), exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
