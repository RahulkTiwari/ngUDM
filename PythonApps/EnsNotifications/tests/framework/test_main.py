# Third party libs
import unittest
# Custom libs
from main import get_exchange_list


class TestMain(unittest.TestCase):
    def test_get_exchange_list(self):
        function_result = get_exchange_list(['nse', 'foo', 'bar','b3_circular', 'nasdaq_copenhagen'])
        self.assertListEqual(['nse', 'b3_circular', 'nasdaq_copenhagen'], function_result['valid'])
        self.assertListEqual(['foo', 'bar'], function_result['invalid'])


if __name__ == '__main__':
    unittest.main()
