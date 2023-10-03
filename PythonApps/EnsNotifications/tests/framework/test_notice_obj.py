# Third party libs
import unittest
from datetime import datetime
# Custom libs
from objects.notice import Notice


class TestNoticeObject(unittest.TestCase):

    def test_notice_init(self):
        notice = Notice('tfex')
        self.assertIsInstance(notice, Notice)
        self.assertEqual('A', notice.status)
        self.assertEqual(datetime.now().strftime('%d%m%Y'), notice.insDate.strftime('%d%m%Y'))
        self.assertEqual('TFEX_TRADING_NOTICE', notice.exchangeSourceName)
        self.assertFalse(notice.valid_notice)

    def test_notice_to_json(self):
        result = {
            'stormKey': {
                    'RDU': {'value': 'NSE_EXCHANGE_COMMUNICATION_CIRCULARS|123456'}
                },
            'workItemStatus': {
                'RDU': {'value': {'normalizedValue': 'A'}}
            },
            'workItemType': {
                'RDU': {'value': {'normalizedValue': 'EN Notice Tracker Item'}}
            },
            'workItemDataLevel': {
                'RDU': {'value': {'normalizedValue': 'EN'}}
            },
            'insUser': {
                'RDU': {'value': 'Notice Tracker'}}
        }

        notice = Notice('nse')
        notice.valid_notice = True
        notice.noticeSourceUniqueId = '123456'
        notice.stormKey = 'NSE_EXCHANGE_COMMUNICATION_CIRCULARS|123456'
        function_result = notice.notice_to_json()
        # Delete insDate, since it'll never match
        del function_result['insDate']
        self.assertIsInstance(function_result, dict)
        self.assertDictEqual(result, function_result)


if __name__ == '__main__':
    unittest.main()
