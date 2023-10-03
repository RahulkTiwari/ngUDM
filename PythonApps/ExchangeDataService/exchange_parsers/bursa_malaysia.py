# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
import pandas as pd
import requests
# Custom libs
from modules.source_config import source_config_obj
from modules.generic_functions import create_download_folder
from modules.config import config_object
from objects.exchange_data import ExchangeData


source_code = 'bursa_malaysia'


def retrieve_data():

    exchange_data = ExchangeData()

    url = source_config_obj[source_code]['url']

    headers = {
        'User-Agent': (
            'Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) '
            'AppleWebKit/537.36 (KHTML, like Gecko) '
            'Chrome/81.0.4044.141 Safari/537.36'
        )
    }

    session = HTMLSession()
    response = session.get(
        url,
        headers=headers
    )

    soup = BeautifulSoup(response.text, 'lxml')

    excel_element = soup.find('a', {'title': 'Bursa Approved Securities'})
    ss_excel_link = 'https://www.bursamalaysia.com' + excel_element['href']

    response = requests.get(
        ss_excel_link,
        headers=headers
    )

    root_dir = config_object['PROCESSING']['resource_root_dir']
    download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')

    short_sell_pdf = f'{download_folder}/short_selling_list_{source_code}.xlsx'
    open(short_sell_pdf, 'wb').write(response.content)

    ss_df = pd.read_excel(
        short_sell_pdf,
        header=5,
        converters={
            'No': str,
            'STOCK CODE': str
        }
    )

    for idx, each_sec in ss_df.iterrows():
        exchange_data.data[each_sec['ISIN CODE']] = {
            'short_sell': 'Y',
            'exchange_ticker': each_sec['STOCK CODE']
        }

    exchange_data.success = True

    return exchange_data
