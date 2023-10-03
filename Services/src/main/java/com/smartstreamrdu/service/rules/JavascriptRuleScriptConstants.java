/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	JavascriptRuleScriptConstants.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

public class JavascriptRuleScriptConstants {
	
	/**
	 * 
	 */
	private static final String VAR_DATA_SOURCE = "var dataSource = arguments[1];";

	private static final String RETURN_VALUE = "return value;";

	private static final String RETURN_FUNCTIONS_GET_DOMAIN_DATA_ATTRIBUTE_VALUE_RULE_DATA_CONTAINERS_FIELD_CONFIG_KEYS_KEYS1 = "return functions.getDomainDataAttributeValue(rule,dataContainers,fieldConfig,keys,keys1);";

	private static final String RETURN_FUNCTIONS_GET_DOMAIN_DATA_ATTRIBUTE_VALUE_RULE_DATA_CONTAINERS_FIELD_CONFIG_KEYS_NULL = "return functions.getDomainDataAttributeValue(rule,dataContainers,fieldConfig,keys,null);";

	private static final String VAR_FEED_VAL_FUNCTION = "var feedVal = function () {";

	private static final String IF_DEFAULT_VALUE_UNDEFINED = "if (defaultValue == undefined) {";

	private static final String VAR_KEYS_ARGUMENTS_0 = "var keys = arguments[0];";
	
	private static final String IF_DATA_SOURCE_UNDEFINED = "if (dataSourceValue == undefined) {";

	private JavascriptRuleScriptConstants() {
		
	}
	
	public static final String VALUES_CLASS = ""
			+ ""
			+ "var Values = Java.type('com.smartstreamrdu.service.rules.ValueWrapper');"
			+ "";
	
	public static final String FEEDVALUE_CUSTOM_FUCTION_SCRIPT = ""
			+ "function feedValue() {"
					+ VAR_KEYS_ARGUMENTS_0
					+ "var defaultValue = arguments[1];"
					+ IF_DEFAULT_VALUE_UNDEFINED
					+ 	"return functions.feedValue(rule, record, keys);"
					+ "} else {"
						+ "return functions.feedValue(rule, record, keys, defaultValue);"
					+ "}"
			+ "}"
				
			+ VAR_FEED_VAL_FUNCTION
				+ "feedValue.apply(this, arguments);"
			+ "}";
	
	
	public static final String DOMAIN_LOOKUP_CUSTOM_FUNCTION_SCRIPT = ""
			+ ""
			+ "var DomainType = Java.type('com.smartstreamrdu.domain.DomainType');"
			+ "function domainLookupFunction() {"
			+ "for(i=0; i < arguments.length; i++) {"
			+ "var data = functions.domainLookup(dataSource, rule, record, arguments[i]);"
			+ "if (data != null && (functions.isNormalizedValueAavailable(dataSource, rule, data, arguments[i]) || i+1 == arguments.length)) {"
			+ "return data;"
			+ "}"
			+ "}"
			+ "}"
			+ ""
			+ "var domainLookup = function () {"
			+ "return domainLookupFunction.apply(this, arguments);"
			+ "}";
	public static final String NORMALIZED_LOOKUP_FUNCTION_SCRIPT = ""
			+ ""
			+ "function normalizedLookupFunction() {"
			+ VAR_KEYS_ARGUMENTS_0
			+ "return functions.normalizedValue(dataSource, rule, dataContainer, keys)"
			+ "}"
			+ ""
			+ "var normalizedValue = function () {"
			+ "return normalizedLookupFunction.apply(this, arguments);"
			+ "}";
	
	public static final String NORMALIZED_DOMAIN_LOOKUP_FUNCTION_SCRIPT = ""
			+ ""
			+ "function normalizedDomainLookupFunction() {"
			+ "for(i=0; i < arguments.length; i++) {"
			+ "var data = functions.normalizedDomainLookup(dataSource, rule, dataContainer, arguments[i]);"
			+ "if (data != null) {"
			+ "return data;"
			+ "}"
			+ "}"
			+ "}"
			+ ""
			+ "var normalizedDomainLookup = function () {"
			+ "return normalizedDomainLookupFunction.apply(this, arguments);"
			+ "}";
	
