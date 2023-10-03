# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
# Custom libs
from modules.source_config import source_config_obj
from objects.exchange_data import ExchangeData

source_code = 'pse'


def retrieve_data():

    exchange_data = ExchangeData()

    root_url = source_config_obj[source_code]['url']

    # Get both indices and etf tickers
    url_map = {
        'indices':  'index-symbol',
        'etf': 'text-left font-pse text-capitalize historical-body border-0'
    }

    header = {
        'user_agent': (
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
            'AppleWebKit/537.36 (KHTML, like Gecko) '
            'Chrome/69.0.3497.100 Safari/537.36'
        )
    }

    session = HTMLSession()

    for each_url in url_map:
        url = root_url + each_url
        response = session.get(
            url,
            headers=header
        )

        soup = BeautifulSoup(response.text, 'lxml')

        table_rows = soup.find('div', class_='table-responsive').table.tbody.find_all('tr')

        for each_row in table_rows:
            symbol = each_row.find('td', class_=url_map[each_url]).a.text
            exchange_data.data[symbol] = {
                'short_sell': 'Y'
            }

    exchange_data.success = True

    return exchange_data
