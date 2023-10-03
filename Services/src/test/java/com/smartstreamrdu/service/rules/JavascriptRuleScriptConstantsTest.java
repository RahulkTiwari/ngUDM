/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	JavascriptRuleScriptConstantsTest.java
 * Author:	S Padgaonakar
 * Date:	11-09-2018
 *
 *******************************************************************
 */


package com.smartstreamrdu.service.rules;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author S Padgaonakar
 *
 */
public class JavascriptRuleScriptConstantsTest {
	private static final String VAR_KEYS_ARGUMENTS_0 = "var keys = arguments[0];";

	@Test
	public void test() {

		Assert.assertEquals(JavascriptRuleScriptConstants.DATA_ATTRIBUTE_LOOKUP_FUNCTION_SCRIPT,
				"function getDataAttributeValueFunction() {var keys = arguments[0];return functions.getDataAttributeValue( rule, dataContainers,fieldConfig, keys)}var getDataAttributeValue = function () {return getDataAttributeValueFunction.apply(this, arguments);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.DOMAIN_ATTRIBUTE_LOOKUP_FUNCTION_SCRIPT,
				""
						+""
						+"function getDomainDataAttributeValueFunction() {"
						+"var keys = arguments[0];"
						+"var keys1= arguments[1];"
						+"if(keys1 == undefined){"
						+"return functions.getDomainDataAttributeValue(rule,dataContainers,fieldConfig,keys,null);"
						+"}else{"
						+"return functions.getDomainDataAttributeValue(rule,dataContainers,fieldConfig,keys,keys1);"
						+"}"
						+"}"
						+"var getDomainDataAttributeValue = function () {"
						+"return getDomainDataAttributeValueFunction.apply(this, arguments);"
						+"}");
		Assert.assertEquals(JavascriptRuleScriptConstants.DOMAIN_LOOKUP_CUSTOM_FUNCTION_SCRIPT,
				"var DomainType = Java.type('com.smartstreamrdu.domain.DomainType');function domainLookupFunction() {for(i=0; i < arguments.length; i++) {var data = functions.domainLookup(dataSource, rule, record, arguments[i]);if (data != null && (functions.isNormalizedValueAavailable(dataSource, rule, data, arguments[i]) || i+1 == arguments.length)) {return data;}}}var domainLookup = function () {return domainLookupFunction.apply(this, arguments);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.FEEDVALUE_CUSTOM_FUCTION_SCRIPT,
				"function feedValue() {var keys = arguments[0];var defaultValue = arguments[1];if (defaultValue == undefined) {return functions.feedValue(rule, record, keys);} else {return functions.feedValue(rule, record, keys, defaultValue);}}var feedVal = function () {feedValue.apply(this, arguments);}"
						+ "");
		Assert.assertEquals( "function getIssuersFunction() {return functions.getIssuers(compositeContainer)}var getIssuers = function () {return getIssuersFunction.apply(this);}", 
				JavascriptRuleScriptConstants.GET_ISSUERS_FUNCTION_SCRIPT);
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_NORMALIZED_ATTRIBUTE_VALUE_SCRIPT,
				"function getNormalizedAttributeValueFunction() {var keys = arguments[0];return functions.getNormalizedAttributeValue(rule,dataContainers,fieldConfig,keys)}var getNormalizedAttributeValue = function () {return getNormalizedAttributeValueFunction.apply(this,arguments);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_RDU_ID_FUNCTION_SCRIPT,
				"function getRduIdFunction() {return functions.getRduId(compositeContainer)}var  getRduId = function () {return getRduIdFunction.apply(this);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_SECURITIES_FUNCTION_SCRIPT,
				"function getSecuritiesFunction() {return functions.getSecurities(compositeContainer)}var getSecurities = function () {return getSecuritiesFunction.apply(this);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_UNDERLYING_INSTRUMENT_FUNCTION_SCRIPT,
				"function getUnderlyingInstrumentFunction() {return functions.getUnderlyingInstrument(compositeContainer)}var  getUnderlyingInstrument = function () {return getUnderlyingInstrumentFunction.apply(this);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_UNDERLYING_SECURITIES_FUNCTION_SCRIPT,
				"function getUnderlyingSecuritiesFunction() {return functions.getUnderlyingSecurities(compositeContainer)}var getUnderlyingSecurities = function () {return getUnderlyingSecuritiesFunction.apply(this);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_UNDERLYINGS_FUNCTION_SCRIPT,"function getUnderlyingsFunction() {return functions.getUnderlyings(compositeContainer,parentChildMap,distributionService,profile)}var getUnderlyings = function () {return getUnderlyingsFunction.apply(this);}"
				+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.LOOKUP_BY_CODE_FUNCTION_SCRIPT,
				"function lookupByCodeFunction() {var collectionName = arguments[0];var normalizedValueMap = arguments[1];var returnColName = arguments[2];return functions.lookupByCode(rule,collectionName,normalizedValueMap,returnColName);}var lookupByCode = function () {return lookupByCodeFunction.apply(this,arguments);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.LOOKUP_BY_NAME_FUNCTION_SCRIPT,
				"function lookupByNameFunction() {var collectionName = arguments[0];var normalizedValueMap = arguments[1];var returnColName = arguments[2];return functions.lookupByName(rule,collectionName,sourceValueMap,returnColName);}var lookupByName = function () {return lookupByNameFunction.apply(this,arguments);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.NORMALIZED_DOMAIN_LOOKUP_FUNCTION_SCRIPT,
				"function normalizedDomainLookupFunction() {for(i=0; i < arguments.length; i++) {var data = functions.normalizedDomainLookup(dataSource, rule, dataContainer, arguments[i]);if (data != null) {return data;}}}var normalizedDomainLookup = function () {return normalizedDomainLookupFunction.apply(this, arguments);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.NORMALIZED_LOOKUP_FUNCTION_SCRIPT,
				"function normalizedLookupFunction() {var keys = arguments[0];return functions.normalizedValue(dataSource, rule, dataContainer, keys)}var normalizedValue = function () {return normalizedLookupFunction.apply(this, arguments);}"
						+ "");
		Assert.assertEquals(JavascriptRuleScriptConstants.SLICE,
				"function sliceFunction() {var attributeName = arguments[0];var startIndex = arguments[1];var endIndex = arguments[2];return functions.slice(rule,dataContainers,fieldConfig,attributeName,startIndex,endIndex);}var slice = function () {return sliceFunction.apply(this,arguments);}"
						+ "");
		Assert.assertEquals(
				"var Values = Java.type('com.smartstreamrdu.service.rules.ValueWrapper');", JavascriptRuleScriptConstants.VALUES_CLASS);
		
