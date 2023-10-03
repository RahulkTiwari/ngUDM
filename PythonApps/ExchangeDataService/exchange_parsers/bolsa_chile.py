# Third party libs
import pandas as pd
import glob
# Custom libs
from modules.config import config_object
from objects.exchange_data import ExchangeData


source_code = 'bolsa_chile'


def retrieve_data():
    
    exchange_data = ExchangeData()

    root_dir = config_object['PROCESSING']['resource_root_dir']

    tickers_csv = glob.glob(f'{root_dir}/exchange_files/bolsa_chile/downloads/*XBCL*.csv')[0]

    tickers = pd.read_csv(
        tickers_csv,
        header=None
    )

    exchange_data.data = {ticker: {'short_sell': 'Y'} for ticker in tickers[0].tolist()}
    exchange_data.success = True

    return exchange_data
