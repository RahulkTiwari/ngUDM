# Third party
import unittest
# Custom libs
from exchange_parsers.jpx_market_news import get_exchange_ref_id


class TestJpxMarketNews(unittest.TestCase):
    def test_get_exchange_ref_id_numeric(self):
        test_url = '/english/corporate/news/news-releases/0060/20230120-01.html'
        publish_date = '2022/12/20'
        function_result = get_exchange_ref_id(test_url, publish_date)
        self.assertEqual('0060/20230120-01', function_result)

    def test_get_exchange_ref_id_othr_numeric(self):
        test_url = 'https://www.jpx.co.jp/english/derivatives/products/jgb/jgb-futures/03.html'
        publish_date = '2022/12/20'
        function_result = get_exchange_ref_id(test_url, publish_date)
        self.assertEqual('jgb-futures/03|2022/12/20', function_result)

    def test_get_exchange_ref_id_non_numeric(self):
        test_url = '/english/equities/products/tpm/issues/index.html'
        publish_date = '2022/12/20'
        function_result = get_exchange_ref_id(test_url, publish_date)
        self.assertEqual('issues/index|2022/12/20', function_result)

    def test_get_exchange_ref_id_non_numeric_ending_slash(self):
        test_url = 'https://www.jpx.co.jp/english/markets/derivatives/special-quotation/'
        publish_date = '2022/12/20'
        function_result = get_exchange_ref_id(test_url, publish_date)
        self.assertEqual('derivatives/special-quotation|2022/12/20', function_result)

    def test_get_exchange_ref_id_numeric_extended(self):
        test_url = '/english/corporate/news/news-releases/0060/copy_of_20230104-01.html'
        publish_date = '2022/12/20'
        function_result = get_exchange_ref_id(test_url, publish_date)
        self.assertEqual('0060/copy_of_20230104-01', function_result)


if __name__ == '__main__':
    unittest.main()
