# Third party libs
import pandas as pd
from datetime import datetime
from pytz import timezone
import requests
# Custom libs
from modules.source_config import source_config_obj
from modules.logger import main_logger
from objects.exchange_data import ExchangeData
from modules.generic_functions import create_download_folder
from modules.config import config_object

source_code = 'borsa_istanbul'


def get_period():
    hour = datetime.now(timezone('Asia/Istanbul')).hour
    if 0 < hour < 22:
        return 1
    else:
        return 2


def retrieve_data():

    exchange_data = ExchangeData()

    root_url = source_config_obj[source_code]['url']

    this_year = datetime.now().year
    this_month = datetime.now().strftime('%m')
    todays_date = datetime.today().strftime('%Y%m%d')
    period = get_period()

    url = f'{root_url}/{this_year}/{this_month}/thm{todays_date}{period}.zip'

    response = requests.get(
        f'{url}',
        headers={
            "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
        }
    )

    root_dir = config_object['PROCESSING']['resource_root_dir']
    download_folder = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')

    zipped_file = f'{download_folder}/thm{todays_date}{period}.zip'

    open(zipped_file, 'wb').write(response.content)

    main_logger.info(f'Downloaded file {zipped_file}')

    ticker_df = pd.read_csv(
        zipped_file,
        compression='zip',
        header=1,
        converters={
            'TRANSACTION CODE': str,
            'SHORT SELL': str
        },
        sep=';'
    )

    ss_transaction_codes = ticker_df[ticker_df['SHORT SELL'] == '1']['TRANSACTION CODE']

    # TODO: add check that the tickers are actually short sell eligable. Wait for normal market circumstances to implement
    exchange_data.data = {ticker: {'short_sell': 'Y'} for ticker in ss_transaction_codes.values.tolist()}
    exchange_data.success = True

    return exchange_data
