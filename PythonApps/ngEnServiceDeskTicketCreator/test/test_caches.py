# Third party libraries
import unittest
# Override statics
import libraries.static_vars as static
static.TEST_MODE = True
# Custom libraries
from libraries.caches.config import get_all_configurations
from libraries.caches.database_connection import database_connection
from libraries.caches.ops_cache import create_ops_cache
from libraries.caches.datasources_cache import ExchangeList
from libraries.objects.Exchange import Exchange
from libraries.caches.domain_cache import create_event_type_cache, create_asset_class_cache
from libraries.caches.work_item_cache import WorkItemStormCache
from libraries.objects.WorkItemsStorm import WorkItemsStormObj


class Config(unittest.TestCase):
    def test_creating_config(self):
        config = get_all_configurations()
        self.assertEqual('Task', config['JIRA']['JIRA_ISSUE_TYPE'])


class DatabaseConnection(unittest.TestCase):

    def test_database_connection(self):
        connection = database_connection
        self.assertEqual(('192.168.118.94', 27017), connection.client.address)


class OpsCache(unittest.TestCase):

    def test_create_ops_cache(self):
        ops_user_cache = create_ops_cache()
        self.assertEqual(31, len(ops_user_cache))
        self.assertEqual('62c670ed40923988ba0addda', ops_user_cache['rajendra.bala@smartstreamrdu.com'])
        self.assertRaises(KeyError, lambda:  ops_user_cache['Chandan.Ashwathanarayana@smartstreamrdu.com'])


class DatasourcesCache(unittest.TestCase):

    def test_create_datasources_cache(self):
        data_source_cache = ExchangeList()
        self.assertEqual(87, len(data_source_cache.exchanges))
        self.assertIsInstance(data_source_cache.exchanges[0], Exchange)


class DomainCache(unittest.TestCase):

    def test_event_type_domain_cache(self):
        function_result = create_event_type_cache()
        self.assertEqual(490, len(function_result))
        self.assertEqual('errorCode102', function_result['']['normalized_value'])
        self.assertEqual('Bonus Issue', function_result['BONUS SHARES ADJUSTMENT|EUREX_PRODUCTION_NEWSBOARD']['normalized_value'])
        self.assertEqual('Bonus Issue', function_result['Bonus Issue']['normalized_name'])

    def test_asset_classification_domain_cache(self):
        function_result = create_asset_class_cache()
        self.assertEqual(309, len(function_result))
        self.assertEqual('errorCode102', function_result['']['normalized_value'])
        self.assertEqual('56', function_result['STOCK AND TRUST UNIT OPTIONS|TMX_CIRCULAR']['normalized_value'])
        self.assertEqual('Option', function_result['STOCK AND TRUST UNIT OPTIONS|TMX_CIRCULAR']['normalized_name'])
        self.assertEqual('Entitlement', function_result['64']['normalized_name'])


class WorkItemCache(unittest.TestCase):

    def test_work_item_cache(self):
        function_result = WorkItemStormCache()
        self.assertEqual(1, len(function_result.work_item_objects))
        self.assertIsInstance(function_result.work_item_objects[0], WorkItemsStormObj)


if __name__ == '__main__':
    unittest.main()
