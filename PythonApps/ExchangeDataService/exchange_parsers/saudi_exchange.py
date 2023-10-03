# Third party libs
import json
import requests
from requests_html import HTMLSession
from datetime import datetime, timedelta
from bs4 import BeautifulSoup
# Custom libs
from modules.source_config import source_config_obj
from objects.exchange_data import ExchangeData

source_code = 'saudi_exchange'

url_id_map = {
    'url_main': 'companyRef',
    'url_parellel': 'companyRef',
    'url_etfs': 'symbol',
    'url_main_suspended': 'symbol',
    'url_parellel_suspended': 'symbol',
    'url_main_rights': 'companyRef',
    'url_parellel_rights': 'companyRef'
}


def get_date(string_date):
    formats = ['%d-%m-%Y', '%Y-%m-%d']

    for each_format in formats:
        try:
            return datetime.strptime(string_date, each_format)
        except ValueError:
            pass


def get_date_range():

    date_range = {
        'from_date': (datetime.today() - timedelta(days=30)).strftime('%d-%m-%Y'),
        'to_date': datetime.today().date().strftime('%d-%m-%Y')
    }
    return date_range


def get_url(sub_market):
    root_url = source_config_obj[source_code][sub_market]
    suffix_prop = f'{sub_market}_suf'

    date_range = get_date_range()
    suffix_url = f'{source_config_obj[source_code][suffix_prop]}'.format(from_date=date_range['from_date'], to_date=date_range['to_date'])

    headers = {
        'User-Agent': (
            'Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) '
            'AppleWebKit/537.36 (KHTML, like Gecko) '
            'Chrome/81.0.4044.141 Safari/537.36'
        )
    }

    session = HTMLSession()
    response = session.get(
        root_url,
        headers=headers
    )

    soup = BeautifulSoup(response.text, 'lxml')

    link = soup.find('base')['href'] + suffix_url

    return link


def retrieve_data():

    exchange_data = ExchangeData()

    # First get all the short sell securities
    for each_url in ['url_main', 'url_parellel', 'url_etfs']:
        url = source_config_obj[source_code][each_url]

        raw_data = requests.get(
            url,
            headers={
                "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) "
                              "AppleWebKit/537.36 (KHTML, like Gecko) "
                              "Chrome/81.0.4044.141 Safari/537.36"
            }
        )

        json_data = json.loads(raw_data.text)['data']

        for each_sec in json_data:
            # id_list.append(str(each_sec[url_id_map[each_url]]))
            exchange_data.data[str(each_sec[url_id_map[each_url]])] = {
                'short_sell': 'Y'
            }

    # Remove securities which are suspended from short sell
    today = datetime.today()
    for each_url in ['url_main_suspended', 'url_parellel_suspended', 'url_main_rights', 'url_parellel_rights']:
        url = get_url(each_url)

        # url = source_config_obj[source_code][each_url]

        raw_data = requests.get(
            url,
            headers={
                "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) "
                              "AppleWebKit/537.36 (KHTML, like Gecko) "
                              "Chrome/81.0.4044.141 Safari/537.36"
            }
        )

        json_data = json.loads(raw_data.text)['data']

        for each_sec in json_data:
            if 'suspended' in each_url:
                to_date = datetime(2100, 1, 1) if each_sec['toDate'] == '-' else get_date(each_sec['toDate'])
                from_date = get_date(each_sec['fromDate'])
                if from_date < today < to_date:
                    exchange_data.data.pop(each_sec[url_id_map[each_url]])
            else:
                try:
                    exchange_data.data.pop(each_sec[url_id_map[each_url]])
                except KeyError:
                    pass

    exchange_data.success = True

    return exchange_data
