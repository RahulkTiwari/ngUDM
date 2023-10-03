# Third party libs
import re
from bs4 import BeautifulSoup
from datetime import datetime
from playwright.sync_api import sync_playwright
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url

exchange = 'gpw_news'


def get_publish_date(html_input):
    match = re.search('[0-3]\d-[0-2][0-9]-202[0-9]', html_input)
    if match:
        start_pos = match.span()[0]
        end_pos = match.span()[1]
        return html_input[start_pos:end_pos]
    else:
        return datetime.now().strftime('%d-%m-%Y')


def get_exchange_ref_id(html_input):
    url = Url(html_input['href'])

    return url.parameters['cmn_id']


def get_notice_list():

    playwright = sync_playwright().start()

    browser = playwright.firefox.launch(headless=True)
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table = soup.find('ul', class_='list')
        notice_table_entries = notice_table.find_all('li', recursive=False)
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

    exchange_reference_id = get_exchange_ref_id(notice_table_row.h4.a)

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.gpw.pl/{notice_table_row.h4.a["href"]}'
    notice.eventSubject = notice_table_row.h4.a.text.strip()
    notice.eventPublishDate = convert_date(get_publish_date(notice_table_row.span.text), exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