	public static final String DATA_ATTRIBUTE_LOOKUP_FUNCTION_SCRIPT = ""
			+ ""
			+ "function getDataAttributeValueFunction() {"
			+ VAR_KEYS_ARGUMENTS_0
			+ "return functions.getDataAttributeValue( rule, dataContainers,fieldConfig, keys)"
			+ "}"
			+ ""
			+ "var getDataAttributeValue = function () {"
			+ "return getDataAttributeValueFunction.apply(this, arguments);"
			+ "}";
	
	public static final String DATA_ATTRIBUTE_FOR_DATASOURCE_LOOKUP_FUNCTION_SCRIPT = ""
			+ ""
			+ "function getDataAttributeValueForDatasourceFunction() {"
			+ VAR_KEYS_ARGUMENTS_0
			+ VAR_DATA_SOURCE
			+ "return functions.getDataAttributeValueForDatasource( dataContainers, fieldConfig, keys, dataSource)"
			+ "}"
			+ ""
			+ "var getDataAttributeValueForDatasource = function () {"
			+ "return getDataAttributeValueForDatasourceFunction.apply(this, arguments);"
			+ "}";
	
	public static final String DOMAIN_ATTRIBUTE_LOOKUP_FUNCTION_SCRIPT=""
			+""
			+"function getDomainDataAttributeValueFunction() {"
			+VAR_KEYS_ARGUMENTS_0
			+"var keys1= arguments[1];"
			+"if(keys1 == undefined){"
			+RETURN_FUNCTIONS_GET_DOMAIN_DATA_ATTRIBUTE_VALUE_RULE_DATA_CONTAINERS_FIELD_CONFIG_KEYS_NULL
			+"}else{"
			+RETURN_FUNCTIONS_GET_DOMAIN_DATA_ATTRIBUTE_VALUE_RULE_DATA_CONTAINERS_FIELD_CONFIG_KEYS_KEYS1
			+"}"
			+"}"
			+"var getDomainDataAttributeValue = function () {"
			+"return getDomainDataAttributeValueFunction.apply(this, arguments);"
			+"}";
			
	public static final String GET_SECURITIES_FUNCTION_SCRIPT=""
			+""
			+"function getSecuritiesFunction() {"
			+"return functions.getSecurities(compositeContainer)"
			+"}"
			+""
			+"var getSecurities = function () {"
			+"return getSecuritiesFunction.apply(this);"
			+"}";
			
	public static final String GET_UNDERLYINGS_FUNCTION_SCRIPT=""
			+""
			+"function getUnderlyingsFunction() {"
			+"return functions.getUnderlyings(compositeContainer,parentChildMap,distributionService,profile)"
			+"}"
			+""
			+"var getUnderlyings = function () {"
			+"return getUnderlyingsFunction.apply(this);"
			+"}";
	public static final String GET_UNDERLYING_INSTRUMENT_FUNCTION_SCRIPT=""
			+""
			+"function getUnderlyingInstrumentFunction() {"
			+"return functions.getUnderlyingInstrument(compositeContainer)"
			+"}"
			+""
			+"var  getUnderlyingInstrument = function () {"
			+"return getUnderlyingInstrumentFunction.apply(this);"
			+"}";
	public static final String GET_UNDERLYING_SECURITIES_FUNCTION_SCRIPT=""
			+""
			+"function getUnderlyingSecuritiesFunction() {"
			+"return functions.getUnderlyingSecurities(compositeContainer)"
			+"}"
			+""
			+"var getUnderlyingSecurities = function () {"
			+"return getUnderlyingSecuritiesFunction.apply(this);"
			+"}";
	public static final String GET_RDU_ID_FUNCTION_SCRIPT=""
			+""
			+"function getRduIdFunction() {"
			+"return functions.getRduId(compositeContainer)"
			+"}"
			+""
			+"var  getRduId = function () {"
			+"return getRduIdFunction.apply(this);"
			+"}";
	public static final String GET_ISSUERS_FUNCTION_SCRIPT=""
			+""
			+"function getIssuersFunction() {"
			+"return functions.getIssuers(compositeContainer)"
			+"}"
			+""
			+"var getIssuers = function () {"
			+"return getIssuersFunction.apply(this);"
			+"}";
	
	
	public static final String GET_RATINGS_FUNCTION_SCRIPT=""
			+""
			+"function getRatingsFunction() {"
			+"return functions.getRatings(compositeContainer)"
			+"}"
			+""
			+"var getRatings = function () {"
			+"return getRatingsFunction.apply(this);"
			+"}";

