# Third party libs
import unittest
# Custom libs
from exchange_parsers.meff_fin_cmdty import get_exchange_reference_id


class TestMeffFinCmdty(unittest.TestCase):
    def test_get_exchange_reference_id(self):
        href_string = '/docs/comunicadosMercado/2022/04/Notice_2210_-_ENERGY_SEGMENT_-_Spanish_electricity_contracts.pdf'
        function_result = get_exchange_reference_id(href_string)
        self.assertEqual('10/22', function_result)


if __name__ == '__main__':
    unittest.main()
