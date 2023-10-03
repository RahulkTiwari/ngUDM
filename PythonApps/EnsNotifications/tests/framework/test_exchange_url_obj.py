# Third party libs
import unittest
from unittest.mock import patch
# Custom libs
from objects.exchange_url import ExchangeUrl


class TestExchangeUrlObj(unittest.TestCase):

    @patch('exchange_parsers.tfex.get_notice_list')
    def test_exchange_url_obj_init(self, mock_notice_table):
        mock_notice_table.return_value = 'return table'
        exchange_url_obj = ExchangeUrl('tfex')
        self.assertEqual('tfex', exchange_url_obj.exchange)
        self.assertEqual('TFEX trading notices', exchange_url_obj.name_proper)
        self.assertEqual('%d %b %Y %H:%M', exchange_url_obj.dateformat)
        self.assertEqual('TFEX_TRADING_NOTICE', exchange_url_obj.exchange_source_name)


if __name__ == '__main__':
    unittest.main()
