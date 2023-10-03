# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice

exchange = 'b3_circular'


def get_notice_list():

    playwright = sync_playwright().start()

    browser = playwright.firefox.launch(headless=True)
    page = browser.new_page()

    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find('ul', class_='accordion list').find_all('li', class_='accordion-navigation')
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

    notice.exchangeReferenceId = notice_table_row.find_all('div', class_='content')[1].ul.li.a['title'].replace('-', '/', 1)
    notice.noticeSourceUniqueId = notice_table_row.find_all('div', class_='content')[1].ul.li.a['title'].replace('-', '/', 1)
    notice.eventInitialUrl = f'https://www.b3.com.br{notice_table_row.find_all("div", class_="content")[1].ul.li.a["href"]}'
    notice.eventSubject = notice_table_row.find('p', class_='resumo-oficio').text
    notice.eventPublishDate = convert_date(notice_table_row.find_all('div', class_='least-content')[0].text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
