import datetime
from dateutil.tz import tzutc


class EnsTestStatic(object):

    ins_type_domain = {
        "0": {"name": "Unknown"},
        "51": {"name": "Equity"},
        "5101": {"name": "Common Share"},
        "5102": {"name": "Preferred Share"},
        "5105": {"name": "Master Limited Partnership"},
        "5106": {"name": "Depositary Receipt"},
        "5107": {"name": "Dividend"},
        "5199": {"name": "Other Equity"},
        "52": {"name": "Fund"},
        "5201": {"name": "Mutual Fund"},
        "5202": {"name": "Unit Investment Trust"},
        "5203": {"name": "Exchange-Traded Fund"},
        "5204": {"name": "Alternative Investment Fund"},
        "5206": {"name": "Pension Fund"},
        "5207": {"name": "Real Estate Investment Trust"},
        "5208": {"name": "Royalty Trust"},
        "5299": {"name": "Other Fund"},
        "53": {"name": "Structured"},
        "5301": {"name": "Pass-through"},
        "5302": {"name": "Collateralized Mortgage Obligation"},
        "5303": {"name": "Asset Backed Security"},
        "5399": {"name": "Other Structured"},
        "54": {"name": "Fixed Income"},
        "5401": {"name": "Corporate Bond"},
        "5402": {"name": "Sovereign"},
        "5403": {"name": "Municipal Security"},
        "5404": {"name": "Investment Certificate"},
        "5405": {"name": "Certificate of Deposit"},
        "5406": {"name": "Linked Note"},
        "5407": {"name": "Commercial Paper"},
        "5408": {"name": "Bank Loan"},
        "5409": {"name": "Synthetic Bond"},
        "5410": {"name": "Banker`s Note"},
        "5411": {"name": "Banker's Acceptance"},
        "5412": {"name": "Bearer Deposit Note"},
        "5413": {"name": "Whole Loan"},
        "5414": {"name": "Debt Depositary Receipt"},
        "5499": {"name": "Other Fixed Income"},
        "55": {"name": "Commodity"},
        "5501": {"name": "Precious Metal"},
        "5502": {"name": "Base Metal"},
        "5503": {"name": "Timber"},
        "5504": {"name": "Dairy"},
        "5505": {"name": "Livestock"},
        "5506": {"name": "Grains"},
        "5511": {"name": "Oilseeds"},
        "5512": {"name": "Oil"},
        "5513": {"name": "Electric Power"},
        "5514": {"name": "Gas"},
        "5515": {"name": "Inter Energy"},
        "5516": {"name": "Softs"},
        "5517": {"name": "Emissions"},
        "5518": {"name": "Freight Dry"},
        "5519": {"name": "Multi Commodity"},
        "5520": {"name": "Coal"},
        "5521": {"name": "Weather"},
        "5522": {"name": "Foodstuffs"},
        "5523": {"name": "Other Agriculture"},
        "5524": {"name": "Seafood"},
        "5525": {"name": "Freight Wet"},
        "5528": {"name": "Plastic"},
        "5529": {"name": "Renewable Energy"},
        "5526": {"name": "Other Energy"},
        "5527": {"name": "Other Environmental"},
        "5599": {"name": "Other Commodity"},
        "56": {"name": "Option"},
        "5601": {"name": "Equity Option"},
        "5602": {"name": "Commodity Option"},
        "5603": {"name": "Equity Index Option"},
        "5604": {"name": "Commodity Index Option"},
        "5605": {"name": "Bond Option"},
        "5606": {"name": "FX Option"},
        "5607": {"name": "ETF Option"},
        "5608": {"name": "Dividend Option"},
        "5610": {"name": "Interest Rate Option"},
        "5611": {"name": "Swaption"},
        "5615": {"name": "Volatility Index Option"},
        "5614": {"name": "Credit Option"},
        "5699": {"name": "Other Option"},
        "57": {"name": "Future"},
        "5701": {"name": "Equity Future"},
        "5702": {"name": "Commodity Future"},
        "5703": {"name": "Equity Index Future"},
        "5704": {"name": "Commodity Index Future"},
        "5705": {"name": "Bond Future"},
        "5706": {"name": "FX Future"},
        "5707": {"name": "Dividend Future"},
        "5709": {"name": "Credit Future"},
        "5710": {"name": "ETF Future"},
        "5711": {"name": "Interest Rate Future"},
        "5712": {"name": "Volatility Index Future"},
        "5713": {"name": "Future on IR swap"},
        "5715": {"name": "Forward"},
        "5799": {"name": "Other Future"},
        "58": {"name": "Currency"},
        "5801": {"name": "Spot Rate"},
        "5802": {"name": "Cross Rate"},
        "5803": {"name": "Cash"},
        "5899": {"name": "Other Currency"},
        "59": {"name": "Index"},
        "5901": {"name": "Equity Index"},
        "5902": {"name": "Fixed Income Index"},
        "5903": {"name": "Commodity Index"},
        "5904": {"name": "Currency Index"},
        "5905": {"name": "Basket"},
        "5906": {"name": "Interest Rate Index"},
        "5907": {"name": "Volatility Index"},
        "5908": {"name": "Credit Index"},
        "5909": {"name": "Dividend Index"},
        "5910": {"name": "Housing Index"},
        "5999": {"name": "Other Index"},
        "60": {"name": "Economic Indicator"},
        "6001": {"name": "Economic Non-Financial"},
        "6002": {"name": "Market Statistics"},
        "6099": {"name": "Other Economic Indicator"},
        "61": {"name": "Swap"},
        "6101": {"name": "Interest Rate Swap"},
        "6102": {"name": "Volatility Index Swap"},
        "6103": {"name": "Inflation Swap"},
        "6104": {"name": "Credit Default SWAP"},
        "6105": {"name": "Equity Swap"},
        "6106": {"name": "Currency Swap"},
        "6107": {"name": "Commodity Swap"},
        "6108": {"name": "Equity Index Swap"},
        "6199": {"name": "Other Swap"},
        "62": {"name": "Strategy"},
        "6201": {"name": "Future Strategy"},
        "6202": {"name": "Option Strategy"},
        "6203": {"name": "Strategy on Strategy"},
        "6204": {"name": "Synthetic Strategy"},
        "6299": {"name": "Other Strategy"},
        "63": {"name": "Combined instrument"},
        "6301": {"name": "Combination of shares"},
        "6302": {"name": "Combination of bonds"},
        "6303": {"name": "Share and bond"},
        "6304": {"name": "Share and warrant"},
        "6305": {"name": "Combinations of warrant"},
        "6306": {"name": "Fund unit and other components"},
        "6307": {"name": "Other Combined instrument"},
        "64": {"name": "Entitlement"},
        "6401": {"name": "Warrant"},
        "6402": {"name": "Right"},
        "6403": {"name": "Mini-futures certificate"},
        "6404": {"name": "Other Entitlement"},
    }

    ins_type_map = {
        "OCC|OPTION": '56',
        "HKFE|OPTIONS": '56'
    }

    en_record_with_all_fields_cmeg_globex = {
        "dataSource" : {
            "FEED" : {
                "value" : {
                    "val" : "rduEns"
                }
            }
        },
        "enRawDataId" : {
            "FEED" : {
                "value" : "62cad0fcab0b8b34f5f57e43"
            }
        },
        "eventSourceUniqueId" : {
            "FEED" : {
                "value" : "20220710_CMEG_GLOBEX_PRODUCT LAUNCHES_2022/07/20220704.HTML#LFPLF_2"
            }
        },
        "exchangeCode" : {
            "FEED" : {
                "value" : {
                    "val" : "CME",
                    "val2" : "CMEG_GLOBEX",
                    "domain" : "exchangeCodeMap"
                }
            }
        },
        "exchangeSourceName" : {
            "FEED" : {
                "value" : {
                    "val" : "CMEG_GLOBEX",
                    "domain" : "enDataSourceCodeMap"
                }
            }
        },
        "eventPublishDate" : {
            "FEED" : {
                'value': datetime.datetime(2022, 7, 7, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventInsertDate" : {
            "FEED" : {
                'value': datetime.datetime(2022, 7, 10, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventEffectiveDate" : {
            "FEED" : {
                'value': datetime.datetime(2022, 8, 7, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "underlyings": [
            {
                "instrumentCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "nameLong": {
                    "FEED": {
                        "value": "Lumber Futures"
                    }
                },
                "quantity": {
                    "FEED": {
                        "value": {
                            "$numberDecimal": "100"
                        }
                    }
                }
            }
        ],
        "eventType" : {
            "FEED" : {
                "value" : {
                    "val" : "PRODUCT LAUNCHES",
                    "val2" : "CMEG_GLOBEX",
                    "domain" : "eventTypeMap"
                }
            }
        },
        "eventSubject" : {
            "FEED" : {
                "value" : "Lumber Futures and Options on Lumber Futures"
            }
        },
        "eventSummaryText" : {
            "FEED" : {
                "value" : "Effective Sunday, August 7 (trade date Monday, August 8), pending completion of all regulatory review periods, Lumber "
                          "Futures and Options on Lumber Futures will be listed for trading on CME Globex and for submission for clearing via CME "
                          "ClearPort."
            }
        },
        "instrumentTypeCode" : {
            "FEED" : {
                "value" : {
                    "val" : "OPTIONS ON FUTURES",
                    "val2" : "CMEG_GLOBEX",
                    "domain" : "assetClassificationsMap"
                }
            }
        },
        "exchangePrefix" : {
            "FEED" : {
                "value" : "LBR"
            }
        },
        "productName" : {
            "FEED" : {
                "value" : "Options on Lumber Futures"
            }
        },
        "eventInitialUrl" : {
            "FEED" : {
                "value" : "https://www.cmegroup.com/notices/electronic-trading/2022/07/2022 0704.html#lfplf"
            }
        },
        "exchangeReferenceId" : {
            "FEED" : {
                "value" : "2022/07/20220704.html#lfplf"
            }
        },
        "exchangeReferenceCounter" : {
            "FEED" : {
                "value" : 2
            }
        },
        "events" : {
            "FEED" : {
                "value" : [
                    "20220710_CMEG_GLOBEX_PRODUCT LAUNCHES_2022-07-20220704.HTML-LFPLF-LANDING_PAGE.pdf",
                    "20220710_CMEG_GLOBEX_PRODUCT LAUNCHES_2022-07-20220704.HTML-LFPLF.pdf"
                ]
            }
        },
        "eventStatus" : {
            "ENRICHED" : {
                "value" : {
                    "normalizedValue" : "A"
                }
            }
        }
    }

    en_record_with_all_fields = {
        "dataSource": {
            "FEED": {
                "value": {
                    "val": "rduEns"
                }
            }
        },
        "enRawDataId": {
            "FEED": {
                "value": "60addb848f6f224ea2d20cba"
            }
        },
        "eventSourceUniqueId": {
            "FEED": {
                "value": "20210526_OCC_CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS_48757_1"
            }
        },
        "newUnderlyings": [
            {
                "newInstrumentCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "newNameLong": {
                    "FEED": {
                        "value": "Cash ($17.25 x 100)"
                    }
                },
                "newQuantity": {
                    "FEED": {
                        "value": {
                            "$numberDecimal": "1725"
                        }
                    }
                },
                "newQuantityCurrencyCode": {
                    "FEED": {
                        "value": {
                            "val": "$",
                            "val2": "OCC",
                            "domain": "currencyCodesMap"
                        }
                    }
                }
            }
        ],
        "eventPreviousReferenceIds": [
            {
                "exchangeReferenceIdCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "exchangePreviousReferenceId": {
                    "FEED": {
                        "value": "23707"
                    }
                }
            }
        ],
        "exchangeSourceName": {
            "FEED": {
                "value": {
                    "val": "OCC",
                    "domain": "enDataSourceCodeMap"
                }
            }
        },
        "eventPublishDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 25, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventInsertDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 26, 00, 00, 00, tzinfo=tzutc())
                }
        },
        "eventEffectiveDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 25, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventType": {
            "FEED": {
                "value": {
                    "val": "CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS",
                    "val2": "OCC",
                    "domain": "eventTypeMap"
                }
            }
        },
        "eventSubject": {
            "FEED": {
                "value": "Tribune Publishing Company - Cash Settlement/Acceleration of\nExpirations\nOption Symbol: TPCO\nDate: 05/25/21"
            }
        },
        "eventSummaryText": {
            "FEED": {
                "value": "On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger "
                         "with a wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. "
                         "The merger was approved and subsequently consummated before the open on May 25, 2021. As a result, each existing "
                         "TPCO Common Share will be converted into the right to receive $17.25 net cash per share."
            }
        },
        "instrumentTypeCode": {
            "FEED": {
                "value": {
                    "val": "OPTION",
                    "val2": "OCC",
                    "domain": "assetClassificationsMap"
                }
            }
        },
        "exchangePrefix": {
            "FEED": {
                "value": "TPCO"
            }
        },
        "eventInitialUrl": {
            "FEED": {
                "value": "https://infomemo.theocc.com/infomemos?number=48757"
            }
        },
        "exchangeReferenceId": {
            "FEED": {
                "value": "48757"
            }
        },
        "exchangeReferenceCounter": {
            "FEED": {
                "value": 1
            }
        },
        "newExpirationDate": {
            "FEED": {
                'value': datetime.datetime(2021, 6, 18, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "events": {
            "FEED": {
                "value": [
                    "20210526_OCC_CASH SETTLEMENT-ACCELERATION OF EXPIRATIONS_48757.pdf"
                ]
            }
        },
        "eventStatus": {
            "ENRICHED": {
                "value": {
                    "normalizedValue": "A"
                }
            }
        }
    }

    en_manual_record_with_all_fields = {
        "dataSource": {
            "FEED": {
                "value": {
                    "val": "rduEns"
                }
            }
        },
        "insDate": {
            "RDU": {
                "value": datetime.datetime(2022, 7, 21, 6, 7, 15, tzinfo=tzutc())
            }
        },
        "insUser": {
            "RDU": {
                "value": "jaime.rengifo@smartstreamrdu.com"
            }
        },
        "eventSourceUniqueId": {
            "FEED": {
                "value": "20220721_OCC_ACCELERATION OF MATURITY/EXPIRATION_48757_1_RDU"
            }
        },
        "underlyings": [
            {
                "instrumentCounter": {
                    "RDU": {
                        "value": 1
                    }
                },
                "nameLong": {
                    "RDU": {
                        "value": "Tribune Publishing Company - Very long underlying name for testing purposes which needs to be longer than 255 "
                                 "characters to be truncated. Repeating the same:Tribune Publishing Company - Very long underlying name for testing "
                                 "purposes which needs to be longer than 255 characters to be truncated."
                    }
                },
                "quantity": {
                    "RDU": {
                        "value": "1725.0"
                    }
                },
                "tradeCurrencyCode": {
                    "RDU": {
                        "value": {
                            "normalizedValue": "USD"
                        }
                    }
                }
            }
        ],
        "eventPreviousReferenceIds": [
            {
                "exchangeReferenceIdCounter": {
                    "RDU": {
                        "value": 1
                    }
                },
                "exchangePreviousReferenceId": {
                    "RDU": {
                        "value": "23707"
                    }
                }
            }
        ],
        "exchangeSourceName": {
            "RDU": {
                "value": {
                    "normalizedValue": "OCC"
                }
            }
        },
        "eventPublishDate": {
            "RDU": {
                "value": datetime.datetime(2021, 5, 25, 0, 0, 0, tzinfo=tzutc())
            }
        },
        "eventInsertDate": {
            "RDU": {
                "value": datetime.datetime(2022, 7, 21, 6, 7, 15, tzinfo=tzutc())
            }
        },
        "eventEffectiveDate": {
            "RDU": {
                "value": datetime.datetime(2021, 5, 25, 0, 0, 0, tzinfo=tzutc())
            }
        },
        "eventType": {
            "RDU": {
                "value": {
                    "normalizedValue": "Acceleration Of Maturity/Expiration"
                }
            }
        },
        "eventSubject": {
            "RDU": {
                "value": "Tribune Publishing Company - Cash Settlement/Acceleration of\\nExpirations\\nOption Symbol: TPCO\\nDate: 05/25/21"
            }
        },
        "productName": {
            "RDU": {
                "value": "Tribune Publishing Company - Very long company name for testing purposes which needs to be longer than 255 characters to be"
                         " truncated. Repeating the same:Tribune Publishing Company - Very long company name for testing purposes which needs to be "
                         "longer than 255 characters to be truncated."
            }
        },
        "eventSummaryText": {
            "RDU": {
                "value": "On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger with a wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. The merger was approved and subsequently consummated before the open on May 25, 2021. As a result, each existing TPCO Common Share will be converted into the right to receive $17.25 net cash per share."
            }
        },
        "instrumentTypeCode": {
            "RDU": {
                "value": {
                    "normalizedValue": "56"
                }
            }
        },
        "exchangePrefix": {
            "RDU": {
                "value": "TPCO"
            }
        },
        "eventInitialUrl": {
            "RDU": {
                "value": "https://infomemo.theocc.com/infom emos?number=48757"
            }
        },
        "exchangeReferenceId": {
            "RDU": {
                "value": "48757"
            }
        },
        "exchangeReferenceCounter": {
            "RDU": {
                "value": 1
            }
        },
        "newExpirationDate": {
            "RDU": {
                'value': datetime.datetime(2021, 6, 18, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "events": {
            "RDU": {
                "value": [
                    "20220721_OCC_ACCELERATION OF MATURITY_EXPIRATION_48757_RDU.pdf"
                ]
            }
        },
        "eventStatus": {
            "RDU": {
                "value": {
                    "normalizedValue": "A"
                }
            }
        }
    }

    en_record_with_all_fields_output = {
        'ticket_type': 'New',
        'storm_key': 'CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS.2021-05-25.Option.48757.OCC',
        'exchange_reference_id': '48757',
        'event_initial_url': 'https://infomemo.theocc.com/infomemos?number=48757',
        'exchange_group_name': 'OCC',
        'exchange_source_name': 'OCC',
        'exchange_owner': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1',
        'exchange_owners': [{
            'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'
        }, {
            'accountId': '5c82012316effa74fa9dfc45'
        }, {
            'accountId': '5ef3475360d3c80ac906c37b'
        }
        ],
        'publish_date': '2021-05-25',
        'insert_date': '2021-05-26',
        'event_subject': 'Tribune Publishing Company - Cash Settlement/Acceleration of|Expirations|Option Symbol: TPCO|Date: 05/25/21',
        'event_summary': 'On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger with a '
                         'wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. The merger was approved and '
                         'subsequently consummated before the open on May 25, 2021. As a result, each existing TPCO Common Share will be converted '
                         'into the right to receive $17.25 net cash per share.',
        'effective_date': '2021-05-25',
        'norm_instrument_type': 'Option',
        'norm_event_type': 'Acceleration Of Maturity/Expiration'
    }

    en_record_with_all_fields_basic_output = {
        'ticket_type': 'New',
        'storm_key': 'CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS.2021-05-25.Option.48757.OCC',
        'exchange_reference_id': '48757',
        'event_initial_url': 'https://infomemo.theocc.com/infomemos?number=48757',
        'exchange_group_name': 'OCC',
        'exchange_source_name': 'OCC',
        'exchange_owner': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1',
        'exchange_owners': [{
            'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'
        }, {
            'accountId': '5c82012316effa74fa9dfc45'
        }, {
            'accountId': '5ef3475360d3c80ac906c37b'
        }
        ],
        'publish_date': '2021-05-25',
        'insert_date': '2021-05-26',
        'event_subject': 'Tribune Publishing Company - Cash Settlement/Acceleration of|Expirations|Option Symbol: TPCO|Date: 05/25/21',
        'event_summary': 'On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger with a '
                         'wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. The merger was approved and '
                         'subsequently consummated before the open on May 25, 2021. As a result, each existing TPCO Common Share will be converted '
                         'into the right to receive $17.25 net cash per share.',
        'effective_date': '2021-05-25',
        'norm_instrument_type': 'Option',
        'norm_event_type': 'Acceleration Of Maturity/Expiration'
    }

    en_record_with_all_fields_including_discrepant_output = {
        'ticket_type': 'New',
        'storm_key': 'CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS.2021-05-25.Option.48757.OCC',
        'exchange_reference_id': '48757',
        'event_initial_url': 'https://infomemo.theocc.com/infomemos?number=48757',
        'exchange_group_name': 'OCC',
        'exchange_source_name': 'OCC',
        'exchange_owner': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1',
        'exchange_owners': [{
            'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'
        }, {
            'accountId': '5c82012316effa74fa9dfc45'
        }, {
            'accountId': '5ef3475360d3c80ac906c37b'
        }
        ],
        'publish_date': '2021-05-25',
        'insert_date': '2021-05-26',
        'event_subject': 'Tribune Publishing Company - Cash Settlement/Acceleration of|Expirations|Option Symbol: TPCO|Date: 05/25/21',
        'event_summary': 'On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger with a '
                         'wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. The merger was approved and '
                         'subsequently consummated before the open on May 25, 2021. As a result, each existing TPCO Common Share will be converted '
                         'into the right to receive $17.25 net cash per share.',
        'effective_date': '2021-05-25',
        'norm_instrument_type': 'Option',
        'norm_event_type': 'Acceleration Of Maturity/Expiration',
        'exchange_prefixes': ['TPCO'],
        'previous_references': ['23707'],
        'product_names': ['']
    }

    en_record_with_all_fields_discrepant_fields_api_output = {
        'fields': {'customfield_10259': ['TPCO'],
                   'customfield_10260': ['23707'],
                   'customfield_10261': ['']}}

    en_record_with_all_fields_api_input = {
        'fields': {
            'project': {
                'key': 'ENSTEST'
            },
            'summary': 'OCC:Option:Acceleration Of Maturity/Expiration:Tribune Publishing Company - Cash Settlement/Acceleration of|Expirations|'
                       'Option Symbol: TPCO|Date: 05/25/21',
            'issuetype': {'name': 'Task'},
            'description': 'On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger with a '
                           'wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. The merger was approved '
                           'and subsequently consummated before the open on May 25, 2021. As a result, each existing TPCO Common Share will be '
                           'converted into the right to receive $17.25 net cash per share.',
            'customfield_10030': '247',
            'assignee': {
                'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'
            },
            'reporter': {
                'accountId': '5c8a4fd2677d763daafa57c7'
            },
            'customfield_10251': {
                'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'
            },
            'customfield_10031': [{
                'accountId': '557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1'
            }, {
                'accountId': '5c82012316effa74fa9dfc45'
            }, {
                'accountId': '5ef3475360d3c80ac906c37b'
            }
            ],
            'customfield_10242': '2021-05-25',
            'customfield_10243': '2021-05-26',
            'customfield_10282': '48757',
            'customfield_10274': 'OCC',
            'customfield_10275': 'OCC',
            'customfield_10279': {'value': 'Option'},
            'customfield_10285': 'CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS.2021-05-25.Option.48757.OCC',
            'customfield_10283': 'https://infomemo.theocc.com/infomemos?number=48757',
            'customfield_10237': '2021-05-25',
            'customfield_10238': '2021-05-25',
        }
    }

    en_record_with_all_fields_unknown_exchange_source_name = {
        "dataSource": {
            "FEED": {
                "value": {
                    "val": "rduEns"
                }
            }
        },
        "enRawDataId": {
            "FEED": {
                "value": "60addb848f6f224ea2d20cba"
            }
        },
        "eventSourceUniqueId": {
            "FEED": {
                "value": "20210526_OCC_CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS_48757_1"
            }
        },
        "newUnderlyings": [
            {
                "newInstrumentCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "newNameLong": {
                    "FEED": {
                        "value": "Cash ($17.25 x 100)"
                    }
                },
                "newQuantity": {
                    "FEED": {
                        "value": {
                            "$numberDecimal": "1725"
                        }
                    }
                },
                "newQuantityCurrencyCode": {
                    "FEED": {
                        "value": {
                            "val": "$",
                            "val2": "OCC",
                            "domain": "currencyCodesMap"
                        }
                    }
                }
            }
        ],
        "eventPreviousReferenceIds": [
            {
                "exchangeReferenceIdCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "exchangePreviousReferenceId": {
                    "FEED": {
                        "value": "23707"
                    }
                }
            }
        ],
        "exchangeSourceName": {
            "FEED": {
                "value": {
                    "val": "UNKNOWN",
                    "domain": "enDataSourceCodeMap"
                }
            }
        },
        "eventPublishDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 25, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventInsertDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 26, 00, 00, 00, tzinfo=tzutc())
                }
        },
        "eventEffectiveDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 25, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventType": {
            "FEED": {
                "value": {
                    "val": "CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS",
                    "val2": "OCC",
                    "domain": "eventTypeMap"
                }
            }
        },
        "eventSubject": {
            "FEED": {
                "value": "Tribune Publishing Company - Cash Settlement/Acceleration of\nExpirations\nOption Symbol: TPCO\nDate: 05/25/21"
            }
        },
        "eventSummaryText": {
            "FEED": {
                "value": "On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger "
                         "with a wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. "
                         "The merger was approved and subsequently consummated before the open on May 25, 2021. As a result, each existing "
                         "TPCO Common Share will be converted into the right to receive $17.25 net cash per share."
            }
        },
        "instrumentTypeCode": {
            "FEED": {
                "value": {
                    "val": "OPTION",
                    "val2": "OCC",
                    "domain": "assetClassificationsMap"
                }
            }
        },
        "exchangePrefix": {
            "FEED": {
                "value": "TPCO"
            }
        },
        "exchangeReferenceId": {
            "FEED": {
                "value": "48757"
            }
        },
        "exchangeReferenceCounter": {
            "FEED": {
                "value": 1
            }
        },
        "newExpirationDate": {
            "FEED": {
                'value': datetime.datetime(2021, 6, 18, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "events": {
            "FEED": {
                "value": [
                    "20210526_OCC_CASH SETTLEMENT-ACCELERATION OF EXPIRATIONS_48757.pdf"
                ]
            }
        },
        "eventStatus": {
            "ENRICHED": {
                "value": {
                    "normalizedValue": "A"
                }
            }
        }
    }

    en_record_with_all_fields_and_publish_date_with_error_code = {
        "dataSource": {
            "FEED": {
                "value": {
                    "val": "rduEns"
                }
            }
        },
        "enRawDataId": {
            "FEED": {
                "value": "60addb848f6f224ea2d20cba"
            }
        },
        "eventSourceUniqueId": {
            "FEED": {
                "value": "20210526_OCC_CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS_48757_1"
            }
        },
        "newUnderlyings": [
            {
                "newInstrumentCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "newNameLong": {
                    "FEED": {
                        "value": "Cash ($17.25 x 100)"
                    }
                },
                "newQuantity": {
                    "FEED": {
                        "value": {
                            "$numberDecimal": "1725"
                        }
                    }
                },
                "newQuantityCurrencyCode": {
                    "FEED": {
                        "value": {
                            "val": "$",
                            "val2": "OCC",
                            "domain": "currencyCodesMap"
                        }
                    }
                }
            }
        ],
        "eventPreviousReferenceIds": [
            {
                "exchangeReferenceIdCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "exchangePreviousReferenceId": {
                    "FEED": {
                        "value": "23707"
                    }
                }
            }
        ],
        "exchangeSourceName": {
            "FEED": {
                "value": {
                    "val": "OCC",
                    "domain": "enDataSourceCodeMap"
                }
            }
        },
        "eventPublishDate": {
            "FEED" : {
                "errorCode" : 103
            }
        },
        "eventInsertDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 26, 00, 00, 00, tzinfo=tzutc())
                }
        },
        "eventEffectiveDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 25, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventType": {
            "FEED": {
                "value": {
                    "val": "CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS",
                    "val2": "OCC",
                    "domain": "eventTypeMap"
                }
            }
        },
        "eventSubject": {
            "FEED": {
                "value": "Tribune Publishing Company - Cash Settlement/Acceleration of\nExpirations\nOption Symbol: TPCO\nDate: 05/25/21"
            }
        },
        "eventSummaryText": {
            "FEED": {
                "value": "On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger "
                         "with a wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. "
                         "The merger was approved and subsequently consummated before the open on May 25, 2021. As a result, each existing "
                         "TPCO Common Share will be converted into the right to receive $17.25 net cash per share."
            }
        },
        "instrumentTypeCode": {
            "FEED": {
                "value": {
                    "val": "OPTION",
                    "val2": "OCC",
                    "domain": "assetClassificationsMap"
                }
            }
        },
        "exchangePrefix": {
            "FEED": {
                "value": "TPCO"
            }
        },
        "eventInitialUrl": {
            "FEED": {
                "value": "https://infomemo.theocc.com/infomemos?number=48757"
            }
        },
        "exchangeReferenceId": {
            "FEED": {
                "value": "48757"
            }
        },
        "exchangeReferenceCounter": {
            "FEED": {
                "value": 1
            }
        },
        "newExpirationDate": {
            "FEED": {
                'value': datetime.datetime(2021, 6, 18, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "events": {
            "FEED": {
                "value": [
                    "20210526_OCC_CASH SETTLEMENT-ACCELERATION OF EXPIRATIONS_48757.pdf"
                ]
            }
        },
        "eventStatus": {
            "ENRICHED": {
                "value": {
                    "normalizedValue": "A"
                }
            }
        }
    }


    en_record_with_all_fields_and_ops_lock = {
        "dataSource": {
            "FEED": {
                "value": {
                    "val": "rduEns"
                }
            }
        },
        "enRawDataId": {
            "FEED": {
                "value": "60addb848f6f224ea2d20cba"
            }
        },
        "eventSourceUniqueId": {
            "FEED": {
                "value": "20210526_OCC_CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS_48757_1"
            }
        },
        "newUnderlyings": [
            {
                "newInstrumentCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "newNameLong": {
                    "FEED": {
                        "value": "Cash ($17.25 x 100)"
                    }
                },
                "newQuantity": {
                    "FEED": {
                        "value": {
                            "$numberDecimal": "1725"
                        }
                    }
                },
                "newQuantityCurrencyCode": {
                    "FEED": {
                        "value": {
                            "val": "$",
                            "val2": "OCC",
                            "domain": "currencyCodesMap"
                        }
                    }
                }
            }
        ],
        "eventPreviousReferenceIds": [
            {
                "exchangeReferenceIdCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "exchangePreviousReferenceId": {
                    "FEED": {
                        "value": "23707"
                    }
                }
            }
        ],
        "exchangeSourceName": {
            "FEED": {
                "value": {
                    "val": "OCC",
                    "domain": "enDataSourceCodeMap"
                }
            }
        },
        "eventPublishDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 25, 00, 00, 00, tzinfo=tzutc())
            },
            "RDU": {
                'value': datetime.datetime(2022, 5, 26, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventInsertDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 26, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventEffectiveDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 25, 00, 00, 00, tzinfo=tzutc())
            },
            "RDU": {
                'value': datetime.datetime(2022, 5, 26, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventType": {
            "FEED": {
                "value": {
                    "val": "CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS",
                    "val2": "OCC",
                    "domain": "eventTypeMap"
                }
            }
        },
        "eventSubject": {
            "FEED": {
                "value": "Tribune Publishing Company - Cash Settlement/Acceleration of\nExpirations\nOption Symbol: TPCO\nDate: 05/25/21"
            },
            "RDU": {
                "value": "RDU Lock test value for eventSubject"
            }
        },
        "eventSummaryText": {
            "FEED": {
                "value": "On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger "
                         "with a wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. "
                         "The merger was approved and subsequently consummated before the open on May 25, 2021. As a result, each existing "
                         "TPCO Common Share will be converted into the right to receive $17.25 net cash per share."
            }
        },
        "instrumentTypeCode": {
            "FEED": {
                "value": {
                    "val": "UNEXPECTED VALUE",
                    "val2": "OCC",
                    "domain": "assetClassificationsMap"
                }
            },
            "RDU": {
                "value": {
                    "normalizedValue": "59"
                }
            }
        },
        "exchangePrefix": {
            "FEED": {
                "value": "TPCO"
            }
        },
        "eventInitialUrl": {
            "FEED": {
                "value": "https://infomemo.theocc.com/infomemos?number=48757"
            }
        },
        "exchangeReferenceId": {
            "FEED": {
                "value": "48757"
            }
        },
        "exchangeReferenceCounter": {
            "FEED": {
                "value": 1
            }
        },
        "newExpirationDate": {
            "FEED": {
                'value': datetime.datetime(2021, 6, 18, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "events": {
            "FEED": {
                "value": [
                    "20210526_OCC_CASH SETTLEMENT-ACCELERATION OF EXPIRATIONS_48757.pdf"
                ]
            }
        },
        "eventStatus": {
            "ENRICHED": {
                "value": {
                    "normalizedValue": "A"
                }
            }
        }
    }

    en_record_with_all_fields_wo_valid_values_for_some_fields = {
        "dataSource": {
            "FEED": {
                "value": {
                    "val": "rduEns"
                }
            }
        },
        "enRawDataId": {
            "FEED": {
                "value": "60addb848f6f224ea2d20cba"
            }
        },
        "eventSourceUniqueId": {
            "FEED": {
                "value": "20210526_OCC_CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS_48757_1"
            }
        },
        "newUnderlyings": [
            {
                "newInstrumentCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "newNameLong": {
                    "FEED": {
                        "value": "Cash ($17.25 x 100)"
                    }
                },
                "newQuantity": {
                    "FEED": {
                        "value": {
                            "$numberDecimal": "1725"
                        }
                    }
                },
                "newQuantityCurrencyCode": {
                    "FEED": {
                        "value": {
                            "val": "$",
                            "val2": "OCC",
                            "domain": "currencyCodesMap"
                        }
                    }
                }
            }
        ],
        "eventPreviousReferenceIds": [
            {
                "exchangeReferenceIdCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "exchangePreviousReferenceId": {
                    "FEED": {
                        "value": "23707"
                    }
                }
            }
        ],
        "exchangeSourceName": {
            "FEED": {
                "value": {
                    "val": "OCC",
                    "domain": "enDataSourceCodeMap"
                }
            }
        },
        "eventInsertDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 26, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventType": {
            "FEED": {
                "value": {
                    "val": "CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS",
                    "val2": "OCC",
                    "domain": "eventTypeMap"
                }
            }
        },
        "eventSummaryText": {
            "FEED": {
                "value": "On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger "
                         "with a wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. "
                         "The merger was approved and subsequently consummated before the open on May 25, 2021. As a result, each existing "
                         "TPCO Common Share will be converted into the right to receive $17.25 net cash per share."
            }
        },
        "instrumentTypeCode": {
            "FEED": {
                "value": {
                    "val": "UNEXPECTED VALUE",
                    "val2": "OCC",
                    "domain": "assetClassificationsMap"
                }
            }
        },
        "exchangePrefix": {
            "FEED": {
                "value": "TPCO"
            }
        },
        "eventInitialUrl": {
            "FEED": {
                "value": "https://infomemo.theocc.com/infomemos?number=48757"
            }
        },
        "exchangeReferenceId": {
            "FEED": {
                "value": "48757"
            }
        },
        "exchangeReferenceCounter": {
            "FEED": {
                "value": 1
            }
        },
        "newExpirationDate": {
            "FEED": {
                "value": {
                    "$date": "2021-06-18T00:00:00Z"
                }
            }
        },
        "events": {
            "FEED": {
                "value": [
                    "20210526_OCC_CASH SETTLEMENT-ACCELERATION OF EXPIRATIONS_48757.pdf"
                ]
            }
        },
        "eventStatus": {
            "ENRICHED": {
                "value": {
                    "normalizedValue": "A"
                }
            }
        }
    }

    en_record_with_missing_event_type_and_ins_type_fields = {
        "dataSource": {
            "FEED": {
                "value": {
                    "val": "rduEns"
                }
            }
        },
        "enRawDataId": {
            "FEED": {
                "value": "60addb848f6f224ea2d20cba"
            }
        },
        "eventSourceUniqueId": {
            "FEED": {
                "value": "20210526_OCC_CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS_48757_1"
            }
        },
        "newUnderlyings": [
            {
                "newInstrumentCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "newNameLong": {
                    "FEED": {
                        "value": "Cash ($17.25 x 100)"
                    }
                },
                "newQuantity": {
                    "FEED": {
                        "value": {
                            "$numberDecimal": "1725"
                        }
                    }
                },
                "newQuantityCurrencyCode": {
                    "FEED": {
                        "value": {
                            "val": "$",
                            "val2": "OCC",
                            "domain": "currencyCodesMap"
                        }
                    }
                }
            }
        ],
        "eventPreviousReferenceIds": [
            {
                "exchangeReferenceIdCounter": {
                    "FEED": {
                        "value": 1
                    }
                },
                "exchangePreviousReferenceId": {
                    "FEED": {
                        "value": "23707"
                    }
                }
            }
        ],
        "exchangeSourceName": {
            "FEED": {
                "value": {
                    "val": "OCC",
                    "domain": "enDataSourceCodeMap"
                }
            }
        },
        "eventInsertDate": {
            "FEED": {
                'value': datetime.datetime(2021, 5, 26, 00, 00, 00, tzinfo=tzutc())
            }
        },
        "eventSummaryText": {
            "FEED": {
                "value": "On May 21, 2021, Shareholders of Tribune Publishing Company (TPCO) voted concerning the proposed merger "
                         "with a wholly-owned subsidiary of Tribune Enterprises, LLC, an affiliate of Alden Global Capital LLC. "
                         "The merger was approved and subsequently consummated before the open on May 25, 2021. As a result, each existing "
                         "TPCO Common Share will be converted into the right to receive $17.25 net cash per share."
            }
        },
        "exchangePrefix": {
            "FEED": {
                "value": "TPCO"
            }
        },
        "eventInitialUrl": {
            "FEED": {
                "value": "https://infomemo.theocc.com/infomemos?number=48757"
            }
        },
        "exchangeReferenceId": {
            "FEED": {
                "value": "48757"
            }
        },
        "exchangeReferenceCounter": {
            "FEED": {
                "value": 1
            }
        },
        "newExpirationDate": {
            "FEED": {
                "value": {
                    "$date": "2021-06-18T00:00:00Z"
                }
            }
        },
        "events": {
            "FEED": {
                "value": [
                    "20210526_OCC_CASH SETTLEMENT-ACCELERATION OF EXPIRATIONS_48757.pdf"
                ]
            }
        },
        "eventStatus": {
            "ENRICHED": {
                "value": {
                    "normalizedValue": "A"
                }
            }
        }
    }

    en_domain_dic = {
        "OCC": {
            "exchangeGroupName": "OCC",
            "exchangeOwner": "pallavi.kulkarni@smartstreamrdu.com",
            "allOpsAnalysts": "pallavi.kulkarni@smartstreamrdu.com,Arunkumar.Murthy@smartstreamrdu.com,sudhindra.muralidhar@smartstreamrdu.com"
        },
        "EUREX_COAX_INFORMATION": {
            "exchangeGroupName": "EUREX",
            "exchangeOwner": "ashish.patil@smartstreamrdu.com",
            "allOpsAnalysts": "ashish.patil@smartstreamrdu.com,amzad.khan@smartstreamrdu.com,renson.alva@smartstreamrdu.com"
        }
    }

    ops_users_domain_dic = {
        "pallavi.kulkarni@smartstreamrdu.com": {
            "accountId": "557058:e6f5eaab-89f5-4c22-8e45-6c2349687af1",
        },
        "arunkumar.murthy@smartstreamrdu.com": {
            "accountId": "5c82012316effa74fa9dfc45",
        },
        "sudhindra.muralidhar@smartstreamrdu.com": {
            "accountId": "5ef3475360d3c80ac906c37b",
        },
        "ashish.patil@smartstreamrdu.com": {
            "accountId": "5b51d50fd09e562c1b7b75a4",
        },
        "amzad.khan@smartstreamrdu.com": {
            "accountId": "5a8c12aea6ca96323a2dd3e9",
        },
        "renson.alva@smartstreamrdu.com": {
            "accountId": "611f8330ee94700071b35b16",
        },
    }

