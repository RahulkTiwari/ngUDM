# Third party libs
import pandas as pd
# Custom libs
from modules.source_config import source_config_obj
from objects.exchange_data import ExchangeData

source_code = 'hkex_stockconnect'


def get_stock_connect_eligibility(row):
    # If the ticker is from the 'Special*Securities' csv the ticker should be marked as sell only
    # Note: 'Buy Suspended' from the other files mark as both buy and sell until further notice!!
    if row['source'] in ['xsec_s', 'xssc_s']:
        return 'sell_only'
    # If the Number is any number than mark the ticker as buy sell stock connect
    elif pd.notnull(row['Sc_No']):
        return 'buy_sell'
    # In all other scenanarios the ticker is not eligible
    else:
        return 'not_eligible'


def get_short_sell_tickers():
    urls = ['xsec_url', 'xssc_url']
    header = ['Ss_No', 'Code', 'Name', 'Instrument Type']

    # Initialize concatenated dataframe
    short_sell_df = pd.DataFrame(columns=header)

    for each_url in urls:
        excel_location = source_config_obj[source_code][each_url]

        each_url_df = pd.read_excel(
            excel_location,
            engine='xlrd',
            header=None,
            skiprows=5,
            dtype=str,
            names=header
        )

        each_url_df['Code'] = each_url_df['Code'].str.zfill(6)
        each_url_df['short sell'] = 'Y'
        short_sell_df = pd.concat([short_sell_df, each_url_df], join='outer')

    return short_sell_df


def get_stock_connect_tickers():
    urls = ['xsec_bs', 'xsec_s', 'xssc_bs', 'xssc_s']
    header = ['Sc_No', 'Code', 'CCASS', 'Name', 'Face value', 'Instrument Type']

    # Initialize concatenated dataframe
    stock_connect_df = pd.DataFrame(columns=header)

    for each_url in urls:
        each_url_df = pd.read_csv(
            source_config_obj[source_code][each_url],
            skiprows=5,
            sep='\t',
            encoding='utf-16',
            header=None,
            dtype=str,
            names=header
        )
        each_url_df['source'] = each_url
        stock_connect_df = pd.concat([stock_connect_df, each_url_df], join='outer')

    stock_connect_df['Code'] = stock_connect_df['Code'].str.zfill(6)

    return stock_connect_df


def retrieve_data():

    exchange_data = ExchangeData()

    # First create the short sell and stock connect dataframes from the several Excels
    short_sell_df = get_short_sell_tickers()
    stock_connect_df = get_stock_connect_tickers()

    # Merge the short sell and stock connect dataframes
    china_off_shore_df = short_sell_df.merge(stock_connect_df, how='outer', on='Code')

    # Create a dictionary with the data
    for idx, each_row in china_off_shore_df.iterrows():
        exchange_data.data[each_row['Code']] = {
            'short_sell': 'Y' if each_row['short sell'] == 'Y' else 'N',
            'stock_connect': get_stock_connect_eligibility(each_row)
        }
    exchange_data.success = True

    return exchange_data
