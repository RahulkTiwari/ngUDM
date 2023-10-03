# Third party libs
import unittest
# Custom libs
from objects.source_code import SourceCode


class TestSourceCodeObject(unittest.TestCase):

    def test_source_code_init_w_exchange_fields(self):
        source_code = SourceCode('tpex')

        self.assertEqual('tpex', source_code.source_code)
        self.assertEqual(['short_sell', 'intraday_trading'], source_code.exchange_fields)

    def test_source_code_init_wo_exchange_fields(self):
        source_code = SourceCode('ads')

        self.assertEqual('ads', source_code.source_code)
        self.assertEqual(['short_sell'], source_code.exchange_fields)
