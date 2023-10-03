# Third party libs
import unittest
# Custom libs
from modules.mongo_connection import database_con

class TestMongoConnection(unittest.TestCase):

    def test_get_index_attributes(self):
        self.assertEqual('192.168.118.94', database_con.client.address[0])
        self.assertEqual(27017, database_con.client.address[1])
