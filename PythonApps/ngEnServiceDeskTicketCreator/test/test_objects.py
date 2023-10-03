# Third party libraries
import unittest
from unittest import mock
from unittest.mock import patch
import json
import datetime
import copy
import os
import requests
from pymongo import MongoClient
# Override statics
import libraries.static_vars as static
static.TEST_MODE = True
# Custom libraries
from test.test_generic_functions import read_properties
from libraries.objects.Exchange import Exchange, ExchangeList
from libraries.objects.WorkItem import get_ops_user, cleansed_globex_subject, string_hash, clean_subject, WorkItem
from libraries.objects.WorkItemsStorm import get_work_items_from_database, update_work_items_collection, WorkItemStormCache, WorkItemsStormObj

work_item = None
db_instance = None

MONGO_ENS_DOC = {
        'dataSource': {'FEED': {'value': {'val': 'rduEns'}}},
        'enRawDataId': {'FEED': {'value': '62f4e1265c4469503fd22f51'}},
        'eventSourceUniqueId': {'FEED': {'value': '20220811_HKFE_NEW OPTIONS STRIKE SERIES_MO/DT/208/22_1'}},
        'exchangeCode': {'FEED': {'value': {'val': 'HONG KONG FUTURES EXCHANGE LIMITED', 'val2': 'HKFE', 'domain': 'exchangeCodeMap'}}},
        'exchangeSourceName': {'FEED': {'value': {'val': 'HKFE', 'domain': 'enDataSourceCodeMap'}}},
        'eventPublishDate': {'FEED': {'value': datetime.datetime(2022, 8, 11, 0, 0)}},
        'eventInsertDate': {'FEED': {'value': datetime.datetime(2022, 8, 11, 0, 0)}},
        'eventEffectiveDate': {'FEED': {'value': datetime.datetime(2022, 8, 12, 0, 0)}},
        'eventType': {'FEED': {'value': {'val': 'NEW OPTIONS STRIKE SERIES', 'val2': 'HKFE', 'domain': 'eventTypeMap'}}},
        'eventSubject': {'FEED': {'value': 'New Index Options Strike Series'}},
        'eventSummaryText': {'FEED': {'value': 'Hong Kong Futures Exchange announces the addition of the following strike series effective 12 August '
                                               '2022.'}},
        'instrumentTypeCode': {'FEED': {'value': {'val': 'INDEX OPTIONS', 'val2': 'HKFE', 'domain': 'assetClassificationsMap'}}},
        'exchangePrefix': {'FEED': {'value': 'MTW'}},
        'productName': {'FEED': {'value': 'MTW Options \nMSCI'}},
        'series': {'FEED': {'value': ['August 2022', 'September 2023']}},
        'eventInitialUrl': {'FEED': {'value': 'https://www.hkex.com.hk/-/media/HKEX-Market/Services/Circulars-and-Notices/Participant-and-Members-'
                                              'Circulars/HKFE/2022/''MO_DT_208_22_ec.pdf'}},
        'exchangeReferenceId': {'FEED': {'value': 'MO/DT/208/22'}},
        'exchangeReferenceCounter': {'FEED': {'value': 1}},
        'events': {'FEED': {'value': ['20220811_HKFE_NEW OPTIONS STRIKE SERIES_MO-DT-208-22.pdf']}}
}


