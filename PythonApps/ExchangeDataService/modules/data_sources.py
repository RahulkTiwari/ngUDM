# Custom libs
from modules.mongo_connection import database_con
# from pymongo import MongoClient
from objects.data_source import DataSource


def create_datasource_cache():

    # TODO: remove db connection for PROD
    # client = MongoClient(
    #     host='localhost',
    #     port=27017,
    #     username='test',
    #     password='test123',
    #     authSource='testdb',
    #     authMechanism='SCRAM-SHA-1'
    # )
    #
    # database_con = client['testdb']

    datasources_col = {}

    cursors = database_con.get_collection('edDataSourceCodes').find({'status.RDU.value': 'A'})

    for each_cursor in cursors:
        exchange = DataSource().set_data_source(each_cursor)
        datasources_col[exchange.code] = exchange

    return datasources_col


datasources_cache = create_datasource_cache()
