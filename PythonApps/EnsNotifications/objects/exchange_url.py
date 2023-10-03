# Third party libs
import importlib
# Custom libs
from modules.url_config import url_configuration
from modules.regex import regex_list


class ExchangeUrl(object):

    def __init__(self, exchange):
        exchange_module = importlib.import_module(f'exchange_parsers.{exchange}')
        exchange_config = url_configuration[exchange]
        self.exchange = exchange
        self.url = exchange_config['url']
        self.name_proper = exchange_config['nameProper']
        self.dateformat = exchange_config['dateformat']
        self.exchange_source_name = exchange_config['exchangeSourceName']
        self.subject_regex_list = list(regex_list[exchange]['Positive regex'])
        self.notice_table = exchange_module.get_notice_list()
