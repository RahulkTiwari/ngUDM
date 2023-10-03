# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import time
from datetime import datetime, timedelta
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice

exchange = 'tase_notices'


def get_notices(raw_content):

    soup = BeautifulSoup(raw_content, 'lxml')
    notice_table = soup.find('table', class_='table_c')
    notice_table_entries = notice_table.find_all('tr')

    return notice_table_entries[1:]


def get_notice_list():

    playwright = sync_playwright().start()
    # NOTE: apparently requires Firefox browser to fetch data
    browser = playwright.firefox.launch(headless=True)
    page = browser.new_page()
    url_root = url_configuration[exchange]['url']
    from_date = datetime.strftime(datetime.now() - timedelta(days=int(config_object['PROCESSING']['lookback_period'])), '%Y-%m-%dT%H:%M:%S.000Z')
    to_date = datetime.strftime(datetime.now(), '%Y-%m-%dT%H:%M:%S.000Z')
    parameters_url = f'q=%7B"DateFrom":"{from_date}","DateTo":"{to_date}","Page":1,"events":%5B15000,15200%5D,"subevents":%5B35,13,16,29,30,36,40,' \
                     f'19%5D%7D'

    try:
        page.goto(f'{url_root}?{parameters_url}', timeout=int(config_object['PROCESSING']['website_timeout']))
        page.wait_for_load_state('networkidle')
        time.sleep(5)

        page_content = page.content()
        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find_all('div', class_='feedItemMessage')

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
    notice = Notice(exchange)

    notice.exchangeReferenceId = notice_table_row.find('a', class_='messageContent ng-binding ng-isolate-scope')['param']
    notice.noticeSourceUniqueId = notice_table_row.find('a', class_='messageContent ng-binding ng-isolate-scope')['param']
    notice.eventInitialUrl = f'https://maya.tase.co.il/{notice_table_row.find("a", class_="messageContent ng-binding ng-isolate-scope")["href"]}'
    notice.eventSubject = notice_table_row.find('a', class_='messageContent ng-binding ng-isolate-scope').text.strip()
    notice.eventPublishDate = convert_date(notice_table_row.find('div', class_='feedItemDate hidden-md hidden-lg ng-binding').text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
