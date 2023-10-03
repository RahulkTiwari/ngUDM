# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
from datetime import datetime
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.logger import main_logger
from objects.notice import Notice
from objects.url import Url

exchange = 'asx'


def get_publish_date(raw_value):
    if 'Today' in raw_value:
        publish_time = raw_value.replace('Today at ', '')
        publish_day = datetime.now().strftime('%d %b %Y')
        return convert_date(f'{publish_day} {publish_time}', exchange)
    else:
        return convert_date(raw_value, exchange)


def route_intercept(route):

    if route.request.resource_type == 'image':
        main_logger.debug(f'Blocking the image request to: {route.request.url}')
        return route.abort()
    if 'google' in route.request.url:
        main_logger.debug(f'blocking {route.request.url} as it contains Google')
        return route.abort()
    return route.continue_()


def get_notice_list():

    playwright = sync_playwright().start()

    browser = playwright.firefox.launch(headless=True)
    page = browser.new_page()

    try:
        page.route("**/*", route_intercept)
        page.route(re.compile(r'\.(jpg|png|svg)$'), lambda route: route.abort())
        page.goto(url_configuration[exchange]['url'], timeout=int(url_configuration[exchange]['website_timeout']))

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table = soup.find('div', id='filter-results-section')
        notice_table_entries = notice_table.find_all('a', class_='tile-anchor')
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

    publish_date = get_publish_date(notice_table_row.find('div', class_='time-notification').text)

    notice.exchangeReferenceId = Url(notice_table_row['href']).document_root
    notice.noticeSourceUniqueId = f'{Url(notice_table_row["href"]).document_root}|{publish_date}'
    notice.eventInitialUrl = f'https://asxonline.com{notice_table_row["href"]}'
    notice.eventSubject = notice_table_row.h2.text
    notice.eventPublishDate = publish_date
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
