# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
# Custom libs
from modules.generic_functions import convert_date
from modules.logger import main_logger
from objects.notice import Notice
from modules.url_config import url_configuration
from modules.config import config_object

exchange = 'budapest_news'


def get_notice_list():

    # Specify the number of webpages to lookback to
    pages_lookback = 8
    root_url = url_configuration[exchange]['url']

    notice_list = []

    for each_page in list(range(1, pages_lookback)):
        playwright = sync_playwright().start()

        browser = playwright.chromium.launch()
        page = browser.new_page()
        try:
            page.goto(f'{root_url}#{each_page}', timeout=int(config_object['PROCESSING']['website_timeout']))
            page_content = page.content()

            soup = BeautifulSoup(page_content, 'lxml')
            notice_table = soup.find('ul', class_='content-list-elements')
            notice_table_entries = notice_table.find_all('li')

            notice_list.extend(notice_table_entries)
            browser.close()
            playwright.stop()

        except Exception as e:
            main_logger.info(f'Error when scraping {exchange}\n\n')
            main_logger.info(e, exc_info=True)
            browser.close()
            playwright.stop()
            pass

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)

    notice.exchangeReferenceId = f'{notice_table_row.a["href"].split("/")[-1]}'
    notice.noticeSourceUniqueId = f'{notice_table_row.a["href"].split("/")[-1]}'
    notice.eventInitialUrl = f'https://bse.hu/{notice_table_row.a["href"]}'
    notice.eventSubject = f'{notice_table_row.a.div.h2.text}'
    notice.eventPublishDate = convert_date(notice_table_row.a.div.span.text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
