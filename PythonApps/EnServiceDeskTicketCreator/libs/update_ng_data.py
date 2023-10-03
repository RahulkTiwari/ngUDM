def update_storm_keys(database, new_keys_dic):

    col = database["workItem"]

    for key, value in new_keys_dic.items():
        try:
            js_data_unique = {
                "stormKey.RDU.value": key,
                "workItemType.RDU.value.normalizedValue": "EN Workflow Item",
                "workItemStatus.RDU.value.normalizedValue": "A"
            }
            js_data = {
                "workItemDataLevel.RDU.value.normalizedValue": "EN",
                "workItemType.RDU.value.normalizedValue": "EN Workflow Item",
                "stormKey.RDU.value": key,
                "hash.RDU.value": str(value['hash']),
                "workItemId.RDU.value": str(value['jira']),
                "workItemStatus.RDU.value.normalizedValue": "A"
            }

            col.update_one(js_data_unique, {"$set": js_data}, upsert=True)
        except KeyError:
            js_data_unique = {
                "stormKey.RDU.value": key
            }
            js_data = {
                "hash.RDU.value": value['hash'],
            }

            col.update_one(js_data_unique, {"$set": js_data})


def update_en_data_storm_key(database, unique_id, storm_key, manual_exchange_notification):

    col = database["enData"]

    if manual_exchange_notification:
        js_data_unique = {
            "eventSourceUniqueId.FEED.value": unique_id,
            "eventStatus.RDU.value.normalizedValue": "A"
        }
    else:
        js_data_unique = {
            "eventSourceUniqueId.FEED.value": unique_id,
            "eventStatus.ENRICHED.value.normalizedValue": "A"
        }
    js_data = {
        "workItemType": {"RDU": {"value": {"normalizedValue": "EN Workflow Item"}}},
        "stormKey": {"RDU": {"value": storm_key}},
        "workItemStatus": {"RDU": {"value": {"normalizedValue": "A"}}}
    }

    col.update_one(js_data_unique, {"$addToSet": {"enWorkItemReference":js_data}})


def update_en_data_ticket_status(database, ticket_id, status):

    col = database["workItem"]

    js_data_unique = {
        "workItemId.RDU.value": ticket_id
    }
    js_data = {
        "workItemStatus.RDU.value.normalizedValue": status
    }

    col.update_one(js_data_unique, {"$set": js_data})


def update_en_data_storm_key_status(database, storm_key, status):

    col = database["enData"]

    js_data_unique = {
        "enWorkItemReference.stormKey.RDU.value": storm_key
    }
    js_data = {
        "enWorkItemReference.$.workItemStatus.RDU.value.normalizedValue": status
    }

    col.update_many(js_data_unique, {"$set": js_data})