		Assert.assertEquals("function feedValueWithDefaultDateFallback() {var keys = arguments[0];var regex = arguments[1];var pattern = arguments[2];var defaultValue = arguments[3];if (defaultValue == undefined) {return functions.feedValueWithDefaultDateFallback(rule, record, keys);} else if (regex != undefined) {return functions.feedValueWithDefaultDateFallback(rule, record, keys, regex, pattern, defaultValue);}else {return functions.feedValueWithDefaultDateFallback(rule, record, keys, defaultValue);}}var feedVal = function () {feedValueWithDefaultDateFallback.apply(this, arguments);}", 
				JavascriptRuleScriptConstants.FEEDVALUE_WITH_DEFAULT_DATE_FALLBACK
				);
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_SERIALIZABLE_DATA_ATTRIBUTE_VALUE_FUNCTION_SCRIPT, ""
				+""
				+"function getSerializableDataAttributeValueFunction() {"
				+ "var keys = arguments[0];"
				+"return functions.getSerializableDataAttributeValue(rule,dataContainers,fieldConfig,keys)"
				+""
				+ "}"
				+""
				+"var getSerializableDataAttributeValue = function () {"
				+"return getSerializableDataAttributeValueFunction.apply(this , arguments);"
				+"}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_SERIALIZABLE_DATA_ATTRIBUTE_VALUE_FOR_DATASOURCE_FUNCTION_SCRIPT, ""
				+""
				+"function getSerializableDataAttributeValueForDataSourceFunction() {"
				+ "var keys = arguments[0];"
				+ "var dataSource = arguments[1];"
				+"return functions.getSerializableDataAttributeValueForDataSource(rule,allDataContainers,leContainer,fieldConfig,keys,dataSource)"
				+""
				+ "}"
				+""
				+"var getSerializableDataAttributeValueForDataSource = function () {"
				+"return getSerializableDataAttributeValueForDataSourceFunction.apply(this , arguments);"
				+"}");
		
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_DIS_RECORD_VALUE, ""
                +""
                +"function getDisRecordValueFunction() {"
                +"var disField = arguments[0];"
                +"var source = arguments[1];"
                +"return functions.getDisRecordValue(disRecordContainer,fieldConfig,disField,source)"
                +"}"
                +""
                +"var getDisRecordValue = function () {"
                +"return getDisRecordValueFunction.apply(this,arguments);"
                +"}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_DIS_SOURCE_FOR_DIS_ATTRIBUTE, ""
				+ ""
				+ "function getDisSourceForDisAttributeFunction() {"
				+"var disField = arguments[0];"
				+"return functions.getDisSourceForDisAttribute(disRecordContainer,fieldConfig,disField)"
				+"}"
				+""
				+"var getDisSourceForDisAttribute = function () {"
				+"return getDisSourceForDisAttributeFunction.apply(this,arguments);"
				+"}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_NORMALIZED_VALUE_FOR_ATTRIBUTE_FROM_DIS_CONTAINER_FUNCTION, ""
				+ ""
				+ "function getNormalizedValueForAttributeFromDisContainerFunction(){"
				+ "var parentAttribute = arguments[0];"
				+ "return functions.getNormalizedValueForAttributeFromDisContainer(disRecordContainer,fieldConfig,parentAttribute,rule);"
				+ "}"
				+ ""
				+ "var getNormalizedValueForAttributeFromDisContainer = function() {"
				+ "return getNormalizedValueForAttributeFromDisContainerFunction.apply(this,arguments);"
				+"}"
				);
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_INSTRUMENT_STATUS_FUNCTION_SCRIPT, ""
				+""
				+"function getInstrumentStatusFunction(){"
				+"return functions.getInstrumentStatus(compositeContainer,disRecordContainer,fieldConfig,primaryDataSources);"
				+"}"
				+""
				+"var getInstrumentStatus = function(){"
				+"return getInstrumentStatusFunction.apply(this);"
				+"}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_PRIMARY_EXCHANGE_CODE_FUNCTION_SCRIPT, ""
				+""
				+"function getPrimaryExchangeCodeFunction(){"
				+"return functions.getPrimaryExchangeCode(compositeContainer,disRecordContainer,fieldConfig);"
				+"}"
				+""
				+"var getPrimaryExchangeCode = function(){"
				+"return getPrimaryExchangeCodeFunction.apply(this);"
				+"}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.FIRST_VALUE,"function firstValueFunction(){" + 
			"var input = arguments[0];" +
			"return functions.firstValue(input);" + 
			"};" +
			"var firstValue = function() {" + 
			"return firstValueFunction.apply(this,arguments);" + 
			"};");		
		
		Assert.assertEquals(JavascriptRuleScriptConstants.INACTIVATION_BASED_ON_SECURITY_FUNCTION,""
				+ ""
				+ "function inactivationBasedOnSecurityFunction() {"
				+ "var noOfDays = arguments[0];"
				+ "var dataSourceValue = arguments[1];"
				+ "if (dataSourceValue == undefined) {"
				+ "return functions.inactivationBasedOnSecurity(noOfDays,null,dataContainer);"
                + "} else {"
                + "return functions.inactivationBasedOnSecurity(noOfDays,dataSourceValue,dataContainer);"
				+ "}"
				+ "}"
				+ "var inactivationBasedOnSecurity = function () {"
				+ "return inactivationBasedOnSecurityFunction.apply(this, arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.INACTIVATION_BASED_ON_ATTRIBUTE_FUNCTION, 
				"function inactivateBasedOnAttributeFunction() {"
				+ "var noOfDays = arguments[0];"
				+ "var attributeName = arguments[1];"
				+ "var value = functions.inactivateBasedOnAttribute(noOfDays,attributeName,dataContainer);"
				+ "return value;"
				+"}"
				+ ""
				+ "var inactivateBasedOnAttribute = function () {"
				+ "return inactivateBasedOnAttributeFunction.apply(this, arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_EARLIEST, ""
				+ "function getEarliestFunction() {"
				+ "var o1 = arguments[0];"
				+ "var o2 = arguments[1];"
				+ "var value = functions.getEarliest(o1,o2);"
				+ "return value;"
				+"}"
				+ ""
				+ "var getEarliest = function () {"
				+ "return getEarliestFunction.apply(this, arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_NESTED_ARRAY_FUNCTION_SCRIPT, "function getNestedArrayFunction() {"
				+ "var attribute = arguments[0];"
				+"return functions.getNestedArray(compositeContainer, attribute)"
				+"}"
				+""
				+"var getNestedArray = function () {"
				+"return getNestedArrayFunction.apply(this, arguments);"
				+"}");
		
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_RAW_VALUE_USING_JSON_PATH, ""
				+""
				+"function getRawValueUsingJsonPathFunction() {"
				+ "var jsonPath = arguments[0];"
				+ "var dataSource = arguments[1];"
				+"return functions.getRawValueUsingJsonPath(compositeContainer,jsonPath,dataSource)"
				+""
				+ "}"
				+""
				+"var getRawValueUsingJsonPath = function () {"
				+"return getRawValueUsingJsonPathFunction.apply(this , arguments);"
				+"}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_FROM_CODE_MAP_FUNCTION, ""
				+""
				+"function getFromCodeForMapFunction() {"
				+"return loaderfunctions.getFromCodeForMap(arguments[0]);"
				+"}"
				+""
				+"var getFromCodeForMap = function () {"
				+"return getFromCodeForMapFunction.apply(this,arguments);"
				+"}");
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_TAKE_AND_MAP_RESULT,  ""
				+""
				+"function getTakeAndMapResultFunction(){"
				+"return functions.getTakeAndMapResult(disField,compositeContainer,fieldConfig);"
				+"}"
				+""
				+"var getTakeAndMapResult = function(){"
				+"return getTakeAndMapResultFunction.apply(this);"
				+"}");

		Assert.assertEquals(JavascriptRuleScriptConstants.GET_DATE_BASED_ON_OFFSET, ""
				+""
				+"function getDateBasedOnOffsetFunction() {"
				+ "var attributeOffset = arguments[0];"
				+ "var dataSource = arguments[1];"
				+"return functions.getDateBasedOnOffset(dataContainers,attributeOffset,dataSource)"
				+""
				+ "}"
				+""
				+"var getDateBasedOnOffset = function () {"
				+"return getDateBasedOnOffsetFunction.apply(this , arguments);"
				+"}");

		Assert.assertEquals(JavascriptRuleScriptConstants.CREATE_DIS_RESULT, ""
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
				+"}");

		Assert.assertEquals(JavascriptRuleScriptConstants.GET_ERROR_CODE_FUNCTION, ""
				+""
				+"function getErrorCodeFunction() {"
				+ VAR_KEYS_ARGUMENTS_0
				+"return functions.getErrorCode(rule,dataContainers,fieldConfig,keys)"
				+""
				+ "}"
				+""
				+"var getErrorCode = function () {"
				+"return getErrorCodeFunction.apply(this , arguments);"
				+"}");

		Assert.assertEquals(JavascriptRuleScriptConstants.GET_FROM_CODE_MAP_FUNCTION, ""
				+""
				+"function getFromCodeForMapFunction() {"
				+"return loaderfunctions.getFromCodeForMap(arguments[0]);"
				+"}"
				+""
				+"var getFromCodeForMap = function () {"
				+"return getFromCodeForMapFunction.apply(this,arguments);"
				+"}");

		Assert.assertEquals(JavascriptRuleScriptConstants.GET_ERROR_CODE_FROM_MAP,""
				+""
				+"function getErrorCodeFromMapFunction() {"
				+"return loaderfunctions.getErrorCodeFromMap(arguments[0]);"
				+"}"
				+""
				+"var getErrorCodeFromMap = function () {"
				+"return getErrorCodeFromMapFunction.apply(this,arguments);"
				+"}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.DOMAIN_VALUE,""
				+"function domainValueFunction(){"
				+ "return dataContainerEnrichmentFunctions.getDomainValue(arguments[0]);"
				+ "}"
				+""
				+"var domainValue = function(){"
				+"return domainValueFunction.apply(this,arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_NORMALIZED_DOMAIN_VALUE_ENRICHMENT_FUNCTION,""
				+ "function getNormalizedDomainValueFunction(){"
				+ "return dataContainerEnrichmentFunctions.getNormalizedDomainValue(arguments[0]);"
				+ "} "
				+ ""
				+ "var getNormalizedDomainValue = function(){"
				+ "return getNormalizedDomainValueFunction.apply(this,arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_ATTRIBUTE_VALUE_FROM_DATA_CONTAINER,""
				+ "function getAttributeValueFromDcFunction(){"
				+ " return dataContainerEnrichmentFunctions.getAttributeValueFromDataContainer(dataContainer,arguments[0]);"
				+ "} "
				+ ""
				+ "var getAttributeValueFromDataContainer = function(){"
				+ "return getAttributeValueFromDcFunction.apply(this,arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_NORMALIZED_ATTRIBUTE_VALUE_FROM_DATA_CONTAINER,""
				+ "function getNormalizedAttrValueFromDc(){"
				+ "return dataContainerEnrichmentFunctions.getNormalizedAttributeValueFromDataContainer(dataContainer,arguments[0]);"
				+ "} "
				+ ""
				+ "var getNormalizedAttributeValueFromDataContainer = function(){"
				+ "return getNormalizedAttrValueFromDc.apply(this,arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.FORMAT_DATE,""
				+ "function formatDateFunction(){"
				+ "return dataContainerEnrichmentFunctions.formatDate(arguments[0],arguments[1]);"
				+ "} "
				+ ""
				+ "var formatDate = function(){"
				+ "return formatDateFunction.apply(this,arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_CURRENT_DATE_UTC,""
				+ "function getCurrentDateInUtcFunction(){"
				+ "return dataContainerEnrichmentFunctions.getCurrentDateInUtc();"
				+ "} "
				+ ""
				+ "var getCurrentDateInUTC = function(){"
				+ "return getCurrentDateInUtcFunction.apply(this);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.ENRICH_EVENT_FILE_NAMES,""
				+ "function enrichEventFileNamesFunction(){"
				+ "return dataContainerEnrichmentFunctions.enrichEventFileNames(dataContainer,dataAttribute,arguments[0],arguments[1]);"
				+ "} "
				+ ""
				+ "var enrichEventFileNames=function(){"
				+ "return enrichEventFileNamesFunction.apply(this,arguments);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_NESTED_ARRAY_COUNTER,""
				+ "function getNestedArrayCounterFunction(){"
				+ "return dataContainerEnrichmentFunctions.getNestedArrayCounter(dataContainer,dataAttribute);"
				+ "} "
				+ ""
				+ "var getNestedArrayCounter =function(){"
				+ "return getNestedArrayCounterFunction.apply(this);"
				+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.GET_ERROR_CODE_FOR_SOURCE,""
				+ "function getErrorCodeForSourceFunction(){ "
				+ "var key=arguments[0];"
				+ "var source=arguments[1];"
				+ "return functions.getErrorCodeForSource(rule,dataContainers,fieldConfig,key,source);"
				+ ""
				+ "} var getErrorCodeForSource =function(){ "
				+ "return getErrorCodeForSourceFunction.apply(this,arguments);"
				+ "}");
		
		
		Assert.assertEquals(JavascriptRuleScriptConstants.CONVERT_JSON_ARRAY_TO_LIST_OF_STRINGS,""
						+ "function convertJsonArrayToListOfStringsFunction() {"
								+"var jsonArray = arguments[0];"
								+ "return functions.convertJsonArrayToListOfStrings(jsonArray);"
						+ "}"
						+ ""
						+ "var convertJsonArrayToListOfStrings = function () {"
							+ "return convertJsonArrayToListOfStringsFunction.apply(this, arguments);"
						+ "}");
		
		Assert.assertEquals(JavascriptRuleScriptConstants.INITIATE_ARRAY_MERGE_FUNCTION,""
				+ "function initiateArrayMergeFunction() {"
				+"var jsonArray = arguments[0];"
				+ "var mergeAttribute = arguments[1];"
				+ "var resultKey = arguments[2];"
				+ "return jsonfunctions.initiateArrayMerge(jsonArray, mergeAttribute, resultKey);"
				+ "}"
				+ ""
				+ "var initiateArrayMerge = function () {"
					+ "return initiateArrayMergeFunction.apply(this, arguments);"
				+ "}");
	}
}