	public static final String GET_NESTED_ARRAY_FUNCTION_SCRIPT=""
			+""
			+"function getNestedArrayFunction() {"
			+ "var attribute = arguments[0];"
			+"return functions.getNestedArray(compositeContainer, attribute)"
			+"}"
			+""
			+"var getNestedArray = function () {"
			+"return getNestedArrayFunction.apply(this, arguments);"
			+"}";

	public static final String GET_NORMALIZED_ATTRIBUTE_VALUE_SCRIPT=""
			+""
			+"function getNormalizedAttributeValueFunction() {"
			+VAR_KEYS_ARGUMENTS_0
			+"return functions.getNormalizedAttributeValue(rule,dataContainers,fieldConfig,keys)"
			+"}"
			+""
			+"var getNormalizedAttributeValue = function () {"
			+"return getNormalizedAttributeValueFunction.apply(this,arguments);"
			+"}";
	
	public static final String LOOKUP_BY_CODE_FUNCTION_SCRIPT=""
			+""
			+"function lookupByCodeFunction() {"
			+"var collectionName = arguments[0];"
			+"var normalizedValueMap = arguments[1];"
			+"var returnColName = arguments[2];"
			+"return functions.lookupByCode(rule,collectionName,normalizedValueMap,returnColName);"
			+"}"
			+""
			+"var lookupByCode = function () {"
			+"return lookupByCodeFunction.apply(this,arguments);"
			+"}";
	
	public static final String SLICE=""
			+""
			+"function sliceFunction() {"
			+"var attributeName = arguments[0];"
			+"var startIndex = arguments[1];"
			+"var endIndex = arguments[2];"
			+"return functions.slice(rule,dataContainers,fieldConfig,attributeName,startIndex,endIndex);"
			+"}"
			+""
			+"var slice = function () {"
			+"return sliceFunction.apply(this,arguments);"
			+"}";
	public static final String LOOKUP_BY_NAME_FUNCTION_SCRIPT=""
			+""
			+"function lookupByNameFunction() {"
			+"var collectionName = arguments[0];"
			+"var normalizedValueMap = arguments[1];"
			+"var returnColName = arguments[2];"
			+"return functions.lookupByName(rule,collectionName,sourceValueMap,returnColName);"
			+"}"
			+""
			+"var lookupByName = function () {"
			+"return lookupByNameFunction.apply(this,arguments);"
			+"}";
	public static final String GET_FROM_CODE_MAP=""
			+""
			+"function getFromCodeForMapFunction() {"
			+"return functions.getFromCodeForMap(arguments[0]);"
			+"}"
			+""
			+"var getFromCodeForMap = function () {"
			+"return getFromCodeForMapFunction.apply(this,arguments);"
			+"}";
	
	public static final String RAW_FEED_VALUE = ""
			+ "function rawFeedValue() {"
					+ VAR_KEYS_ARGUMENTS_0
					+ "var defaultValue = arguments[1];"
					+ IF_DEFAULT_VALUE_UNDEFINED
					+ 	"return functions.rawFeedValue(rule, record, keys);"
					+ "} else {"
						+ "return functions.rawFeedValue(rule, record, keys, defaultValue);"
					+ "}"
			+ "}"
				
			+ VAR_FEED_VAL_FUNCTION
				+ "rawFeedValue.apply(this, arguments);"
			+ "}";
	
	public static final String GET_DEFAULT_OBJECT=""
			+"function getDefaultObjectFunction() {"
			+"var source = arguments[0];"
			+"var value = arguments[1];"
			+"var dataType = arguments[2];"
			+"return functions.getDefaultObject(source,value,dataType);"
			+"}"
			+""
			+"var getDefaultObject = function () {"
			+"return getDefaultObjectFunction.apply(this,arguments);"
			+"}";
	
	public static final String GET_LEGAL_JURISDICTION_COUNTRY_CODE=""
			+""
			+"function getLegalJurisdictionCountryCodeFunction() {"
			+VAR_KEYS_ARGUMENTS_0
			+"return functions.getLegalJurisdictionCountryCode(compositeContainer,fieldConfig,keys)"
			+"}"
			+""
			+"var getLegalJurisdictionCountryCode = function () {"
			+"return getLegalJurisdictionCountryCodeFunction.apply(this,arguments);"
			+"}";

