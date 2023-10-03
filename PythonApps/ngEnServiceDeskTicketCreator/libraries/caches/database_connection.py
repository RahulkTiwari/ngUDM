# Custom libraries
from libraries.objects.MongoConnection import MongoConnection
from libraries.static_vars import TEST_MODE
from libraries.service_desk_logger import Logger

# Global MongoDb connection
if not TEST_MODE:
    properties_file = './resources/database.properties'
else:
    properties_file = './test/resources/database.properties'

Logger.logger.info(f'Using properties file: {properties_file}')

database_instance = MongoConnection(properties_file)
database_connection = database_instance.connect()
