# Third party libs
import datetime
from unittest.mock import patch
from unittest import mock
import unittest
import requests
# Custom libs
from objects.notice import Notice
from modules.data_sources import DataSource
from modules.notice_processor import set_service_desk_json, create_service_desk_ticket


class TestNoticeProcessor(unittest.TestCase):

    # Mocks
    exchange_cache = DataSource()
    exchange_cache.exchange_group_name = 'NASDAQ OMX NORDIC'
    exchange_cache.exchange_owner = '5ef34fa360d3c80ac90711b3'
    exchange_cache.ops_analysts = ['5ef34fa360d3c80ac90711b3', '5a8c12f1169207417fa5a52f']

    mock_data_sources_cache = {
        'NASDAQ_OMX_NORDIC_IT_NOTICES': exchange_cache
    }

    mock_config_obj = {
        'SERVICE_DESK': {
            'JIRA_URL': 'https://smartstreamrdu.atlassian.net',
            'JIRA_USERNAME': 'rdudataquality@smartstreamrdu.com',
            'JIRA_TOKEN': 'tI4L0JAwA2xAnrEiX46M8A50',
            'JIRA_PROJECT_KEY': 'ENSWEB',
            'JIRA_ISSUE_TYPE': 'Task',
            'JIRA_REPORTER': '5c8a4fd2677d763daafa57c7',
            'JIRA_FALL_BACK_EXCH_OWNER': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1',
            'JIRA_FALL_BACK_EXCH_OWNER_NAME': 'Pallavi Kulkarni',
            'JIRA_FALLBACK_OWNER_EMAIL': 'pallavi.kulkarni@smartstreamrdu.com'
        }
    }

    expected_dict = {
        'update': {},
        'fields': {
            'project': {
                'key': 'ENSWEB'
            },
            'summary': 'This is subject to testing',
            'issuetype': {'name': 'Task'},
            'description': 'This is subject to testing \n\nhttps://www.smartstreamrdu.com',
            'customfield_10030': '247',
            'assignee': {'accountId': '5ef34fa360d3c80ac90711b3'},
            'reporter': {'accountId': '5c8a4fd2677d763daafa57c7'},
            'customfield_10251': {'accountId': '5ef34fa360d3c80ac90711b3'},
            'customfield_10031': [{'accountId': '5ef34fa360d3c80ac90711b3'}, {'accountId': '5a8c12f1169207417fa5a52f'}],
            'customfield_10242': '2022-12-19',
            'customfield_10243': '2022-12-20',
            'customfield_10282': '231-654',
            'customfield_10283': 'https://www.smartstreamrdu.com',
            'customfield_10274': 'NASDAQ OMX NORDIC',
            'customfield_10275': 'NASDAQ_OMX_NORDIC_IT_NOTICES'
        }
    }

    maxDiff = None

    @patch('modules.notice_processor.datasources_cache', mock_data_sources_cache)
    @patch('modules.generic_functions.config_object', mock_config_obj)
    def test_set_service_desk_json(self):

        test_notice = Notice('nasdaq_it')
        test_notice.exchangeReferenceId = '231-654'
        test_notice.insDate = datetime.datetime(2022, 12, 20, 0, 0, 0)
        test_notice.eventPublishDate = datetime.datetime(2022, 12, 19, 22, 10, 5)
        test_notice.eventSubject = 'This is subject to testing'
        test_notice.eventInitialUrl = 'https://www.smartstreamrdu.com'
        test_notice.exchangeSourceName = 'NASDAQ_OMX_NORDIC_IT_NOTICES'
        function_result = set_service_desk_json(test_notice)
        self.assertDictEqual(TestNoticeProcessor.expected_dict, function_result)

    @patch('modules.notice_processor.datasources_cache', mock_data_sources_cache)
    @patch('modules.generic_functions.config_object', mock_config_obj)
    def test_set_service_desk_json_long_summary(self):

        expected_dict = TestNoticeProcessor.expected_dict
        expected_dict['fields']['description'] = 'This is a very long subject to testing. This is a very long subject to testing. This is a very ' \
                                                 'long subject to testing. This is a very long subject to testing. This is a very long subject to ' \
                                                 'testing. This is a very long subject to testing. This is a very long subject to testing. This is ' \
                                                 'a very long subject to testing \n\nhttps://www.smartstreamrdu.com'
        expected_dict['fields']['summary'] = 'This is a very long subject to testing. This is a very long subject to testing. This is a ' \
                                             'very long subject to testing. This is a very long subject to testing. This is a very long subject to ' \
                                             'testing. This is a very long subject to testing. This is a very '

        test_notice = Notice('nasdaq_it')
        test_notice.exchangeReferenceId = '231-654'
        test_notice.insDate = datetime.datetime(2022, 12, 20, 0, 0, 0)
        test_notice.eventPublishDate = datetime.datetime(2022, 12, 19, 22, 10, 5)
        test_notice.eventSubject = 'This is a very long subject to testing. This is a very long subject to testing. This is a very long subject to ' \
                                   'testing. This is a very long subject to testing. This is a very long subject to testing. This is a very long ' \
                                   'subject to testing. This is a very long subject to testing. This is a very long subject to testing'
        test_notice.eventInitialUrl = 'https://www.smartstreamrdu.com'
        test_notice.exchangeSourceName = 'NASDAQ_OMX_NORDIC_IT_NOTICES'
        function_result = set_service_desk_json(test_notice)
        self.assertDictEqual(expected_dict, function_result)

    @patch('requests.request')
    @patch('modules.generic_functions.config_object', mock_config_obj)
    def test_make_service_desk_request_error(self, mock_reply):

        mocked_response = requests.Response()
        mocked_response.status_code = 405
        mocked_response.reason = 'Method Not Allowed'
        type(mocked_response).text = mock.PropertyMock(return_value='')
        mock_reply.return_value = mocked_response

        with self.assertLogs() as logs:
            create_service_desk_ticket([TestNoticeProcessor.expected_dict])
        self.assertEqual(len(logs.records), 2)
        self.assertEqual(logs.records[1].getMessage(), 'Failed to create tickets due to: Method Not Allowed. Error: status_code: 405, errors: ')

    @patch('requests.request')
    @patch('modules.generic_functions.config_object', mock_config_obj)
    def test_make_service_desk_request_create_201(self, mock_reply):
        mocked_response = requests.Response()
        mocked_response.status_code = 201
        mocked_response.reason = 'Created'
        type(mocked_response).text = mock.PropertyMock(return_value='{"id":"3011031","key":"ENSWEB-40875","self":"https://smartstreamrdu.atlassian'
                                                                    '.net/rest/api/2/issue/3011031"}')
        mock_reply.return_value = mocked_response

        with self.assertLogs() as logs:
            create_service_desk_ticket([TestNoticeProcessor.expected_dict])
        self.assertEqual(len(logs.records), 2)
        self.assertEqual(logs.records[1].getMessage(), 'Service desk tickets successfully created')


if __name__ == '__main__':
    unittest.main()