	public static final String FEEDVALUE_WITH_DEFAULT_DATE_FALLBACK = "" 
			+ "function feedValueWithDefaultDateFallback() {"
				+ VAR_KEYS_ARGUMENTS_0 
				+ "var regex = arguments[1];" 
				+ "var pattern = arguments[2];"
				+ "var defaultValue = arguments[3];"
				+ IF_DEFAULT_VALUE_UNDEFINED 
					+ "return functions.feedValueWithDefaultDateFallback(rule, record, keys);"
					+ "} "
				+ "else if (regex != undefined) {"
						+ "return functions.feedValueWithDefaultDateFallback(rule, record, keys, regex, pattern, defaultValue);" 
					+ "}" 
				+ "else {"
						+ "return functions.feedValueWithDefaultDateFallback(rule, record, keys, defaultValue);" 
				+ "}" 
			+ "}"

			+ VAR_FEED_VAL_FUNCTION 
				+ "feedValueWithDefaultDateFallback.apply(this, arguments);" 
			+ "}";
	
	
	public static final String NORMALIZED_DOMAIN_VALUE_FUNCTION_SCRIPT = ""
			+ ""
			+ "function normalizedDomainValueFunction() {"
			+ "var normalizedValue = arguments[0];"
			+ "var value = functions.normalizedDomainValue(rule,normalizedValue);"
			+ RETURN_VALUE
			+"}"
			+ ""
			+ "var normalizedDomainValue = function () {"
			+ "return normalizedDomainValueFunction.apply(this, arguments);"
			+ "}";
	
	public static final String GET_LEI_ISSUERS_FUNCTION_SCRIPT=""
			+""
			+"function getLeiIssuersFunction() {"
			+"return functions.getLeiIssuers(compositeContainer)"
			+"}"
			+""
			+"var getLeiIssuers = function () {"
			+"return getLeiIssuersFunction.apply(this);"
			+"}";
	
	
	public static final String GET_SERIALIZABLE_DATA_ATTRIBUTE_VALUE_FUNCTION_SCRIPT =""
			+""
			+"function getSerializableDataAttributeValueFunction() {"
			+ VAR_KEYS_ARGUMENTS_0
			+"return functions.getSerializableDataAttributeValue(rule,dataContainers,fieldConfig,keys)"
			+""
			+ "}"
			+""
			+"var getSerializableDataAttributeValue = function () {"
			+"return getSerializableDataAttributeValueFunction.apply(this , arguments);"
			+"}";
	
	public static final String GET_SERIALIZABLE_DATA_ATTRIBUTE_VALUE_FOR_DATASOURCE_FUNCTION_SCRIPT =""
			+""
			+"function getSerializableDataAttributeValueForDataSourceFunction() {"
			+ VAR_KEYS_ARGUMENTS_0
			+ VAR_DATA_SOURCE
			+"return functions.getSerializableDataAttributeValueForDataSource(rule,allDataContainers,leContainer,fieldConfig,keys,dataSource)"
			+""
			+ "}"
			+""
			+"var getSerializableDataAttributeValueForDataSource = function () {"
			+"return getSerializableDataAttributeValueForDataSourceFunction.apply(this , arguments);"
			+"}";	
	public static final String GET_DIS_RECORD_VALUE=""
			+""
			+"function getDisRecordValueFunction() {"
			+"var disField = arguments[0];"
			+"var source = arguments[1];"
			+"return functions.getDisRecordValue(disRecordContainer,fieldConfig,disField,source)"
			+"}"
			+""
			+"var getDisRecordValue = function () {"
			+"return getDisRecordValueFunction.apply(this,arguments);"
			+"}";
	
	public static final String GET_DIS_SOURCE_FOR_DIS_ATTRIBUTE = ""
			+ ""
			+ "function getDisSourceForDisAttributeFunction() {"
			+"var disField = arguments[0];"
			+"return functions.getDisSourceForDisAttribute(disRecordContainer,fieldConfig,disField)"
			+"}"
			+""
			+"var getDisSourceForDisAttribute = function () {"
			+"return getDisSourceForDisAttributeFunction.apply(this,arguments);"
			+"}";
	
