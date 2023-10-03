"""
Module is to re-direct the meff_commodity_notices to the common implementation module meff_fin_cmdty which is for both
meff_financial_notices and meff_commodity_notices
"""

# Custom libs
from exchange_parsers.meff_fin_cmdty import find_table_rows, find_values

exchange = 'meff_commodity_notices'


def get_notice_list():

    notices_table_entries = find_table_rows(exchange)

    return notices_table_entries


def set_values(notice_table_row):

    notice = find_values(notice_table_row, exchange)

    return notice
