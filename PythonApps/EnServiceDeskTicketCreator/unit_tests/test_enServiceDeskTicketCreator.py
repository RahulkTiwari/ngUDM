import unittest
from enServiceDeskTicketCreator import create_service_desk_ticket, generate_new_service_desk_ticket_dict, add_effective_date_to_dict, \
    calculate_storm_key, generate_dict_entries_with_discrepant_value
from libs.conversions import date_field_value, eff_date_field_value, string_field_value, date_feed_field_value, domain_normalized_value, \
    string_hash, lookup_exchange_source_name_domain, lookup_normalized_parent_instrument_type_name, domain_feed_value, \
    string_field_no_spaces_max_255_value, string_field_within_nested_array_no_spaces_max_255_value, url_string_field_value
from libs.data_for_api_calls import create_servicedesk_new_ticket_data,generate_issue_upd_data
from unit_tests.test_static import EnsTestStatic as EnsTestStatic


class CalculateStormKey(unittest.TestCase):
    def test_valid_feed_value_standard_exchange(self):
        function_result = calculate_storm_key("OCC", "2021-05-25", "Options",
                                              "Tribune Publishing Company - Cash Settlement/Acceleration of\nExpirations\nOption Symbol: "
                                              "TPCO\nDate: 05/25/21", EnsTestStatic.en_record_with_all_fields)
        self.assertEqual('CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS|2021-05-25|Options|48757|OCC',
                         function_result)

    def test_valid_feed_value_cmeg_globex(self):
        function_result = calculate_storm_key("CMEG_GLOBEX", "2022-08-07", "Options", "Lumber Futures and Options on Lumber Futures",
                                              EnsTestStatic.en_record_with_all_fields_cmeg_globex)
        self.assertEqual('PRODUCT LAUNCHES|2022-08-07|Options|Lumber Futures and Options on Lumber Futures|CMEG_GLOBEX',
                         function_result)


class GenerateNewServiceDeskTicketDict(unittest.TestCase):
    def test_valid_complete_record(self):
        function_result = generate_new_service_desk_ticket_dict(
            EnsTestStatic.en_record_with_all_fields,
            "2021-05-25",
            "Tribune Publishing Company - Cash Settlement/Acceleration of|Expirations|Option Symbol: TPCO|Date: 05/25/21",
            "Option",
            "Acceleration Of Maturity/Expiration",
            "CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS.2021-05-25.Option.48757.OCC",
            EnsTestStatic.en_domain_dic,
            EnsTestStatic.ops_users_domain_dic)
        self.assertEqual(EnsTestStatic.en_record_with_all_fields_output, function_result)


class CreateServicedeskNewTicketData(unittest.TestCase):
    def test_valid_full_record(self):
        function_result = create_servicedesk_new_ticket_data(EnsTestStatic.en_record_with_all_fields_output)
        self.assertEqual(EnsTestStatic.en_record_with_all_fields_api_input, function_result)


class GenerateDictEntriesWithDiscrepantValue(unittest.TestCase):
    def test_valid_complete_record_with_discrepant_fields(self):
        function_result = generate_dict_entries_with_discrepant_value(EnsTestStatic.en_record_with_all_fields,
                                                                      EnsTestStatic.en_record_with_all_fields_basic_output)
        self.assertEqual(EnsTestStatic.en_record_with_all_fields_including_discrepant_output, function_result)


class GenerateIssueUpdData(unittest.TestCase):
    def test_valid_discrepant_fields_record(self):
        function_result = generate_issue_upd_data(EnsTestStatic.en_record_with_all_fields_including_discrepant_output)
        self.assertEqual(EnsTestStatic.en_record_with_all_fields_discrepant_fields_api_output, function_result)


