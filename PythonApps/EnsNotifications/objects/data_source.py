# Custom libs
from modules.ops_users_cache import ops_users_cache
from modules.config import config_object
from modules.logger import main_logger


def get_ops_usr_account_id(user_mail):
    try:
        account_id = ops_users_cache[user_mail.lower()]
    except KeyError:
        main_logger.warning(f'User: {user_mail.lower()} not found in opsServiceDeskUsers collection.')
        account_id = config_object['SERVICE_DESK']['JIRA_FALL_BACK_EXCH_OWNER']

    return account_id


class DataSource:

    def __init__(self):
        self.code = None
        self.exchange_group_name = None
        self.exchange_owner = None
        self.ops_analysts = []

    def __str__(self):
        return self.code

    def set_data_source(self, exchange_doc):

        self.code = exchange_doc['code']['RDU']['value']
        self.exchange_group_name = exchange_doc['exchangeGroupName']['RDU']['value']
        self.exchange_owner = get_ops_usr_account_id(exchange_doc['exchangeOwner']['RDU']['value'])
        self.ops_analysts = [get_ops_usr_account_id(user) for user in exchange_doc['allOpsAnalysts']['RDU']['value'].split(',') if user != '']

        return self
