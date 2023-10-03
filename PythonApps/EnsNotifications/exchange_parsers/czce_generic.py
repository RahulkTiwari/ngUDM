# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import time
# Custom libs
from ext_libs.googletrans import Translator
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url


def get_czce_notice_list(exchange):

    playwright = sync_playwright().start()
    user_agent = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/69.0.3497.100 Safari/537.36'
    )

    browser = playwright.firefox.launch(headless=True)
    page = browser.new_page(user_agent=user_agent)

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))
        # Waiting for data to be loaded on the website
        page.wait_for_load_state('networkidle')
        time.sleep(5)

        page_content = page.content()
        soup = BeautifulSoup(page_content, 'lxml')
        notice_table = soup.find('div', class_='xxgkbiaoge').table.tbody
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


def set_czce_values(notice_table_row, exchange):
    column_values = notice_table_row.find_all('td')
    notice = Notice(exchange)

    event_publish_date = convert_date(column_values[2].text, exchange)
    exchange_ref_id = Url(column_values[1].a['href']).document_root

    # Only translate the Chinese language notice page
    if exchange == 'czce_exchange_announcements':
        translator = Translator()
        try:
            en_subject = translator.translate(column_values[1].a.text, src='zh-CN', dest='en').text
        except (AttributeError, TypeError):
            en_subject = 'Unable to translate subject'
    else:
        en_subject = column_values[1].a.text

    notice.exchangeReferenceId = exchange_ref_id
    notice.noticeSourceUniqueId = exchange_ref_id
    notice.eventInitialUrl = column_values[1].a['href']
    notice.eventSubject = en_subject
    notice.eventPublishDate = event_publish_date
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
