# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import time
import tabula
import requests
# Custom libs
from modules.source_config import source_config_obj
from modules.generic_functions import create_download_folder
from modules.logger import main_logger
from objects.exchange_data import ExchangeData
from modules.config import config_object

source_code = 'egx'


def get_check_list():
    pdf_url = source_config_obj[source_code]['check_url']

    response = requests.get(
        f'{pdf_url}',
        verify=False,
        headers={
            "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
        }
    )

    root_dir = config_object['PROCESSING']['resource_root_dir']
    download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/exchange_files/downloads')

    isin_pdf = f'{download_folder}/short_selling_list__english.pdf'
    open(isin_pdf, 'wb').write(response.content)

    tables = tabula.read_pdf(isin_pdf, pages="all", multiple_tables=True)[0]

    return tables['ISIN Code'].tolist()


def retrieve_data():

    exchange_data = ExchangeData()

    playwright = sync_playwright().start()

    browser = playwright.firefox.launch(headless=True)
    context = browser.new_context(ignore_https_errors=True)
    page = context.new_page()

    try:
        page.goto(source_config_obj[source_code]['url'])
        time.sleep(10)

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'html.parser')

        notices_table = soup.find('table', id='ctl00_C_MI_GridView3')
        notices_table_entries = notices_table.find_all('tr')[1:]

        for each_row in notices_table_entries:
            exchange_data.data[each_row.find_all('td')[1].span.text] = {
                'short_sell': 'Y'
            }

        exchange_data.success = True

        browser.close()
        playwright.stop()
    except Exception as e:
        main_logger.info(e, exc_info=True)
        browser.close()
        playwright.stop()
        pass

    # Don't check currently, since this check is only applicable on 1 of the ~250 working days. So not worth the effort
    # isin_check_list = get_check_list()
    # eligible_isins = [isin for isin in isin_list if isin in isin_check_list]

    return exchange_data
