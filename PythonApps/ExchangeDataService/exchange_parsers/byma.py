# Third party libs
import requests
import json
from pdfminer.high_level import extract_text
import re
# Custom libs
from modules.source_config import source_config_obj
from modules.generic_functions import create_download_folder
from modules.config import config_object
from modules.logger import main_logger
from objects.exchange_data import ExchangeData

# Module globals
source_code = 'byma'
source_config = source_config_obj[source_code]
headers = {
    'User-Agent': (
        'Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/81.0.4044.141 Safari/537.36'
    )
}
txt_patterns = {
    'section_start': 'Valores.*negociables',
    'equity_section_start': 'Valores.*negociables.*de.*Renta.*Variable',
    'ticker': '^[A-Z0-9]+( )?$',
}


def get_link(content):
    result = False
    url = content['archivo']['url']
    match = re.search('venta.*en.*corto.*\.pdf', url, re.IGNORECASE)

    if match:
        result = True

    return result


def get_pdf_url():

    pdf_url = None

    response = requests.get(
        source_config['url'],
        headers=headers,
        verify=False
    )

    response_data = json.loads(response.text)['data'][0]['contenido']
    for each_notice in response_data:
        if get_link(each_notice):
            pdf_url = each_notice['archivo']['url']

    return pdf_url


def get_section(text_line):
    if re.search(txt_patterns['equity_section_start'], text_line):
        return 'Equity'
    elif re.search(txt_patterns['section_start'], text_line):
        return 'Other'
    else:
        return None


def get_tickers_from_pdf(pdf_loc):

    text = extract_text(pdf_loc)

    ticker_list = []
    current_section = None

    for each_line in text.split('\n'):
        section = get_section(each_line)
        current_section = section if section else current_section
        if current_section == 'Equity':
            if re.search(txt_patterns['ticker'], each_line):
                ticker_list.append(each_line.strip())

    return ticker_list


def retrieve_data():

    exchange_data = ExchangeData()

    pdf_url = get_pdf_url()

    if not pdf_url:
        main_logger.fatal(f'Unable to get pdf url for {source_code}')
        raise FileNotFoundError

    pdf_download = requests.get(
        pdf_url,
        headers=headers,
        verify=False
    )

    root_dir = config_object['PROCESSING']['resource_root_dir']
    download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')

    short_sell_pdf = f'{download_folder}/short_selling_list_{source_code}.pdf'
    open(short_sell_pdf, 'wb').write(pdf_download.content)

    short_sell_tickers = get_tickers_from_pdf(short_sell_pdf)

    # Adding trailing '3' to each ticker to mimic the trdse exchange ticker field. E.g. MELI would be MELI3 in trdse
    exchange_data.data = {ticker + '3': {'short_sell': 'Y', 'exchange_ticker': ticker} for ticker in short_sell_tickers}
    exchange_data.success = True

    return exchange_data
