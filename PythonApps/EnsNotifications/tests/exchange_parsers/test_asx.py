# Third party libs
import unittest
from datetime import datetime
from unittest.mock import patch, Mock
# Custom libs
from exchange_parsers.asx import get_publish_date


class TestAsx(unittest.TestCase):
    def test_get_publish_date_basic(self):
        raw_date = '04 Jan 2023 10:58am'
        function_result = get_publish_date(raw_date)
        self.assertEqual(datetime(2023, 1, 4, 10, 58), function_result)

    @patch('exchange_parsers.asx.datetime')
    def test_get_publish_date_today_string(self, datetime_mock):
        datetime_mock.now = Mock(return_value=datetime.strptime('04 Jan 2023', '%d %b %Y'))
        value = get_publish_date('Today at 4:55pm')
        self.assertEqual(datetime(2023, 1, 4, 4, 55), value)


if __name__ == '__main__':
    unittest.main()
