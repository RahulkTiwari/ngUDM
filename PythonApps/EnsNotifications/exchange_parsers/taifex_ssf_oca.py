"""
Module is to re-direct the dce_see_dashang to the generic implementation module dce_generic which is for all taifex exchanges
taifex_press_releases, taifex_notices and taifex_ssf_oca
"""

# Custom libs
from exchange_parsers.taifex_generic import find_table_rows, find_values

exchange = 'taifex_ssf_oca'


def get_notice_list():

    notices_table_entries = find_table_rows(exchange)

    return notices_table_entries


def set_values(notice_table_row):

    notice = find_values(notice_table_row, exchange)

    return notice
