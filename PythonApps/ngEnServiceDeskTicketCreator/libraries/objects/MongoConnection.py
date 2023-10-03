# Third party libraries
from pymongo import MongoClient


def read_properties(database_properties_file, comment_char='#', sep='='):
    properties = {}
    with open(database_properties_file, "rt") as f:
        for line in f:
            var_l = line.strip()
            if var_l and not var_l.startswith(comment_char):
                key_value = var_l.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip().strip('"')
                if value != '':
                    properties[key] = value
    return properties


class MongoConnection(object):

    def __init__(self, properties_file):
        connection_properties = read_properties(properties_file)

        self.host = connection_properties['host']
        self.port = int(connection_properties['port'])
        self.database = connection_properties['database']
        self.user = connection_properties['user']
        self.secret = connection_properties['password']
        self.auth_mech = connection_properties['mechanism']
        self.client = None
        self.database_connection = None

    def connect(self):
        self.client = MongoClient(
            host=self.host,
            port=self.port,
            username=self.user,
            password=self.secret,
            authSource=self.database,
            authMechanism=self.auth_mech
        )

        return self.client[self.database]
