from pymongo import MongoClient, errors as errs
import os
import time
# Custom libs
from libs.mongo_monitoring import CommandLogger, HeartbeatLogger, ServerLogger
from libs.en_logger import Logger


def read_properties(properties_file_name, sep='=', comment_char='#'):
    """
    Function to read the database connection properties
    :param properties_file_name: filename which holds the properties
    :param sep: the separator used by the properties file
    :param comment_char: indicator of comments in the properties file
    :return: dict containing the database properties
    """
    props = {}
    with open(properties_file_name, "rt") as f:
        for line in f:
            var_l = line.strip()
            if var_l and not var_l.startswith(comment_char):
                key_value = var_l.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip().strip('"')
                if value != "":
                    props[key] = value
    return props


def set_up_mongo():
    """
    Function to setup the connection to MongoDB
    :param param_task: reason why database connection is used
    :return: database instance
    """

    # Connect to MongoDB
    try:
        Logger.logger.info("Trying to connect to database...")
        properties_file = os.path.dirname(os.path.realpath(__file__)) + "/../properties/database_prod.properties"
        properties = read_properties(properties_file, sep='=')
        if 'user' in properties:
            client = MongoClient(host=properties['address'],
                                 port=int(properties['port']),
                                 username=properties['user'],
                                 password=properties['password'],
                                 authSource=properties['database'],
                                 authMechanism=properties['mechanism'],
                                 event_listeners=[CommandLogger(), ServerLogger(), HeartbeatLogger()])
        else:
            client = MongoClient(host=properties['address'],
                                 port=int(properties['port']),
                                 event_listeners=[CommandLogger(), ServerLogger(), HeartbeatLogger()])
    except errs.ConnectionFailure as connection_error:
        Logger.logger.warning("Unable to connect to MongoDb instance " + str(connection_error))
        time.sleep(2.5)
        exit(1)

    db = client[properties['database']]
    Logger.logger.info("Connecting to database {host}:{port}/{database}".format(host=properties['address'],
                                                                                port=properties['port'],
                                                                                database=properties['database']))

    return db
