/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	AbstractDsosFeedVsDbRawdataMergeServiceImpl.java
 * Author:	Dedhia
 * Date:	Mar 13, 2023
 *
 *******************************************************************/
package com.smartstreamrdu.service.rawdata;

import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.smartstreamrdu.domain.Record;

/**
 * @author Dedhia
 *
 */
public abstract class AbstractDsosFeedVsDbRawdataMergeServiceImpl extends AbstractFeedVsDbRawData {

	private static final String SECURITY = "security";
	private static final String ID_OBJ_XREF_HIST = "id_obj_xref_hist";
	private static final String RDU_SECURITIES = "rduSecurities";
	protected static final String QUOTE_ID = "quote_id";
	protected static final String QUOTE = "quote";

	@Override
	public Record merge(Record feedRecord, JSONObject dbRecord) {
		JSONObject feedRecordJson = feedRecord.getRecordRawData().getRawData();
		JSONObject modifiedFeedRecordJson = new JSONObject(dbRecord);

		String key = getBaseKey();
		
		JSONObject dsosDbObject = getObject(modifiedFeedRecordJson, key);
		JSONObject dsosFeedObject = getObject(feedRecordJson, key);

		matchFieldAndMerge(dsosFeedObject, dsosDbObject);

		feedRecord.getRecordRawData().setRawData(modifiedFeedRecordJson);
		return feedRecord;
	}

	/**
	 * This method is use to get base key as per feed. 
	 * E.g If feed is GOVCORP, then it will return base key as govcorp.
	 */
	protected abstract String getBaseKey();

	/**
	 * This method returns feed specific object from db object.
	 * 
	 * @param modifiedFeedRecordJson
	 * @return
	 */
	public JSONObject getObject(JSONObject modifiedFeedRecordJson, String key) {
		JSONObject govcorpDbObject = (JSONObject) modifiedFeedRecordJson.get(key);
		return govcorpDbObject;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void mergeArrayObject(String arrayTag, JSONArray feedValueArray, JSONObject govcorpDbObject) {

		Object arrayObjectFromDb = govcorpDbObject.get(arrayTag);

		// In case the array is not available in the database or if the array tag
		// is ID_OBJ_XREF_HIST, we will simply put the feedArray in the db object
		// against the array tag.
		// Refer to UDM-66918 for ID_OBJ_XREF_HIST use-case.
		if (null == arrayObjectFromDb || ID_OBJ_XREF_HIST.equals(arrayTag)) {
			govcorpDbObject.put(arrayTag, feedValueArray);
			return;
		}

		JSONArray dbArray = (JSONArray) arrayObjectFromDb;

		for (Object object : feedValueArray) {
			JSONObject jsonObjectFromArray = (JSONObject) object;

			JSONObject dbEquivalentObject = getEquivalentDbObjectFromDbArray(arrayTag, jsonObjectFromArray, dbArray);

			if (null != dbEquivalentObject) {
				matchFieldAndMerge(jsonObjectFromArray, dbEquivalentObject);
			} else {
				dbArray.add(jsonObjectFromArray);
			}
		}
	}

	/**
	 * This method will check if jsonObject from feed array & db object from dbArray are equal.
	 * If both objects are equal , then this method will return dbObject.
	 * If both objects are not equal,then it will return null equivalentObject JSONObject.
	 * 
	 * @param arrayTag
	 * @param jsonObjectFromArray
	 * @param dbValue
	 * @return
	 */
	private JSONObject getEquivalentDbObjectFromDbArray(String arrayTag, JSONObject jsonObjectFromArray,
			JSONArray dbValue) {
		JSONObject equivalentObject = null;
		for (Object dbArrayObject : dbValue) {
			JSONObject dbObject = (JSONObject) dbArrayObject;
			if (areObjectsSame(arrayTag, jsonObjectFromArray, dbObject)) {
				return dbObject;
			}
		}
		return equivalentObject;
	}

	private boolean areObjectsSame(String arrayTag, JSONObject jsonObjectFromArray, JSONObject dbObject) {

		String feedKey = getArrayObjectKey(arrayTag, jsonObjectFromArray);
		String dbKey = getArrayObjectKey(arrayTag, dbObject);

		return Objects.equals(feedKey, dbKey);
	}

	/**
	 * This method returns key attribute value for securities where array tag is rduSecurities.
	 */
	@Override
	public String getArrayObjectKey(String arrayTag, JSONObject arrayEntry) {
		String arrayObjectKeyValue = null;

		if (arrayTag.equals(RDU_SECURITIES)) {
			arrayObjectKeyValue = getArrayObjecKeyAttributeValueForSecurities(arrayEntry);
		}
		return arrayObjectKeyValue;
	}

	private String getArrayObjecKeyAttributeValueForSecurities(JSONObject arrayEntry) {
		JSONObject security = (JSONObject) arrayEntry.get(SECURITY);

		return getArrayObjectKeyValue(security);
	}

	/**
	 * This method returns quote_id value where security tag is QUOTE & 
	 * sedol value where security tag is MIC_OPOL.
	 * 
	 * @param keyValue
	 * @param security
	 * @return
	 */
	protected abstract String getArrayObjectKeyValue(JSONObject security);

}
