# Third party libs
import copy
import unittest
# Custom libs
from objects.security import Securities, Security


class TestSecuritiesObject(unittest.TestCase):

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

    def test_get_selected_sec_update_ric(self):

        securities_copy = copy.deepcopy(self.securities)

        sec_obj = Security('pse')
        sec_obj.sec_id = '1'
        sec_obj.lookup_attr_val = 'AKBNK'
        sec_obj.doc_id = '5e201dec888c557a94e4fe4e'
        sec_obj.sedol = ''
        sec_obj.long_name = 'bar'
        sec_obj.ric = 'AKBNK'
        sec_obj.securities_key = 'pse|5e201dec888c557a94e4fe4e'

        securities_copy.get_selected_sec(sec_obj)

        self.assertEqual(2, len(securities_copy.securities))
        self.assertEqual('bar', securities_copy.securities['pse|5e201dec888c557a94e4fe4e']['long_name'])
        self.assertEqual('1', securities_copy.securities['pse|5e201dec888c557a94e4fe4e']['sec_id'])
        self.assertEqual('AKBNK', securities_copy.securities['pse|5e201dec888c557a94e4fe4e']['ric'])

    def test_get_selected_sec_update_sedol(self):

        securities_copy = copy.deepcopy(self.securities)

        sec_obj = Security('pse')
        sec_obj.sec_id = '1'
        sec_obj.lookup_attr_val = 'AKBNK'
        sec_obj.doc_id = '5e201dec888c557a94e4fe4e'
        sec_obj.sedol = 'NL123456'
        sec_obj.ric = 'AKBNK.AB'
        sec_obj.securities_key = 'pse|5e201dec888c557a94e4fe4e'

        securities_copy.get_selected_sec(sec_obj)

        self.assertEqual(2, len(securities_copy.securities))
        self.assertEqual('NL123456', securities_copy.securities['pse|5e201dec888c557a94e4fe4e']['sedol'])
        self.assertEqual('1', securities_copy.securities['pse|5e201dec888c557a94e4fe4e']['sec_id'])
        self.assertEqual('AKBNK.AB', securities_copy.securities['pse|5e201dec888c557a94e4fe4e']['ric'])

    def test_get_selected_sec_keep_on_sedol(self):

        securities_copy = copy.deepcopy(self.securities)

        sec_obj = Security('pse')
        sec_obj.sec_id = '1'
        sec_obj.lookup_attr_val = 'AKBNK'
        sec_obj.doc_id = '63915b69b93f1a5b2759c6c5'
        sec_obj.sedol = ''
        sec_obj.ric = 'AKBNK.AB'
        sec_obj.securities_key = 'pse|63915b69b93f1a5b2759c6c5'

        securities_copy.get_selected_sec(sec_obj)

        self.assertEqual(2, len(securities_copy.securities))
        self.assertEqual('GB123456', securities_copy.securities['pse|63915b69b93f1a5b2759c6c5']['sedol'])
        self.assertEqual('AKBNK.IS', securities_copy.securities['pse|63915b69b93f1a5b2759c6c5']['ric'])

    def test_get_selected_sec_keep_on_ric(self):

        securities_copy = copy.deepcopy(self.securities)

        sec_obj = Security('pse')
        sec_obj.sec_id = '1'
        sec_obj.lookup_attr_val = 'AKBNK'
        sec_obj.doc_id = '63915b69b93f1a5b2759c6c5'
        sec_obj.sedol = 'GB123456'
        sec_obj.ric = 'AKBNKbrok.AB'
        sec_obj.securities_key = 'pse|63915b69b93f1a5b2759c6c5'

        securities_copy.get_selected_sec(sec_obj)

        self.assertEqual(2, len(securities_copy.securities))
        self.assertEqual('GB123456', securities_copy.securities['pse|63915b69b93f1a5b2759c6c5']['sedol'])
        self.assertEqual('AKBNK.IS', securities_copy.securities['pse|63915b69b93f1a5b2759c6c5']['ric'])

