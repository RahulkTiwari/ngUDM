# Custom libraries
from libraries.generic_functions import get_string_value
from libraries.caches.database_connection import database_connection


def create_ops_cache():
    ops_users = {}

    ops_service_desk_col = database_connection['opsServiceDeskUsers']

    reply = ops_service_desk_col.find({'status.RDU.value': 'A'})

    for each_ops_user in reply:
        ops_users[get_string_value(each_ops_user, 'name').lower()] = get_string_value(each_ops_user, 'accountId')

    return ops_users


ops_user_cache = create_ops_cache()
