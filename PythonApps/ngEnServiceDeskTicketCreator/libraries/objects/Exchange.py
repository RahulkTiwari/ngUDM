# Custom libraries
from libraries.generic_functions import get_string_value
from libraries.caches.ops_cache import ops_user_cache
from libraries.caches.config import config_object
from libraries.caches.database_connection import database_connection


def get_en_data_source_data():
    data_source_coll = database_connection['enDataSourceCodes']

    reply = data_source_coll.find({'status.RDU.value': 'A'})

    exchange_list = []

    for each_data_source in reply:
        exchange = Exchange()
        exchange_instance = exchange.set_exchange(each_data_source)
        exchange_list.append(exchange_instance)

    return exchange_list


def get_ops_user(user_mail):
    try:
        account_id = ops_user_cache[user_mail.lower()]
    except KeyError:
        account_id = config_object['JIRA']['JIRA_FALL_BACK_EXCH_OWNER']

    return {'user': user_mail.lower(), 'account_id': account_id}


class Exchange:

    def __init__(self):
        self.url = None
        self.code = None
        self.exchange_group_name = None
        self.exchange_owner = None
        self.ops_analysts = []

    def __str__(self):
        return self.code

    def set_exchange(self, exchange_doc):

        self.url = get_string_value(exchange_doc, 'url')
        self.code = get_string_value(exchange_doc, 'code')
        self.exchange_group_name = get_string_value(exchange_doc, 'exchangeGroupName')
        self.exchange_owner = get_ops_user(get_string_value(exchange_doc, 'exchangeOwner'))
        self.ops_analysts = [get_ops_user(user) for user in get_string_value(exchange_doc, 'allOpsAnalysts').split(',') if user != '']

        return self


class ExchangeList:

    def __init__(self):
        self.exchanges = get_en_data_source_data()

    def __iter__(self):
        self.index = 0
        return self

    def __next__(self):
        if self.index < len(self.exchanges):
            self.index += 1
            return self.exchanges[self.index - 1].code
        else:
            raise StopIteration

    def get_exchange_by_code(self, exchange_code):
        for each_exchange in self.exchanges:
            if exchange_code == each_exchange.code:
                return each_exchange

        # Add default owner in case exchange source is not in enDataSourceCodes collection
        jira_config = config_object['JIRA']

        default_exchange = Exchange()
        default_exchange.exchange_group_name = 'default'
        default_exchange.exchange_owner = get_ops_user(jira_config['JIRA_FALLBACK_OWNER_EMAIL'])
        default_exchange.ops_analysts = [get_ops_user(jira_config['JIRA_FALLBACK_OWNER_EMAIL'])]

        return default_exchange