class TestSecurityObject(unittest.TestCase):

    def test_security_obj_init(self):
        security = Security('ads')
        self.assertIsInstance(security, Security)
        self.assertEqual('XADS', security.mic_code)
        self.assertEqual('exchangeTicker', security.lookup_attr)

    def test_set_values_basic(self):
        sd_doc = {
            'isin' : 'BMG2345T1099',
            'ric' : '0182.HZ',
            'exchangeTicker' : '182',
            'segmentMic' : 'SZSC',
            'tradeCurrencyCode' : 'HKD',
            'nameLong' : 'Concord New Energy Group Ord Shs',
            'lookupAttribute': '182',
            '_securityId' : '1',
            'docId' : '5e20172a430d9c29dc51235d'
        }
        
        sec_obj = Security('ads')
        sec_obj.set_values(sd_doc)
        self.assertEqual('1', sec_obj.sec_id)
        self.assertEqual('182', sec_obj.exchange_id)
        self.assertEqual('BMG2345T1099', sec_obj.isin)
        self.assertEqual('', sec_obj.sedol)
        self.assertEqual('0182.HZ', sec_obj.ric)
        self.assertEqual('SZSC', sec_obj.mic_code)
        self.assertEqual('HKD', sec_obj.trade_currency)
        self.assertEqual('182', sec_obj.lookup_attr_val)
        self.assertEqual('Concord New Energy Group Ord Shs', sec_obj.long_name)
        self.assertEqual('5e20172a430d9c29dc51235d', sec_obj.doc_id)
        self.assertEqual('ads|182', sec_obj.securities_key)

    def test_set_values_skeletal(self):
        sec_obj = Security('ads')
        sec_obj.set_skeletal('182')
        self.assertEqual('', sec_obj.sec_id)
        self.assertEqual('182', sec_obj.exchange_id)
        self.assertEqual('', sec_obj.isin)
        self.assertEqual('', sec_obj.sedol)
        self.assertEqual('', sec_obj.ric)
        self.assertEqual('XADS', sec_obj.mic_code)
        self.assertEqual('', sec_obj.trade_currency)
        self.assertEqual('182', sec_obj.lookup_attr_val)
        self.assertEqual('', sec_obj.long_name)
        self.assertEqual('', sec_obj.doc_id)
        self.assertEqual('ads|182', sec_obj.securities_key)
        self.assertEqual('No record found', sec_obj.skeletal_reason)

    def test_set_json(self):
        sd_doc = {
            'isin': 'BMG2345T1099',
            'ric': '0182.HZ',
            'exchangeTicker': '182',
            'segmentMic': 'SZSC',
            'tradeCurrencyCode': 'HKD',
            'nameLong': 'Concord New Energy Group Ord Shs',
            'lookupAttribute': '182',
            '_securityId': '1',
            'docId': '5e20172a430d9c29dc51235d'
        }

        expected_json = {
            'exchange_id': '182',
            'instrument_unique_id': 'ads.182',
            'update_date': '2023-05-16T11:50:54.829406',
            'source': 'ads',
            'mic_code': 'SZSC',
            'long_name': 'Concord New Energy Group Ord Shs',
            'isin': 'BMG2345T1099',
            'sedol': '',
            'ric': '0182.HZ',
            'trade_currency': 'HKD',
            'lookup_attr': 'exchangeTicker',
            'lookup_attr_val': '182',
            'doc_id': '5e20172a430d9c29dc51235d',
            'sec_id': '1',
            'securities_key': 'ads|182',
            'security_status': 'active',
            'security_unique_id': 'SZSC.182',
            'skeletal_reason': None,
            'exchange_fields': {}
        }

        sec_obj = Security('ads')
        sec_obj.set_values(sd_doc)
        sec_obj.update_date = '2023-05-16T11:50:54.829406'
        sec_json = sec_obj.set_json()
        self.assertEqual(expected_json, sec_json)
