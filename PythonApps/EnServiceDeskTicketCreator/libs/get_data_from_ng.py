import datetime
from libs.en_logger import Logger


def get_ops_users(database):
    en_ops_users_dic = {}

    ops_users = database.get_collection("opsServiceDeskUsers").find()

    for user in ops_users:
        try:
            en_ops_users_dic[user["name"]["RDU"]["value"].lower()] = \
                {
                    "accountId": user["accountId"]["RDU"]["value"]
                }
        except KeyError:
            Logger.logger.warning(f'User: {user["name"]["RDU"]["value"]}, does not have a Service Desk accountId associated in opsServiceDeskUsers '
                                  f'domain.')
            pass

    return en_ops_users_dic


def get_en_data_source_codes(database):
    en_data_sources_dic = {}

    en_data_sources = database.get_collection("enDataSourceCodes").find()

    for entry in en_data_sources:
        try:
            en_data_sources_dic[entry["code"]["RDU"]["value"]] =\
                {
                  "exchangeGroupName": entry["exchangeGroupName"]["RDU"]["value"].strip(),
                  "exchangeOwner": entry["exchangeOwner"]["RDU"]["value"].strip(),
                  "allOpsAnalysts": entry["allOpsAnalysts"]["RDU"]["value"].strip(),
                }
        except KeyError:
            Logger.logger.warning(f'exchangeSourceName: {entry["code"]["RDU"]["value"]}, does not have one or multiple required fields populated '
                                  f'in enDataSourceCodes domain. Required fields are: exchangeGroupName, exchangeOwner and allOpsAnalysts')
            pass

    return en_data_sources_dic


def get_instrument_type_codes(database):
    en_ins_type_dic = {}

    en_data_sources = database.get_collection("assetClassifications").find()

    for entry in en_data_sources:
        try:
            en_ins_type_dic[entry["code"]["RDU"]["value"]] = {"name": entry["name"]["RDU"]["value"]}
        except KeyError:
            Logger.logger.warning(f'instrumentTypeCode: {entry["code"]["RDU"]["value"]}, does not have name field populated in '
                                  f'assetClassifications domain.')
            pass

    return en_ins_type_dic


def get_storm_keys(database):
    en_storm_keys_dic = {}

    en_storm_keys = database.get_collection("workItem").find({"$and":[{"workItemStatus.RDU.value.normalizedValue": "A"},
                                                                      {"workItemType.RDU.value.normalizedValue": "EN Workflow Item"}]})

    for key in en_storm_keys:
        try:
            en_storm_keys_dic[key["stormKey"]["RDU"]["value"]] =\
                {
                    "hash": key["hash"]["RDU"]["value"],
                    "ticketId": key["workItemId"]["RDU"]["value"]
                }
        except KeyError:
            Logger.logger.warning(f'Document from issueStorming collection with stormKey: {key["stormKey"]["RDU"]["value"]}, does not have '
                                  f'either hash oor ticketId field populated.')
            pass

    return en_storm_keys_dic


def get_tickets_status(database):
    en_storm_keys_dic = {}

    en_storm_keys = database.get_collection("workItem").find({"$and": [{"workItemStatus.RDU.value.normalizedValue": "A"},
                                                                      {"workItemType.RDU.value.normalizedValue": "EN Workflow Item"}]})

    for key in en_storm_keys:
        try:
            en_storm_keys_dic[key["workItemId"]["RDU"]["value"]] =\
                {
                    "status": key["workItemStatus"]["RDU"]["value"]["normalizedValue"],
                    "storm_key": key["stormKey"]["RDU"]["value"]
                }
        except KeyError:
            Logger.logger.warning(f'Document from issueStorming collection with stormKey: {key["stormKey"]["RDU"]["value"]}, does not have '
                                  f'workItemStatus populated.')
            pass

    return en_storm_keys_dic


def get_normalized_map(database, domain):
    map_dic = {}

    en_map = database.get_collection("dvDomainMap").aggregate([{"$match": {"rduDomain.RDU.value": domain}}, {"$unwind": "$vendorMappings"},
                                                               {"$match": {"$and": [{"vendorMappings.domainSource.RDU.value": "rduEns"},
                                                                                    {"vendorMappings.status.RDU.value": "A"}]}}])
    for entry in en_map:
        try:
            map_dic[entry["vendorMappings"]["domainValue"]["RDU"]["value"]["val2"] + "|" +
                    entry["vendorMappings"]["domainValue"]["RDU"]["value"]["val"]] = entry["normalizedValue"]["RDU"]["value"]
        except KeyError:
            try:
                map_dic[entry["vendorMappings"]["domainValue"]["RDU"]["value"]["val2"] + "|" +
                        entry["vendorMappings"]["domainValue"]["RDU"]["value"]["val"]] = entry["normalizedValue"]["RDU"]["errorCode"]
            except KeyError:
                Logger.logger.info(f'dvDomainMap entry for RDU domain: {domain}, does not have all the required elements or maps to erroCode 102. '
                                      f'Ignoring dvDomainMap entry: {str(entry)}')
                pass

    return map_dic


