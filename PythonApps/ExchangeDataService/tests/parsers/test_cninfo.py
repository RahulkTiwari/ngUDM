# Third party libs
import datetime
import unittest
from unittest.mock import patch, Mock
from unittest import mock
import requests
# Custom libs
from exchange_parsers.cninfo import get_token, determine_url


class TestMainLandChinaParser(unittest.TestCase):
    @patch('requests.request')
    def test_get_token(self, mock_reply):
        return_value = '{"access_token":"4A69687963584D745A4477646E76416E47554976366F2F37622F6F58682B672F6D6F3341475753615169627058774E41435' \
                       '650576476422B46354B45644B6F6A","refresh_token":"4A69687963584D745A4477646E76416E47554976366E7643787A7636695765386A7A' \
                       '387A7A727763383671594546433445504E493143466E41336C4A2B4B4730","expires_in":3599}'

        mocked_response = requests.Response()
        mocked_response.status_code = 200
        type(mocked_response).text = mock.PropertyMock(return_value=return_value)
        mock_reply.return_value = mocked_response

        self.assertEqual('4A69687963584D745A4477646E76416E47554976366F2F37622F6F58682B672F6D6F3341475753615169627058774E41435650576476422B46'
                         '354B45644B6F6A', get_token())

    @unittest.skip('Difficulty mocking datetime')
    @patch('exchange_parsers.cninfo.get_token')
    @patch('exchange_parsers.cninfo.datetime')
    def test_determine_url_basic(self, mock_datetime, mock_token):
        mock_token.return_value = 'foobar'
        mock_datetime.now.return_value = Mock(return_value=datetime.datetime(2023, 6, 9, 23, 1, 0, 0))
        expected_url = 'http://webapi.cninfo.com.cn/api/en/p_MARGIN_SEC_INFORMATION_INOUT/?tdate=20230609&access_token=foobar'
        result = determine_url()
        self.assertEqual(expected_url, result)
