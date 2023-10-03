# Third party libs
import time
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeoutError
# Custom libs
from ext_libs.googletrans import Translator
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from modules.config import config_object
from modules.logger import main_logger
from objects.notice import Notice

exchange = 'borsa_italiana_market_connect'


def get_notice_list():

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        page.goto(url_configuration[exchange]['url'], timeout=int(config_object['PROCESSING']['website_timeout']))

        # login to page
        page.locator('#inputUsername').click()
        page.locator('#inputUsername').fill('smartstream01')
        page.locator('#inputPassword').click()
        page.locator('#inputPassword').fill('smartstream01')
        page.locator('#btnLogin').click()
        # waiting for page to finish loading
        time.sleep(5)
        # accessing the filter menu
        page.locator('i[class="fa fa-sliders"]').first.click()
        time.sleep(2)
        # uncollapsing Mercato
        page.locator('//*[@id="mercatoAvviso"]/div[1]/div[1]/div[2]/i').click()
        # selecting Mercato = Idem & blank
        page.locator('//*[@id="mercatoAvviso"]//*/input[@id="Idem"]').locator('xpath=..').click()
        page.locator('//*[@id="mercatoAvviso"]//*/input[@id=""]').locator('xpath=..').click()
        page.locator('#sumbitGenericId').click()

        # waiting for page to finish loading
        time.sleep(5)
        for i in range(3):
            page.locator('i[class="fa fa-share"]').click()
            time.sleep(1)

        page_content = page.content()
        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find('tbody').find_all('tr', {'role': 'row'})
        time.sleep(5)
        # logout from page
        page.locator('#off-button').click()
        time.sleep(2)
        page.locator('button[class="btn btn-primary"]').click()
        browser.close()
        playwright.stop()
    except Exception as e:
        main_logger.info(f'Error when scraping {exchange}\n\n')
        main_logger.info(e, exc_info=True)
        try:
            # logout from page
            page.locator('#off-button').click()
            time.sleep(2)
            page.locator('button[class="btn btn-primary"]').click()
        except PlaywrightTimeoutError:
            main_logger.info(f'Unable to logout from {exchange}\n\n')
            pass
        browser.close()
        playwright.stop()
        pass

    return notice_table_entries


def set_values(notice_table_row):
    notice = Notice(exchange)

    publication_date = convert_date(notice_table_row.find('td', class_='dataEm all').text, exchange)
    publication_year = publication_date.year
    exchange_reference_id = notice_table_row.find("td", class_="nAvv").text
    url = f'https://mcwp3.it.infrontfinance.com/PdfViewer/PdfShow.aspx/' \
          f'?username=itsborsa&password=itsborsa&service=mcw&type=avvisi&year={publication_year}&file={exchange_reference_id}.pdf'

    notice.exchangeReferenceId = f'{exchange_reference_id}|{publication_year}'
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = url
    translator = Translator()
    try:
        notice.eventSubject = translator.translate(f'{notice_table_row.find("td", class_="mittente").text} - '
                                               f'{notice_table_row.find("td", class_="oggetto").text}', src='it', dest='en').text
    except (AttributeError, TypeError):
        notice.eventSubject = 'Unable to translate subject'

    notice.eventPublishDate = publication_date
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