def get_en_data(database, query_date_time, mode, hours_offset, days_offset):

    collection = database['enData']

    ignored_fields = {
        "_id": 0,
        "insDate": 0,
        "insUser": 0,
        "updDate": 0,
        "updUser": 0,
        "eventStatus": 0,
        "audit": 0,
        "_class": 0,
        "version": 0
    }

    date = datetime.datetime.strptime(query_date_time, '%Y%m%d %H:%M:%S')

    if mode == "daily":

        ins_offset_date = date - datetime.timedelta(hours=hours_offset)
        insert_offset_date = date - datetime.timedelta(days=days_offset)
        upd_offset_date = date - datetime.timedelta(hours=hours_offset)
        publish_offset_date = date - datetime.timedelta(days=30)

                                     # Feed notifications (here we check by eventPublishDate to prevent to create tickets from old data sent by
                                     #  finStinct by mistake
        query = [{"$match": {"$or": [{"$and": [{"eventPublishDate.FEED.value": {"$gte": publish_offset_date}},
                                               {"insDate.RDU.value": {"$gte": ins_offset_date}},
                                               {"eventStatus.ENRICHED.value.normalizedValue": "A"}]},
                                     # Manually imported notifications
                                     {"$and": [{"eventInsertDate.RDU.value": {"$gte": insert_offset_date}},
                                               {"insDate.RDU.value": {"$gte": ins_offset_date}},
                                               {"eventStatus.RDU.value.normalizedValue": "A"}]},
                                     # All notifications which have been updated
                                     {"$and": [{"updDate.RDU.value": {"$gte": upd_offset_date}}]}
                                     ]}},
                 {"$project": ignored_fields}]
    else:
        publish_offset_date = date - datetime.timedelta(days=30)
        effective_offset_date = date - datetime.timedelta(days=3)
                                    # Feed notifications by eventEffectiveDate
        query = [{"$match": {"$or": [{"$and": [{"eventEffectiveDate.FEED.value": {"$gte": effective_offset_date}},
                                               {"eventStatus.ENRICHED.value.normalizedValue": "A"}]},
                                     # Feed notifications with OPS lock on eventEffectiveDate
                                     {"$and": [{"eventEffectiveDate.RDU.value": {"$gte": effective_offset_date}},
                                               {"eventStatus.ENRICHED.value.normalizedValue": "A"}]},
                                     # Manually imported notifications by eventEffectiveDate
                                     {"$and": [{"eventEffectiveDate.RDU.value": {"$gte": effective_offset_date}},
                                               {"eventStatus.RDU.value.normalizedValue": "A"}]},
                                     # Feed notifications with eventEffectiveDate CANC
                                     {"$and": [{"eventEffectiveDate.FEED.errorCode": {"$in": [105]}},
                                               {"eventStatus.ENRICHED.value.normalizedValue": "A"}]},
                                     # Manually imported notifications with eventEffectiveDate CANC
                                     {"$and": [{"eventEffectiveDate.RDU.errorCode": {"$in": [105]}},
                                               {"eventStatus.RDU.value.normalizedValue": "A"}]},
                                     # Feed notifications with eventEffectiveDate N.A. or TBA by eventPublishDate
                                     {"$and": [{"eventEffectiveDate.FEED.errorCode": {"$in": [103, 104]}},
                                               {"eventPublishDate.FEED.value": {"$gte": publish_offset_date}},
                                               {"eventStatus.ENRICHED.value.normalizedValue": "A"}]},
                                     # Manually imported notifications with eventEffectiveDate N.A. or TBA by eventPublishDate
                                     {"$and": [{"eventEffectiveDate.RDU.errorCode": {"$in": [103, 104]}},
                                               {"eventPublishDate.RDU.value": {"$gte": publish_offset_date}},
                                               {"eventStatus.RDU.value.normalizedValue": "A"}]},
                                     # Feed notifications without eventEffectiveDate by eventPublishDate
                                     {"$and": [{"eventEffectiveDate": {"$exists": False}},
                                               {"eventPublishDate.FEED.value": {"$gte": publish_offset_date}},
                                               {"eventStatus.ENRICHED.value.normalizedValue": "A"}]}
                                     ]}},
                 {"$project": ignored_fields}]

    query_result = collection.aggregate(query, allowDiskUse=True)

    return query_result
