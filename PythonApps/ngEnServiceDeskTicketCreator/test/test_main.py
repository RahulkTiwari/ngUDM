# Third party libraries
import unittest
# Custom libraries
from test.test_generic_functions import GetStringValue, GetDomainValue, GetValueFromArray, GetDateValue, GetFromCode
from test.test_caches import Config, OpsCache, DatabaseConnection, DatasourcesCache, DomainCache, WorkItemCache
from test.test_objects import ExchangeObj, WorkItemObj, WorkItemStormObj
from test.test_retrieve_notices import RetrieveNotices
from test.test_work_item_inactivation import TestInactivatingWorkItems

generic_function_tests = (GetStringValue, GetDomainValue, GetValueFromArray, GetDateValue, GetFromCode)
cache_tests = (Config, OpsCache, DatabaseConnection, DatasourcesCache, DomainCache, WorkItemCache)
objects_tests = (ExchangeObj, WorkItemObj, WorkItemStormObj)


def set_suite():
    suite = unittest.TestSuite()
    for test_class in (*generic_function_tests, *cache_tests, *objects_tests, RetrieveNotices, TestInactivatingWorkItems):
        tests = unittest.TestLoader().loadTestsFromTestCase(test_class)
        suite.addTests(tests)
    return suite


if __name__ == '__main__':
    runner = unittest.TextTestRunner()
    runner.run(set_suite())
