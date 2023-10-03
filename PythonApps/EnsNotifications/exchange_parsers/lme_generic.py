# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice


def get_press_release_data(html_block):
    elements = html_block.find('ul', class_='search-listing-card__meta meta')
    element_parsed = re.sub(r'\n\s', '|', elements.text).split('|')
    element_list = [el.strip() for el in element_parsed if el.strip() != '']

    elements_dict = {
        'exchange_reference_id': '/'.join(html_block.find('a', class_='search-listing-card__link')['href'].split('/')[-2:]),
        'publish_date': element_list[1]
    }

    return elements_dict


def get_data_points(html_block):
    elements = html_block.find('ul', class_='search-listing-card__meta meta')
    element_parsed = re.sub(r'\n\s', '|', elements.text).split('|')
    element_list = [el.strip() for el in element_parsed if el.strip() != '']
    elements_dict = {
        'exchange_reference_id': element_list[2].replace('/', '-'),
        'publish_date': element_list[1]
    }

    return elements_dict


def find_table_rows(exchange):

    playwright = sync_playwright().start()
    user_agent = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/69.0.3497.100 Safari/537.36'
    )

    browser = playwright.firefox.launch(headless=True)
    context = browser.new_context(
        user_agent=user_agent
    )

    page = context.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))
        page.wait_for_load_state('networkidle')

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find('ol').find_all('li', class_='search-listing__item')
        browser.close()
        playwright.stop()

    except Exception as e:
        main_logger.info(f'Error when scraping {exchange}\n\n')
        main_logger.info(e, exc_info=True)
        browser.close()
        playwright.stop()
        pass

    return notice_table_entries


def find_values(notice_table_row, exchange):
    notice = Notice(exchange)

    if exchange in ['lme_clearing_circ', 'lme_member_notice']:
        elements = get_data_points(notice_table_row)
    elif exchange in ['lme_press_release']:
        elements = get_press_release_data(notice_table_row)

    notice.exchangeReferenceId = elements['exchange_reference_id']
    notice.noticeSourceUniqueId = elements['exchange_reference_id']
    notice.eventInitialUrl = notice_table_row.find('a')['href']
    notice.eventSubject = notice_table_row.find('a').text.strip()
    notice.eventPublishDate = convert_date(elements['publish_date'], exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
