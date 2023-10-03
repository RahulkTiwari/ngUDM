# Third party libs
import unittest
# Custom libs
from modules.run_args_parser import read_arguments, get_codes
from modules import run_arg_cache as rat

class TestArgsParser(unittest.TestCase):

    def test_read_arguments_basic(self):
        argv1 = ['--source_codes', 'twse',
                 '--out_format', 'json',
                 '--enrich_data', 'n',
                 '--create_jira', 'n'
                 ]

        read_arguments(argv1)

        self.assertEqual(['twse'], rat.source_codes)
        self.assertEqual('json', rat.output_format)
        self.assertFalse(rat.create_jira)

    def test_read_arguments_all_codes(self):
        argv1 = ['--source_codes', 'all_codes',
                 '--out_format', 'csv',
                 '--enrich_data', 'n',
                 '--create_jira', 'n'
                 ]

        read_arguments(argv1)

        self.assertEqual(['bolsa_de_santiago', 'borsa_istanbul', 'egx', 'hkex'], rat.source_codes)
        self.assertEqual('csv', rat.output_format)
        self.assertFalse(rat.enrich_data)

    def test_read_arguments_defaults(self):
        argv1 = ['--source_codes', 'borsa_istanbul']

        read_arguments(argv1)

        self.assertEqual(['borsa_istanbul'], rat.source_codes)
        self.assertEqual('json', rat.output_format)
        self.assertTrue(rat.enrich_data)
        self.assertTrue(rat.create_jira)

    def test_get_codes_basic(self):
        result = get_codes('twse')
        self.assertEqual(['twse'], result)

    def test_get_codes_all_codes(self):
        result = get_codes('all_codes')
        self.assertEqual(['bolsa_de_santiago', 'borsa_istanbul', 'egx', 'hkex'], result)

    def test_get_codes_multiple(self):
        result = get_codes('twse,egx')
        self.assertEqual(['twse', 'egx'], result)

    def test_get_codes_multiple_strip(self):
        result = get_codes('tpex, egx, hkex ')
        self.assertEqual(['tpex', 'egx', 'hkex'], result)

    def test_get_codes_no_input(self):
        result = get_codes('')
        self.assertEqual([''], result)
