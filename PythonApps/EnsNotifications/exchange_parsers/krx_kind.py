# Third party libs
import math
import re
import time
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
# Custom libs
from ext_libs.googletrans import Translator
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from objects.notice import Notice
from modules.logger import main_logger

exchange = 'krx_kind'


def get_doc_id(column_value):
    on_click_value = column_value.find('a')['onclick']
    doc_regex_pattern = re.compile('\d+')
    match = doc_regex_pattern.search(on_click_value)
    start_pos = match.start()
    end_pos = match.end()
    doc_id = on_click_value[start_pos:end_pos]

    return doc_id


def get_notices(raw_content):

    soup = BeautifulSoup(raw_content, 'lxml')
    notice_table = soup.find('table', class_='list type-00 mt10')
    notice_table_entries = notice_table.find_all('tr')

    return notice_table_entries


def translate(foreign_string):
    translator = Translator()
    try:
        translated_string = translator.translate(foreign_string, src='ko', dest='en').text
    except (AttributeError, TypeError):
        translated_string = ''

    return translated_string


def get_notice_list():
    notices_list = []

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        # Only selecting Derivate notifications
        page.locator('input[id="rDrv"]').check()
        page.locator('a[title="GO"]').click()
        # Waiting for data to be loaded on the website
        page.wait_for_load_state('networkidle')
        time.sleep(5)

        page_counter = 1
        # Calculate the number of pages to iterate through
        number_of_pages = math.ceil(int(BeautifulSoup(page.content(), 'lxml').find('div', class_='info type-00').em.text)/15)
        main_logger.debug(f'Number of pages is {number_of_pages}')

        while page_counter <= number_of_pages:
            main_logger.debug(f'Scrape page {page_counter} of {number_of_pages}')
            page.locator('a[href="#nextPage"]').click()
            page_content = page.content()
            notices_list.extend(get_notices(page_content))
            page_counter += 1
        browser.close()
        playwright.stop()

    except Exception as e:
        main_logger.info(f'Error when scraping {exchange}\n\n')
        main_logger.info(e, exc_info=True)
        browser.close()
        playwright.stop()
        pass

    return notices_list


def set_values(notice_table_row):
    column_values = notice_table_row.find_all('td')
    notice = Notice(exchange)

    if not column_values:
        if notice_table_row.find_all('th'):
            notice.raise_warning = False
            return notice
        else:
            return notice

    document_id = get_doc_id(column_values[3])

    event_sub = []

    for each_item in [2, 3]:
        try:
            event_sub.append(translate(column_values[each_item].a.text))
        except AttributeError:
            event_sub.append('')

    event_subject = '|'.join([string for string in event_sub if string != ''])

    notice.exchangeReferenceId = document_id
    notice.noticeSourceUniqueId = document_id
    notice.eventInitialUrl = f'https://kind.krx.co.kr/common/disclsviewer.do?method=search&acptno={document_id}&docno=&viewerhost=&viewerport='
    notice.eventSubject = event_subject if event_subject != '' else 'Unable to translate subject'
    notice.eventPublishDate = convert_date(column_values[1].text.strip(), exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
