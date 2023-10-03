# Third party libs
import unittest
# Custom libs
from tests.test_globals import ROOT_DIR
from exchange_parsers.bolsa_de_santiago import get_tickers_from_pdf, clean_line


class TestChileParser(unittest.TestCase):

    def test_clean_line_basic(self):
        title = 'ACCIONES, CFI Y CFM AUTORIZADAS DESDE EL 03.04.2023 '
        sub_title = 'ACCIONES >=25% '
        header = 'Nº  ACCIÓN '
        ticker_one = 'AGUAS-A '
        number_one = '1 '
        ticker_two = 'ANTARCHILE '
        number_two = '5 '
        ticker_three = '38  MALLPLAZA '

        self.assertIsNone(clean_line(title))
        self.assertIsNone(clean_line(sub_title))
        self.assertIsNone(clean_line(header))
        self.assertEqual('AGUAS-A', clean_line(ticker_one))
        self.assertIsNone(clean_line(number_one))
        self.assertEqual('ANTARCHILE', clean_line(ticker_two))
        self.assertIsNone(clean_line(number_two))
        self.assertEqual('MALLPLAZA', clean_line(ticker_three))

    def test_get_tickers_from_pdf(self):
        expected_tickers = ['AGUAS-A', 'ALMENDRAL', 'ANDINA-A', 'ANDINA-B', 'ANTARCHILE', 'BCI', 'BESALCO', 'BICECORP', 'BLUMAR', 'BSANTANDER',
                            'CAP', 'CCU', 'CENCOSHOPP', 'CENCOSUD', 'CHILE', 'CMPC', 'COLBUN', 'CONCHATORO', 'COPEC', 'ECL', 'EMBONOR-B', 'ENELAM',
                            'ENELCHILE', 'ENELGXCH', 'ENJOY', 'ENTEL', 'FALABELLA', 'IAM', 'ILC', 'INDISA', 'INGEVEC', 'INVERCAP', 'ITAUCORP',
                            'FORUS', 'HF', 'HITES', 'LTM', 'MALLPLAZA', 'MASISA', 'MULTI X', 'NORTEGRAN', 'ORO BLANCO', 'PARAUCO', 'QUINENCO',
                            'RIPLEY', 'SALFACORP', 'SECURITY', 'SK', 'SMSAAM', 'SMU', 'SONDA', 'SQM-A', 'SQM-B', 'VAPORES', 'AAISA', 'AESANDES',
                            'BANVIDA', 'CAMANCHACA', 'CINTAC', 'CRISTALES', 'EISA', 'ENAEX', 'GASCO', 'HABITAT', 'LAS CONDES', 'LIPIGAS',
                            'MANQUEHUE', 'MINERA', 'NAVIERA', 'NITRATOS', 'NUEVAPOLAR', 'PASUR', 'PAZ', 'PUCOBRE', 'SALMOCAM', 'SOCOVESA',
                            'SOQUICOM', 'TRICOT', 'WATTS', 'ZOFRI']
        actual_tickers = get_tickers_from_pdf(f'{ROOT_DIR}/tests/resources/chile/short_selling_list_chile.pdf')

        expected_dict = {
            'CENCOSHOPP': {
                'short_sell': 'Y'
            }
        }

        self.assertEqual(expected_tickers, list(actual_tickers.keys()))
        self.assertDictEqual(expected_dict['CENCOSHOPP'], actual_tickers['CENCOSHOPP'])
