# Third party libs
import pandas as pd
# Custom libs
from modules.config import config_object


def get_regex():

    file_location = config_object['SHAREPOINT']['download_location']

    regex_df = pd.read_excel(
        f'{file_location}/regex.xlsx',
        header=0,
        sheet_name=None,
        converters={
            'Positive regex': str
        }
    )

    return regex_df


regex_list = get_regex()
