# Third party libs
import unittest
from unittest import mock
from unittest.mock import patch
import os
from pathlib import Path
import requests
from bs4 import BeautifulSoup
# Custom libs
from exchange_parsers.nzx import get_short_sell_restriction, get_ticker_info


class TestNewZealandParser(unittest.TestCase):

    @patch('requests_html.HTMLSession')
    def test_get_short_sell_restriction(self, mock_reply):
        # Loading website html as text
        project_dir = os.path.dirname(os.path.abspath(__file__))
        website_html = Path(project_dir + '/../resources/nzx/NZXTradingInformation.html')
        with open(website_html, 'r', encoding='utf-8') as f:
            html_string = f.read()
        f.close()

        return_value = html_string
        mocked_response = requests.Response()
        mocked_response.status_code = 200
        type(mocked_response).text = mock.PropertyMock(return_value=return_value)
        mock_reply.return_value = mocked_response

        result = get_short_sell_restriction()
        sorted_tickers = sorted(result)
        self.assertListEqual(['FCG', 'LIC'], sorted_tickers)

    @patch('requests_html.HTMLSession')
    @patch('exchange_parsers.nzx.get_short_sell_restriction')
    def test_get_ticker_info(self, mock_restrict, mock_reply):
        # Loading website html as text
        project_dir = os.path.dirname(os.path.abspath(__file__))
        website_html = Path(project_dir + '/../resources/nzx/AFCGroupHoldingsLimitedOrdinaryShares.html')
        with open(website_html, 'r', encoding='utf-8') as f:
            html_string = f.read()
        f.close()

        return_value = html_string
        mocked_response = requests.Response()
        mocked_response.status_code = 200
        type(mocked_response).text = mock.PropertyMock(return_value=return_value)
        mock_reply.return_value = mocked_response

        mock_restrict.return_value = ['FOO', 'BAR']

        # Function input
        afc_website_html = Path(project_dir + '/../resources/nzx/AFCTableRow.html')
        with open(afc_website_html, 'r', encoding='utf-8') as f:
            afc_html_string = f.read()

        table_row = BeautifulSoup(afc_html_string, 'html.parser')
        result = get_ticker_info(table_row)

        expected_result = {
            'in_scope': True,
            'ticker': 'AFC',
            'isin': 'NZVIKE0002S2'
        }
        self.assertDictEqual(expected_result, result)

    @patch('requests_html.HTMLSession')
    @patch('exchange_parsers.nzx.RESTRICTED_TICKERS', ['AFC', 'FOO', 'BAR'])
    def test_get_ticker_info_restrict(self, mock_reply):
        # Loading website html as text
        project_dir = os.path.dirname(os.path.abspath(__file__))
        website_html = Path(project_dir + '/../resources/nzx/AFCGroupHoldingsLimitedOrdinaryShares.html')
        with open(website_html, 'r', encoding='utf-8') as f:
            html_string = f.read()
        f.close()

        return_value = html_string
        mocked_response = requests.Response()
        mocked_response.status_code = 200
        type(mocked_response).text = mock.PropertyMock(return_value=return_value)
        mock_reply.return_value = mocked_response

        # Function input
        afc_website_html = Path(project_dir + '/../resources/nzx/AFCTableRow.html')
        with open(afc_website_html, 'r', encoding='utf-8') as f:
            afc_html_string = f.read()

        table_row = BeautifulSoup(afc_html_string, 'html.parser')
        expected_result = {
            'in_scope': False,
            'ticker': 'AFC',
            'isin': None
        }

        with self.assertLogs() as logs:
            result = get_ticker_info(table_row)

        self.assertEqual('Skipping ticker AFC since short sell for ticker is restricted', logs.records[0].getMessage())
        self.assertDictEqual(expected_result, result)

    @patch('requests_html.HTMLSession')
    @patch('exchange_parsers.nzx.RESTRICTED_TICKERS', ['FOO', 'BAR'])
    def test_get_ticker_info_unsupported(self, mock_reply):
        # Loading website html as text
        project_dir = os.path.dirname(os.path.abspath(__file__))
        website_html = Path(project_dir + '/../resources/nzx/MLNWFMarlinGlobalLimitedWarrant.html')
        with open(website_html, 'r', encoding='utf-8') as f:
            html_string = f.read()
        f.close()

        return_value = html_string
        mocked_response = requests.Response()
        mocked_response.status_code = 200
        type(mocked_response).text = mock.PropertyMock(return_value=return_value)
        mock_reply.return_value = mocked_response

        # Function input
        afc_website_html = Path(project_dir + '/../resources/nzx/MLNWFTableRow.html')
        with open(afc_website_html, 'r', encoding='utf-8') as f:
            afc_html_string = f.read()

        table_row = BeautifulSoup(afc_html_string, 'html.parser')
        expected_result = {
            'in_scope': False,
            'ticker': 'MLNWF',
            'isin': None
        }

        with self.assertLogs() as logs:
            result = get_ticker_info(table_row)

        self.assertEqual('Skipping ticker MLNWF since type Equity Warrants is not in scope', logs.records[0].getMessage())
        self.assertDictEqual(expected_result, result)
