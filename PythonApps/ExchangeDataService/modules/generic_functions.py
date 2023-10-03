# Third party libs
from pathlib import Path


def recursive_lookup(attr, doc):
    if attr in doc:
        return doc[attr]
    for v in doc.values():
        if isinstance(v, dict):
            attr_val = recursive_lookup(attr, v)
            if attr_val is not None:
                return attr_val
    return None


def get_value(mongo_doc, attribute):
    try:
        return mongo_doc[attribute]
    except KeyError:
        return ''


def get_string_value(mongo_doc, attribute):

    lock_level = ['RDU', 'FEED']

    interim_value = recursive_lookup(attribute, mongo_doc)

    for each_lock_level in lock_level:
        try:
            return interim_value[each_lock_level]['value']
        except (KeyError, TypeError):
            pass

    return ''


def get_domain_value(mongo_doc, attribute):

    lock_level = ['RDU', 'FEED']

    interim_value = recursive_lookup(attribute, mongo_doc)

    for each_lock_level in lock_level:
        try:
            return interim_value[each_lock_level]['value']['val']
        except (KeyError, TypeError):
            pass

    return ''


def create_download_folder(folder_string):
    try:
        Path(folder_string).mkdir(parents=True, exist_ok=False)
    except FileExistsError:
        pass

    return folder_string


def convert_string_to_list(string, sep=','):
    string = string.replace(sep*2, sep)
    string_to_list = [item.strip() for item in string.split(sep)]

    return string_to_list
