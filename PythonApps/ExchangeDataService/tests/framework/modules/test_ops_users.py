# Third party libs
import unittest
import time
from pymongo import MongoClient
import os
import glob
# Custom libs
from modules.ops_users import create_ops_cache
from modules.config import config_object


class TestOpsUsersCache(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        mongo_details = config_object['CONNECTION']

        client = MongoClient(
            host=mongo_details['host'],
            port=int(mongo_details['port']),
            username=mongo_details['user'],
            password=mongo_details['password'],
            authSource=mongo_details['database'],
            authMechanism=mongo_details['mechanism']
        )

        database_con = client[mongo_details['database']]

        list_of_json_files = glob.glob(os.path.realpath(os.curdir) + '/tests/resources/mongo_data/*.json', recursive=False)
        for each_json in list_of_json_files:
            collection = os.path.basename(each_json).split(".")[0]
            database_con.drop_collection(collection)
            os.system(f'mongoimport '
                      f'--host {mongo_details["host"]} '
                      f'--db {mongo_details["database"]} '
                      f'--port {int(mongo_details["port"])} '
                      f'--username {mongo_details["user"]} '
                      f'--password  {mongo_details["password"]} '
                      f'--collection {collection} '
                      f'--file {each_json} ')

        time.sleep(60)  # Increase before creating Jenkins job, to enable loading of documents

    def test_create_ops_cache(self):
        function_result = create_ops_cache()
        self.assertIsInstance(function_result, dict)
        self.assertEqual('abc123def769', function_result['foo.bar@smartstreamrdu.com'])
        self.assertEqual('sayAye', function_result['jaime.forpresident@smartstreamrdu.com'])
