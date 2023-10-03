# Third party libraries
import datetime
# Custom libraries


def retrieve_notices_from_db(run_arguments):

    # Custom libraries
    from . caches.database_connection import database_connection

    collection = database_connection['enData']
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

    date = datetime.datetime.strptime(run_arguments.date, '%Y%m%d %H:%M:%S')
    ins_offset_date = date - datetime.timedelta(hours=run_arguments.hours_offset)
    insert_offset_date = date - datetime.timedelta(days=run_arguments.days_offset)
    update_offset_date = date - datetime.timedelta(hours=run_arguments.hours_offset)
    publish_offset_date = date - datetime.timedelta(days=30)

    if run_arguments.mode == 'daily':
        query = [{"$match": {"$or": [{"$and": [{"eventPublishDate.FEED.value": {"$gte": publish_offset_date}},
                                               {"insDate.RDU.value": {"$gte": ins_offset_date}},
                                               {"eventStatus.ENRICHED.value.normalizedValue": "A"},
                                               ]},
                                     {"$and": [{"eventInsertDate.RDU.value": {"$gte": insert_offset_date}},
                                               {"insDate.RDU.value": {"$gte": ins_offset_date}},
                                               {"eventStatus.RDU.value.normalizedValue": "A"}
                                               ]},
                                     {"$and": [{"updDate.RDU.value": {"$gte": update_offset_date}}]}
                                     ]
                             }
                  },
                 {"$project": ignored_fields}]
    else:
        effective_offset_date = date - datetime.timedelta(days=3)
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

    reply = collection.aggregate(query, allowDiskUse=True)

    return reply
