# Third party libs
import unittest
import pandas as pd
# Custom libs
from modules.sd_data import get_sd_data

class TestSdData(unittest.TestCase):

    def test_get_sd_data_basic(self):
        securities_list = ['1', '182']
        result = get_sd_data(securities_list, 'hkex')

        # Converting to DataFrame to make asserting easier
        result_df = pd.DataFrame.from_records(result)

        self.assertEqual(4, len(result_df))
        self.assertEqual(2, len(result_df[result_df['exchangeTicker']=='1']))
        self.assertEqual(['0182.HK', '0182stat.HK'], sorted(result_df[result_df['exchangeTicker']=='182']['ric'].tolist()))
