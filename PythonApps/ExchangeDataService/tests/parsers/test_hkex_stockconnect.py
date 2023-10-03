# Third party libs
import unittest
import pandas as pd
import numpy as np
# Custom libs
from exchange_parsers.hkex_stockconnect import get_stock_connect_eligibility, get_short_sell_tickers, get_stock_connect_tickers


class TestChinaOffshoreParser(unittest.TestCase):
    def test_get_stock_connect_eligibility_sell(self):
        stock_connect_df = pd.DataFrame(
            columns=['Sc_No', 'Code', 'CCASS', 'Name', 'Face value', 'Instrument Type', 'source'],
            data=[
                ['Buy Suspended', '603605', '93605', 'PROYA COSMETICS', '1', 'EQTY', 'xsec_s']
            ]
        )
        result = get_stock_connect_eligibility(stock_connect_df.iloc[0])
        self.assertEqual('sell_only', result)

    def test_get_stock_connect_eligibility_buy_sell(self):
        stock_connect_df = pd.DataFrame(
            columns=['Sc_No', 'Code', 'CCASS', 'Name', 'Face value', 'Instrument Type', 'source'],
            data=[
                ['2', '603605', '93605', 'PROYA COSMETICS', '1', 'EQTY', 'xssc_bs']
            ]
        )
        result = get_stock_connect_eligibility(stock_connect_df.iloc[0])
        self.assertEqual('buy_sell', result)

    def test_get_stock_connect_eligibility_n_eligible(self):
        stock_connect_df = pd.DataFrame(
            columns=['Sc_No', 'Code', 'CCASS', 'Name', 'Face value', 'Instrument Type', 'source'],
            data=[
                [np.NaN, '603605', '93605', 'PROYA COSMETICS', '1', 'EQTY', 'xssc_bs']
            ]
        )
        result = get_stock_connect_eligibility(stock_connect_df.iloc[0])
        self.assertEqual('not_eligible', result)

    def test_get_short_sell_tickers(self):
        result = get_short_sell_tickers()
        self.assertEqual(2, len(result))
        self.assertEqual('Y', result.iloc[0]['short sell'])
        self.assertEqual('000001', result.iloc[0]['Code'])

    def test_get_stock_connect_tickers(self):
        result = get_stock_connect_tickers()
        self.assertEqual(10, len(result))
        self.assertEqual('000002', result[result['Name'] == 'CHINA VANKE'].iloc[0]['Code'])
        self.assertEqual('Buy Suspended', result[result['Name'] == 'PROYA COSMETICS'].iloc[0]['Sc_No'])
