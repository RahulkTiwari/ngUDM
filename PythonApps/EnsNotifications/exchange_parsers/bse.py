# Third party libs
from bs4 import BeautifulSoup
from datetime import datetime, timedelta
from playwright.sync_api import sync_playwright
from datetime import datetime
import time
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.logger import main_logger
from modules.config import config_object
from objects.notice import Notice

exchange = 'bse'


def get_notice_list():

    today = datetime.now()
    lookback = config_object['PROCESSING']['lookback_period']
    start_date = (today - timedelta(days=int(lookback))).strftime('%d/%m/%Y')
    end_date = today.strftime('%d/%m/%Y')

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch(headless=True)

    page = browser.new_page()
    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        page.locator('#ContentPlaceHolder1_txtDate').click()
        page.locator('#ContentPlaceHolder1_txtDate').fill(f'{start_date}')
        page.locator('#ContentPlaceHolder1_txtTodate').click()
        page.locator('#ContentPlaceHolder1_txtTodate').fill(f'{end_date}')
        page.locator('#ContentPlaceHolder1_btnSubmit').click()
        # Adding sleep to allow the filter to be applied
        time.sleep(3)

        notice_table_entries = []
        segment_names = ["Derivatives", "Commodity Derivatives", "Currency Derivatives", "IRD"]

        for segment in segment_names:
            page.locator('#ContentPlaceHolder1_ddlSegment').click()
            page.locator('#ContentPlaceHolder1_ddlSegment').select_option(label= segment)
            page.locator('#ContentPlaceHolder1_btnSubmit').click()
            time.sleep(3)

            page_content = page.content()

            soup = BeautifulSoup(page_content, 'lxml')
            notice_table_entries.extend(soup.find_all('a', {'class': 'tablebluelink', 'target': '_blank'}))
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

    notice.exchangeReferenceId = notice_table_row.parent.find_previous_sibling('td').text
    notice.noticeSourceUniqueId = notice_table_row.parent.find_previous_sibling('td').text
    notice.eventInitialUrl = f'https://www.bseindia.com/{notice_table_row.get("href")}'
    notice.eventSubject = notice_table_row.text
    notice.eventPublishDate = convert_date(notice.exchangeReferenceId.split('-')[0], exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
