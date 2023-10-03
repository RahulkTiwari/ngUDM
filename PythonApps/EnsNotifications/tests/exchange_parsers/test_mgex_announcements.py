# Third party libs
import unittest
from datetime import datetime
# Custom libs
from exchange_parsers.mgex_announcements import get_date, get_exchange_reference_id


class TestMgexAnnouncements(unittest.TestCase):
    def test_get_date_d_m_y(self):
        # Test date format d-m-y
        function_result = get_date('MGEX Exchange Fee Update - Agricultural Products 9-2-22')
        self.assertEqual(datetime(2022, 9, 2, 0, 0, 0), function_result)

    def test_get_date_double_digit_month(self):
        function_result = get_date('Regulatory Relief Related to Open Outcry Suspension 10-15-21')
        self.assertEqual(datetime(2021, 10, 15, 0, 0, 0), function_result)

    def test_get_date_leading_zeros(self):
        function_result = get_date('Tellers Report 02-06-09')
        self.assertEqual(datetime(2009, 2, 6, 0, 0, 0), function_result)

    def test_get_date_future_year(self):
        function_result = get_date('foobar03-01-31')
        today_date = datetime.now().date()
        self.assertEqual(today_date, function_result.date())

    def test_get_date_invalid_date_with_slahes(self):
        function_result = get_date('foobar 04/24/21 foobar')
        today_date = datetime.now().date()
        self.assertEqual(today_date, function_result.date())

    def test_get_date_invalid_date_by_month_value(self):
        function_result = get_date('foobar 13/10/21')
        today_date = datetime.now().date()
        self.assertEqual(today_date, function_result.date())

    def test_get_date_invalid_date_by_day_value(self):
        function_result = get_date('foobar 10/32/21')
        today_date = datetime.now().date()
        self.assertEqual(today_date, function_result.date())

    def test_get_date_no_date(self):
        function_result = get_date('Open Outcry Options Trading Permanently Closed')
        today_date = datetime.now().date()
        self.assertEqual(today_date, function_result.date())

    def test_get_date_invalid_date_by_leading_century(self):
        function_result = get_date('foobar 10/15/2021')
        today_date = datetime.now().date()
        self.assertEqual(today_date, function_result.date())

    def test_get_exchange_reference_id(self):
        href_string = 'documents/20220929-MGEXMemoreTaxDelistingMemotoClearingMembersandMarketParticipants.pdf'
        function_result = get_exchange_reference_id(href_string)
        self.assertEqual('20220929-MGEXMemoreTaxDelistingMemotoClearingMembersandMarketParticipants', function_result)


if __name__ == '__main__':
    unittest.main()
