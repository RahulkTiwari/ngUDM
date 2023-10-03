/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedVsDbRawdataMergeService.java
 * Author:	GMathur
 * Date:	29-Oct-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import org.json.simple.JSONObject;

import com.smartstreamrdu.domain.Record;

/**
 * Rawdata merge service that will compare feed rawdata to DB rawdata and merge both based on the type of JSON element.
 */
public interface FeedVsDbRawdataMergeService {

	/**
	 * This will compare feed & db rawdata and will merge them based on the JSON key and type of JSON element.
	 * It will iterate over all fields of input JSON object and compare each JSON field with corresponding db JSON object.
	 * If any field is of type JSONArray/JSONObject then it will again iterate over all fields inside that input field structure and repeat the same process of comparison (i.e. feed vs db).
	 * 
	 *  This will be a recursive process till all fields get merged.
	 *  
	 * @param feedRecord
	 * @param dbRecord
	 * @return
	 */
	public Record merge(Record feedRecord, JSONObject dbRecord);
	
	/**
	 * This will generate a unique key for input JSONObject.
	 * Key generation logic will vary for each vendor(feed) and implementation for same will be written in overridden methods.
	 * 
	 * @param arrayTag
	 * @param arrayEntry
	 * @return
	 */
	public String getArrayObjectKey(String arrayTag, JSONObject arrayEntry);
}
