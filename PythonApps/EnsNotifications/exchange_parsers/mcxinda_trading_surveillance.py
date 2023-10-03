# Third party libs
import re
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

exchange = 'mcxinda_trading_surveillance'


def get_previous_page():
    this_month = datetime.now().month
    if this_month == 1:
        previous_date_dict = {
            'month': '12',
            'year': str(datetime.now().year - 1)
        }
    else:
        previous_date_dict = {
            'month': str(this_month - 1).zfill(2),
            'year': str(datetime.now().year)
        }

    return previous_date_dict


def get_publish_date(html_input):
    container_string = html_input.span.text
    match = re.search('[0-3]\d [A-Z][a-z]{2} 202[0-9]', container_string)
    if match:
        start_pos = match.span()[0]
        end_pos = match.span()[1]
        return container_string[start_pos:end_pos]
    else:
        return datetime.now().strftime('%d %b %Y')


def get_notices(raw_content):

    soup = BeautifulSoup(raw_content, 'lxml')
    notice_table_entries = soup.find('table', id='tblSimpleCircular').tbody.find_all('tr')

    return notice_table_entries


def get_notice_list():
    notices_list = []

    playwright = sync_playwright().start()
    ua = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/69.0.3497.100 Safari/537.36'
    )

    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page(user_agent=ua)

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))
        # Waiting for data to be loaded on the website
        page.wait_for_load_state('networkidle')
        time.sleep(10)

        page_content = page.content()
        notices_list.extend(get_notices(page_content))

        # Also scrape the previous month's circulars
        previous_page = get_previous_page()
        page.locator('#cph_InnerContainerMiddleContent1_C002_ddlYear').select_option(previous_page['year'])
        page.locator('#ddlMonth').select_option(previous_page['month'])
        page.locator('#btnSimple').click()
        # Add sleep to allow the next page to load
        time.sleep(5)

        next_page_content = page.content()
        notices_list.extend(get_notices(next_page_content))
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

    if len(column_values) < 2:
        notice.raise_warning = False
        return notice

    event_publish_date = convert_date(column_values[0].text, exchange)

    notice.exchangeReferenceId = f'MCX/TRD/{column_values[3].text.zfill(3)}/{event_publish_date.year}'
    notice.noticeSourceUniqueId = f'MCX/TRD/{column_values[3].text.zfill(3)}/{event_publish_date.year}'
    notice.eventInitialUrl = column_values[2].a['href']
    notice.eventSubject = column_values[2].a.text
    notice.eventPublishDate = event_publish_date
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
