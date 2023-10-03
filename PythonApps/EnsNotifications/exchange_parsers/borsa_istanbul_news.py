# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import time
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url

exchange = 'borsa_istanbul_news'


def get_notice_list():

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch()
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))
        time.sleep(5)
        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table = soup.find('div', class_='content-area')
        notice_table_entries = notice_table.find_all('div', class_='allAnnouncementsListText')
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

    notice.exchangeReferenceId = Url(notice_table_row.a['href']).last_element
    notice.noticeSourceUniqueId = Url(notice_table_row.a['href']).last_element
    notice.eventInitialUrl = f'https://www.borsaistanbul.com{notice_table_row.a["href"]}'
    notice.eventSubject = notice_table_row.a.text
    notice.eventPublishDate = convert_date(notice_table_row.span.text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