	public static final String GET_INSTRUMENT_STATUS_FUNCTION_SCRIPT= ""
			+""
			+"function getInstrumentStatusFunction(){"
			+"return functions.getInstrumentStatus(compositeContainer,disRecordContainer,fieldConfig,primaryDataSources);"
			+"}"
			+""
			+"var getInstrumentStatus = function(){"
			+"return getInstrumentStatusFunction.apply(this);"
			+"}";
	
	public static final String GET_NORMALIZED_VALUE_FOR_ATTRIBUTE_FROM_DIS_CONTAINER_FUNCTION = ""
			+ ""
			+ "function getNormalizedValueForAttributeFromDisContainerFunction(){"
			+ "var parentAttribute = arguments[0];"
			+ "return functions.getNormalizedValueForAttributeFromDisContainer(disRecordContainer,fieldConfig,parentAttribute,rule);"
			+ "}"
			+ ""
			+ "var getNormalizedValueForAttributeFromDisContainer = function() {"
			+ "return getNormalizedValueForAttributeFromDisContainerFunction.apply(this,arguments);"
			+"}";
	
	public static final String GET_PRIMARY_EXCHANGE_CODE_FUNCTION_SCRIPT=""
			+""
			+"function getPrimaryExchangeCodeFunction(){"
			+"return functions.getPrimaryExchangeCode(compositeContainer,disRecordContainer,fieldConfig);"
			+"}"
			+""
			+"var getPrimaryExchangeCode = function(){"
			+"return getPrimaryExchangeCodeFunction.apply(this);"
			+"}";
	
	public static final String CURRENT_INDEX_FUNCTION_SCRIPT = ""
			+ ""
			+ "function currentIndexFunction() {"
			+ "return index;"
			+ "}"
			+ ""
			+ "var currentIndex = function() {"
			+ "return currentIndexFunction.apply(this);"
			+ "}";
	
	public static final String FIRST_VALUE = "function firstValueFunction(){" + 
			"var input = arguments[0];"
			+ "return functions.firstValue(input);"
			+ "};"
			+ "var firstValue = function() {" 
			+ "return firstValueFunction.apply(this,arguments);" 
			+ "};";
	
	public static final String INACTIVATION_BASED_ON_ATTRIBUTE_FUNCTION = ""
			+ ""
			+ "function inactivateBasedOnAttributeFunction() {"
			+ "var noOfDays = arguments[0];"
			+ "var attributeName = arguments[1];"
			+ "var value = functions.inactivateBasedOnAttribute(noOfDays,attributeName,dataContainer);"
			+ RETURN_VALUE
			+"}"
			+ ""
			+ "var inactivateBasedOnAttribute = function () {"
			+ "return inactivateBasedOnAttributeFunction.apply(this, arguments);"
			+ "}";
	
	public static final String GET_EARLIEST = ""
			+ ""
			+ "function getEarliestFunction() {"
			+ "var o1 = arguments[0];"
			+ "var o2 = arguments[1];"
			+ "var value = functions.getEarliest(o1,o2);"
			+ RETURN_VALUE
			+"}"
			+ ""
			+ "var getEarliest = function () {"
			+ "return getEarliestFunction.apply(this, arguments);"
			+ "}";
	
	public static final String INACTIVATION_BASED_ON_SECURITY_FUNCTION = ""
			+ ""
			+ "function inactivationBasedOnSecurityFunction() {"
			+ "var noOfDays = arguments[0];"
			+ "var dataSourceValue = arguments[1];"
            + IF_DATA_SOURCE_UNDEFINED
            +   "return functions.inactivationBasedOnSecurity(noOfDays,null,dataContainer);"
            + "} else {"
                + "return functions.inactivationBasedOnSecurity(noOfDays,dataSourceValue,dataContainer);"
            + "}"
            + "}"
			+ "var inactivationBasedOnSecurity = function () {"
			+ "return inactivationBasedOnSecurityFunction.apply(this, arguments);"
			+ "}";

	public static final String UDM_ERRORCODE = ""
			+ ""
			+ "var UdmErrorCodes = Java.type('com.smartstreamrdu.domain.UdmErrorCodes');"
			+ "";
	
