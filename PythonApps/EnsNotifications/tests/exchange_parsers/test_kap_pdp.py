# Third party libs
import unittest
from unittest.mock import patch, Mock
from datetime import datetime
# Custom libs
from exchange_parsers.kap_pdp import get_date


class TestKapPdp(unittest.TestCase):

    @patch('exchange_parsers.kap_pdp.datetime')
    def test_get_date_today_string_today(self, datetime_mock):
        datetime_mock.now = Mock(return_value=datetime.strptime('04 Jan 2023', '%d %b %Y'))
        function_result = get_date('Today 16:50')
        self.assertEqual('04.01.2023 16:50', function_result)

    @patch('exchange_parsers.kap_pdp.datetime')
    def test_get_date_today_string_full_date(self, datetime_mock):
        datetime_mock.now = Mock(return_value=datetime.strptime('04 Jan 2023', '%d %b %Y'))
        function_result = get_date('\n                                03.01.23 10:55')
        self.assertEqual('03.01.23 10:55', function_result)


if __name__ == '__main__':
    unittest.main()
