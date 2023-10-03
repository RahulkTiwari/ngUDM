# Third party libs
import requests
import pandas as pd
from requests_html import HTMLSession
from bs4 import BeautifulSoup
from datetime import datetime
# Custom libs
from modules.source_config import source_config_obj
from modules.generic_functions import create_download_folder
from modules.config import config_object
from objects.exchange_data import ExchangeData

source_code = 'hkex'

root_dir = config_object['PROCESSING']['resource_root_dir']
download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')


def get_current_eligible():
    url = source_config_obj[source_code]['url_current']

    response = requests.get(
        f'{url}',
        headers={
            "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
        }
    )

    shortsell_excel = f'{download_folder}/ListOfSecurities.xlsx'

    open(shortsell_excel, 'wb').write(response.content)

    securities_df = pd.read_excel(
        shortsell_excel,
        header=2,
        sheet_name='ListOfSecurities',
        converters={
            'Stock Code': int,
            'Shortsell Eligible': str
        }
    )

    # Initial data type as int to remove leading zeros. Now convert to intended data type str
    securities_df['Stock Code'] = securities_df['Stock Code'].astype(str)

    ss_securities = securities_df[securities_df['Shortsell Eligible'] == 'Y']['Stock Code']

    return ss_securities


def get_future_eligible():
    added_securities_df = pd.Series(dtype='object')

    session = HTMLSession()
    root_url = 'https://www.hkex.com.hk'

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }

    url = source_config_obj[source_code]['url_coming']

    page_content = session.get(url, headers=headers)

    soup = BeautifulSoup(page_content.text, 'html.parser')

    overview_table = soup.find('table', class_='table migrate').tbody

    first_row_cols = overview_table.find('tr').find_all('td')

    today = datetime.today()

    csv_date = datetime.strptime(first_row_cols[0].text, '%d/%m/%Y')

    if csv_date > today and 'Add' in first_row_cols[3].text:
        doc_url = first_row_cols[2].find('a', string='Download CSV')['href']
        url = root_url + doc_url
        file_name = doc_url.split('/')[-1]

        response = requests.get(
            f'{url}',
            headers={
                "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
            }
        )

        shortsell_csv = f'{download_folder}/{file_name}'

        open(shortsell_csv, 'wb').write(response.content)

        securities_df = pd.read_csv(
            shortsell_csv,
            header=3,
            index_col=False,
            converters={
                'No.': str,
                'Stock Code': str,
                'Remarks': str
            }
        )

        added_securities_df = securities_df[securities_df['Remarks'] == 'Add']['Stock Code']

    return added_securities_df


def retrieve_data():

    exchange_data = ExchangeData()

    current_short_sell = get_current_eligible()
    announced_short_sell = get_future_eligible()

    ss_securities = pd.concat([current_short_sell, announced_short_sell])
    exchange_data.data = {ticker: {'short_sell': 'Y'} for ticker in ss_securities.values.tolist()}
    exchange_data.success = True

    return exchange_data
