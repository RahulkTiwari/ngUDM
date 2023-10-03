# Custom libs
from modules.mongo_connection import database_con
from objects.data_source import DataSource


def create_datasource_cache():
    datasources_col = {}

    cursors = database_con.get_collection('enDataSourceCodes').find({'status.RDU.value': 'A'})

    for each_cursor in cursors:
        exchange = DataSource().set_data_source(each_cursor)
        datasources_col[exchange.code] = exchange

    return datasources_col


datasources_cache = create_datasource_cache()
