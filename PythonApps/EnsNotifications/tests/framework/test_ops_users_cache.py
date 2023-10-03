# Third party libs
import unittest
from unittest.mock import patch
from pymongo import MongoClient
# Custom libs
from modules.ops_users_cache import create_ops_cache


class TestOpsUsersCache(unittest.TestCase):

    def test_create_ops_cache(self):
        function_result = create_ops_cache()
        self.assertIsInstance(function_result, dict)
        self.assertEqual('abc123def769', function_result['foo.bar@smartstreamrdu.com'])
        self.assertEqual('sayAye', function_result['jaime.forpresident@smartstreamrdu.com'])


if __name__ == '__main__':
    unittest.main()
