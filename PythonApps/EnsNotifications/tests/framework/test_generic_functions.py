# Third party libs
from unittest.mock import patch
import unittest.mock
import datetime
import pandas as pd
import os
# Custom libs
from objects.notice import Notice
from modules.generic_functions import check_notice, convert_date, evaluate_regex, read_credials, clean_string


class TestGenericFunctions(unittest.TestCase):

    mock_regex_list = {
        'nse':
            pd.DataFrame(
                ['^(?=.*Change/s*in/s*Underlying)', '^(?=.*(Revision|Modification).*Strikes)', '^(?=.*Trading/s*Hours)'],
                columns=['Positive regex']
            )
    }

    mock_config_obj = {
        'SHAREPOINT': {
            'sharepoint_url': 'https://smartstreamstpcom.sharepoint.com/sites/referencedatautility',
            'excel_url': '/sites/ReferenceDataUtility/Data R and D/Design/UDL/ENS/Service_Desk/UDM-64622/regex.xlsx',
            'credential_file_path': os.path.realpath(os.curdir) + '/tests/resources/credentials.txt',
            'download_location': os.path.realpath(os.curdir) + '/tests/resources'
        },
        'PROCESSING': {
            'lookback_period': 10
        }
    }

    mock_invalid_cred_config_obj = {
        'SHAREPOINT': {
            'credential_file_path': './tests/resources/credentials_invalid.txt',
        }
    }

    def test_check_notice_not_found(self):
        notice = Notice('nse')
        notice.stormKey = 'NSE_EXCHANGE_COMMUNICATION_CIRCULARS|123-456'
        query = {'stormKey.RDU.value': notice.stormKey}
        function_result = check_notice(query)
        self.assertFalse(function_result)

    def test_check_notice_found(self):
        notice = Notice('nse')
        notice.stormKey = 'NSE_EXCHANGE_COMMUNICATION_CIRCULARS|55066'
        query = {'stormKey.RDU.value': notice.stormKey}
        function_result = check_notice(query)
        self.assertTrue(function_result)

    def test_convert_date_basic(self):
        funtion_result = convert_date('2023-01-25 23:59:24 +0000', 'nasdaq_copenhagen')
        expected_date = datetime.datetime(2023, 1, 25, 23, 59, 24)
        self.assertEqual(expected_date, funtion_result)

    def test_convert_date_invalid_date(self):
        self.assertRaises(ValueError, lambda: convert_date('2023/01/25 23:59:24 +0000', 'nasdaq_copenhagen'))

    # @unittest.skip('skipping since we don\'t have Sharepoint credentials for testing')
    # @patch('modules.generic_functions.config_object', mock_config_obj)
    # @patch('modules.generic_functions.datetime')
    # def test_get_offset_date(self, datetime_mock):
    #     datetime_mock.now = Mock(return_value=datetime.datetime.strptime('21 Dec 2022', '%d %b %Y'))
    #     function_result = get_offset_date()
    #     self.assertEqual(datetime.datetime(2022, 12, 11, 0, 0, 0), function_result)

    @patch('modules.generic_functions.regex_list', mock_regex_list)
    def test_evaluate_regex_match(self):
        function_result = evaluate_regex('nse', 'Update of revision of strikes of XXX)')
        self.assertTrue(function_result)

    @patch('modules.generic_functions.regex_list', mock_regex_list)
    def test_evaluate_regex_non_match(self):
        function_result = evaluate_regex('nse', 'This is a foobar subject)')
        self.assertFalse(function_result)

    # @unittest.skip('Unable to mock file open and read date and don\'t have credentials for Sharepoint testing')
    # # TODO: see test_asx how to mock datetime.now()
    # @patch('modules.generic_functions.config_object', mock_config_obj)
    # @patch('builtins.open')
    # def test_update_regex_excel(self, mock_read):
    #     mock_read('tests/resources/last_modified_date.txt').read.return_value = '2020-01-04 13:20:57'
    #     function_result = update_regex_excel()
    #     self.assertFalse(function_result)

    @patch('modules.generic_functions.config_object', mock_config_obj)
    def test_read_credials(self):
        credentials = read_credials()
        expected_credentials = {
            'username': 'foo.bar@smartstreamrdu.com',
            'password': 'myVerySecretPassword'
        }
        self.assertEqual(expected_credentials, credentials)

    @patch('modules.generic_functions.config_object', mock_invalid_cred_config_obj)
    def test_read_credials_error(self):
        properties = read_credials()
        self.assertEqual('foo.bar@smartstreamrdu.com', properties['username'])
        self.assertEqual('', properties['password'])

    def test_clean_string(self):
        test_string = '\rthis   is\n\r a \r test\nstring\r\n\r\n\n'
        result = clean_string(test_string)
        self.assertEqual('this is a test string', result)
