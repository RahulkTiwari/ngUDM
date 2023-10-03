# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
from datetime import datetime
import time
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url


def get_notices(raw_content):

    soup = BeautifulSoup(raw_content, 'lxml')
    notice_table = soup.find('table', class_='table_c')
    notice_table_entries = notice_table.find_all('tr')

    return notice_table_entries[1:]


def find_table_rows(exchange):
    notices_list = []
    this_month = datetime.now().month
    this_year = datetime.now().year

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))
        # Waiting for data to be loaded on the website
        page.wait_for_load_state('networkidle')

        page_content = page.content()
        notices_list.extend(get_notices(page_content))

        if this_month == 1:
            # In january also scrape the previous year releases
            page.locator('#query_year').select_option(str(this_year - 1))
            page.get_by_role('button', name='Query').click()
            # page.locator('#button').click()
            # Add sleep to allow the next page to load
            time.sleep(5)

            next_page_content = page.content()
            notices_list.extend(get_notices(next_page_content))
    except Exception as e:
        main_logger.info(f'Error when scraping {exchange}\n\n')
        main_logger.info(e, exc_info=True)
        browser.close()
        playwright.stop()
        pass

    browser.close()
    playwright.stop()

    return notices_list


def find_values(notice_table_row, exchange):
    columns = notice_table_row.find_all('td')
    notice = Notice(exchange)

    if Url(columns[1].a['href']).document_root:
        exchange_reference_id = Url(columns[1].a['href']).document_root
    else:
        exchange_reference_id = columns[1].a['href']

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = columns[1].a['href']
    notice.eventSubject = columns[1].a.text
    notice.eventPublishDate = convert_date(columns[0].text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
