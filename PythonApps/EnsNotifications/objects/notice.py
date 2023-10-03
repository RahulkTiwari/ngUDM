# Third party libs
from datetime import datetime
# Custom lins
from modules.url_config import url_configuration


class Notice:

    def __init__(self, exchange):
        self.exchangeReferenceId = None
        self.noticeSourceUniqueId = None
        self.stormKey = None
        self.eventInitialUrl = None
        self.eventSubject = None
        self.eventPublishDate = datetime(1970, 1, 1)
        self.insDate = datetime.now()
        self.status = 'A'
        self.exchangeSourceName = url_configuration[exchange]['exchangeSourceName']
        self.valid_notice = False
        self.raise_warning = True

    def notice_to_json(self):
        notice_dict = {
            'stormKey': {
                    'RDU': {'value': self.stormKey}
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
            'insDate': {
                'RDU': {'value': self.insDate}
            },
            'insUser': {
                'RDU': {'value': 'Notice Tracker'}
            }
        }

        return notice_dict
