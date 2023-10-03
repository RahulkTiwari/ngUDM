# Third party libs
import pandas as pd
import glob
import shutil
import os
import traceback
import re
# Custom libs
from modules.logger import main_logger
from objects.exchange_data import ExchangeData
from modules.source_config import source_config_obj
from modules.config import config_object
from modules.generic_functions import create_download_folder

source_code = 'tpex'
root_dir = config_object['PROCESSING']['resource_root_dir']


def get_file_not_found(exception):
    file_line_map = {
        'short_sell_csv': 'A42E_TWT96U*.csv',
        'intraday_roco_csv': 'DayTradeMark.CSV'
    }
    traceback_lines = traceback.format_exception(exception.__class__, exception, exception.__traceback__)

    for each_key in file_line_map:
        expression = re.compile(each_key)
        match = re.search(expression, '~'.join(traceback_lines))
        if match:
            return file_line_map[each_key]

    return 'Unknown file'


def retrieve_data():

    exchange_data = ExchangeData()

    try:
        # Get the short sell csv and create dataframe
        filename_structure = source_config_obj[source_code]['short_sell_filename']
        short_sell_csv = glob.glob(f'{root_dir}/exchange_files/{source_code}/downloads/{filename_structure}')[0]
        short_sell_df = pd.read_csv(
            short_sell_csv,
            encoding='latin1',
            engine='python',
            header=0,
            names=['Security Code', 'Security Name', 'Market ID.', 'SBL short sales eligibility', 'current day available volume for SBL short sales'],
            index_col=False
        )
        short_sell_df = short_sell_df[short_sell_df['SBL short sales eligibility'].isnull() & short_sell_df['Market ID.'].str.startswith('O')]

        # Get the intraday trading csv and create dataframe
        intraday_csv = f'{root_dir}/exchange_files/{source_code}/downloads/{source_config_obj[source_code]["intraday_filename"]}'
        intraday_df = pd.read_csv(
            intraday_csv,
            encoding='latin1',
            engine='python',
            header=0,
            names=['RocoCode', 'Name', 'Asterix'],
            index_col=False
        )
    except IndexError as ex:
        missing_file = get_file_not_found(ex)
        log_string = f'{source_code}: Missing file {missing_file}'
        raise FileNotFoundError(log_string)

    # To consolidation easier add intraday eligible flag, default to Y
    intraday_df['Intraday Eligible'] = 'Y'

    # Merge the intraday trading dataframe with the short sell dataframe
    merged_df = short_sell_df.set_index('Security Code').join(intraday_df.set_index('RocoCode'), how='outer')

    # Populating the reply dictionary
    for idx, values in merged_df.iterrows():
        short_sell_flag = 'Y' if pd.notnull(values['Market ID.']) else 'N'
        intraday_flag = 'Y' if values['Intraday Eligible'] == 'Y' else 'N'
        exchange_data.data[idx] = {
            'short_sell': short_sell_flag,
            'intraday_trading': intraday_flag
        }

    # TODO: how to deal with removals in case something goes wrong
    create_download_folder(f'{os.path.dirname(short_sell_csv)}/archive')
    if exchange_data.data:
        shutil.move(f'{short_sell_csv}', f'{os.path.dirname(short_sell_csv)}/archive/{os.path.basename(short_sell_csv)}')
        shutil.move(f'{intraday_csv}', f'{os.path.dirname(intraday_csv)}/archive/{os.path.basename(intraday_csv)}')
        exchange_data.success = True
        return exchange_data
    else:
        main_logger.warning(f'Something went wrong processing {source_code}. Moving file to archive folder')
        shutil.move(f'{short_sell_csv}', f'{os.path.abspath(short_sell_csv)}/archive/{os.path.basename(short_sell_csv)}')
        shutil.move(f'{intraday_csv}', f'{os.path.dirname(intraday_csv)}/archive/{os.path.basename(intraday_csv)}')

    return exchange_data
