# Custom libs
from exchange_parsers.tfx_generic import get_tfx_notice_list, set_tfx_values


exchange = 'tfx_en_newsfile_notice'


def get_notice_list():

    notice_table_entries = get_tfx_notice_list(exchange)

    return notice_table_entries


def set_values(notice_table_row):

    notice = set_tfx_values(notice_table_row, exchange)

    return notice
