# Third party libraries
import unittest
import datetime
import glob
import os
from pymongo import MongoClient
import time
# Custom libraries
from libraries.generic_functions import get_string_value, get_date_value, get_domain_value, get_value_from_array, get_from_code


# Mongo document to test generic functions
MONGO_DOC = {
    # String attribute values
    'string_rdu_lock': {'RDU': {'value': 'rdu_value'}, 'FEED': {'value': 'feed_value'}},
    'string_feed_lock': {'FEED': {'value': 'feed_value'}},
    'string_error_code': {'FEED': {'errorCode': 103}},
    # Date attribute values
    'date_rdu_lock': {'RDU': {'value': datetime.datetime(2022, 6, 6, 3, 0)}, 'FEED': {'value': datetime.datetime(2022, 7, 6, 6, 0)}},
    'date_feed_lock': {'FEED': {'value': datetime.datetime(2022, 7, 6, 6, 0)}},
    'date_error_code': {'FEED': {'errorCode': 105}},
    # Domain attribute values
    'domain_rdu_lock': {'RDU': {'value': {'normalizedValue': 'Restructure'}}, 'FEED': {'value': {'val': 'REDEMPTION/CASH SETTLEMENT', 'val2': 'OCC',
                                                                                                 'domain': 'eventTypeMap'}}},
    'domain_feed_2_vals_lock': {'FEED': {'value': {'val': 'REDEMPTION/CASH SETTLEMENT', 'val2': 'OCC', 'domain': 'eventTypeMap'}}},
    'domain_feed_val_lock': {"FEED": {"value": {"val": "OCC", "domain": "enDataSourceCodeMap"}}},
    'domain_error_code': {"FEED": {"errorCode": 103}},
    'missing_mapping': {'FEED': {'value': {'val': 'FOOBAR', 'val2': 'OCC', 'domain': 'eventTypeMap'}}},
    'normalized_value': {'RDU': {'value': {'normalizedValue': 'FOOBAR'}}},
    'long': {'FEED': {'value': 'Thisisastringover254charactersThisisastringover254charactersThisisastringover254charactersThisisastringover254chara'
                               'ctersThisisastringover254charactersThisisastringover254charactersThisisastringover254charactersThisisastringover254'
                               'charactersThisisastringover254characters'}},
    # Array attribute values
    'array_rdu_lock': {'RDU': {'value': ['rdu_value1', 'rdu_value2', 'rdu_value3']}, 'FEED': {'value': ['feed_value']}},
    'array_feed_lock': {'FEED': {'value': ['rdu_value1', 'rdu value2', 'rdu_value3']}},
    'nested': [
        {'array_feed_lock': {'FEED': {'value': 'feed value2'}},
         'skipping_attribute': {'FEED': {'value': 'feed_value3'}}
         },
        {'array_feed_lock': {'FEED': {'value': 'feed_value1'}},
         'skipping_attribute': {'FEED': {'value': 'feed_value4'}}
         }
    ],
    'array_error_code': {'FEED': {'errorCode': 103}}
}


def read_properties(database_properties_file, comment_char='#', sep='='):
    properties = {}
    with open(database_properties_file, 'rt') as f:
        for line in f:
            var_l = line.strip()
            if var_l and not var_l.startswith(comment_char):
                key_value = var_l.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip().strip('"')
                if value != '':
                    properties[key] = value
    return properties


class GetStringValue(unittest.TestCase):

    @classmethod
    def setUpClass(cls):

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

        list_of_json_files = glob.glob(os.path.realpath(os.curdir) + '/test/resources/*.json', recursive=False)

        for each_json in list_of_json_files:
            collection = os.path.basename(each_json).split(".")[0]
            db_instance.drop_collection(collection)
            os.system(f'mongoimport '
                      f'--host {connection_properties["host"]} '
                      f'--db {connection_properties["database"]} '
                      f'--port {int(connection_properties["port"])} '
                      f'--username {connection_properties["user"]} '
                      f'--password {connection_properties["password"]} '
                      f'--collection {collection} '
                      f'--file {each_json} ')

        # Wait for the documents to be imported
        time.sleep(50)

    def test_rdu_lock_level(self):
        function_result = get_string_value(MONGO_DOC, 'string_rdu_lock')
        self.assertEqual('rdu_value', function_result)

    def test_feed_lock_level(self):
        function_result = get_string_value(MONGO_DOC, 'string_feed_lock')
        self.assertEqual('feed_value', function_result)

    def test_missing_value(self):
        function_result = get_string_value(MONGO_DOC, 'string_error_code')
        self.assertEqual('default', function_result)

    def test_error_code(self):
        function_result = get_string_value(MONGO_DOC, 'missing')
        self.assertEqual('default', function_result)

    def test_string_longer_than_254_chars(self):
        trimmed_string = 'Thisisastringover254charactersThisisastringover254charactersThisisastringover254charactersThisisastringover254charactersT' \
                         'hisisastringover254charactersThisisastringover254charactersThisisastringover254charactersThisisastringover254charactersTh' \
                         'isisastringov'
        function_result = get_string_value(MONGO_DOC, 'long')
        self.assertEqual(trimmed_string, function_result)

    def test_string_longer_than_254_chars_not_trimmed(self):
        trimmed_string = 'Thisisastringover254charactersThisisastringover254charactersThisisastringover254charactersThisisastringover254charactersT' \
                         'hisisastringover254charactersThisisastringover254charactersThisisastringover254charactersThisisastringover254charactersTh' \
                         'isisastringover254characters'
        function_result = get_string_value(MONGO_DOC, 'long', trim=None)
        self.assertEqual(trimmed_string, function_result)

    def test_normalized_value(self):
        function_result = get_string_value(MONGO_DOC, 'normalized_value')['normalizedValue']
        self.assertEqual('FOOBAR', function_result)