	public static final String GET_RAW_VALUE_USING_JSON_PATH =""
			+""
			+"function getRawValueUsingJsonPathFunction() {"
			+ "var jsonPath = arguments[0];"
			+ VAR_DATA_SOURCE
			+"return functions.getRawValueUsingJsonPath(compositeContainer,jsonPath,dataSource)"
			+""
			+ "}"
			+""
			+"var getRawValueUsingJsonPath = function () {"
			+"return getRawValueUsingJsonPathFunction.apply(this , arguments);"
			+"}";
	public static final String GET_DATE_BASED_ON_OFFSET =""
			+""
			+"function getDateBasedOnOffsetFunction() {"
			+ "var attributeOffset = arguments[0];"
			+ VAR_DATA_SOURCE
			+"return functions.getDateBasedOnOffset(dataContainers,attributeOffset,dataSource)"
			+""
			+ "}"
			+""
			+"var getDateBasedOnOffset = function () {"
			+"return getDateBasedOnOffsetFunction.apply(this , arguments);"
			+"}";
	
	public static final String GET_TAKE_AND_MAP_RESULT= ""
			+""
			+"function getTakeAndMapResultFunction(){"
			+"return functions.getTakeAndMapResult(disField,compositeContainer,fieldConfig);"
			+"}"
			+""
			+"var getTakeAndMapResult = function(){"
			+"return getTakeAndMapResultFunction.apply(this);"
			+"}";
	
	public static final String CREATE_DIS_RESULT=""
			+"function createDisResultFunction() {"
			+"var source = arguments[0];"
			+"var value = arguments[1];"
			+"var dataType = arguments[2];"
			+"var shouldReturnErrorCode = arguments[3];"
			+"var dateFormat = arguments[4];"
			+"return functions.createDisResult(source,value,dataType,shouldReturnErrorCode,dateFormat);"
			+"}"
			+""
			+"var createDisResult = function () {"
			+"return createDisResultFunction.apply(this,arguments);"
			+"}";
	
	public static final String GET_FROM_CODE_MAP_FUNCTION=""
			+""
			+"function getFromCodeForMapFunction() {"
			+"return loaderfunctions.getFromCodeForMap(arguments[0]);"
			+"}"
			+""
			+"var getFromCodeForMap = function () {"
			+"return getFromCodeForMapFunction.apply(this,arguments);"
			+"}";
	
	public static final String LOWER_CASE_CAMEL =""
			+""
			+"function convertToLowerCaseCamelFunction() {"
			+ "var inputString = arguments[0];"
			+"return functions.convertToLowerCaseCamel(inputString)"
			+""
			+ "}"
			+""
			+"var convertToLowerCaseCamel = function () {"
			+"return convertToLowerCaseCamelFunction.apply(this,arguments);"
			+"}";	
	
	public static final String GET_ERROR_CODE_FUNCTION =""
			+""
			+"function getErrorCodeFunction() {"
			+ VAR_KEYS_ARGUMENTS_0
			+"return functions.getErrorCode(rule,dataContainers,fieldConfig,keys)"
			+""
			+ "}"
			+""
			+"var getErrorCode = function () {"
			+"return getErrorCodeFunction.apply(this , arguments);"
			+"}";
	public static final String GET_ERROR_CODE_FOR_SOURCE=""
			+ "function getErrorCodeForSourceFunction(){ "
			+ "var key=arguments[0];"
			+ "var source=arguments[1];"
			+ "return functions.getErrorCodeForSource(rule,dataContainers,fieldConfig,key,source);"
			+ ""
			+ "} var getErrorCodeForSource =function(){ "
			+ "return getErrorCodeForSourceFunction.apply(this,arguments);"
			+ "}";
		
	public static final String GET_ERROR_CODE_FROM_MAP=""
			+""
			+"function getErrorCodeFromMapFunction() {"
			+"return loaderfunctions.getErrorCodeFromMap(arguments[0]);"
			+"}"
			+""
			+"var getErrorCodeFromMap = function () {"
			+"return getErrorCodeFromMapFunction.apply(this,arguments);"
			+"}";
	
	public static final String DOMAIN_VALUE=""
			+"function domainValueFunction(){"
			+ "return dataContainerEnrichmentFunctions.getDomainValue(arguments[0]);"
			+ "}"
			+""
			+"var domainValue = function(){"
			+"return domainValueFunction.apply(this,arguments);"
			+ "}";
	