class LookupExchangeSourceNameDomain(unittest.TestCase):
    def test_expected_source_name_to_return_exchange_owner(self):
        function_result = lookup_exchange_source_name_domain("OCC", "exchangeOwner", EnsTestStatic.en_domain_dic)
        self.assertEqual("pallavi.kulkarni@smartstreamrdu.com", function_result)

    def test_unexpected_source_name_to_return_exchange_owner(self):
        function_result = lookup_exchange_source_name_domain("ABCD", "exchangeOwner", EnsTestStatic.en_domain_dic)
        self.assertEqual("UNKNOWN", function_result)

    def test_expected_source_name_to_return_all_ops_analysts(self):
        function_result = lookup_exchange_source_name_domain("EUREX_COAX_INFORMATION", "allOpsAnalysts", EnsTestStatic.en_domain_dic)
        self.assertEqual("ashish.patil@smartstreamrdu.com,amzad.khan@smartstreamrdu.com,renson.alva@smartstreamrdu.com", function_result)

    def test_unexpected_source_name_to_return_all_ops_analysts(self):
        function_result = lookup_exchange_source_name_domain("ABCD", "allOpsAnalysts", EnsTestStatic.en_domain_dic)
        self.assertEqual("UNKNOWN", function_result)

    def test_expected_source_name_to_return_unexpected_column(self):
        function_result = lookup_exchange_source_name_domain("EUREX_COAX_INFORMATION", "abcd", EnsTestStatic.en_domain_dic)
        self.assertEqual("UNKNOWN", function_result)

    def test_expected_source_name_to_return_exchange_group_name(self):
        function_result = lookup_exchange_source_name_domain("EUREX_COAX_INFORMATION", "exchangeGroupName", EnsTestStatic.en_domain_dic)
        self.assertEqual("EUREX", function_result)


class LookupNormalizedParentInstrumentTypeName(unittest.TestCase):
    def test_5601_instrumentTypeCode(self):
        function_result = lookup_normalized_parent_instrument_type_name("5601", EnsTestStatic.ins_type_domain)
        self.assertEqual("Option", function_result)

    def test_errorCode101_instrumentTypeCode(self):
        function_result = lookup_normalized_parent_instrument_type_name("errorCode101", EnsTestStatic.ins_type_domain)
        self.assertEqual("errorCode101", function_result)


