# Third party libs
import unittest
import os
import glob
from pymongo import MongoClient
import time
# Custom libs
from modules.data_sources import create_datasource_cache
from objects.data_source import get_ops_usr_account_id, DataSource
from modules.config import config_object


class TestDataSources(unittest.TestCase):

    datasources_collection = [
        {
            'status': {
                'RDU': {
                    'value': 'A'
                }
            },
            'code': {
                'RDU': {
                    'value': 'ATHEXGROUP_EMAIL'
                }
            },
            'url': {
                'RDU': {

                }
            },
            'exchangeGroupName': {
                'RDU': {
                    'value': 'Athens Derivatives Exchange'
                }
            },
            'exchangeOwner': {
                'RDU': {
                    'value': 'siva.kunduru@smartstreamrdu.com'
                }
            },
            'allOpsAnalysts': {
                'RDU': {
                    'value': 'siva.kunduru@smartstreamrdu.com,pallavi.kulkarni@smartstreamrdu.com'
                }
            }
        },
        {
            'status': {
                'RDU': {
                    'value': 'A'
                }
            },
            'code': {
                'RDU': {
                    'value': 'MGEX_NEWS_RELEASES_MARKET_ALERTS'
                }
            },
            'url': {
                'RDU': {
                    'value': 'https://www.mgex.com/news_rel.html'
                }
            },
            'exchangeGroupName': {
                'RDU': {
                    'value': 'CMEG'
                }
            },
            'exchangeOwner': {
                'RDU': {
                    'value': 'ramesh.sahu@smartstreamrdu.com'
                }
            },
            'allOpsAnalysts': {
                'RDU': {
                    'value': 'ramesh.sahu@smartstreamrdu.com,abigail.andersen@smartstreamrdu.com,Subramani.Rangappa@smartstreamrdu.com,'
                             'abdul.majith@smartstreamrdu.com'
                }
            }
        },
        {
            "code": {
                "RDU": {
                    "value": "DCE_BUSINESS_ANNOUNCEMENTS_NOTICES"
                }
            },
            "exchangeGroupName": {
                "RDU": {
                    "value": "DCE"
                }
            },
            "exchangeOwner": {
                "RDU": {
                    "value": "amrutha.byravamurthy@smartstreamrdu.com"
                }
            },
            "allOpsAnalysts": {
                "RDU": {
                    "value": "amrutha.byravamurthy@smartstreamrdu.com,lavanya.bellamkonda@smartstreamrdu.com,"
                }
            }
        }
    ]

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

        list_of_json_files = glob.glob(os.path.realpath(os.curdir) + '/tests/resources/*.json', recursive=False)
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

        time.sleep(70)

    def test_get_ops_user_basic(self):
        ops_user = 'foo.bar@smartstreamrdu.com'
        function_result = get_ops_usr_account_id(ops_user)
        self.assertEqual('abc123def769', function_result)

    def test_get_ops_user_uppercase(self):
        ops_user = 'Jaime.ForPresident@smartstreamrdu.com'
        function_result = get_ops_usr_account_id(ops_user)
        self.assertEqual('sayAye', function_result)

    def test_get_ops_user_fallback(self):
        ops_user = 'donot.exist@smartstreamrdu.com'
        function_result = get_ops_usr_account_id(ops_user)
        self.assertEqual('557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1', function_result)

    def test_create_datasource_cache(self):
        function_result = create_datasource_cache()
        self.assertEqual(87, len(function_result))
        self.assertEqual('6029fe367f77e3006893ebff', function_result['ATHEXGROUP_EMAIL'].exchange_owner)
        self.assertEqual(['6029fe367f77e3006893ebff', '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'],
                         function_result['ATHEXGROUP_EMAIL'].ops_analysts)
        self.assertIsInstance(function_result['CMEG_GLOBEX'], DataSource)

    def test_set_data_source(self):
        function_result = DataSource().set_data_source(TestDataSources.datasources_collection[1])
        self.assertEqual('MGEX_NEWS_RELEASES_MARKET_ALERTS', function_result.code)
        self.assertEqual('CMEG', function_result.exchange_group_name)
        self.assertEqual('5ef34fa360d3c80ac90711b3', function_result.exchange_owner)
        self.assertEqual(['5ef34fa360d3c80ac90711b3', '5d8ca52827fe990dc2d3604b', '5a8c12f1169207417fa5a52f', '61d686be7aa7ac007040d0bb'],
                         function_result.ops_analysts)

    def test_set_data_source_missing_analyst(self):
        function_result = DataSource().set_data_source(TestDataSources.datasources_collection[2])
        self.assertEqual('DCE_BUSINESS_ANNOUNCEMENTS_NOTICES', function_result.code)
        self.assertEqual('DCE', function_result.exchange_group_name)
        self.assertEqual('5da9437aae16d60d8e6e60f6', function_result.exchange_owner)
        self.assertEqual(['5da9437aae16d60d8e6e60f6', '5c775c61017b4a53c68ada6b'], function_result.ops_analysts)


if __name__ == '__main__':
    unittest.main()
