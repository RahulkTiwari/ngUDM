# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url

exchange = 'dce_clearing_notices'


def get_notice_list():

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
        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find_all('li', class_='news_list_li')
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

    event_publish_date = convert_date(notice_table_row.span.text, exchange)
    exchange_ref_id = Url(notice_table_row.a['href']).root.split('/')[-1]

    notice.exchangeReferenceId = exchange_ref_id
    notice.noticeSourceUniqueId = exchange_ref_id
    notice.eventInitialUrl = f'http://www.dce.com.cn{notice_table_row.a["href"]}'
    notice.eventSubject = notice_table_row.a['title']
    notice.eventPublishDate = event_publish_date
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
