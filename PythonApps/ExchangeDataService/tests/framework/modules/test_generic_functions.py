# Third party libs
from unittest.mock import patch
import unittest
# Custom libs
from modules.generic_functions import convert_string_to_list, create_download_folder, get_value


class TestGenericFunctions(unittest.TestCase):
    def test_convert_string_to_list_default(self):
        test_string = 'this,is,a,test'
        result = convert_string_to_list(test_string)
        self.assertEqual(['this', 'is', 'a', 'test'], result)

    def test_convert_string_to_list_pipe(self):
        test_string = 'this|is|a|test'
        result = convert_string_to_list(test_string, sep='|')
        self.assertEqual(['this', 'is', 'a', 'test'], result)

    def test_convert_string_to_list_error(self):
        test_string = 'this|is|a|test'
        result = convert_string_to_list(test_string)
        self.assertEqual(['this|is|a|test'], result)

    def test_convert_string_to_list_trim(self):
        test_string = 'this | is|a | test '
        result = convert_string_to_list(test_string, sep='|')
        self.assertEqual(['this', 'is', 'a', 'test'], result)

    @patch('modules.generic_functions.Path.mkdir')
    def test_create_download_folder(self, mock_make_dirs):
        mock_make_dirs.return_value = True

        create_download_folder('thing_to_create')

        mock_make_dirs.assert_called_with(parents=True, exist_ok=False)

    def test_get_value_basic(self):
        mongo_doc = {
            'isin': 'NL132456789',
            'ric': 'FOO.BAR',
            'exchangeTicker': 'FOO',
            'sedol': 'FDS825',
            'segmentMic': 'XAMS',
            'tradeCurrencyCode': 'EUR',
            'nameLong': 'Foobar name long',
            '_securityId': '641c54c64f83bb0c84afd719',
            'lookupAttribute': 'FOO',
            'docId': '6447c0ca316d3c50c35b825e',
            '_id': '6447c0ca316d3c50c35b825e'
        }

        result_valid = get_value(mongo_doc, 'ric')
        result_fallback = get_value(mongo_doc, 'foobar')
        self.assertEqual('FOO.BAR', result_valid)
        self.assertEqual('', result_fallback)


if __name__ == '__main__':
    unittest.main()
