# Third party libs
import unittest
# Custom libs
from modules.logger import log_traceback
from tests.test_globals import ROOT_DIR


def broken_function():
    raise ValueError('Test log')


class TestLogger(unittest.TestCase):

    def test_log_traceback_basic(self):
        expected_log = f'ads: Traceback (most recent call last): ~  File ' \
                       f'"{ROOT_DIR}/tests/framework/modules/test_logger.py", line 23, ' \
                       f'in test_log_traceback_basic     broken_function() ~  ' \
                       f'File "{ROOT_DIR}/tests/framework/modules/test_logger.py", line 9, ' \
                       f'in broken_function     raise ValueError(\'Test log\') ~ValueError: Test log '

        with self.assertLogs() as logs:
            try:
                broken_function()
            except Exception as ex:
                log_traceback(ex, 'ads')

        self.assertEqual.__self__.maxDiff = None
        self.assertEqual(1, len(logs.records))
        self.assertEqual(expected_log, logs.records[0].getMessage())

    def test_log_traceback_level(self):
        expected_log = 'ads: Traceback (most recent call last): ~  File ' \
                       '"/opt/jenkins/jenkins_home/workspace/Dev_Equity_ExchangeDataService/PythonApps/ExchangeDataService/tests/framework/modules/test_logger.py", line 40, ' \
                       'in test_log_traceback_level     broken_function() ~  ' \
                       'File "/opt/jenkins/jenkins_home/workspace/Dev_Equity_ExchangeDataService/PythonApps/ExchangeDataService/tests/framework/modules/test_logger.py", line 9, ' \
                       'in broken_function     raise ValueError(\'Test log\') ~ValueError: Test log '

        with self.assertLogs() as logs:
            try:
                broken_function()
            except Exception as ex:
                log_traceback(ex, 'ads', level='fatal')

        self.assertEqual.__self__.maxDiff = None
        self.assertEqual(1, len(logs.records))
        # self.assertEqual(expected_log, logs.records[0].getMessage())
        self.assertEqual('CRITICAL', logs[0][0].levelname)
