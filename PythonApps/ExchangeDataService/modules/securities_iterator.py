# Third party libs
import copy
# Custom libs
from modules.jira import create_jira
from modules.logger import main_logger
from objects.security import Security, Securities
from modules import run_arg_cache as rac
from modules.sd_data import get_sd_data
from modules.source_config import source_config_obj
from modules.compare_db_file import find_inactives


def iterate_securities(src_cd_instance):
    # Initiate object instances

    list_of_secs = src_cd_instance.get_security_list()

    # Finding the corresponding sdData
    if rac.enrich_data:
        cursors = get_sd_data(list_of_secs, src_cd_instance.source_code)

    lookup_attr = source_config_obj[src_cd_instance.source_code]['lookup_attribute']

    for each_security in list_of_secs:
        main_logger.debug(f'Processing exchange security {each_security}')

        sec_obj = Security(src_cd_instance.source_code)

        # Populating the exchange data points
        for each_field in src_cd_instance.exchange_fields:
            sec_obj.exchange_fields[each_field] = src_cd_instance.exchange_data.data[each_security][each_field]

        # Find Refinitiv security for the exchange security in cursor list
        if rac.enrich_data:
            docs = [doc for doc in cursors if doc['lookupAttribute'] == each_security]
        else:
            docs = []

        if docs:
            # If corresponding record exists in our database, get data from there.....
            for each_doc in docs:
                # Create deep copy to prevent the obj instance in the cache is already updated
                current_sec = copy.deepcopy(sec_obj)
                current_sec.set_values(each_doc)
                # In case multiple securities of one instrument are replied, select the proper security
                if current_sec.securities_key not in src_cd_instance.securities:
                    src_cd_instance.securities[current_sec.securities_key] = current_sec.set_json()
                else:
                    main_logger.debug(f'Multiple documents found for {current_sec.lookup_attr_val}')
                    # TODO: Potentially add validation on the multiple securities found (e.g. MIC check)
                    src_cd_instance.get_selected_sec(current_sec)
        # ...else create skeletal record
        else:
            sec_obj.set_skeletal(each_security)
            src_cd_instance.securities[each_security] = sec_obj.set_json()
            if rac.enrich_data:
                main_logger.warning(f'No corresponding record found for {lookup_attr} {each_security}.')
                if rac.create_jira:
                    create_jira(sec_obj)

    src_cd_instance = find_inactives(src_cd_instance)

    return src_cd_instance
