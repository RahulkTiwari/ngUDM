# Third party
import unittest
from unittest.mock import patch, Mock
from datetime import datetime
from bs4 import BeautifulSoup
# Custom libs
from exchange_parsers.gpw_news import get_publish_date, get_exchange_ref_id


class TestGpwNews(unittest.TestCase):
    def test_get_publish_date_basic(self):
        html = 'Press releases | 03-01-2023'
        function_result = get_publish_date(html)
        self.assertEqual('03-01-2023', function_result)

    @patch('exchange_parsers.gpw_news.datetime')
    def test_get_publish_date_today_no_match(self, datetime_mock):
        html = 'Press releases |  | foobar'
        datetime_mock.now = Mock(return_value=datetime.strptime('02 Jan 2023', '%d %b %Y'))
        function_result = get_publish_date(html)
        self.assertEqual('02-01-2023', function_result)

    def test_get_exchange_ref_id_resolution(self):
        html_string = '<a href="news?cmn_id=113374&amp;title=Investor+Activity+On+GPW+Group+Markets+%E2%80%93+December+2022">Investor Activity On GPW Group Markets – December 2022</a>'
        html_soup = BeautifulSoup(html_string, 'lxml')
        function_result = get_exchange_ref_id(html_soup.find('a'))
        self.assertEqual('113374', function_result)


if __name__ == '__main__':
    unittest.main()
