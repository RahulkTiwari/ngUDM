# Third party libs
import unittest
from datetime import datetime
# Custom libs
from exchange_parsers.saudi_exchange import get_date


class TestSaudiArabiaParser(unittest.TestCase):

    def test_get_date(self):
        date_string_one = '2023-05-01'
        date_string_two = '25-11-2022'
        date_string_three = '45-45-45'
        date_string_four = '2023/05/01'

        self.assertEqual(datetime(2023, 5, 1), get_date(date_string_one))
        self.assertEqual(datetime(2022, 11, 25), get_date(date_string_two))
        self.assertIsNone(get_date(date_string_three))
        self.assertIsNone(get_date(date_string_four))
