# Custom libs
from modules.mongo_connection import database_con
from modules.config import config_object
from modules.source_config import source_config_obj


def get_sd_data(securities_list, source_code):
    """
    Function to find the securities in the database given the exchange identifier (e.g. ticker or isin)
    :param securities_list: list containg the exchanges' ids
    :param source_code: the MIC code of the exchange which is being processed
    :return: list of cursors
    """

    # In case there are no securities eligible for short sell, then return empty list
    if len(securities_list) < 1:
        return []

    attribute_query_dict = {
        'exchangeTicker': 'securities.exchangeTicker',
        'isin': 'isin'
    }

    query_attr = source_config_obj[source_code]['lookup_attribute']
    id_query = f'instrument.{attribute_query_dict[query_attr]}.FEED'
    query_ids = [{'value': each_id} for each_id in securities_list]
    mic_list = [mic.strip() for mic in source_config_obj[source_code]['mic'].split(',')]

    pipeline = [
        {'$match':
            {
                id_query: {'$in': query_ids},
                'instrument.securities.segmentMic.FEED.value.val': {'$in': mic_list},
                'dataSource.FEED.value.val': 'trdse'
            }
        },
        {'$unwind': '$instrument.securities'},
        {'$match':
            {
                id_query: {'$in': query_ids},
                'instrument.securities.segmentMic.FEED.value.val': {'$in': mic_list},
                '$and': [
                    {'instrument.securities.securityStatus.FEED.value.val': '1'},
                    {'instrument.securities.securityStatus.RDU.value.normalizedValue': {'$nin': ['I', 'D']}},
                    {'instrument.securities.securityStatus.ENRICHED.value.normalizedValue': {'$nin': ['I', 'D']}}
                ]
            }
        },
        {'$project': {
            'isin': '$instrument.isin.FEED.value',
            'ric': '$instrument.securities.ric.FEED.value',
            'exchangeTicker': '$instrument.securities.exchangeTicker.FEED.value',
            'sedol': '$instrument.securities.sedol.FEED.value',
            'segmentMic': '$instrument.securities.segmentMic.FEED.value.val',
            'tradeCurrencyCode': '$instrument.securities.tradeCurrencyCode.FEED.value.val',
            'nameLong': '$instrument.nameLong.FEED.value',
            '_securityId': '$instrument.securities._securityId.FEED.value',
            'lookupAttribute': f'${id_query}.value',
            'docId': '$_id',
            '_id': 0
        }}
    ]

    query_result = database_con.get_collection(config_object['CONNECTION']['collection']).aggregate(pipeline, allowDiskUse=True)

    return list(query_result)
