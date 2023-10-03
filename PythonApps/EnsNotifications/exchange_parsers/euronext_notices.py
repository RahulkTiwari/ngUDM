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

exchange = 'euronext_notices'


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
        notice_table_entries = soup.find_all('tbody', {'id': re.compile(r'row_ecap_\d')})
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
    notice_row = notice_table_row.find('tr', {'class': re.compile(r'row_\d.+')})
    notice = Notice(exchange)

    publish_date = notice_row.find('td', {'class': re.compile(r'noticedate')}).text
    event_type = prepare_text(notice_row.find('td', {'class': re.compile(r'noticename')}).div.text)
    instrument = prepare_text(notice_row.find('td', {'class': re.compile(r'instruments')}).text)
    ref_id = notice_row.find('td', {'class': re.compile(r'noticenumber')}).text
    notice_id = notice_row['class'][0].replace('row_', '')
    url = f'https://connect2.euronext.com/en/listview/notice-download?noticeNumber={ref_id}&id={notice_id}&type=PDF'

    notice.exchangeReferenceId = ref_id
    notice.noticeSourceUniqueId = ref_id
    notice.eventInitialUrl = url
    notice.eventSubject = ' - '.join([instrument, event_type])
    notice.eventPublishDate = convert_date(publish_date, exchange)
    notice.stormKey = '|'.join([notice.exchangeSourceName, notice.noticeSourceUniqueId, publish_date.replace(' ','')])
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