class ExchangeObj(unittest.TestCase):

    def test_set_exchange(self):

        exchange_mongo_doc = {
            'status': {'RDU': {'value': 'A'}},
            'code': {'RDU': {'value': 'KRX_KIND'}},
            'url': {'RDU': {'value': 'https://kind.krx.co.kr/disclosure/details.do?method=searchDetailsMain'}},
            'exchangeGroupName': {'RDU': {'value': 'KRX'}},
            'exchangeOwner': {'RDU': {'value': 'lavanya.bellamkonda@smartstreamrdu.com'}},
            'allOpsAnalysts': {'RDU': {'value': 'lavanya.bellamkonda@smartstreamrdu.com,pallavi.kulkarni@smartstreamrdu.com'}}
        }

        ops_users = [
            {'user': 'lavanya.bellamkonda@smartstreamrdu.com', 'account_id': '5c775c61017b4a53c68ada6b'},
            {'user': 'pallavi.kulkarni@smartstreamrdu.com', 'account_id': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'}
        ]

        exchange = Exchange()
        exchange.set_exchange(exchange_mongo_doc)
        self.assertEqual('KRX_KIND', exchange.code)
        self.assertEqual('https://kind.krx.co.kr/disclosure/details.do?method=searchDetailsMain', exchange.url)
        self.assertEqual('KRX', exchange.exchange_group_name)
        self.assertEqual('5c775c61017b4a53c68ada6b', exchange.exchange_owner['account_id'])
        self.assertEqual(ops_users, exchange.ops_analysts)
        self.assertEqual('KRX_KIND', exchange.__str__())

    def test_get_exchange_by_code(self):
        list_of_exchanges = ExchangeList()
        function_result = list_of_exchanges.get_exchange_by_code('ATHEXGROUP_EMAIL')
        self.assertIsInstance(function_result, Exchange)
        self.assertEqual('Athens Derivatives Exchange', function_result.exchange_group_name)

    def test_get_exchange_by_code_fallback(self):
        list_of_exchanges = ExchangeList()
        function_result = list_of_exchanges.get_exchange_by_code('FOOBAR')
        self.assertIsInstance(function_result, Exchange)
        self.assertEqual('default', function_result.exchange_group_name)
        self.assertEqual({'user': 'pallavi.kulkarni@smartstreamrdu.com', 'account_id': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'},
                         function_result.exchange_owner)
        self.assertEqual([{'user': 'pallavi.kulkarni@smartstreamrdu.com', 'account_id': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'}],
                         function_result.ops_analysts)
        self.assertIsNone(function_result.code)
        self.assertIsNone(function_result.url)

    def test_get_ops_user(self):
        function_result = get_ops_user('amrutha.byravamurthy@smartstreamrdu.com')
        self.assertEqual({'user': 'amrutha.byravamurthy@smartstreamrdu.com', 'account_id': '5da9437aae16d60d8e6e60f6'}, function_result)

    def test_get_ops_user_default(self):
        function_result = get_ops_user('foo.bar@smartstreamrdu.com')
        self.assertEqual({'user': 'foo.bar@smartstreamrdu.com', 'account_id': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'}, function_result)


class WorkItemObj(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        global work_item

        notice = MONGO_ENS_DOC
        work_item = WorkItem(notice)

    def test_cleansed_globex_subject(self):
        org_subject = 'Update - foobar'
        function_result = cleansed_globex_subject(org_subject)
        self.assertEqual('foobar', function_result)

    def test_cleansed_globex_subject_multi_keywords(self):
        org_subject = 'Updated - Correction - foobar'
        function_result = cleansed_globex_subject(org_subject)
        self.assertEqual('foobar', function_result)

    def test_cleansed_globex_subject_no_keyword(self):
        org_subject = 'Update cfoobar'
        function_result = cleansed_globex_subject(org_subject)
        self.assertEqual('Update cfoobar', function_result)

    def test_string_has(self):
        function_result = string_hash('this|is|a|001|test%20| string')
        self.assertEqual('238627852', function_result)

    def test_clean_subject(self):
        org_subject = 'New - cfoobar\n continues on next line'
        function_result = clean_subject(org_subject)
        self.assertEqual('cfoobar| continues on next line', function_result)

    def test_clean_subject_wo_keyword(self):
        org_subject = 'cfoobar\n continues on next line'
        function_result = clean_subject(org_subject)
        self.assertEqual('cfoobar| continues on next line', function_result)

    def test_clean_subject_wo_keyword_and_linebreak(self):
        org_subject = 'cfoobar continues on next line'
        function_result = clean_subject(org_subject)
        self.assertEqual('cfoobar continues on next line', function_result)

    def test_initalization_work_item_object(self):
        self.assertEqual('OPEN', work_item.issue_status)
        self.assertEqual('EN NOTIFICATION', work_item.issue_type)
        self.assertEqual('20220811_HKFE_NEW OPTIONS STRIKE SERIES_MO/DT/208/22_1', work_item.event_source_unique_id)
        self.assertIsNone(work_item.work_item_id)
        self.assertEqual('New', work_item.work_item_type)
        self.assertEqual('2022-08-11', work_item.insert_date)
        self.assertEqual('2022-08-11', work_item.publish_date)
        self.assertEqual('2022-08-12', work_item.event_effective_date)
        self.assertEqual('New Index Options Strike Series', work_item.event_subject)
        self.assertEqual('NEW OPTIONS STRIKE SERIES|HKFE', work_item.event_type)
        self.assertEqual('HANGSENG', work_item.exchange_group_name)
        self.assertEqual('MO/DT/208/22', work_item.exchange_reference_id)
        self.assertEqual('HKFE', work_item.exchange_source_name)
        self.assertEqual([], work_item.previous_references)
        self.assertEqual('MTW', work_item.exchange_prefix)
        self.assertEqual('MTW_Options_MSCI', work_item.product_name)
        self.assertEqual([], work_item.underlying_names)
        self.assertEqual([], work_item.underlying_tickers)
        self.assertEqual('Hong Kong Futures Exchange announces the addition of the following strike series effective 12 August 2022.',
                         work_item.event_summary)
        self.assertEqual('https://www.hkex.com.hk/-/media/HKEX-Market/Services/Circulars-and-Notices/Participant-and-Members-Circulars/HKFE/2022/'
                         'MO_DT_208_22_ec.pdf', work_item.event_initial_url)
        self.assertEqual('Option', work_item.norm_instrument_type)
        self.assertEqual('Strike Minimum Price Increment/Range Change', work_item.norm_event_type)
        self.assertEqual({'user': 'prashanth.krishna@smartstreamrdu.com', 'account_id': '5e424ba590dfb70c9e6067f0'}, work_item.exchange_owner)
        self.assertEqual([{'accountId': '5e424ba590dfb70c9e6067f0'}, {'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'}],
                         work_item.exchange_owners)
        self.assertEqual('NEW OPTIONS STRIKE SERIES|2022-08-12|Option|MO/DT/208/22|HKFE', work_item.storm_key)
        self.assertEqual('762106423', work_item.storm_hash)
        self.assertIsNone(work_item.previous_storm_hash)
        self.assertFalse(work_item.hash_update)
        self.assertIsNone(work_item.create_work_item_json)
        self.assertIsNone(work_item.update_work_item_json)
        self.assertIsNone(work_item.work_item_transition_json)

    def test_test_init_work_item_object_error_code_insert_date(self):
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        del mongo_update_doc['eventInsertDate']['FEED']['value']
        mongo_update_doc['eventInsertDate']['FEED']['errorCode'] = 103
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual('9999-12-31', updated_work_item.insert_date)

    def test_test_init_work_item_object_error_code_publish_date(self):
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        del mongo_update_doc['eventPublishDate']['FEED']['value']
        mongo_update_doc['eventPublishDate']['FEED']['errorCode'] = 104
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual('9999-12-31', updated_work_item.publish_date)

    def test_test_init_work_item_object_long_exchange_reference_id(self):
        long_reference_id = 'ThisIsAVeryLongReferenceId_%ThisIsAVeryLongReferenceId_%ThisIsAVeryLongReferenceId_%ThisIsAVeryLongReferenceId_%ThisI' \
                            'sAVeryLongReferenceId_%ThisIsAVeryLongReferenceId_%ThisIsAVeryLongReferenceId_%ThisIsAVeryLongReferenceId_%ThisIsAVer' \
                            'yLongReferenceId_%ThisIsAVeryLongReferenceId_%ThisIsAVeryLongReferenceId_%'
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        mongo_update_doc['exchangeReferenceId']['FEED']['value'] = long_reference_id
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual(long_reference_id, updated_work_item.exchange_reference_id)

    def test_test_init_work_item_object_exchange_prefix_with_space(self):
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        mongo_update_doc['exchangePrefix']['FEED']['value'] = 'FOO BAR'
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual('FOO_BAR', updated_work_item.exchange_prefix)

    def test_test_init_work_item_object_long_event_summary(self):
        long_summary = 'ThisIsAVeryLongEventSummary_%ThisIsAVeryLongEventSummary_%ThisIsAVeryLongEventSummary_%ThisIsAVeryLongEventSummary_%ThisIsA' \
                       'VeryLongEventSummary_%ThisIsAVeryLongEventSummary_%ThisIsAVeryLongEventSummary_%ThisIsAVeryLongEventSummary_%ThisIsAVeryLon' \
                       'gEventSummary_%ThisIsAVeryLongEventSummary_%ThisIsAVeryLongEventSummary_%'
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        mongo_update_doc['eventSummaryText']['FEED']['value'] = long_summary
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual(long_summary, updated_work_item.event_summary)

    def test_test_init_work_item_object_event_summary_missing(self):
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        del mongo_update_doc['eventSummaryText']
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual('', updated_work_item.event_summary)

    def test_test_init_work_item_object_event_initial_url_with_space(self):
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        mongo_update_doc['eventInitialUrl']['FEED']['value'] = 'https://www.something.com/url with space'
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual('https://www.something.com/url%20with%20space', updated_work_item.event_initial_url)

    def test_test_init_work_item_object_norm_instrument_type_invalid_type(self):
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        del mongo_update_doc['instrumentTypeCode']['FEED']
        mongo_update_doc['instrumentTypeCode'] = {'RDU': {'value': {'normalizedValue': '6401'}}}
        updated_work_item = WorkItem(mongo_update_doc)
        self.assertEqual('errorCode101', updated_work_item.norm_instrument_type)

    def test_str_representation_work_item(self):
        self.assertEqual('20220811_HKFE_NEW OPTIONS STRIKE SERIES_MO/DT/208/22_1', work_item.__str__())

    def test_get_storm_key_basic(self):
        mongo_storm_key_doc = {
            'exchangeSourceName': {'FEED': {'value': {'val': 'HKFE', 'domain': 'enDataSourceCodeMap'}}},
            'eventEffectiveDate': {'FEED': {'value': datetime.datetime(2022, 8, 12, 0, 0)}},
            'eventType': {'FEED': {'value': {'val': 'NEW OPTIONS STRIKE SERIES', 'val2': 'HKFE', 'domain': 'eventTypeMap'}}},
            'eventSubject': {'FEED': {'value': 'New Index Options Strike Series'}},
            'instrumentTypeCode': {'FEED': {'value': {'val': 'INDEX OPTIONS', 'val2': 'HKFE', 'domain': 'assetClassificationsMap'}}},
            'exchangeReferenceId': {'FEED': {'value': 'MO/DT/208/22'}}
        }

        function_result = WorkItem(mongo_storm_key_doc).get_storm_key(mongo_storm_key_doc)
        self.assertEqual('NEW OPTIONS STRIKE SERIES|2022-08-12|Option|MO/DT/208/22|HKFE', function_result)

    def test_get_storm_key_globex(self):
        mongo_storm_key_doc = {
            'exchangeSourceName': {'FEED': {'value': {'val': 'CMEG_GLOBEX', 'domain': 'enDataSourceCodeMap'}}},
            'eventEffectiveDate': {'FEED': {'value': datetime.datetime(2022, 8, 12, 0, 0)}},
            'eventType': {'FEED': {'value': {'val': 'NEW OPTIONS STRIKE SERIES', 'val2': 'HKFE', 'domain': 'eventTypeMap'}}},
            'eventSubject': {'FEED': {'value': 'Updated - New Index Options Strike Series'}},
            'instrumentTypeCode': {'FEED': {'value': {'val': 'INDEX OPTIONS', 'val2': 'HKFE', 'domain': 'assetClassificationsMap'}}},
            'exchangeReferenceId': {'FEED': {'value': 'MO/DT/208/22'}}
        }
        function_result = WorkItem(mongo_storm_key_doc).get_storm_key(mongo_storm_key_doc)
        self.assertEqual('NEW OPTIONS STRIKE SERIES|2022-08-12|Option|New Index Options Strike Series|CMEG_GLOBEX', function_result)

    def test_get_storm_key_rdu_lock(self):
        mongo_storm_key_doc = {
            'exchangeSourceName': {'FEED': {'value': {'val': 'CMEG_GLOBEX', 'domain': 'enDataSourceCodeMap'}}},
            'eventEffectiveDate': {'FEED': {'value': datetime.datetime(2022, 8, 12, 0, 0)}},
            'eventType': {'FEED': {'value': {'val': 'NEW OPTIONS STRIKE SERIES', 'val2': 'HKFE', 'domain': 'eventTypeMap'}}},
            'eventSubject': {'FEED': {'value': 'Updated - New Index Options Strike Series'}},
            'instrumentTypeCode': {
                'FEED': {'value': {'val': 'INDEX OPTIONS', 'val2': 'HKFE', 'domain': 'assetClassificationsMap'}},
                'RDU': {'value': {'normalizedValue': '62', 'domain': 'assetClassificationsMap'}}
               },
            'exchangeReferenceId': {'FEED': {'value': 'MO/DT/208/22'}}
        }
        function_result = WorkItem(mongo_storm_key_doc).get_storm_key(mongo_storm_key_doc)
        self.assertEqual('NEW OPTIONS STRIKE SERIES|2022-08-12|Strategy|New Index Options Strike Series|CMEG_GLOBEX', function_result)

    def test_set_create_work_item_json(self):
        self.maxDiff = None
        create_json = {
            'fields': {
                'project': {
                    'key': 'ENSTEST'
                },
                'summary': 'HANGSENG:Option:Strike Minimum Price Increment/Range Change:New Index Options Strike Series',
                'issuetype': {'name': 'Task'},
                'description': 'Hong Kong Futures Exchange announces the addition of the following strike series effective 12 August 2022.',
                'customfield_10030': '247',
                'assignee': {'accountId': '5e424ba590dfb70c9e6067f0'},
                'reporter': {'accountId': '5c8a4fd2677d763daafa57c7'},
                'customfield_10251': {'accountId': '5e424ba590dfb70c9e6067f0'},
                'customfield_10031': [{'accountId': '5e424ba590dfb70c9e6067f0'}, {'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'}],
                'customfield_10242': '2022-08-11',
                'customfield_10243': '2022-08-11',
                'customfield_10282': 'MO/DT/208/22',
                'customfield_10274': 'HANGSENG',
                'customfield_10275': 'HKFE',
                'customfield_10285': 'NEW OPTIONS STRIKE SERIES|2022-08-12|Option|MO/DT/208/22|HKFE',
                'customfield_10279': {'value': 'Option'},
                'customfield_10259': ['MTW'],
                'customfield_10261': ['MTW_Options_MSCI'],
                'customfield_10262': [],
                'customfield_10263': [],
                'customfield_10260': [],
                'customfield_10283': 'https://www.hkex.com.hk/-/media/HKEX-Market/Services/Circulars-and-Notices/Participant-and-Members-Circulars/'
                                     'HKFE/2022/MO_DT_208_22_ec.pdf',
                'customfield_10238': '2022-08-12'
            }
        }

        function_result = work_item.set_create_work_item_json()
        self.assertEqual(json.dumps(create_json), function_result)

    def test_set_create_work_item_json_long_summary_and_error_code_without_prefix(self):
        self.maxDiff = None
        long_subject = 'New Index Options Strike Series,New Index Options Strike Series,New Index Options Strike Series,New Index Options Strike ' \
                       'Series,New Index Options Strike Series,New Index Options Strike Series,New Index Options Strike Series,New Index Options ' \
                       'Strike Series,New Index Options Strike Series'
        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        mongo_update_doc['eventSubject']['FEED']['value'] = long_subject
        updated_work_item = WorkItem(mongo_update_doc)
        updated_work_item.event_effective_date = 103
        updated_work_item.exchange_prefix = 'default'

        create_json = {
            'fields': {
                'project': {
                    'key': 'ENSTEST'
                },
                'summary': 'HANGSENG:Option:Strike Minimum Price Increment/Range Change:New Index Options Strike Series,New Index Options Strike '
                           'Series,New Index Options Strike Series,New Index Options Strike Series,New Index Options Strike Series,New Index Opt'
                           'ions Strike Series,New',
                'issuetype': {'name': 'Task'},
                'description': 'Hong Kong Futures Exchange announces the addition of the following strike series effective 12 August 2022.',
                'customfield_10030': '247',
                'assignee': {'accountId': '5e424ba590dfb70c9e6067f0'},
                'reporter': {'accountId': '5c8a4fd2677d763daafa57c7'},
                'customfield_10251': {'accountId': '5e424ba590dfb70c9e6067f0'},
                'customfield_10031': [{'accountId': '5e424ba590dfb70c9e6067f0'}, {'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'}],
                'customfield_10242': '2022-08-11',
                'customfield_10243': '2022-08-11',
                'customfield_10282': 'MO/DT/208/22',
                'customfield_10274': 'HANGSENG',
                'customfield_10275': 'HKFE',
                'customfield_10285': 'NEW OPTIONS STRIKE SERIES|2022-08-12|Option|MO/DT/208/22|HKFE',
                'customfield_10279': {'value': 'Option'},
                'customfield_10261': ['MTW_Options_MSCI'],
                'customfield_10262': [],
                'customfield_10263': [],
                'customfield_10260': [],
                'customfield_10283': 'https://www.hkex.com.hk/-/media/HKEX-Market/Services/Circulars-and-Notices/Participant-and-Members-Circulars/'
                                     'HKFE/2022/MO_DT_208_22_ec.pdf',
                'customfield_10273': '103'
            }
        }

        function_result = updated_work_item.set_create_work_item_json()
        self.assertEqual(json.dumps(create_json), function_result)

    def test_set_update_work_item_json(self):
        self.maxDiff = None
        underlyings = [
            {'instrumentCounter': {'FEED': {'value': 1}},
             'cusip': {'FEED': {'value': '131193104'}},
             'nameLong': {'FEED': {'value': 'Topgolf Callaway Brands Corporation (MODG) Common Shares'}},
             'exchangeTicker': {'FEED': {'value': 'MODG'}}
             },
            {'instrumentCounter': {'FEED': {'value': 2}},
             'cusip': {'FEED': {'value': '987654321'}},
             'nameLong': {'FEED': {'value': 'Second underlying name long'}},
             'exchangeTicker': {'FEED': {'value': 'GDOM'}}
             }
        ]

        mongo_update_doc = copy.deepcopy(MONGO_ENS_DOC)
        mongo_update_doc['underlyings'] = underlyings
        updated_work_item = WorkItem(mongo_update_doc)

        json_work_item_update = {
            'update': {
                'customfield_10259': [{'add': 'MTW'}],
                'customfield_10261': [{'add': 'MTW_Options_MSCI'}],
                'customfield_10262': [
                    {'add': 'Topgolf_Callaway_Brands_Corporation_(MODG)_Common_Shares'},
                    {'add': 'Second_underlying_name_long'}
                ],
                'customfield_10263': [{
                        'add': 'MODG'
                    }, {
                        'add': 'GDOM'
                    }
                ]
            },
            'fields': {
                'summary': 'HANGSENG:Option:Strike Minimum Price Increment/Range Change:New Index Options Strike Series',
                'customfield_10238': '2022-08-12'
            }
        }

        function_result = updated_work_item.set_update_work_item_json()
        self.assertEqual(json.dumps(json_work_item_update), function_result)

    def test_set_work_item_transition_json(self):
        transition_json = {
            'update': {'comment': [{'add': {'body': 'Ticket has been reopened by SYSTEM.'}}]},
            'transition': {'id': '261'}
        }

        self.assertEqual(json.dumps(transition_json), work_item.set_work_item_transition_json())

    def test_set_storm_object(self):
        work_item.update_endata()

        connection_properties = read_properties(os.path.realpath(os.curdir) + '/test/resources/database.properties')

        client = MongoClient(
            host=connection_properties['host'],
            port=int(connection_properties['port']),
            username=connection_properties['user'],
            password=connection_properties['password'],
            authSource=connection_properties['database'],
            authMechanism='SCRAM-SHA-256'
        )
        test_db_instance = client[connection_properties['database']]

        collection = test_db_instance['enData']

        query_result = collection.find({
            'enWorkItemReference': {'$exists': True}
        })

        function_result = query_result[0]['enWorkItemReference'][0]['stormKey']['RDU']['value']
        self.assertEqual('NEW OPTIONS STRIKE SERIES|2022-08-12|Option|MO/DT/208/22|HKFE', function_result)

    @patch('requests.request')
    def test_make_service_desk_request_create_201(self, mock_reply):
        mock_work_item = WorkItem(MONGO_ENS_DOC)
        mock_work_item.create_work_item_json = {'foo': 'bar'}

        mocked_response = requests.Response()
        mocked_response.status_code = 201
        mocked_response.reason = 'Created'
        type(mocked_response).text = mock.PropertyMock(return_value='{"id":"3011031","key":"ENSTEST-40875","self":"https://smartstreamrdu.atlassian'
                                                                    '.net/rest/api/2/issue/3011031"}')
        mock_reply.return_value = mocked_response

        function_result = mock_work_item.make_service_desk_request(request_type='create')
        self.assertEqual('ENSTEST-40875', json.loads(function_result.text)["key"])

    @patch('requests.request')
    def test_make_service_desk_request_update_204(self, mock_reply):
        mock_work_item = WorkItem(MONGO_ENS_DOC)
        mock_work_item.create_work_item_json = {'foo': 'bar'}
        mock_work_item.work_item_id = 'ENSTEST-123456'

        mocked_response = requests.Response()
        mocked_response.status_code = 204
        mocked_response.reason = 'No Content'
        mocked_response.text
        mock_reply.return_value = mocked_response

        function_result = mock_work_item.make_service_desk_request(request_type='update')
        self.assertIsNotNone(function_result)
        self.assertEqual('ENSTEST-123456', mock_work_item.work_item_id)

    @patch('requests.request')
    def test_make_service_desk_request_update_404(self, mock_reply):
        mock_work_item = WorkItem(MONGO_ENS_DOC)
        mock_work_item.create_work_item_json = {'foo': 'bar'}
        mock_work_item.work_item_id = 'ENSTEST-123456'

        mocked_response = requests.Response()
        mocked_response.status_code = 404
        mocked_response.reason = 'Not Found'
        type(mocked_response).text = mock.PropertyMock(return_value='{"errorMessages":["Issue does not exist or you do not have permission to see '
                                                                    'it."],"errors":{}}')
        mock_reply.return_value = mocked_response

        function_result = mock_work_item.make_service_desk_request(request_type='update')
        self.assertEqual('Not Found', function_result.reason)

    @patch('requests.request')
    def test_make_service_desk_request_update_405(self, mock_reply):
        mock_work_item = WorkItem(MONGO_ENS_DOC)
        mock_work_item.create_work_item_json = {'foo': 'bar'}
        mock_work_item.work_item_id = 'ENSTEST-123456'

        mocked_response = requests.Response()
        mocked_response.status_code = 405
        mocked_response.reason = 'Method Not Allowed'
        mocked_response.text
        mock_reply.return_value = mocked_response

        function_result = mock_work_item.make_service_desk_request(request_type='update')
        self.assertEqual('Method Not Allowed', function_result.reason)


class WorkItemStormObj(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        global db_instance

        connection_properties = read_properties(os.path.realpath(os.curdir) + '/test/resources/database.properties')

        client = MongoClient(
            host=connection_properties['host'],
            port=int(connection_properties['port']),
            username=connection_properties['user'],
            password=connection_properties['password'],
            authSource=connection_properties['database'],
            authMechanism='SCRAM-SHA-256'
        )
        db_instance = client[connection_properties['database']]

    def test_get_work_items_from_database(self):
        work_item_list = get_work_items_from_database()
        self.assertEqual(1, len(work_item_list))
        self.assertEqual('NEW OPTIONS STRIKE SERIES|2022-08-08|Option|MO/DT/202/22|HKFE', work_item_list[0].storm_key)
        self.assertIsInstance(work_item_list[0], WorkItemsStormObj)

    def test_update_work_items_collection(self):
        update_work_items_collection(work_item)
        collection = db_instance['workItem']
        stored_work_item = collection.find({
            'stormKey.RDU.value': work_item.storm_key
        })
        self.assertRaises(IndexError, lambda:  stored_work_item[1])  # Check length of cursor is 1. However, cursor has no len method
        self.assertEqual('762106423', stored_work_item[0]['hash']['RDU']['value'])
        self.assertEqual('EN', stored_work_item[0]['workItemDataLevel']['RDU']['value']['normalizedValue'])
        self.assertEqual(None, stored_work_item[0]['workItemId']['RDU']['value'])
        self.assertEqual('EN Workflow', stored_work_item[0]['insUser']['RDU']['value'])

    def test_set_storm_object(self):
        collection = db_instance['workItem']
        stored_work_item = collection.find({
            'workItemId.RDU.value': 'ENSPROD-4'
        })[0]
        storm_item = WorkItemsStormObj()
        function_result = storm_item.set_storm_object(stored_work_item)
        self.assertEqual('NEW OPTIONS STRIKE SERIES|2022-08-08|Option|MO/DT/202/22|HKFE', function_result.storm_key)
        self.assertEqual('761729064', function_result.hash)
        self.assertEqual('A', function_result.work_item_status)
        self.assertEqual('EN Workflow Item', function_result.work_item_type)
        self.assertEqual('ENSPROD-4', function_result.work_item_id)
        self.assertEqual('EN', function_result.work_item_data_level)

    def test_set_storm_object_from_work_item(self):
        test_work_item = copy.deepcopy(work_item)
        test_work_item.work_item_id = 'ENSTEST-123456'
        storm_object = WorkItemsStormObj().set_storm_object_from_work_item(test_work_item)
        self.assertEqual('ENSTEST-123456', storm_object.work_item_id)
        self.assertEqual('OPEN', storm_object.work_item_status)
        self.assertEqual('EN Workflow Item', storm_object.work_item_type)
        self.assertEqual('EN', storm_object.work_item_data_level)

    @classmethod
    def tearDownClass(cls):
        # Removing work item to return to initial state of the db
        collection = db_instance['workItem']
        collection.delete_one({'stormKey.RDU.value': work_item.storm_key})


if __name__ == '__main__':
    unittest.main()
