# Third party libs
import json
import datetime
from datetime import timedelta
import requests
from pytz import timezone
# Custom libs
from modules.source_config import source_config_obj
from modules.logger import main_logger
from objects.exchange_data import ExchangeData

source_code = 'cninfo'
supported_exchanges = ['SSE', 'SZSE']


def validate_data(fetched_json, check_date):

    # Check 1: all the supported_exchanges should be in the reply
    distinct_markets = list(set([val['MARKET'] for val in fetched_json]))
    if not all(market in distinct_markets for market in supported_exchanges):
        main_logger.warning(f'Not all markets are posted yet')
        return False

    # Check 2: all the dates of the reply should be the same date as in the request
    date_validation = list(set([True for val in fetched_json if
                                datetime.datetime.strptime(val['LAST_MODIFY_DATET'], '%Y-%m-%d %H:%M:%S').date() == check_date.date() and
                                val['MARKET'] in supported_exchanges]))
    if len(date_validation) != 1 and not date_validation[0]:
        main_logger.warning(f'Date validation has failed. Retrieved dates are {", ".join(date_validation)}')
        return False

    return True


def get_token():
    acces_key = source_config_obj[source_code]['acces_key']
    secret = source_config_obj[source_code]['secret']
    url = source_config_obj[source_code]['token_url']

    post_data = {
        'grant_type': 'client_credentials',
        'client_id': acces_key,
        'client_secret': secret
    }
    req = requests.post(url, data=post_data)
    tokendic = json.loads(req.text)

    return tokendic['access_token']


def determine_url(local_time):
    token = get_token()

    if datetime.time(23, 00, 00) < local_time.time() or local_time.time() <= datetime.time(1, 30, 00):
        main_logger.debug(f'Processing the estimated file')
        root_url = source_config_obj[source_code]['url_est']
        today = local_time.strftime('%Y%m%d') if local_time.hour == 23 else (local_time - timedelta(1)).strftime('%Y%m%d')
    else:
        main_logger.debug(f'Processing the final file')
        root_url = source_config_obj[source_code]['url_fin']
        today = local_time.strftime('%Y%m%d')
    url = f'{root_url}?tdate={today}&access_token={token}'

    return url


def retrieve_data():

    exchange_data = ExchangeData()

    local_india_time = datetime.datetime.now(timezone('Asia/Kolkata'))
    url = determine_url(local_india_time)
    response = requests.get(url)
    json_body = json.loads(response.text)['records']

    # Validate the reply
    validation = validate_data(json_body, local_india_time)

    if validation:
        for each_record in json_body:
            if each_record['MARKET'] in supported_exchanges and each_record['IS_SEC_SELL'] == 'Y':
                exchange_data.data[each_record['SECCODE']] = {
                    'short_sell': 'Y'
                }
        exchange_data.success = True

    return exchange_data
