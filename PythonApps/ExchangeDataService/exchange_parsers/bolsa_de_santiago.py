# Third party libs
from playwright.sync_api import sync_playwright
import requests
from bs4 import BeautifulSoup
import time
from datetime import datetime
import re
from io import StringIO
from pdfminer.high_level import extract_text_to_fp
from pdfminer.layout import LAParams
# Custom libs
from modules.generic_functions import create_download_folder
from modules.source_config import source_config_obj
from modules.logger import main_logger
from objects.exchange_data import ExchangeData
from modules.config import config_object


source_code = 'bolsa_de_santiago'


def clean_line(raw_line):
    if raw_line.strip().isdigit() or raw_line.strip() == '' or re.search('ACCI[OÓ]N', raw_line):
        return None
    else:
        return re.sub('\d*', '', raw_line).strip()


def get_tickers_from_pdf(pdf_loc):
    output_string = StringIO()
    with open(pdf_loc, 'rb') as in_file:
        extract_text_to_fp(in_file, output_string, laparams=LAParams(), output_type='html', codec=None)

    soup_lines = BeautifulSoup(output_string.getvalue(), 'lxml').text.split('\n')

    go_next = True

    tickers = {}

    for each_line in soup_lines:
        if not re.search('ACCI[OÓ]N', each_line) and go_next:
            pass
        elif re.search('CFI-CFM', each_line):
            go_next = True
        else:
            go_next = False
            cleansed_line = clean_line(each_line)
            if cleansed_line:
                tickers[cleansed_line] = {
                    'short_sell': 'Y'
                }

    return tickers


def retrieve_data():

    exchange_data = ExchangeData()

    # Navigate to the main page
    playwright = sync_playwright().start()

    USER_AGENT = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/69.0.3497.100 Safari/537.36'
    )

    HEADER_LIST = {
        'authority': 'www.bolsadesantiago.com',
        'accept': 'application/json, text/plain, */*',
        'accept-language': 'en-US,en;q=0.9,nl-NL;q=0.8,nl;q=0.7',
        'referer': 'https://www.bolsadesantiago.com/informacion_mercado',
        'sec-ch-ua': '"Chromium";v="116", "Not)A;Brand";v="24", "Google Chrome";v="116"',
        'sec-ch-ua-mobile': '?0',
        'sec-ch-ua-platform': '"Windows"',
        'sec-fetch-dest': 'empty',
        'sec-fetch-mode': 'cors',
        'sec-fetch-site': 'same-origin',
        'user-agent': USER_AGENT
    }

    short_sell_pdf = ''
    url = source_config_obj[source_code]['url']

    browser = playwright.firefox.launch(headless=True)
    context = browser.new_context(extra_http_headers=HEADER_LIST)
    page = context.new_page()
    try:
        page.goto(url)

        page.wait_for_selector('div[class="col-lg-9"]')
        time.sleep(5)

        # Apparently months start counting at 0, so month is actual month minus 1
        quarter_months_map = {
            1: {'nav': 1, 'month': 11},
            2: {'nav': 2, 'month': 11},
            3: {'nav': 3, 'month': 11},
            4: {'nav': 1, 'month': 2},
            5: {'nav': 2, 'month': 2},
            6: {'nav': 3, 'month': 2},
            7: {'nav': 1, 'month': 5},
            8: {'nav': 2, 'month': 5},
            9: {'nav': 3, 'month': 5},
            10: {'nav': 1, 'month': 8},
            11: {'nav': 2, 'month': 8},
            12: {'nav': 3, 'month': 8}
        }

        # Navigating back to the proper month in the calendar
        for i in range(quarter_months_map[datetime.today().month]['nav']):
            page.locator('a[class="k-link k-nav-prev"]').click()

        # Determining the select dates
        day_range = range(25, 31)
        select_month = quarter_months_map[datetime.today().month]['month']
        year = datetime.today().year - 1 if datetime.today().month in [1, 2, 3] else datetime.today().year

        # Iterate over the days trying to find the short sell notice
        for each_day in day_range:
            select_date = f'{year}/{select_month}/{each_day}'
            page.locator(f'a[data-value="{select_date}"]').click()
            time.sleep(5)
            notice_content = page.content()

            soup = BeautifulSoup(notice_content, 'lxml')
            short_sell = soup.find('p', string=re.compile('venta corta'))
            if short_sell:
                url = short_sell.find_parent('div', class_='card-body').find('a', {'ng-show': 'noticia.Archivo'})['ng-href']
                main_logger.info(f'Short sell url found: {url}. Year: {year}, month: {select_month + 1}, day: {each_day}')
                pdf_response = requests.get(
                    url,
                    headers={
                        'User-Agent': (
                            'Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) '
                            'AppleWebKit/537.36 (KHTML, like Gecko) '
                            'Chrome/81.0.4044.141 Safari/537.36'
                        )
                    }
                )

                if pdf_response.status_code == 200:
                    root_dir = config_object['PROCESSING']['resource_root_dir']
                    download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')
                    short_sell_pdf = f'{download_folder}/short_selling_list_bolsa_de_santiago.pdf'
                    open(short_sell_pdf, 'wb').write(pdf_response.content)
                    exchange_data.success = True
                break

        browser.close()
        playwright.stop()
    except Exception as e:
        main_logger.info(e, exc_info=True)
        browser.close()
        playwright.stop()
        pass

    exchange_data.data = get_tickers_from_pdf(short_sell_pdf)

    return exchange_data
