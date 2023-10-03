# Third party libs
import pandas as pd
# Custom libs
from modules.config import config_object
from modules.source_config import source_config_obj
from modules.generic_functions import convert_string_to_list
from objects.exchange_data import ExchangeData
from modules.logger import main_logger


def get_ignored_ids(ed_data_source):
    root_dir = config_object['PROCESSING']['resource_root_dir']
    ignored_sec_csv = f'{root_dir}/exchange_files/{ed_data_source}/resources/ignore_securities.csv'
    try:
        to_be_ignored_secs_df = pd.read_csv(
            ignored_sec_csv,
            header=None,
            dtype=str
        )

        return to_be_ignored_secs_df[0].values.tolist()

    except FileNotFoundError:
        return []


class SourceCode(object):

    def __init__(self, source_code):
        self.source_code = source_code
        self.exchange_fields = self.set_exchange_fields()
        self.exchange_data = ExchangeData()
        self.securities = {}

    def set_exchange_fields(self):
        additional_fields = []

        # Enrich the data with generic exchange data points
        generic_fields = convert_string_to_list(config_object['PROCESSING']['exchange_fields'])
        try:
            # Try to add exchange specific fields
            additional_fields = convert_string_to_list(source_config_obj[self.source_code]["additional_fields"])
        except KeyError:
            pass

        self.exchange_fields = generic_fields + additional_fields

        return self.exchange_fields

    def __str__(self):
        return f'Source code {self.source_code}'

    def get_selected_sec(self, security):
        """
        Perform the following check to select the proper security based on:
        - existence of sedol
        - length of ric
        :param security: new security instance
        :return: updated list of securities in Securities object
        """
        existing_sec = self.securities[security.securities_key]
        if security.sedol == '' and existing_sec['sedol'] != '':
            return self.securities
        elif security.sedol != '' and existing_sec['sedol'] != '':
            main_logger.warning(f'Additional sedol ({security.sedol}) found for isin {security.isin}.')
            return self.securities
        elif len(security.ric) > len(existing_sec['ric']):
            return self.securities
        else:
            self.securities[security.securities_key] = security.set_json()
            return self.securities

    def get_security_list(self):
        list_of_ids = list(self.exchange_data.data.keys())
        ignored_ids = get_ignored_ids(self.source_code)

        final_list = [item for item in list_of_ids if item not in ignored_ids]

        return final_list
