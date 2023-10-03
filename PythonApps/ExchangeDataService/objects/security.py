# Third party libs
from datetime import datetime
import copy
# Custom libs
from modules.source_config import source_config_obj
from modules.generic_functions import get_value
from modules.logger import main_logger


class Securities(object):
    """
    Class holding an iterable list of Security objects and method to find security by document id.
    """
    def __init__(self, source_code):
        self.securities = {}
        self.source = source_code

    def __str__(self):
        return self.source

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


class Security(object):
    """
    Security object, holding relevant data of both the queried database security and the data fetched from the exchange's website
    """

    def __init__(self, source_code):
        self.exchange_id = None
        self.update_date = datetime.now().strftime('%Y-%m-%dT%H:%M:%S.%f%z')
        self.source = source_code
        self.mic_code = source_config_obj[source_code]['mic'].split(',')[0].strip()
        self.long_name = None
        self.isin = None
        self.sedol = None
        self.ric = None
        self.trade_currency = None
        self.lookup_attr = source_config_obj[source_code]['lookup_attribute']
        self.lookup_attr_val = None
        self.doc_id = None
        self.sec_id = None
        self.security_unique_id = None
        self.instrument_unique_id = None
        self.security_status = 'active'
        self.securities_key = None
        self.skeletal_reason = None
        self.exchange_fields = {}

    def __str__(self):
        return f'{self.lookup_attr} {self.lookup_attr_val}'

    def set_values(self, mongo_doc):
        self.exchange_id = get_value(mongo_doc, 'exchangeTicker')
        self.isin = get_value(mongo_doc, 'isin')
        self.sedol = get_value(mongo_doc, 'sedol')
        self.ric = get_value(mongo_doc, 'ric')
        self.mic_code = get_value(mongo_doc, 'segmentMic')
        self.trade_currency = get_value(mongo_doc, 'tradeCurrencyCode')
        self.lookup_attr_val = get_value(mongo_doc, source_config_obj[self.source]['lookup_attribute'])
        self.long_name = get_value(mongo_doc, 'nameLong')
        self.doc_id = str(mongo_doc['docId'])
        self.sec_id = get_value(mongo_doc, '_securityId')
        self.security_unique_id = f'{self.mic_code}.{self.lookup_attr_val}'
        self.instrument_unique_id = f'{self.source}.{self.lookup_attr_val}'
        self.securities_key = f'{self.source}|{self.lookup_attr_val}'

        return self

    def set_skeletal(self, lookup_value):
        self.exchange_id = lookup_value if source_config_obj[self.source]['lookup_attribute'] == 'exchangeTicker' else ''
        self.long_name = ''
        self.isin = lookup_value if source_config_obj[self.source]['lookup_attribute'] == 'isin' else ''
        self.sedol = ''
        self.ric = ''
        self.trade_currency = ''
        self.lookup_attr_val = lookup_value
        self.doc_id = ''
        self.sec_id = ''
        self.security_unique_id = f'{self.mic_code}.{self.lookup_attr_val}'
        self.instrument_unique_id = f'{self.source}.{self.lookup_attr_val}'
        self.securities_key = f'{self.source}|{lookup_value}'
        self.skeletal_reason = 'No record found'

    def set_inactive(self, lookup_value):
        applicable_fields = ['source', 'security_unique_id', 'instrument_unique_id', 'security_status']
        self.security_unique_id = f'{self.mic_code}.{lookup_value}'
        self.instrument_unique_id = f'{self.source}.{lookup_value}'
        self.security_status = 'inactive'
        all_attrs_dict = self.__dict__
        all_attrs_list = copy.deepcopy(all_attrs_dict).keys()
        for each_attribute in all_attrs_list:
            if each_attribute not in applicable_fields:
                delattr(self, each_attribute)

    def set_json(self):

        return self.__dict__
