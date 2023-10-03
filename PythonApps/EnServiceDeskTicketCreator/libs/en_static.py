class EnStatic(object):

    FLAT_FIELDS_TO_APPEND = {
        "exchange_prefixes": "exchangePrefix",
        "product_names": "productName"
    }
    NESTED_ARRAY_FIELDS_TO_APPEND = {
        "previous_references": "eventPreviousReferenceIds.exchangePreviousReferenceId",
        "underlying_names": "underlyings.nameLong",
        "underlying_tickers": "underlyings.exchangeTicker"
    }

    CMEG_GLOBEX_STRINGS_TO_STRIP_OUT = ["Updated - ",
                                        "Postponed - ",
                                        "New - ",
                                        "Update - ",
                                        "Correction - "]
