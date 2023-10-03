# Custom libs
from exchange_parsers.nasdaq_generic import get_nasdaq_notice_json, set_nasdaq_notice_values

exchange = 'nasdaq_copenhagen'


def get_notice_list():

    filtered_notices_table_entries = []

    in_scope_cns_categories = ["Product information", "Flexible Derivatives Products", "Derivative market information", "Fixed Income",
                               "Expiration", "Clearing information"]

    notices_table_entries = get_nasdaq_notice_json(exchange)

    for notice in notices_table_entries:
        if notice['cnsCategory'] in in_scope_cns_categories:
            filtered_notices_table_entries.append(notice)

    return filtered_notices_table_entries


def set_values(notice_table_row):

    notice = set_nasdaq_notice_values(notice_table_row, exchange)

    return notice
