# Custom import
from libraries.objects.WorkItem import WorkItem
from libraries.retrieve_notifications import retrieve_notices_from_db
from libraries.service_desk_logger import Logger
from libraries.caches.work_item_cache import all_active_work_items
from libraries.caches.run_args_cache import run_arguments

# Globals
work_item_cache = all_active_work_items


def create_work_item(notice):

    work_item = WorkItem(notice)
    Logger.logger.info(f'Start processing reference id {notice["eventSourceUniqueId"]}.')
    work_item_exists = work_item.storm_key in work_item_cache
    if work_item_exists:
        work_item.work_item_id = work_item_cache.work_item_objects[work_item_cache.index - 1].work_item_id
        work_item.previous_storm_hash = work_item_cache.work_item_objects[work_item_cache.index - 1].hash
        work_item.hash_update = True if work_item.storm_hash != work_item.previous_storm_hash else False
        work_item.update()
        # Update the work item cache and the collection with the new stormed hash
        if work_item.hash_update:
            del work_item_cache.work_item_objects[work_item_cache.index - 1]
            work_item_cache.append_work_item_cache(work_item)
    else:
        work_item.create()
        Logger.logger.debug(f'Processed work item {work_item.storm_key}.')
        if work_item.work_item_id is not None:
            work_item_cache.append_work_item_cache(work_item)


def create_work_items_main():

    # Retrieving notifications within the scoped date range
    all_en_data_notices = retrieve_notices_from_db(run_arguments)

    # Create or update a work item for each notification
    for each_notice in all_en_data_notices:
        create_work_item(each_notice)


if __name__ == '__main__':

    # Start main application
    try:
        create_work_items_main()
    except Exception as exception:
        Logger.logger.error(exception, exc_info=True)
        exit(1)

    Logger.logger.info('Finished processing notifications.')
