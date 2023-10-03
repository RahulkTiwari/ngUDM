# Custom libs
from modules.mongo_connection import database_con


def create_ops_cache():
    ops_users = {}

    ops_service_desk_col = database_con['opsServiceDeskUsers']

    reply = ops_service_desk_col.find({'status.RDU.value': 'A'})

    for each_ops_user in reply:
        ops_users[each_ops_user['name']['RDU']['value'].lower()] = each_ops_user['accountId']['RDU']['value']

    return ops_users


ops_users_cache = create_ops_cache()
