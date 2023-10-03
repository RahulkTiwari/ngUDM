# Third party libs
import unittest
# Custom libs
from exchange_parsers.nasdaq_generic import get_ref_id


class TestNasdaqGeneric(unittest.TestCase):
    def test_get_ref_id(self):
        url = 'https://view.news.eu.nasdaq.com/view?id=b6533dd90d52203c0f5caed703a7782f5&lang=en'
        function_result = get_ref_id(url)
        self.assertEqual('b6533dd90d52203c0f5caed703a7782f5', function_result)


if __name__ == '__main__':
    unittest.main()
