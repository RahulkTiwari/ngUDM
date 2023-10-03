# Third party libs
import unittest
from unittest.mock import patch, Mock
from bs4 import BeautifulSoup
from datetime import datetime
# Custom libs
from exchange_parsers.mcxinda_trading_surveillance import get_publish_date, get_previous_page


class TestMcxIndia(unittest.TestCase):
    def test_retrieve_publish_date(self):
        html_string = '<span>15 Dec 2022 Circular No - <strong> 723</strong> Category - <strong> T&amp;S</strong></span>'
        html_soup = BeautifulSoup(html_string, 'lxml')
        function_result = get_publish_date(html_soup)
        self.assertEqual('15 Dec 2022', function_result)

    def test_retrieve_publish_dat_fallback(self):
        html_string = '<span>90 Dec 2022 Circular No - <strong> 723</strong> Category - <strong> T&amp;S</strong></span>'
        html_soup = BeautifulSoup(html_string, 'lxml')
        function_result = get_publish_date(html_soup)
        self.assertEqual(datetime.now().strftime('%d %b %Y'), function_result)

    @patch('exchange_parsers.mcxinda_trading_surveillance.datetime')
    def test_get_previous_page_januari(self, datetime_mock):
        expected_json = {
            'month': '12',
            'year': '2022'
        }
        datetime_mock.now = Mock(return_value=datetime(2023, 1, 23))
        function_result = get_previous_page()
        self.assertEqual(expected_json, function_result)

    @patch('exchange_parsers.mcxinda_trading_surveillance.datetime')
    def test_get_previous_page_other_months_padding_zeros(self, datetime_mock):
        expected_json = {
            'month': '03',
            'year': '2023'
        }
        datetime_mock.now = Mock(return_value=datetime(2023, 4, 1))
        function_result = get_previous_page()
        self.assertEqual(expected_json, function_result)

    @patch('exchange_parsers.mcxinda_trading_surveillance.datetime')
    def test_get_previous_page_other_months_non_padding(self, datetime_mock):
        expected_json = {
            'month': '10',
            'year': '2023'
        }
        datetime_mock.now = Mock(return_value=datetime(2023, 11, 2))
        function_result = get_previous_page()
        self.assertEqual(expected_json, function_result)


if __name__ == '__main__':
    unittest.main()
