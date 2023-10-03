"""
Module is to re-direct the lme_member_notice to the generic implementation module lme_generic which is for all lme exchanges
"""

# Custom libs
from exchange_parsers.lme_generic import find_table_rows, find_values

exchange = 'lme_member_notice'


def get_notice_list():

    notices_table_entries = find_table_rows(exchange)

    return notices_table_entries


def set_values(notice_table_row):

    notice = find_values(notice_table_row, exchange)

    return notice
