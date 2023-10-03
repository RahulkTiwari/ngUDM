# Third party libs
# from pymongo import MongoClient
# Custom libs
from modules.mongo_connection import database_con
from modules.config import config_object
from objects.security import Security
from modules.logger import main_logger


def find_inactives(src_cd_instance):
    # TODO: remove db connection for PROD
    # client = MongoClient(
    #     host='localhost',
    #     port=27017,
    #     username='test',
    #     password='test123',
    #     authSource='testdb',
    #     authMechanism='SCRAM-SHA-1'
    # )
    #
    # database_con = client['testdb']

    # get sdData
    pipeline = [
        {'$match': {
            'dataSource.FEED': {'value': {'val': 'rduEds'}},
            'instrument.edDataSourceCode.FEED': {'value': {'val': src_cd_instance.source_code, 'domain': 'dataSourceCodeMap'}},
            'instrument.instrumentStatus.FEED': {'value': {'val': 'active', 'domain': 'securityStatusMap'}}
        }},
        {'$project': {
            '_id': 0,
            'insUniqueId': '$instrument.instrumentSourceUniqueId.FEED.value',
            'secUniqueId': '$instrument.securities.securitySourceUniqueId.FEED.value'
        }}
    ]

    query_result = database_con.get_collection(config_object['CONNECTION']['collection']).aggregate(pipeline, allowDiskUse=True)

    sd_data_unique_ids = [each_doc['insUniqueId'] for each_doc in query_result]

    # get the scraped id
    scraped_instrument_unique_ids = [value['instrument_unique_id'] for key, value in src_cd_instance.securities.items()]

    # check for removed securities
    deactived_securities = [each_sec for each_sec in sd_data_unique_ids if each_sec not in scraped_instrument_unique_ids]

    # update securities object with the deleted securities, having inactive status
    for each_security in deactived_securities:
        lookup_value = each_security.split('.')[1]
        security = Security(src_cd_instance.source)
        security.set_inactive(lookup_value)
        src_cd_instance.securities[lookup_value] = security.set_json()
        main_logger.info(f'Created inactivation record for {src_cd_instance.source}, {lookup_value}')

    return src_cd_instance
