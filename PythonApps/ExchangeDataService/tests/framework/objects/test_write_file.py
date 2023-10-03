# Third party libs
from unittest.mock import patch, mock_open
from unittest import mock
import unittest
import datetime
import json
# Custom libs
from objects.write_file import FileWriter, get_separator
from objects.security import Securities
from objects.source_code import SourceCode


class TestWriteFile(unittest.TestCase):

    def test_get_separator(self):
        separator = get_separator()

        self.assertEqual(';', separator)

    @patch('objects.write_file.datetime')
    def test_write_file_obj_basic(self, datetime_mock):
        datetime_mock.now = mock.PropertyMock(return_value=datetime.datetime(2023, 5, 16, 13, 57, 0))
        source_code = SourceCode('ads')
        file_writer = FileWriter(source_code)
        output_fields = ['source', 'exchange_id', 'mic_code', 'long_name', 'isin', 'sedol', 'ric', 'trade_currency', 'lookup_attr',
                         'lookup_attr_val', 'doc_id', 'sec_id', 'security_unique_id', 'instrument_unique_id', 'security_status', 'short_sell']
        self.assertEqual('ads', file_writer.source)
        self.assertEqual(';', file_writer.separator)
        self.assertEqual('json', file_writer.format)
        self.assertEqual('2023-05-16T135700', file_writer.file_date)
        self.assertEqual('rduEds_ads_2023-05-16T135700', file_writer.root_file_name)
        self.assertEqual(output_fields, file_writer.field_list)

    @patch('objects.write_file.datetime')
    @patch('objects.write_file.FileWriter.to_json')
    def test_write_basic(self, mock_to_json, datetime_mock):
        securities = Securities('pse')
        securities.securities = {
            'pse|63915b69b93f1a5b2759c6c5': {
                'sedol': 'GB123456',
                'ric': 'AKBNK.IS',
                'securities_key': 'pse|63915b69b93f1a5b2759c6c5'
            },
            'pse|5e201dec888c557a94e4fe4e': {
                'sedol': '',
                'ric': 'AKBNK.IS',
                'securities_key': 'pse|5e201dec888c557a94e4fe4e'
            }
        }

        datetime_mock.now = mock.PropertyMock(return_value=datetime.datetime(2023, 5, 16, 13, 57, 0))

        source_code = SourceCode('ads')
        file_writer = FileWriter(source_code)
        file_writer.write(securities)
        self.assertTrue(mock_to_json.called)

    def test_to_json(self):
        securities_json = [
            {
                'sedol': 'GB123456',
                'ric': 'AKBNK.IS'
            }
        ]
        source_code = SourceCode('ads')
        file_writer = FileWriter(source_code)
        with patch('objects.write_file.open', mock_open()) as mocked_file:
            file_writer.to_json(securities_json)

        mocked_file().write.assert_called_once_with(f'{json.dumps(securities_json[0])}\n')
