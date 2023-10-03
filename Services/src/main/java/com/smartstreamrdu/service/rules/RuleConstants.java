/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleConstants.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

public class RuleConstants {
	
	private RuleConstants(){
		
	}
	
	public static final String IS_INITIALIZED = "isInitialized"; 
	public static final String CONTEXT = "context";
	public static final String FEED_NAME = "feedName";
	public static final String RECORD = "record";
	public static final String FUNCTIONS = "functions";
	public static final String JSON_FUNCTION = "jsonfunctions";
	public static final String BOOTSTRAP = "bootstrap";
	public static final String FEED = "feed";
	public static final String RULE = "rule";
	public static final String DEPENDENT = "dependent";
	public static final String NESTED_ATTRIBUTES = "nestedAttributes";
	public static final String NON_NESTED_ATTRIBUTES = "nonNestedAttributes";
	public static final String CURRENT_INDEX = "index";
	public static final String SD_DATA_CONTAINER = "dataContainer";
	public static final String ERROR_CODE = "errorCode";
	public static final String ERROR_CODE_SEPARATOR = "-";
	
	
	public static final Object NULL_VALUE_CHECK_OBJECT = new ValueWrapper();

	//This constant is used to specify ruleFilter.ruleType.If ruleType is loaderRule then
	//it indicates that these rules will be executed on dataContainer postMerging.
	public static final String RULE_TYPE_LOADER_RULE = "loaderRule";

	//This constant is used to specify ruleFilter.feedName If feedName is Default then
	//its applicable for all dataSources.
	public static final String FEEDNAME_DEFAULT = "Default";
	
	//This constant is used to specify maxDate in ruleFunctions.
	public static final int MAX_DATE = 31;

	//This constant is used to specify maxMonth in ruleFunctions.
	public static final int MAX_MONTH = 12;

	//This constant is used to specify maxYear in ruleFunctions.
	public static final int MAX_YEAR = 9999;

}
