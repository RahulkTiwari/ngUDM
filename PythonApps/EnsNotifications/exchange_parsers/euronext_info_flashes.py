# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import re
import time
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url

exchange = 'euronext_info_flashes'


def prepare_text(raw_text):
    remove_strings = ['Toggle Visibility', '\n', '\t',  '(Updated)']
    cleansed_string = raw_text

    for each_string in remove_strings:
        cleansed_string = cleansed_string.replace(each_string, '')

    return cleansed_string.strip()

def get_notice_list():
    notice_table_entries = []

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        # First login to the website
        page.locator('#edit-name').click()
        page.locator('#edit-name').fill(url_configuration[exchange]['user'])
        page.locator('#edit-pass').click()
        page.locator('#edit-pass').fill(url_configuration[exchange]['secret'])
        page.locator('#edit-submit').click()
        page.wait_for_load_state('networkidle')
        time.sleep(5)

        # Scrape notifications
        page_content = page.content()
        soup = BeautifulSoup(page_content, 'lxml')
        notice_table = soup.find('table', {'class': re.compile(r'table table-sm')}).tbody
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
    notice_row = notice_table_row.find_all('td')
    notice = Notice(exchange)

    url_obj = Url(notice_row[1].find('a', {'class': 'awl-info-flashes-download'})['href'])

    ref_id = url_obj.document_root

    notice.exchangeReferenceId = ref_id
    notice.noticeSourceUniqueId = ref_id
    notice.eventInitialUrl = f'https://connect2.euronext.com{url_obj.root}/{url_obj.document_full}'
    notice.eventSubject = notice_row[1].find('div', {'class': 'ml-2 mr-1'}).a['title']
    notice.eventPublishDate = convert_date(notice_row[0].div.div.text.strip(), exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
