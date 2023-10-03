def date_field_value(ensdict, field_name):
    # Function used for date fields different than eventEffectiveDate value for populating service desk ticket date fields
    # Highest priority is OPS lock value, followed by FEED value, with a final fallback to "9999-12-31" as we can only store dates in Service desk
    # date fields
    try:
        date_value = ensdict[field_name]["RDU"]['value'].strftime('%Y-%m-%d')
    except KeyError:
        try:
            date_value = ensdict[field_name]["FEED"]['value'].strftime('%Y-%m-%d')
        except KeyError:
            date_value = "9999-12-31"
    return date_value


def eff_date_field_value(ensdict, field_name):
    # Function used for eventEffectiveDate value for populating service desk ticket Event Effective Date, Event Effective Date Error Code and Due Date
    # Highest priority is OPS lock value, followed by FEED value, followed by errorCode value, with a final fallback to 103
    try:
        date_value = ensdict[field_name]["RDU"]['value'].strftime('%Y-%m-%d')
    except KeyError:
        try:
            date_value = ensdict[field_name]["FEED"]['value'].strftime('%Y-%m-%d')
        except KeyError:
            try:
                date_value = ensdict[field_name]["FEED"]['errorCode']
            except KeyError:
                date_value = 103
    return date_value


def date_feed_field_value(ensdict, field_name):
    # Function used for eventEffectiveDate value for the stormingKey
    # Considers FEED values as highest priority, as we do not want to create a new ticket if the eventEffectiveDate is updated with an OPS lock
    # We only consider RDU values as lowest priority for manually imported notifications
    try:
        date_value = ensdict[field_name]["FEED"]['errorCode']
    except KeyError:
        try:
            date_value = ensdict[field_name]["FEED"]['value'].strftime('%Y-%m-%d')
        except KeyError:
            try:
                date_value = ensdict[field_name]["RDU"]['errorCode']
            except KeyError:
                try:
                    date_value = ensdict[field_name]["RDU"]['value'].strftime('%Y-%m-%d')
                except KeyError:
                    date_value = 103
    return date_value


def string_field_value(ensdict, field_name):
    try:
        string_value = ensdict[field_name]["RDU"]['value']
    except KeyError:
        try:
            string_value = ensdict[field_name]["FEED"]['value']
        except KeyError:
            string_value = ""

    return string_value


def url_string_field_value(ensdict, field_name):
    try:
        string_value = ensdict[field_name]["RDU"]['value'].replace(" ", "%20")
    except KeyError:
        try:
            string_value = ensdict[field_name]["FEED"]['value'].replace(" ", "%20")
        except KeyError:
            string_value = ""

    return string_value


def string_field_within_nested_array_no_spaces_max_255_value(ensdict, field_name, row):
    nested_array_field_name = field_name.split(".")[0]
    nested_array_sub_field_name = field_name.split(".")[1]
    try:
        string_value = ensdict[nested_array_field_name][row][nested_array_sub_field_name]["RDU"]['value'].replace(" ", "_")[:255]
    except KeyError:
        try:
            string_value = ensdict[nested_array_field_name][row][nested_array_sub_field_name]["FEED"]['value'].replace(" ", "_")[:255]
        except KeyError:
            string_value = ""

    return string_value


def string_field_no_spaces_max_255_value(ensdict, field_name):

    return string_field_value(ensdict, field_name).replace(" ", "_")[:255]


def domain_normalized_value(record, field, dv_map):
    try:
        norm_value = record[field]["RDU"]["value"]["normalizedValue"]
    except KeyError:
        try:
            norm_value = dv_map[record["exchangeSourceName"]["FEED"]["value"]["val"] + "|" + record[field]["FEED"]["value"]["val"]]
        except KeyError:
            norm_value = "errorCode101"

    return norm_value


def domain_feed_value(record, field):
    try:
        feed_value = record[field]["FEED"]["value"]["val"]
    except KeyError:
        try:
            # In case, notification has been manually imported
            feed_value = record[field]["RDU"]["value"]["normalizedValue"]
        except KeyError:
            feed_value = ""

    return feed_value


def lookup_exchange_source_name_domain(exchange_source_name, return_column, en_ds_domain):
    try:
        value = en_ds_domain[exchange_source_name][return_column]
    except KeyError:
        value = "UNKNOWN"

    return value


def lookup_normalized_parent_instrument_type_name(instrument_type_code, ins_type_domain):

    if instrument_type_code != "errorCode101":
        # Returning long name of the normalized parent instrumentTypeCode
        parent_instrument_type_name = ins_type_domain[instrument_type_code[:2]]["name"]
    else:
        parent_instrument_type_name = instrument_type_code

    return parent_instrument_type_name


def string_hash(string):
    hash_value = 0
    for character in string:
        hash_value = (hash_value*281 ^ ord(character)*997) & 0xFFFFFFFF

    return str(hash_value)
