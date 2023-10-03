# Third party libs
import requests
import json
import pandas as pd
from datetime import datetime, timedelta
import datetime as dt
# Custom libs
from modules.source_config import source_config_obj
from modules.logger import main_logger
from objects.exchange_data import ExchangeData

source_code = 'set'


def determine_prev_day():

    # Map with the offset for each week day (where Monday is 0)
    offset_map = {
        0: 3,
        1: 1,
        2: 1,
        3: 1,
        4: 1,
        5: 1,
        6: 2
    }

    week_day = datetime.now().weekday()

    return offset_map[week_day]


def make_request(report_date):
    root_url = source_config_obj[source_code]['api_url']

    api_key = source_config_obj[source_code]['api_key']

    headers = {
        'api-key': api_key
    }

    url = f'{root_url}?reportDate={report_date}'

    response = requests.get(
        url,
        headers=headers
    )

    return response


def retrieve_data():

    exchange_data = ExchangeData()

    # Subtract one day, since the short sell list for today apparently is published yesterday
    # report_date = datetime.now().strftime('%Y-%m-%d')
    day_offset = determine_prev_day()
    report_date = (datetime.now() - timedelta(days=day_offset)).strftime('%Y-%m-%d')

    raw_reply = make_request(report_date)

    if raw_reply.status_code != 200:
        main_logger.warning(f'No valid response received for {source_code}: {raw_reply.status_code} - {raw_reply.reason}')
    else:
        data_json = json.loads(raw_reply.text)

        # Convert json to DataFrame
        data_df = pd.DataFrame.from_records(data_json['securityList'])

        # Filter on the short sell eligible
        data_df = data_df[data_df['allowShortSell'] == 'Y']

        for idx, row in data_df.iterrows():
            exchange_data.data[row['symbol']] = {
                'short_sell': 'Y'
            }
            # In case the DR is short sell eligible, add the ticker with -R suffix
            if row['allowShortSellOnNvdr'] == 'Y':
                exchange_data.data[f'{row["symbol"]}-R'] = {
                    'short_sell': 'Y'
                }

        exchange_data.success = True

    return exchange_data
