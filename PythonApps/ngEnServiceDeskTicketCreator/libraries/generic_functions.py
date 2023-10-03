# Third party libs
import re

LOCK_LEVELS = ['RDU', 'FEED']


class BreakLoop(Exception):
    pass


def get_string_value(mongo_doc, field_name, trim=255, lock_level=LOCK_LEVELS):

    for each_lock_level in lock_level:
        try:
            return mongo_doc[field_name][each_lock_level]['value'][:trim]
        except TypeError:
            return mongo_doc[field_name][each_lock_level]['value']
        except KeyError:
            pass

    return 'default'


def get_date_value(mongo_doc, field_name, lock_level=LOCK_LEVELS):
    """
    Function to retrieve a date value for an attribute. By defining the lock level hierarchy both value and errorCode of the preceding lock level will
    be returned over the other lock level
    :param mongo_doc: dict holding the entire ens event
    :param field_name: name of the field to retrieve the value from
    :param lock_level: list of the priority of the lock levels
    :return: value or errorCode
    """

    for each_lock_level in lock_level:
        for each_try in ['value', 'errorCode']:
            try:
                result = mongo_doc[field_name][each_lock_level][each_try]
                return result if isinstance(result, int) else result.strftime('%Y-%m-%d')
            except KeyError:
                pass

    return 103


def get_domain_value(mongo_doc, field_name, normalized_cache='', lock_level=LOCK_LEVELS):
    """
    Function to retrieve the domain value for an attribute. When the optional parameter normalized_cache is specified then the name corresponding
    to the normalized key is returned. If no name is found errorCode101 string is returned.
    In case the level is FEED and val and val2 are provided a pipe concatenated string value is returned.
    :param lock_level: list of the priority of the lock levels
    :param mongo_doc: dict holding the entire ens event
    :param field_name: name of the field to retrieve the value from
    :param normalized_cache: (optional) which cache of the normalized domain to retrieve the name from
    :return: either the feed value or the normalized name
    :rtype: str
    """

    # Map with the path for the different lock level. Note that this map is in hierarchical order!
    lock_level_map = {
        'RDU': {
            'rdu': ['RDU', 'value', 'normalizedValue']
        },
        'FEED': {
            'val': ['FEED', 'value', 'val'],
            'val2': ['FEED', 'value', 'val2']
        }
    }

    try:
        init_field_value = mongo_doc[field_name]
    except KeyError:
        return ''

    all_values = []

    # BreakLoop try-except is to exit nested loop
    try:
        for each_lock_prio in lock_level:
            for each_lock_level in lock_level_map[each_lock_prio]:
                field_value = init_field_value
                try:
                    for each_lock_level_path in lock_level_map[each_lock_prio][each_lock_level]:
                        field_value = field_value[each_lock_level_path]
                    if not isinstance(field_value, dict):
                        all_values.append(field_value)
                        if each_lock_level in ['rdu', 'val2']:
                            raise BreakLoop
                except (KeyError, TypeError):
                    pass
    except BreakLoop:
        pass

    from_code = '|'.join(all_values)

    if normalized_cache != '':
        try:
            return normalized_cache[from_code]['normalized_name']
        except (KeyError, TypeError):
            return 'errorCode101'
    else:
        return from_code


def get_value_from_array(mongo_doc, field_name):

    list_of_values = []
    try:
        if '.' in field_name:
            list_of_value_paths = mongo_doc[field_name.split('.')[0]]
            for each_path in list_of_value_paths:
                value = get_string_value(each_path, field_name.split('.')[1])
                if value != 'default':
                    list_of_values.append(re.sub(r'(\s|\n)+', '_', get_string_value(each_path, field_name.split('.')[1])))
        else:
            list_of_values = [re.sub(r'(\s|\n)+', '_', x) for x in get_string_value(mongo_doc, field_name)] \
                if get_string_value(mongo_doc, field_name) != 'default' else []
    except KeyError:
        pass

    return list_of_values


def get_from_code(mongo_doc):

    try:
        return f"{mongo_doc['vendorMappings']['domainValue']['RDU']['value']['val']}|" \
               f"{mongo_doc['vendorMappings']['domainValue']['RDU']['value']['val2']}"
    except KeyError:
        return mongo_doc['vendorMappings']['domainValue']['RDU']['value']['val']
