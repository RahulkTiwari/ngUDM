# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
import re
# Custom libs
from modules.source_config import source_config_obj
from modules.logger import main_logger
from objects.exchange_data import ExchangeData

# Setting module variables
source_code = 'nzx'
source_config = source_config_obj[source_code]
session = HTMLSession()
headers = {
    'User-Agent': (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/69.0.3497.100 Safari/537.36'
    )
}
root_url = source_config['root_url']


def get_short_sell_restriction():
    ss_restricted_tickers = []
    
    url_restricted = source_config['ss_restricted_url']
    res_page_content = session.get(url_restricted, headers=headers)
    res_soup = BeautifulSoup(res_page_content.text, 'html.parser')
    ticker_text_list = res_soup.find('p', string=re.compile('short-selling\srestriction')).find_next_sibling('ul').find_all('li')
    for each_res_ticker in ticker_text_list:
        match = re.search('\(“[A-Z].+”\)', each_res_ticker.text)
        start_pos = match.start() + 2
        end_pos = match.end() - 2
        ss_restricted_tickers.append(each_res_ticker.text[start_pos: end_pos])

    return ss_restricted_tickers


# First get all the tickers which are short sell restricted which is required globally
RESTRICTED_TICKERS = get_short_sell_restriction()


def get_ticker_info(table_row):

    scoped_types = ['Cumulative Preference Shares', 'Index Fund', 'Ordinary Shares', 'Units', 'Tradeable Rights']

    ticker_details = {
        'in_scope': False,
        'ticker': table_row.text.strip(),
        'isin': None
    }

    url = root_url + table_row.a['href']
    instrument_page_content = session.get(url, headers=headers)
    instrument_soup = BeautifulSoup(instrument_page_content.text, 'html.parser')
    equity_type = instrument_soup.find('strong', string='Type').parent.find_next('td').text

    if equity_type in scoped_types and ticker_details['ticker'] not in RESTRICTED_TICKERS:
        isin = instrument_soup.find('strong', string='ISIN').parent.find_next('td').text
        ticker_details['isin'] = isin
        ticker_details['in_scope'] = True
        main_logger.debug(f'Appending isin {isin} to {ticker_details["ticker"]}')
    elif ticker_details['ticker'] in RESTRICTED_TICKERS:
        main_logger.debug(f'Skipping ticker {ticker_details["ticker"]} since short sell for ticker is restricted')
    else:
        main_logger.debug(f'Skipping ticker {ticker_details["ticker"]} since type {equity_type} is not in scope')

    return ticker_details


def retrieve_data():

    exchange_data = ExchangeData()

    # Create a list of NZE urls from the source config markets to iterate over
    market_urls = [f'{root_url}/markets/{market.strip()}' for market in source_config['markets'].split(',')]

    for each_url in market_urls:
        page_content = session.get(each_url, headers=headers)
        soup = BeautifulSoup(page_content.text, 'html.parser')
        soup_ticker_list = soup.find('table', id='instruments-table').tbody.find_all('tr')

        for each_ticker in soup_ticker_list:
            # For each ticker check if it's in scope and get the isin
            ticker_detail = get_ticker_info(each_ticker.find('td', {'data-title': 'Code'}))
            if ticker_detail['in_scope']:
                exchange_data.data[ticker_detail['isin']] = {
                    'short_sell': 'Y'
                }

    exchange_data.success = True

    return exchange_data