class GetDateValue(unittest.TestCase):

    def test_rdu_lock_level(self):
        function_result = get_date_value(MONGO_DOC, 'date_rdu_lock')
        self.assertEqual('2022-06-06', function_result)

    def test_feed_lock_level(self):
        function_result = get_date_value(MONGO_DOC, 'date_feed_lock')
        self.assertEqual('2022-07-06', function_result)

    def test_error_code(self):
        function_result = get_date_value(MONGO_DOC, 'date_error_code')
        self.assertEqual(105, function_result)

    def test_missing_value(self):
        function_result = get_date_value(MONGO_DOC, 'missingDate')
        self.assertEqual(103, function_result)


class GetDomainValue(unittest.TestCase):

    def test_rdu_lock_level(self):
        function_result = get_domain_value(MONGO_DOC, 'domain_rdu_lock')
        self.assertEqual('Restructure', function_result)

    def test_feed_lock_level_val_val2(self):
        function_result = get_domain_value(MONGO_DOC, 'domain_feed_2_vals_lock')
        self.assertEqual('REDEMPTION/CASH SETTLEMENT|OCC', function_result)

    def test_feed_lock_level_val(self):
        function_result = get_domain_value(MONGO_DOC, 'domain_feed_val_lock')
        self.assertEqual('OCC', function_result)

    def test_error_code(self):
        function_result = get_domain_value(MONGO_DOC, 'domain_error_code')
        self.assertEqual('', function_result)

    def test_missing_mapping(self):
        function_result = get_domain_value(MONGO_DOC, 'missing_mapping', 'eventTypeMap')
        self.assertEqual('errorCode101', function_result)

    def test_missing_attribute(self):
        function_result = get_domain_value(MONGO_DOC, 'missing_attribute')
        self.assertEqual('', function_result)

    def test_changed_lock_pro(self):
        function_result = get_domain_value(MONGO_DOC, 'domain_rdu_lock', lock_level=['FEED', 'RDU'])
        self.assertEqual('REDEMPTION/CASH SETTLEMENT|OCC', function_result)


class GetValueFromArray(unittest.TestCase):
    
    def test_rdu_lock_level(self):
        function_result = get_value_from_array(MONGO_DOC, 'array_rdu_lock')
        self.assertListEqual(['rdu_value1', 'rdu_value2', 'rdu_value3'], function_result)

    def test_feed_lock_level(self):
        function_result = get_value_from_array(MONGO_DOC, 'array_feed_lock')
        self.assertListEqual(['rdu_value1', 'rdu_value2', 'rdu_value3'], function_result)

    def test_missing_attribute(self):
        function_result = get_value_from_array(MONGO_DOC, 'array_missing')
        self.assertListEqual([], function_result)

    def test_nested_feed_lock(self):
        function_result = get_value_from_array(MONGO_DOC, 'nested.array_feed_lock')
        self.assertListEqual(['feed_value2', 'feed_value1'], function_result)

    def test_missing_nested_leave(self):
        function_result = get_value_from_array(MONGO_DOC, 'nested.foobar')
        self.assertListEqual([], function_result)

    def test_missing_nested_parent(self):
        function_result = get_value_from_array(MONGO_DOC, 'foobar.array')
        self.assertListEqual([], function_result)


class GetFromCode(unittest.TestCase):

    def test_get_from_code_val_val2(self):

        dv_domain_map = {
            'vendorMappings': {
                'domainName': {'RDU': {'value': 'xrfAdditionalTypesMap'}},
                'domainSource': {'RDU': {'value': 'figi'}},
                'domainValue': {'RDU': {'value': {'val': '.*', 'val2': 'VC'}}},
                'status': {'RDU': {'value': 'A'}}
                }
        }

        function_result = get_from_code(dv_domain_map)
        self.assertEqual('.*|VC', function_result)

    def test_get_from_code_val(self):
        dv_domain_map = {
            'vendorMappings': {
                'domainName': {'RDU': {'value': 'CouponTypeMap'}},
                'domainSource': {'RDU': {'value': 'idcApex'}},
                'domainValue': {'RDU': {'value': {'val': '30'}}},
                'status': {'RDU': {'value': 'A'}}
                }
        }

        function_result = get_from_code(dv_domain_map)
        self.assertEqual('30', function_result)


if __name__ == '__main__':
    unittest.main()
