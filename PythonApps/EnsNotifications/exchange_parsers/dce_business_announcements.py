"""
Module is to re-direct the dce_business_announcements to the generic implementation module dce_generic which is for all dce exchanges
dce_business_announcements, dce_exchange_news and dce_see_dashang
"""

# Custom libs
from exchange_parsers.dce_generic import find_table_rows, find_values

exchange = 'dce_business_announcements'


def get_notice_list():

    notices_table_entries = find_table_rows(exchange)

    return notices_table_entries


def set_values(notice_table_row):

    notice = find_values(notice_table_row, exchange)

    return notice
