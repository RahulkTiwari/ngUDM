# Third party libs
import requests
from pdfminer.high_level import extract_text
import re
# Custom libs
from modules.source_config import source_config_obj
from modules.generic_functions import create_download_folder
from modules.config import config_object
from objects.exchange_data import ExchangeData


source_code = 'dfm'

user_agent = (
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
    'AppleWebKit/537.36 (KHTML, like Gecko) '
    'Chrome/69.0.3497.100 Safari/537.36'
)


def get_tickers_from_pdf(pdf_loc):
    tickers_from_pdf = []
    text = extract_text(pdf_loc)
    go_skip = True

    for each_line in text.split('\n'):
        if re.search(r'Company Names as of', each_line):
            go_skip = False
        elif re.search(r'Contact us', each_line):
            go_skip = True
        if not go_skip:
            if each_line.strip() != '':
                if re.search(r'[A-Z]+ [-–] [A-Za-z0-9 ()]+', each_line):
                    tickers_from_pdf.append(each_line.split(' ')[0].strip().upper())

    return tickers_from_pdf


def get_tickers():

    headers = {
        'User-Agent': user_agent
    }
    pdf_download = requests.get(
        source_config_obj[source_code]['url'],
        headers=headers
    )

    root_dir = config_object['PROCESSING']['resource_root_dir']
    download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')

    short_sell_pdf = f'{download_folder}/ss_eligible_{source_code}.pdf'
    open(short_sell_pdf, 'wb').write(pdf_download.content)

    short_sell_tickers = get_tickers_from_pdf(short_sell_pdf)

    return short_sell_tickers


def retrieve_data():

    exchange_data = ExchangeData()

    list_of_tickers = get_tickers()

    exchange_data.data = {ticker: {'short_sell': 'Y'} for ticker in list_of_tickers}
    exchange_data.success = True

    return exchange_data
