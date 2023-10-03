# Third party libs
import unittest
from unittest.mock import patch, Mock
from bs4 import BeautifulSoup
from datetime import datetime
# Custom libs
from exchange_parsers.gpw_communiques_resolutions import get_exchange_ref_id, get_publish_date


class TestGpwCommuniques(unittest.TestCase):
    def test_get_exchange_ref_id(self):
        html_string = '<a href="/gpw-communiques-and-resolutions?ph_main_01_start=show&amp;cmn_id=113314&amp;title=Getin+Holding+Communique">Getin Holding (Communique)</a>'
        html_soup = BeautifulSoup(html_string, 'lxml')
        function_result = get_exchange_ref_id(html_soup.find('a'))
        self.assertEqual('113314', function_result)

    def test_get_publish_date_basic(self):
        html = 'GPW Management Board resolutions | 05-01-2023'
        function_result = get_publish_date(html)
        self.assertEqual('05-01-2023', function_result)

    def test_get_publish_date_today_no_match(self):
        html = 'GPW Management Board resolutions | foobar'
        function_result = get_publish_date(html)
        self.assertIsNone(function_result)


if __name__ == '__main__':
    unittest.main()
