# Custom libs
from exchange_parsers.czce_generic import get_czce_notice_list, set_czce_values

exchange = 'czce_news_announcements'


def get_notice_list():

    notice_table_entries = get_czce_notice_list(exchange)

    return notice_table_entries


def set_values(notice_table_row):

    notice = set_czce_values(notice_table_row, exchange)

    return notice
