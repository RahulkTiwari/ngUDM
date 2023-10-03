# Third party libs
from unittest.mock import patch
from unittest import mock
import unittest
import requests
# Custom libs
from objects.security import Security
from objects.data_source import DataSource
from modules.jira import set_jira_json, create_jira


class TestJira(unittest.TestCase):
    data_source = DataSource()
    data_source.code = 'ads'
    data_source.description = 'Abu Dhabi Securities Exchange'
    data_source.exchange_owner = '5f0ff759d6803200218b74a8'
    data_source.ops_analysts = ['5a8c12f99632fa3592793c8f', '5ef3475360d3c80ac906c37b']
    mocked_cache = {
        'ads': data_source
    }

    # Security object
    sec_obj = Security('ads')
    sec_obj.isin = 'NL132456789'
    sec_obj.ric = 'FOO.BAR'
    sec_obj.exchange_id = 'FOO'
    sec_obj.sedol = 'FDS825'
    sec_obj.mic_code = 'XADS'
    sec_obj.trade_currency = 'EUR'
    sec_obj.long_name = 'Foobar name long'
    sec_obj.sec_id = '641c54c64f83bb0c84afd719'
    sec_obj.lookup_attr = 'exchangeTicker'
    sec_obj.lookup_attr_val = 'FOO'
    sec_obj.docId = '6447c0ca316d3c50c35b825e'
    sec_obj.skeletal_reason = 'No record found'
    sec_obj._id = '6447c0ca316d3c50c35b825e'

    # Json to make service desk request
    service_desk_json = {
        'fields': {
            'project': {
                'key': 'RDUEMTEST'
            },
            'summary': 'XADS: Unable to find corresponding record for exchangeTicker FOO',
            'issuetype': {
                'name': 'Validation'
            },
            'description': 'ads: Abu Dhabi Securities Exchange\n\nXADS: Unable to find corresponding record for exchangeTicker FOO',
            'assignee': {'accountId': '5f0ff759d6803200218b74a8'},
            'reporter': {'accountId': '5c8a4fd2677d763daafa57c7'},
            'priority': {'name': 'High'},
            'assignee': {'accountId': '5f0ff759d6803200218b74a8'},  # Primary exchange owner
            'customfield_10251': {'accountId': '5f0ff759d6803200218b74a8'},  # Primary exchange owner
            'customfield_10031': [
                {'accountId': '5a8c12f99632fa3592793c8f'},
                {'accountId': '5ef3475360d3c80ac906c37b'}
            ],
            'customfield_10127': 'XADS',
            'customfield_10135': {'value': 'NG_EDS'},
            'customfield_10132': {'value': 'Enrichment'},
            'customfield_10133': {'value': 'Security Lookup Failure'},
            'customfield_10116': 'Exchange Data to trdse Lookup Failure',
            'customfield_10123': 'exchangeTicker',
            'customfield_10121': 'FOO',
            'customfield_10125': 'ads: Abu Dhabi Securities Exchange'

        }
    }

    @patch('modules.jira.datasources_cache', mocked_cache)
    def test_set_jira_json(self):

        result = set_jira_json(self.sec_obj)

        self.assertEqual.__self__.maxDiff = None
        self.assertEqual(self.service_desk_json, result)

    @patch('requests.request')
    @patch('modules.jira.set_jira_json')
    def test_create_jira_fail(self, mock_service_desk_json, mock_reply):
        # Mock requests' repsonse
        mocked_response = requests.Response()
        mocked_response.status_code = 405
        mocked_response.reason = 'Method Not Allowed'
        type(mocked_response).text = mock.PropertyMock(return_value='')
        mock_reply.return_value = mocked_response
        # Mock return of set_jira_json function
        mock_service_desk_json.return_value = self.service_desk_json

        with self.assertLogs() as logs:
            result = create_jira(self.sec_obj)
        self.assertEqual(2, len(logs.records))
        self.assertEqual('Failed to create tickets due to: Method Not Allowed', logs.records[1].getMessage())
        self.assertFalse(result)

    @patch('requests.request')
    @patch('modules.jira.set_jira_json')
    def test_create_jira_success(self, mock_service_desk_json, mock_reply):
        # Mock requests' repsonse
        mocked_response = requests.Response()
        mocked_response.status_code = 201
        mocked_response.reason = 'created'
        type(mocked_response).text = mock.PropertyMock(return_value='{"id":"3011031","key":"ENSTEST-12345",'
                                                                    '"self":"https://smartstreamrdu.atlassian.net/rest/api/2/issue/3011031"}')
        mock_reply.return_value = mocked_response
        # Mock return of set_jira_json function
        mock_service_desk_json.return_value = self.service_desk_json

        with self.assertLogs() as logs:
            result = create_jira(self.sec_obj)
        self.assertEqual(2, len(logs.records))
        self.assertEqual('Created Jira ENSTEST-12345', logs.records[1].getMessage())
        self.assertTrue(result)


if __name__ == '__main__':
    unittest.main()
