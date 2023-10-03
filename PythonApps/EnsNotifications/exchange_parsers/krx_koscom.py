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

exchange = 'krx_koscom'


def get_exchange_reference_id(href_input):
    url = Url(href_input)
    ref_id = url.parameters['nttSeq']

    return ref_id


def get_notice_list():

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        # login to page
        page.locator("#username2").click()
        page.locator("#username2").fill('smartstream2016')
        page.locator("#password2").click()
        page.locator("#password2").fill('Smartstream2017$')
        page.locator('input[value="LOG-IN"]').click()
        # waiting for page to finish loading
        page.wait_for_load_state('networkidle')

        page_content = page.content()
        soup = BeautifulSoup(page_content, 'lxml')

        notice_table_entries = soup.find('table', class_='table brd').tbody.find_all('tr')
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

    if not column_values:
        if notice_table_row.find_all('th'):
            notice.raise_warning = False
            return notice
        else:
            return notice

    exchange_ref_id = get_exchange_reference_id(column_values[2].a['href'])

    notice.exchangeReferenceId = exchange_ref_id
    notice.noticeSourceUniqueId = exchange_ref_id
    notice.eventInitialUrl = f'https://data.koscom.co.kr{column_values[2].a["href"]}'
    notice.eventSubject = column_values[2].a.text
    notice.eventPublishDate = convert_date(column_values[1].text.strip(), exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
