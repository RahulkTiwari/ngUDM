# Third party libs
import unittest
# Custom libs
from objects.data_source import DataSource, get_ops_usr_account_id


class TestDataSource(unittest.TestCase):
    def test_get_ops_user_basic(self):
        ops_user = 'foo.bar@smartstreamrdu.com'
        function_result = get_ops_usr_account_id(ops_user)
        self.assertEqual('abc123def769', function_result)

    def test_get_ops_user_uppercase(self):
        ops_user = 'Jaime.ForPresident@smartstreamrdu.com'
        function_result = get_ops_usr_account_id(ops_user)
        self.assertEqual('sayAye', function_result)

    def test_get_ops_user_fallback(self):
        ops_user = 'Does.NotExist@smartstreamrdu.com'
        with self.assertLogs() as logs:
            function_result = get_ops_usr_account_id(ops_user)
        self.assertEqual('557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1', function_result)
        self.assertEqual('User: does.notexist@smartstreamrdu.com not found in opsServiceDeskUsers collection.', logs.records[0].getMessage())

    def test_set_data_source(self):
        exchange_doc = {
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
                    'value': 'ramesh.sahu@smartstreamrdu.com,abigail.andersen@smartstreamrdu.com'
                }
            },
            "description": {
                "RDU": {
                    "value": "Abu Dhabi Securities Exchange2"
                }
            },
        }

        function_result = DataSource().set_data_source(exchange_doc)
        self.assertEqual('MGEX_NEWS_RELEASES_MARKET_ALERTS', function_result.code)
        self.assertEqual('5ef34fa360d3c80ac90711b3', function_result.exchange_owner)
        self.assertEqual(['5ef34fa360d3c80ac90711b3', '5d8ca52827fe990dc2d3604b'], function_result.ops_analysts)
        self.assertEqual('Abu Dhabi Securities Exchange2', function_result.description)

    def test_set_data_source_missing_analyst(self):
        exchange_doc = {
            "code": {
                "RDU": {
                    "value": "DCE_BUSINESS_ANNOUNCEMENTS_NOTICES"
                }
            },
            "exchangeOwner": {
                "RDU": {
                    "value": "ramesh.sahu@smartstreamrdu.com"
                }
            },
            "allOpsAnalysts": {
                "RDU": {
                    "value": "ramesh.sahu@smartstreamrdu.com,Jaime.ForPresident@smartstreamrdu.com,"
                }
            },
            "description": {
                "RDU": {
                    "value": "Abu Dhabi Securities Exchange3"
                }
            }
        }

        function_result = DataSource().set_data_source(exchange_doc)
        self.assertEqual('DCE_BUSINESS_ANNOUNCEMENTS_NOTICES', function_result.code)
        self.assertEqual('5ef34fa360d3c80ac90711b3', function_result.exchange_owner)
        self.assertEqual(['5ef34fa360d3c80ac90711b3', 'sayAye'], function_result.ops_analysts)
