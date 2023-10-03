# Third party libs
from pymongo import MongoClient
# Custom libs
from modules.config import config_object


environment = config_object['CONNECTION']

client = MongoClient(
    host=environment['host'],
    port=int(environment['port']),
    username=environment['user'],
    password=environment['password'],
    authSource=environment['database'],
    authMechanism=environment['mechanism']
)

database_con = client[environment['database']]
