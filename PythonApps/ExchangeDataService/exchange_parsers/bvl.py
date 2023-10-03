# Third party libs
from bs4 import BeautifulSoup
import json
import requests
import re
import tabula
import pandas as pd
from io import StringIO
from pdfminer.high_level import extract_text_to_fp
from pdfminer.layout import LAParams
# Custom libs
from modules.generic_functions import create_download_folder
from modules.source_config import source_config_obj
from modules.config import config_object
from modules.logger import main_logger
from objects.exchange_data import ExchangeData

source_code = 'bvl'
root_dir = config_object['PROCESSING']['resource_root_dir']
user_agent = 'Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36'

verify_tickers = None


def get_ticker(line):
    ticker = None
    if line.startswith('LISTA') or line.startswith('ACCIONES') or line.startswith('--') or line.startswith('  '):
        pass
    else:
        ticker = line

    return ticker


def list_compare(actual, expected):
    changes = {}

    actual.sort()
    expected.sort()

    changes['added'] = [str(ticker) for ticker in actual if ticker not in expected]
    changes['removed'] = [str(ticker) for ticker in expected if ticker not in actual]

    changes['identical'] = False if changes['added'] or changes['removed'] else True

    return changes


def get_verify_tickers(pdf_doc):
    tickers_df = tabula.read_pdf(
        pdf_doc,
        pages='all'
    )

    all_tickers = []

    for each_row in tickers_df[0]['ACCIONES DE CAPITAL']:
        if get_ticker(each_row):
            all_tickers += each_row.split(' ')

    return all_tickers


def get_expected_tickers():
    csv_df = pd.read_csv(
        f'{root_dir}/exchange_files/{source_code}/resources/{source_config_obj[source_code]["ticker_file"]}',
        header=0,
        dtype=str
    )

    tickers = csv_df['ticker'].values.tolist()

    return tickers


def get_section(html_line):
    if re.search('LISTA\s[12]', html_line):
        return 'LISTA12'
    elif re.search('ACCIONES DE INVERSI[ÓO]N', html_line):
        return 'INVERSION'
    elif re.search('LISTA\s[34]', html_line):
        return 'LISTA34'
    else:
        return None


def get_tickers(pdf_loc):
    tickers = []

    output_string = StringIO()

    with open(pdf_loc, 'rb') as in_file:
        extract_text_to_fp(in_file, output_string, laparams=LAParams(), output_type='html', codec=None)

    data = output_string.getvalue()
    current_section = None

    for each_line in data.split('\n'):
        section = get_section(each_line)
        current_section = section if section else current_section
        if current_section in ['LISTA12', 'INVERSION']:
            try:
                left_pos_expr = re.search(r'left:[0-9]{1,3}px;', each_line, flags=0).group(0)
                left_pos = int(left_pos_expr.replace('left:', '').replace('px;', ''))
            except AttributeError:
                left_pos = 'not found'
            if isinstance(left_pos, int) and 100 < left_pos < 350:
                possible_ticker = get_ticker(BeautifulSoup(each_line, 'lxml').text)
                if possible_ticker and possible_ticker in verify_tickers:
                    tickers.append(possible_ticker)
        elif current_section == 'LISTA34':
            current_section = None
        else:
            pass

    return tickers


def retrieve_data():

    exchange_data = ExchangeData()

    global verify_tickers

    pdf_url = None

    exchange_url = source_config_obj[source_code]['url']

    try:
        response = requests.post(
            url=exchange_url,
            json={'path': 'mercado/movimientos-diarios/tabla-de-valores-referenciales'},
            headers={
                'User-Agent': user_agent
            }
        )

        response_json = json.loads(response.text)['languages'][0]['content']['html']

        soup = BeautifulSoup(response_json, 'lxml')

        pdf_link_tag = soup.find('bvl-items-grid', {'items': re.compile('/TVR[_-]20[0-9]{2}-.+\.pdf')})['items']

        pdf_url = json.loads(pdf_link_tag)[0]['url']
        main_logger.debug(f'Downloading pdf from url {pdf_url}')

    except Exception as ex:
        main_logger.debug(f'Unable to open website of {source_code} do to {ex.__str__()}')

    pdf_response = requests.get(
        pdf_url,
        headers={
            'User-Agent': user_agent
        }
    )

    download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')
    short_sell_pdf = f'{download_folder}/short_selling_list_{source_code}.pdf'
    open(short_sell_pdf, 'wb').write(pdf_response.content)

    verify_tickers = get_verify_tickers(short_sell_pdf)

    short_sell_tickers = get_tickers(short_sell_pdf)

    # Expected ticker check. Additional check to confirm that processing of the pdf went fine.
    expected_tickers = get_expected_tickers()
    compare_to_expected = list_compare(short_sell_tickers, expected_tickers)
    if compare_to_expected['identical']:
        main_logger.debug('Current list of tickers matches the expected list of tickers')
    else:
        main_logger.warning('Unexpected tickers found:')
        if compare_to_expected['added']:
            main_logger.warning(f'Added: {", ".join(compare_to_expected["added"])}')
        if compare_to_expected['removed']:
            main_logger.warning(f'Removed: {", ".join(compare_to_expected["removed"])}')

    exchange_data.data = {ticker: {'short_sell': 'Y'} for ticker in short_sell_tickers}
    exchange_data.success = True

    return exchange_data
