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


def get_exchange_reference_id(href_input):
    pattern = r'Notice[_\- ][0-9]{1,5}'

    match = re.search(pattern, href_input)
    start_pos = match.span()[0] + len('Notice_')
    end_pos = match.span()[1]
    ref_id = href_input[start_pos:end_pos]
    converted_ref_id = f'{ref_id[2:]}/{ref_id[:2]}'

    return converted_ref_id


def find_table_rows(exchange):

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch()
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        # Checking if the website to explicitly indicates there are no notifications
        if soup.find('div', {'class': 'SinDatos'}):
            notice_table_entries = []
            main_logger.info(f'There are no notices published for the current period for {exchange}')
        # If not there then there should be notifications
        else:
            notice_table = soup.find('table', id='Contenido_Contenido_tblDatos').tbody
            notice_table_entries = notice_table.find_all('tr')[1:]
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
    column_values = notice_table_row.find_all('span')
    notice = Notice(exchange)

    try:
        exchange_reference_id = get_exchange_reference_id(column_values[1].a['href'])
        notice.exchangeReferenceId = exchange_reference_id
        notice.noticeSourceUniqueId = exchange_reference_id
        notice.eventInitialUrl = f'https://www.meff.es{column_values[1].a["href"]}'
        notice.eventSubject = column_values[1].text
        notice.eventPublishDate = convert_date(column_values[0].text.strip(), exchange)
        notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
        notice.valid_notice = True if notice.noticeSourceUniqueId else False
    except IndexError:
        notice.valid_notice = False

    return notice
