# Customt libraries
from libraries.caches.database_connection import database_connection
from libraries.generic_functions import get_from_code, get_string_value
from libraries.service_desk_logger import Logger


def create_event_type_cache():

    cache_dict = {
        '': {
            'normalized_value': 'errorCode102',
            'normalized_name': 'errorCode102',
        }
    }
    projection = {
        'name.RDU.value': 1,
        '_id': 0
    }

    domain_collection = database_connection.get_collection('eventTypes').find(
        {'$and': [
            {'status.RDU.value': 'A'}
        ]},
        projection
    )

    # First add the eventTypes names static data to the cache to support locks
    for each_domain_item in domain_collection:
        cache_dict[get_string_value(each_domain_item, 'name')] = {
            'normalized_value': get_string_value(each_domain_item, 'name'),
            'normalized_name': get_string_value(each_domain_item, 'name')
        }

    # Add the mappings to the cache
    domain_map_collection = database_connection.get_collection('dvDomainMap').aggregate([
        {'$match': {'rduDomain.RDU.value': 'eventTypes'}},
        {'$unwind': '$vendorMappings'},
        {'$match': {"$and": [{'vendorMappings.domainSource.RDU.value': 'rduEns'}, {'vendorMappings.status.RDU.value': 'A'}]}}
    ])

    for each_domain_map in domain_map_collection:
        try:
            cache_dict[get_from_code(each_domain_map)] = {
                'normalized_value': get_string_value(each_domain_map, 'normalizedValue'),
                'normalized_name': get_string_value(each_domain_map, 'normalizedValue')
            }
        except KeyError:
            # Normally we should never hit this except. Refer to UDM-62706 as an explanation why it is currently hit.
            cache_dict[get_from_code(each_domain_map)] = {
                'normalized_value': get_string_value(each_domain_map, 'normalizedValue'),
                'normalized_name': 'Invalid normalized value'
            }
            Logger.logger.warning(f"The following normalized key is invalid: {get_string_value(each_domain_map, 'normalizedValue')}, as it is not "
                                  f"found as active domain key within domain: eventTypes.")

    return cache_dict


def create_asset_class_cache():
    cache_dict = {
        '': {
            'normalized_value': 'errorCode102',
            'normalized_name': 'errorCode102',
        }
    }
    projection = {
        'code.RDU.value': 1,
        'name.RDU.value': 1,
        '_id': 0
    }

    parent_asset_class_name_dict = {
        '0': 'Unknown',
        '51': 'Equity',
        '52': 'Fund',
        '53': 'Structured',
        '54': 'Fixed Income',
        '55': 'Commodity',
        '56': 'Option',
        '57': 'Future',
        '58': 'Currency',
        '59': 'Index',
        '60': 'Economic Indicator',
        '61': 'Swap',
        '62': 'Strategy',
        '63': 'Combined instrument',
        '64': 'Entitlement'
    }

    domain_collection = database_connection.get_collection('assetClassifications').find(
        {'$and': [
            {'status.RDU.value': 'A'}
        ]},
        projection
    )

    # First add the eventTypes names static data to the cache to support locks
    for each_domain_item in domain_collection:
        cache_dict[get_string_value(each_domain_item, 'code')] = {
            'normalized_value': get_string_value(each_domain_item, 'code')[:2],
            'normalized_name': parent_asset_class_name_dict[get_string_value(each_domain_item, 'code')[:2]]
        }

    # Add the mappings to the cache
    domain_map_collection = database_connection.get_collection('dvDomainMap').aggregate([
        {'$match': {'rduDomain.RDU.value': 'assetClassifications'}},
        {'$unwind': '$vendorMappings'},
        {'$match': {"$and": [{'vendorMappings.domainSource.RDU.value': 'rduEns'}, {'vendorMappings.status.RDU.value': 'A'}]}}
    ])

    for each_domain_map in domain_map_collection:
        try:
            cache_dict[get_from_code(each_domain_map)] = {
                'normalized_value': get_string_value(each_domain_map, 'normalizedValue')[:2],
                'normalized_name': parent_asset_class_name_dict[get_string_value(each_domain_map, 'normalizedValue')[:2]]
            }
        except KeyError:
            # Normally we should never hit this except. Refer to UDM-62706 as an explanation why it is currently hit.
            cache_dict[get_from_code(each_domain_map)] = {
                'normalized_value': get_string_value(each_domain_map, 'normalizedValue')[:2],
                'normalized_name': 'Invalid normalized value'
            }
            Logger.logger.warning(f"The following normalized key is invalid: {get_string_value(each_domain_map, 'normalizedValue')}, as it is not "
                                  f"found as active domain key within domain: assetClassifications.")

    return cache_dict


event_type_cache = create_event_type_cache()

asset_classification_cache = create_asset_class_cache()
