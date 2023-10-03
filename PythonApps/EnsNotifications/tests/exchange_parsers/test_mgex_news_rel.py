# Third party libs
import unittest
from unittest.mock import patch, Mock
from datetime import datetime
# Custom libs
from exchange_parsers.mgex_news_rel import get_date, get_exchange_reference_id


class TestMgexNewsRel(unittest.TestCase):

    @patch('exchange_parsers.mgex_news_rel.datetime')
    def test_get_date_basic(self, datetime_mock):
        date_string = '01-09-23&nbsp;&nbsp; '
        datetime_mock.now = Mock(return_value=datetime(2023, 1, 23))
        datetime_mock.strptime = Mock(return_value=datetime.strptime('01-23-23', '%m-%d-%y'))
        function_result = get_date(date_string)
        self.assertEqual(datetime(2023, 1, 23), function_result)

    @patch('exchange_parsers.mgex_news_rel.datetime')
    def test_get_date_no_match(self, datetime_mock):
        date_string = 'foobar&nbsp;&nbsp; '
        datetime_mock.now = Mock(return_value=datetime(2022, 12, 18))
        function_result = get_date(date_string)
        self.assertEqual(datetime(2022, 12, 18), function_result)

    def test_get_exchange_reference_id(self):
        href_string = 'documents/20220929-MGEXMemoreTaxDelistingMemotoClearingMembersandMarketParticipants.pdf'
        function_result = get_exchange_reference_id(href_string)
        self.assertEqual('20220929-MGEXMemoreTaxDelistingMemotoClearingMembersandMarketParticipants', function_result)

if __name__ == '__main__':
    unittest.main()
