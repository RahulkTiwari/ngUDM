# Custom libs
from exchange_parsers.shanghai_generic import get_shanghai_notice_list, set_shanghai_values

exchange = 'shfe_circularnews_circular'

def get_notice_list():

    notice_table_entries = get_shanghai_notice_list(exchange)

    return notice_table_entries


def set_values(notice_table_row):

    notice = set_shanghai_values(notice_table_row, exchange)

    return notice
