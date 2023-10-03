# Third party libs
import unittest
from unittest.mock import patch
from unittest import mock
import requests
# Custom libs
from tests.test_globals import ROOT_DIR
from exchange_parsers.byma import get_tickers_from_pdf, get_pdf_url, retrieve_data


class TestArgentinaParser(unittest.TestCase):

    def test_get_tickers_from_pdf(self):
        expected_tickers = ['ALUA', 'BBAR', 'BMA', 'BYMA', 'CEPU', 'COME', 'CRES', 'CVH', 'EDN', 'GGAL', 'IRSA', 'LOMA', 'MIRG', 'PAMP', 'SUPV',
                            'TECO2', 'TGNO4', 'TGSU2', 'TRAN', 'TXAR', 'VALO', 'YPFD']

        pdf_loc = f'{ROOT_DIR}/tests/resources/byma/short_selling_list_byma.pdf'
        pdf_tickers = get_tickers_from_pdf(pdf_loc)

        self.assertEqual(expected_tickers, pdf_tickers)

    @patch('requests.request')
    def test_get_pdf_url(self, mock_reply):
        return_value = '{"lastDate":"21200101","firstDate":"19700102","firstDateArr":null,"lastDateArr":null,"data":[{"nombre":"CIRCULAR - ' \
                       'Short Selling","numero":"3575","fecha":"31\/01\/2017","contenido":[{"nombre":"Short Selling","imagen":{' \
                       '"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2017\/11\/file-small.svg","alt":"file-small"},' \
                       '"archivo":{"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2018\/07\/BYMA-CIR03575-ESP.pdf","alt":' \
                       '"BYMA-CIR03575-ESP"}},{"nombre":"Annex - INTERNAL COMMUNICATION - Short Selling - New types of instruments authorized",' \
                       '"imagen":{"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2017\/11\/file-small.svg","alt":"file-small"},' \
                       '"archivo":{"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2023\/03\/BYMA-Anexo-CIR3575-COM18297-Venta-en-Corto-' \
                       'Marzo-2023.pdf","alt":"BYMA-Anexo CIR3575-COM18297-Venta en Corto-Marzo 2023"}}]}],"has_next":false}'
        mocked_response = requests.Response()
        mocked_response.status_code = 200
        type(mocked_response).text = mock.PropertyMock(return_value=return_value)
        mock_reply.return_value = mocked_response
        pdf_url = get_pdf_url()
        self.assertEqual('https://www.byma.com.ar/wp-content/uploads/2023/03/BYMA-Anexo-CIR3575-COM18297-Venta-en-Corto-Marzo-2023.pdf', pdf_url)

    @patch('requests.request')
    def test_get_pdf_url_fallback(self, mock_reply):
        return_value = '{"lastDate":"21200101","firstDate":"19700102","firstDateArr":null,"lastDateArr":null,"data":[{"nombre":"CIRCULAR - ' \
                       'Short Selling","numero":"3575","fecha":"31\/01\/2017","contenido":[{"nombre":"Short Selling","imagen":{' \
                       '"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2017\/11\/file-small.svg","alt":"file-small"},' \
                       '"archivo":{"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2018\/07\/BYMA-CIR03575-ESP.pdf","alt":' \
                       '"BYMA-CIR03575-ESP"}},{"nombre":"Annex - INTERNAL COMMUNICATION - Short Selling - New types of instruments authorized",' \
                       '"imagen":{"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2017\/11\/file-small.svg","alt":"file-small"},' \
                       '"archivo":{"url":"https:\/\/www.byma.com.ar\/wp-content\/uploads\/2023\/03\/BYMA-Anexo-CIR3575-FOOBAR-Venta-en-Largo-' \
                       'Marzo-2023.pdf","alt":"BYMA-Anexo CIR3575-COM18297-Venta en Corto-Marzo 2023"}}]}],"has_next":false}'
        mocked_response = requests.Response()
        mocked_response.status_code = 200
        type(mocked_response).text = mock.PropertyMock(return_value=return_value)
        mock_reply.return_value = mocked_response
        pdf_url = get_pdf_url()
        self.assertIsNone(pdf_url)

    # @patch('requests.request')
    # @patch('exchange_parsers.byma.get_pdf_url')
    # def test_retrieve_data_basic(self, mock_pdf_url, mock_reply):
    #     mock_pdf_url.return_value = 'foobar'
    #     # Mock requests' repsonse
    #     mocked_response = requests.Response()
    #     mocked_response.status_code = 200
    #     type(mocked_response).text = mock.PropertyMock(return_value='')
    #     mock_reply.return_value = mocked_response
    #
    #     with patch('exchange_parsers.byma.open', mock_open()) as mocked_file:
    #         retrieve_data.to_json(securities_json)

    @patch('exchange_parsers.byma.get_pdf_url')
    def test_retrieve_data_fallback(self, mock_pdf_url):
        mock_pdf_url.return_value = None
        with self.assertRaises(FileNotFoundError):
            _config_obj = retrieve_data()
