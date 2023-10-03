# Third party libs
import unittest
# Custom libs
from modules.source_config import get_all_configurations


class TestSourceConfig(unittest.TestCase):

    def test_get_all_configurations(self):
        result = get_all_configurations()
        expected_sections = ['bolsa_de_santiago', 'borsa_istanbul', 'egx', 'hkex', 'saudi_exchange', 'nzx', 'bvl', 'byma', 'pse', 'bursa_malaysia',
                             'idx', 'set', 'cninfo', 'hkex_stockconnect', 'bolsa_chile', 'dfm', 'ads', 'twse', 'tpex']
        self.assertEqual(sorted(expected_sections), sorted(result.sections()))
        self.assertEqual('exchangeTicker', result['hkex']['lookup_attribute'])
