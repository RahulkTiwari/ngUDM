# Third party libraries
import unittest
import requests
from requests.auth import HTTPBasicAuth
import json
# Custom libraries
from issueStormingTicketStatusUpdate import main
from libraries.caches.database_connection import database_connection

jira_id = None


class TestInactivatingWorkItems(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        # Create service desk ticket, update it to in progress and then closed. Then create the 2 entries in enData and workItem collection to be
        # updated.

        global jira_id

        create_data = json.dumps({
            'fields': {
                'project': {
                    'key': 'ENSTEST'
                },
                'summary': 'FOO:Option:Strike price:New Strikes Stock Products 48/23',
                'issuetype': {
                    'name': 'Task'
                },
                'description': 'FOO:Option:Strike price:New Strikes Stock Products 48/23',
                'customfield_10030': '247',  # Request Type default to EN (247)
                'assignee': {'accountId': '5b18ed5e82e05b22cc7d6200'},
                'reporter': {'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'},
                'customfield_10251': {'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'},  # Primary exchange owner
                'customfield_10031': [{'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'}],  # Additional exchange owners
                'customfield_10242': '2022-08-12',
                'customfield_10243': '2022-08-11',
                'customfield_10282': 'MO/DT/208/22',
                'customfield_10274': 'FOOBAR',
                'customfield_10275': 'FOO',
                'customfield_10285': 'NEW OPTIONS STRIKE SERIES|2022-08-12|Option|MO/DT/208/22|FOO',
                'customfield_10279': {'value': 'Option'}
            }
        })

        create_response = requests.request(
            'POST',
            'https://smartstreamrdu.atlassian.net/rest/api/2/issue',
            data=create_data,
            headers={
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            auth=HTTPBasicAuth('rdudataquality@smartstreamrdu.com', 'tI4L0JAwA2xAnrEiX46M8A50')
        )

        jira_id = json.loads(create_response.text)['key']
        print(f'Created Jira id {jira_id}')

        # Update Jira to in progress
        update_data = json.dumps({
            'transition': {'id': '11'}
        })
        _update_response = requests.request(
            'POST',
            f'https://smartstreamrdu.atlassian.net/rest/api/2/issue/{jira_id}/transitions',
            data=update_data,
            headers={
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            auth=HTTPBasicAuth('rdudataquality@smartstreamrdu.com', 'tI4L0JAwA2xAnrEiX46M8A50')
        )

        # Update Jira to closed invalid
        close_data = json.dumps({
            'transition': {'id': '181'}
        })
        _closed_response = requests.request(
            'POST',
            f'https://smartstreamrdu.atlassian.net/rest/api/2/issue/{jira_id}/transitions',
            data=close_data,
            headers={
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            auth=HTTPBasicAuth('rdudataquality@smartstreamrdu.com', 'tI4L0JAwA2xAnrEiX46M8A50')
        )

        work_item_data = {
            'stormKey': {'RDU': {'value': 'NEW OPTIONS STRIKE SERIES|2022-08-08|Option|MO/DT/202/22|FOO'}},
            'workItemStatus': {'RDU': {'value': {'normalizedValue': 'A'}}},
            'workItemType': {'RDU': {'value': {'normalizedValue': 'EN Workflow Item'}}},
            'hash': {'RDU': {'value': '761729064'}},
            'workItemDataLevel': {'RDU': {'value': {'normalizedValue': 'EN'}}},
            'workItemId': {'RDU': {'value': f'{jira_id}'}}
        }
        work_item_coll = database_connection['workItem']

        work_item_coll.insert_one(work_item_data)

        en_data_data = {
            'enWorkItemReference': [
                {
                    'stormKey': {'RDU': {'value': 'NEW OPTIONS STRIKE SERIES|2022-08-08|Option|MO/DT/202/22|FOO'}},
                    'workItemStatus': {'RDU': {'value': {'normalizedValue': 'A'}}}
                }
            ],
            'eventSourceUniqueId': {'FEED': {'value': '20220811_HKFE_NEW OPTIONS STRIKE SERIES_MO/DT/202/22'}},
            'exchangeCode': {'FEED': {'value': {'val': 'HONG KONG FUTURES EXCHANGE LIMITED','val2': 'HKFE','domain': 'exchangeCodeMap'}}},
            'exchangeSourceName': {'FEED': {'value': {'val': 'HKFE','domain': 'enDataSourceCodeMap'}}},
            'eventStatus': {'ENRICHED': {'value': {'normalizedValue': 'A'}}}
        }

        en_data_coll = database_connection['enData']

        en_data_coll.insert_one(en_data_data)

    def test_main(self):
        main()

        work_item_coll = database_connection['workItem']
        work_item_cursor = work_item_coll.find_one({'stormKey.RDU.value': 'NEW OPTIONS STRIKE SERIES|2022-08-08|Option|MO/DT/202/22|FOO'})
        self.assertEqual('I', work_item_cursor['workItemStatus']['RDU']['value']['normalizedValue'])

        en_data_coll = database_connection['enData']
        en_data_cursor = en_data_coll.find_one({
            'enWorkItemReference.stormKey.RDU.value': 'NEW OPTIONS STRIKE SERIES|2022-08-08|Option|MO/DT/202/22|FOO'
        })
        self.assertEqual('I', en_data_cursor['enWorkItemReference'][0]['workItemStatus']['RDU']['value']['normalizedValue'])

    def tearDown(self):
        # Delete Jira entry
        _delete_response = requests.request(
            'DELETE',
            f'https://smartstreamrdu.atlassian.net/rest/api/2/issue/{jira_id}',
            headers={
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            auth=HTTPBasicAuth('rdudataquality@smartstreamrdu.com', 'tI4L0JAwA2xAnrEiX46M8A50')
        )


if __name__ == '__main__':
    unittest.main()
