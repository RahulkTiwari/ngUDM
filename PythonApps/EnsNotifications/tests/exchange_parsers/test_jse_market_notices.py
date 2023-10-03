# Third party libs
import unittest
# Custom libs
from exchange_parsers.jse_market_notices import get_notice_number


class TestJseMarketNotices(unittest.TestCase):
    def test_get_notice_number(self):
        html_ref_string = '\n              Notice Number:\n              003'
        function_result = get_notice_number(html_ref_string)
        self.assertEqual('003', function_result)

    def test_get_notice_number_no_match(self):
        html_ref_string = 'foobar'
        function_result = get_notice_number(html_ref_string)
        self.assertEqual('foobar', function_result)


if __name__ == '__main__':
    unittest.main()
