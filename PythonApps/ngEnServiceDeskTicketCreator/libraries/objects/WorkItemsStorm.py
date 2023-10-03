# Third party libraries
from datetime import datetime
# Custom libraries
from libraries.generic_functions import get_string_value
from libraries.caches.database_connection import database_connection


def get_work_items_from_database():
    work_item_collection = database_connection['workItem']

    work_item_list = []
    reply = work_item_collection.find({
        'workItemStatus.RDU.value.normalizedValue': {'$ne': 'I'},
        'workItemType.RDU.value.normalizedValue': 'EN Workflow Item'
    })

    for each_work_item_doc in reply:
        work_item = WorkItemsStormObj()
        work_item_instance = work_item.set_storm_object(each_work_item_doc)
        work_item_list.append(work_item_instance)

    return work_item_list


def update_work_items_collection(work_item):
    work_items_collection = database_connection['workItem']

    work_items_colletion_key = {
        'stormKey.RDU.value': work_item.storm_key,
        'workItemType.RDU.value.normalizedValue': 'EN Workflow Item',
        'workItemStatus.RDU.value.normalizedValue': 'A'
    }

    work_items_collection_data = {
        'stormKey': {'RDU': {'value': work_item.storm_key}},
        'hash': {'RDU': {'value': work_item.storm_hash}},
        'workItemStatus': {'RDU': {'value': {'normalizedValue': 'A'}}},
        'workItemType': {'RDU': {'value': {'normalizedValue': 'EN Workflow Item'}}},
        'workItemDataLevel': {'RDU': {'value': {'normalizedValue': 'EN'}}},
        'workItemId': {'RDU': {'value': work_item.work_item_id}}
    }

    if work_item.hash_update:
        work_items_collection_data['updDate'] = {'RDU': {'value': datetime.now()}}
        work_items_collection_data['updUser'] = {'RDU': {'value': 'EN Workflow'}}
    else:
        work_items_collection_data['insDate'] = {'RDU': {'value': datetime.now()}}
        work_items_collection_data['insUser'] = {'RDU': {'value': 'EN Workflow'}}

    work_items_collection.update_one(work_items_colletion_key, {'$set': work_items_collection_data}, upsert=True)


class WorkItemsStormObj:

    def __init__(self):
        self.storm_key = None
        self.hash = None
        self.work_item_status = None
        self.work_item_type = None
        self.work_item_id = None
        self.work_item_data_level = None

    def set_storm_object(self, mongo_doc):
        self.storm_key = get_string_value(mongo_doc, 'stormKey', trim='')
        self.hash = get_string_value(mongo_doc, 'hash')
        self.work_item_status = get_string_value(mongo_doc, 'workItemStatus')['normalizedValue']
        self.work_item_type = get_string_value(mongo_doc, 'workItemType')['normalizedValue']
        self.work_item_id = get_string_value(mongo_doc, 'workItemId')
        self.work_item_data_level = get_string_value(mongo_doc, 'workItemDataLevel')['normalizedValue']

        return self

    def set_storm_object_from_work_item(self, work_item_obj):
        self.storm_key = work_item_obj.storm_key
        self.hash = work_item_obj.storm_hash
        self.work_item_status = 'OPEN'
        self.work_item_type = 'EN Workflow Item'
        self.work_item_id = work_item_obj.work_item_id
        self.work_item_data_level = 'EN'

        return self


class WorkItemStormCache:

    def __init__(self):
        self.work_item_objects = get_work_items_from_database()

    def __iter__(self):
        self.index = 0
        return self

    def __next__(self):
        if self.index < len(self.work_item_objects):
            self.index += 1
            return self.work_item_objects[self.index - 1].storm_key
        else:
            raise StopIteration

    def append_work_item_cache(self, ticket):
        self.work_item_objects.append(WorkItemsStormObj().set_storm_object_from_work_item(ticket))
        update_work_items_collection(ticket)
