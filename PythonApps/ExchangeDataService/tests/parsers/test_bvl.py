# Third party libs
import unittest
import os
from pathlib import Path
# Custom libs
from exchange_parsers.bvl import list_compare, get_verify_tickers, get_ticker, get_section


class TestPeruParser(unittest.TestCase):

    def test_get_ticker(self):
        line_one = 'LISTA 1(al 50% del valor del mercado)'
        line_two = 'AENZAC1 SCCO ABX META'
        line_three = '------------ Valores inscritos por el emisor  ------------ --- Valores listados por Agente Promotor o la BVL ---'
        line_four = 'ACCIONES DE INVERSIÓN'
        line_five = 'MINSURI1'

        self.assertIsNone(get_ticker(line_one))
        self.assertEqual('AENZAC1 SCCO ABX META', get_ticker(line_two))
        self.assertIsNone(get_ticker(line_three))
        self.assertIsNone(get_ticker(line_four))
        self.assertEqual('MINSURI1', get_ticker(line_five))

    def test_list_compare_unequal(self):
        expected_result = {
            'identical': False,
            'added': ['ZRAB'],
            'removed': ['BARZ', 'OOF']
        }

        expected_tickers = ['FOO', 'BAR', 'BARZ', 'OOF']
        actual_tickers = ['FOO', 'BAR', 'ZRAB']
        result = list_compare(actual_tickers, expected_tickers)
        self.assertDictEqual(expected_result, result)

    def test_list_compare_equal(self):
        expected_result = {
            'identical': True,
            'added': [],
            'removed': []
        }

        expected_tickers = ['FOO', 'BAR']
        actual_tickers = ['FOO', 'BAR']
        result = list_compare(actual_tickers, expected_tickers)
        self.assertDictEqual(expected_result, result)

    def test_get_verify_tickers(self):
        project_dir = os.path.dirname(os.path.abspath(__file__))
        pdf_loc = Path(project_dir + '/../resources/bvl/short_selling_list_bvl.pdf')
        tickers = sorted(get_verify_tickers(pdf_loc))

        expected_tickers = sorted(['ALICORC1', 'FERREYC1', 'AAPL', 'MSFT', 'BAP', 'VOLCABC1', 'AMZN', 'QQQ', 'BVN', 'SPY', 'AENZAC1', 'SCCO', 'ABX',
                                   'META', 'BBVAC1', 'SIDERC1', 'BAC', 'MRNA', 'CPACASC1', 'UNACEMC1', 'C', 'NFLX', 'CREDITC1', 'DIA', 'NVDA',
                                   'CVERDEC1', 'DIS', 'PFE', 'ENGIEC1', 'EEM', 'PYPL', 'ETFPERUD', 'EFA', 'V', 'GBVLAC1', 'FCX', 'XLE', 'IFS',
                                   'GLD', 'XLF', 'INRETC1', 'HBM', 'ATACOBC1', 'HIDRA2C1', 'AAL', 'GOOGL', 'CASAGRC1', 'LUSURC1', 'BA', 'IWM',
                                   'CORAREC1', 'NEXAPEC1', 'BRK/B', 'JPM', 'ENDISPC1', 'SCOTIAC1', 'FXI', 'TSLA', 'ENGEPEC1', 'GDX', 'SLV',
                                   'CORAREI1', 'BACKUSI1', 'MINCORI1', 'MINSURI1'])
        self.assertListEqual(expected_tickers, tickers)

    def test_get_section(self):
        line_one = 'LISTA 1(al 50% del valor del mercado)'
        line_two = 'AENZAC1 SCCO ABX META'
        line_three = '------------ Valores inscritos por el emisor  ------------ --- Valores listados por Agente Promotor o la BVL ---'
        line_four = 'ACCIONES DE INVERSIÓN'
        line_five = 'MINSURI1'
        line_six = 'LISTA 2(al 50% del valor del mercado)'
        line_seven = 'ACCIONES DE INVERSION'
        line_eight = 'LISTA 3 (al 20% del valor del mercado)'
        line_nine = 'LISTA 4 - OPERACIONES DE REPORTE EXTENDIDAS (*)'

        self.assertEqual('LISTA12', get_section(line_one))
        self.assertIsNone(get_section(line_two))
        self.assertIsNone(get_section(line_three))
        self.assertEqual('INVERSION', get_section(line_four))
        self.assertIsNone( get_section(line_five))
        self.assertEqual('LISTA12', get_section(line_six))
        self.assertEqual('INVERSION', get_section(line_seven))
        self.assertEqual('LISTA34', get_section(line_eight))
        self.assertEqual('LISTA34', get_section(line_nine))