	public static final String GET_NORMALIZED_DOMAIN_VALUE_ENRICHMENT_FUNCTION=""
			+ "function getNormalizedDomainValueFunction(){"
			+ "return dataContainerEnrichmentFunctions.getNormalizedDomainValue(arguments[0]);"
			+ "} "
			+ ""
			+ "var getNormalizedDomainValue = function(){"
			+ "return getNormalizedDomainValueFunction.apply(this,arguments);"
			+ "}";
	
	public static final String GET_ATTRIBUTE_VALUE_FROM_DATA_CONTAINER=""
			+ "function getAttributeValueFromDcFunction(){"
			+ " return dataContainerEnrichmentFunctions.getAttributeValueFromDataContainer(dataContainer,arguments[0]);"
			+ "} "
			+ ""
			+ "var getAttributeValueFromDataContainer = function(){"
			+ "return getAttributeValueFromDcFunction.apply(this,arguments);"
			+ "}";
	
	public static final String GET_NORMALIZED_ATTRIBUTE_VALUE_FROM_DATA_CONTAINER=""
			+ "function getNormalizedAttrValueFromDc(){"
			+ "return dataContainerEnrichmentFunctions.getNormalizedAttributeValueFromDataContainer(dataContainer,arguments[0]);"
			+ "} "
			+ ""
			+ "var getNormalizedAttributeValueFromDataContainer = function(){"
			+ "return getNormalizedAttrValueFromDc.apply(this,arguments);"
			+ "}";
	
	public static final String FORMAT_DATE=""
			+ "function formatDateFunction(){"
			+ "return dataContainerEnrichmentFunctions.formatDate(arguments[0],arguments[1]);"
			+ "} "
			+ ""
			+ "var formatDate = function(){"
			+ "return formatDateFunction.apply(this,arguments);"
			+ "}";
	
	public static final String GET_CURRENT_DATE_UTC=""
			+ "function getCurrentDateInUtcFunction(){"
			+ "return dataContainerEnrichmentFunctions.getCurrentDateInUtc();"
			+ "} "
			+ ""
			+ "var getCurrentDateInUTC = function(){"
			+ "return getCurrentDateInUtcFunction.apply(this);"
			+ "}";
	
	public static final String ENRICH_EVENT_FILE_NAMES=""
			+ "function enrichEventFileNamesFunction(){"
			+ "return dataContainerEnrichmentFunctions.enrichEventFileNames(dataContainer,dataAttribute,arguments[0],arguments[1]);"
			+ "} "
			+ ""
			+ "var enrichEventFileNames=function(){"
			+ "return enrichEventFileNamesFunction.apply(this,arguments);"
			+ "}";
	
	public static final String GET_NESTED_ARRAY_COUNTER=""
			+ "function getNestedArrayCounterFunction(){"
			+ "return dataContainerEnrichmentFunctions.getNestedArrayCounter(dataContainer,dataAttribute);"
			+ "} "
			+ ""
			+ "var getNestedArrayCounter =function(){"
			+ "return getNestedArrayCounterFunction.apply(this);"
			+ "}";
			
	
	public static final String CONVERT_JSON_ARRAY_TO_LIST_OF_STRINGS = ""
			+ "function convertJsonArrayToListOfStringsFunction() {"
					+"var jsonArray = arguments[0];"
					+ "return functions.convertJsonArrayToListOfStrings(jsonArray);"
			+ "}"
			+ ""
			+ "var convertJsonArrayToListOfStrings = function () {"
				+ "return convertJsonArrayToListOfStringsFunction.apply(this, arguments);"
			+ "}";
	
	public static final String INITIATE_ARRAY_MERGE_FUNCTION = ""
			+ "function initiateArrayMergeFunction() {"
					+"var jsonArray = arguments[0];"
					+ "var mergeAttribute = arguments[1];"
					+ "var resultKey = arguments[2];"
					+ "return jsonfunctions.initiateArrayMerge(jsonArray, mergeAttribute, resultKey);"
			+ "}"
			+ ""
			+ "var initiateArrayMerge = function () {"
				+ "return initiateArrayMergeFunction.apply(this, arguments);"
			+ "}";
}

