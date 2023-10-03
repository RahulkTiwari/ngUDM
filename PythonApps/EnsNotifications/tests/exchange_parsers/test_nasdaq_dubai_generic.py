# Third party libs
import unittest
# Custom libs
from exchange_parsers.nasdaq_dubai_generic import get_exchange_ref_id


class TestNasdaqDubGeneric(unittest.TestCase):

    def test_get_exchange_ref_id(self):
        headline_string = 'CIRCULAR NO. 63/22 - MARGIN PARAMETERS'
        function_result = get_exchange_ref_id(headline_string)
        self.assertEqual('63/22', function_result)


if __name__ == '__main__':
    unittest.main()