class StringFieldValue(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = string_field_value(EnsTestStatic.en_record_with_all_fields, 'eventSubject')
        self.assertEqual('Tribune Publishing Company - Cash Settlement/Acceleration of\nExpirations\nOption Symbol: TPCO\nDate: 05/25/21',
                         function_result)

    def test_valid_ops_lock_value(self):
        function_result = string_field_value(EnsTestStatic.en_record_with_all_fields_and_ops_lock, 'eventSubject')
        self.assertEqual('RDU Lock test value for eventSubject', function_result)

    def test_missing_value(self):
        function_result = string_field_value(EnsTestStatic.en_record_with_all_fields_wo_valid_values_for_some_fields, 'eventSubject')
        self.assertEqual('', function_result)


class DomainNormalizedValue(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = domain_normalized_value(EnsTestStatic.en_record_with_all_fields, 'instrumentTypeCode', EnsTestStatic.ins_type_map)
        self.assertEqual('56', function_result)

    def test_valid_ops_lock_value(self):
        function_result = domain_normalized_value(EnsTestStatic.en_record_with_all_fields_and_ops_lock, 'instrumentTypeCode',
                                                  EnsTestStatic.ins_type_map)
        self.assertEqual('59', function_result)

    def test_value_not_mapped(self):
        function_result = domain_normalized_value(EnsTestStatic.en_record_with_all_fields_wo_valid_values_for_some_fields, 'instrumentTypeCode',
                                                  EnsTestStatic.ins_type_map)
        self.assertEqual('errorCode101', function_result)


class DomainFeedValue(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = domain_feed_value(EnsTestStatic.en_record_with_all_fields, "eventType")
        self.assertEqual("CASH SETTLEMENT/ACCELERATION OF EXPIRATIONS", function_result)

    def test_valid_imported_record(self):
        function_result = domain_feed_value(EnsTestStatic.en_manual_record_with_all_fields, "eventType")
        self.assertEqual("Acceleration Of Maturity/Expiration", function_result)

    def test_record_without_field_tag(self):
        function_result = domain_feed_value(EnsTestStatic.en_record_with_missing_event_type_and_ins_type_fields, "eventType")
        self.assertEqual("", function_result)


class StringHash(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = string_hash("this is a test value|vaue2|value3")
        self.assertEqual("3071563377", function_result)


class DateFieldValue(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = date_field_value(EnsTestStatic.en_record_with_all_fields, 'eventPublishDate')
        self.assertEqual('2021-05-25', function_result)

    def test_valid_ops_lock_value(self):
        function_result = date_field_value(EnsTestStatic.en_record_with_all_fields_and_ops_lock, 'eventPublishDate')
        self.assertEqual('2022-05-26', function_result)

    def test_missing_value(self):
        function_result = date_field_value(EnsTestStatic.en_record_with_all_fields_wo_valid_values_for_some_fields, 'eventPublishDate')
        self.assertEqual('9999-12-31', function_result)


class EffDateFieldValue(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = eff_date_field_value(EnsTestStatic.en_record_with_all_fields, 'eventEffectiveDate')
        self.assertEqual('2021-05-25', function_result)

    def test_valid_ops_lock_value(self):
        function_result = eff_date_field_value(EnsTestStatic.en_record_with_all_fields_and_ops_lock, 'eventEffectiveDate')
        self.assertEqual('2022-05-26', function_result)

    def test_missing_value(self):
        function_result = eff_date_field_value(EnsTestStatic.en_record_with_all_fields_wo_valid_values_for_some_fields, 'eventEffectiveDate')
        self.assertEqual(103, function_result)


class DateFeedFieldValue(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = date_feed_field_value(EnsTestStatic.en_record_with_all_fields, 'eventEffectiveDate')
        self.assertEqual('2021-05-25', function_result)

    def test_valid_ops_lock_value(self):
        function_result = date_feed_field_value(EnsTestStatic.en_record_with_all_fields_and_ops_lock, 'eventEffectiveDate')
        self.assertEqual('2021-05-25', function_result)

    def test_missing_value(self):
        function_result = date_feed_field_value(EnsTestStatic.en_record_with_all_fields_wo_valid_values_for_some_fields, 'eventEffectiveDate')
        self.assertEqual(103, function_result)


class StringFieldNoSpacesMax255Value(unittest.TestCase):

    def test_valid_feed_value(self):
        function_result = string_field_no_spaces_max_255_value(EnsTestStatic.en_record_with_all_fields_cmeg_globex, 'productName')
        self.assertEqual('Options_on_Lumber_Futures', function_result)

    def test_valid_ops_lock_value_longer_than_255(self):
        function_result = string_field_no_spaces_max_255_value(EnsTestStatic.en_manual_record_with_all_fields, 'productName')
        self.assertEqual('Tribune_Publishing_Company_-_Very_long_company_name_for_testing_purposes_which_needs_to_be_longer_than_255_characters_to_be'
                         '_truncated._Repeating_the_same:Tribune_Publishing_Company_-_Very_long_company_name_for_testing_purposes_which_needs_to_be_'
                         'longer_tha', function_result)


class StringFieldWithinNestedArrayNoSpacesMax255Value(unittest.TestCase):
    def test_valid_feed_value(self):
        function_result = string_field_within_nested_array_no_spaces_max_255_value(EnsTestStatic.en_record_with_all_fields_cmeg_globex,
                                                                                   'underlyings.nameLong', 0)
        self.assertEqual('Lumber_Futures', function_result)

    def test_valid_ops_lock_value_longer_than_255(self):
        function_result = string_field_within_nested_array_no_spaces_max_255_value(EnsTestStatic.en_manual_record_with_all_fields,
                                                                                   'underlyings.nameLong', 0)
        self.assertEqual('Tribune_Publishing_Company_-_Very_long_underlying_name_for_testing_purposes_which_needs_to_be_longer_than_255_characters_'
                         'to_be_truncated._Repeating_the_same:Tribune_Publishing_Company_-_Very_long_underlying_name_for_testing_purposes_which_needs'
                         '_to_be_long', function_result)


class UrlStringFieldValue(unittest.TestCase):
    def test_valid_feed_value_with_spaces(self):
        function_result = url_string_field_value(EnsTestStatic.en_record_with_all_fields_cmeg_globex, 'eventInitialUrl')
        self.assertEqual("https://www.cmegroup.com/notices/electronic-trading/2022/07/2022%200704.html#lfplf", function_result)

    def test_valid_ops_lock_value_with_spaces(self):
        function_result = url_string_field_value(EnsTestStatic.en_manual_record_with_all_fields, 'eventInitialUrl')
        self.assertEqual("https://infomemo.theocc.com/infom%20emos?number=48757", function_result)

    def test_valid_feed_value_without_spaces(self):
        function_result = url_string_field_value(EnsTestStatic.en_record_with_all_fields, 'eventInitialUrl')
        self.assertEqual("https://infomemo.theocc.com/infomemos?number=48757", function_result)


if __name__ == '__main__':
    unittest.main()
