# Custom libs
from modules.mongo_connection import database_con
# from pymongo import MongoClient


def create_ops_cache():
    # TODO: remove db connection for PROD
    # client = MongoClient(
    #     host='localhost',
    #     port=27017,
    #     username='test',
    #     password='test123',
    #     authSource='testShortSell',
    #     authMechanism='SCRAM-SHA-1'
    # )
    #
    #
    #
    # database_con = client['testShortSell']

    ops_users = {}

    ops_service_desk_col = database_con['opsServiceDeskUsers']

    reply = ops_service_desk_col.find({'status.RDU.value': 'A'})

    for each_ops_user in reply:
        ops_users[each_ops_user['name']['RDU']['value'].lower()] = each_ops_user['accountId']['RDU']['value']

    return ops_users


ops_users_cache = create_ops_cache()
