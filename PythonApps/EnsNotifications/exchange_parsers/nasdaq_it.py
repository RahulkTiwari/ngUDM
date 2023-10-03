# Custom libs
from exchange_parsers.nasdaq_generic import get_nasdaq_notice_json, set_nasdaq_notice_values

exchange = 'nasdaq_it'


def get_notice_list():

    notices_table_entries = get_nasdaq_notice_json(exchange)

    return notices_table_entries


def set_values(notice_table_row):

    notice = set_nasdaq_notice_values(notice_table_row, exchange)

    return notice
