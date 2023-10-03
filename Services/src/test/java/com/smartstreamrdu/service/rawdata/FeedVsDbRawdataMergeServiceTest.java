/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedVsDbRawdataMergeServiceTest.java
 * Author:	GMathur
 * Date:	04-Jan-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class FeedVsDbRawdataMergeServiceTest {
	@Autowired
	@Qualifier("DefaultFeedVsDbRawdataMergeService")
	private FeedVsDbRawdataMergeServiceImpl feedVsDbMergeService;
	
	final static String FEED_JSON = "{	\"Asset_ID\": \"0x00102c59eb961008\",	\"Reference\": {		\"Action\": \"I\",		\"Active_Instrument_Flag\": \"Y\",		\"Announcement_Date\": \"20171128\",		\"Annualized_Cash_Rate\": \"5.75\",		\"Annuity_Flag\": \"N\",		\"Asset_Duplicate_Code\": \"\",		\"Asset_Effective_Date\": \"20171207\",		\"Asset_ID\": \"0x00102c59eb961008\",		\"Asset_Setup_Date\": \"20171128\",		\"Asset_Status_Code\": \"RPN\"	}}";
	final static String FEED_ARRAY_JSON = "{	\"Asset_ID\": \"0x00102c59eb961008\",	\"Reference\": {		\"Action\": \"I\",		\"Active_Instrument_Flag\": \"Y\",		\"Announcement_Date\": \"20171128\",		\"Annualized_Cash_Rate\": \"5.75\",		\"Annuity_Flag\": \"N\",		\"Asset_Duplicate_Code\": \"\",		\"Asset_Effective_Date\": \"20171207\",		\"Asset_ID\": \"0x00102c59eb961008\",		\"Asset_Setup_Date\": \"20171128\",		\"Asset_Status_Code\": \"RPN\"	},	\"Symbol_Cross_Reference\": [		{			\"Action\": \"I\",			\"Change_Date\": \"\",			\"Current_Flag\": \"Y\",			\"Identifier_Characteristic_Code\": \"\",			\"Identifier_Scope\": \"\",			\"Identifier_Type_Code\": \"SED\",			\"Identifier_Value\": \"BD2BVX1\",			\"Object_Identifier\": \"0x00102c59eb961008\",			\"Object_Type_Code\": \"GCBD\",			\"Perm_ID\": \"192772322821\"		},		{			\"Action\": \"I\",			\"Change_Date\": \"\",			\"Current_Flag\": \"Y\",			\"Identifier_Characteristic_Code\": \"\",			\"Identifier_Scope\": \"\",			\"Identifier_Type_Code\": \"WPK\",			\"Identifier_Value\": \"A19S7U\",			\"Object_Identifier\": \"0x00102c59eb961008\",			\"Object_Type_Code\": \"GCBD\",			\"Perm_ID\": \"192772322821\"		}	]}";

	@Test
	public void testMergeWithoutDbData() {
		JSONObject feedJsonObj = JsonConverterUtil.convertToSimpleJson(FEED_JSON);
		JSONObject dbJsonObj = new JSONObject();
		Record feedRecord = new Record();
		feedRecord.getRecordRawData().setRawData(feedJsonObj);
		Record mergedRecord = feedVsDbMergeService.merge(feedRecord, dbJsonObj);
		assertTrue(mergedRecord.getRecordRawData().getRawData().containsKey("Asset_ID"));
		assertTrue(mergedRecord.getRecordRawData().getRawData().containsKey("Reference"));
	}
	
	@Test
	public void testMergeWithDbData_MissingSimpleObject() {
		JSONObject feedJsonObj = JsonConverterUtil.convertToSimpleJson(FEED_JSON);
		String dbJson = "{	\"Asset_ID\": \"0x00102c59eb961008\",	\"Reference\": {		\"Action\": \"I\",		\"Active_Instrument_Flag\": \"Y\",		\"Announcement_Date\": \"20171128\",		\"Annualized_Cash_Rate\": \"5.85\",		\"Annuity_Flag\": \"N\",		\"Asset_Duplicate_Code\": \"\",		\"Asset_Effective_Date\": \"20171208\",		\"Asset_ID\": \"0x00102c59eb961008\",		\"Asset_Setup_Date\": \"20171128\", 	\"Baby_Bond_Flag\": \"N\"}}";
		JSONObject dbJsonObj = JsonConverterUtil.convertToSimpleJson(dbJson);
		Record feedRecord = new Record();
		feedRecord.getRecordRawData().setRawData(feedJsonObj);
		Record mergedRecord = feedVsDbMergeService.merge(feedRecord, dbJsonObj);
		assertEquals("20171207", mergedRecord.getRecordRawData().getRawDataAttribute("$.Reference.Asset_Effective_Date"));
		assertEquals("5.75", mergedRecord.getRecordRawData().getRawDataAttribute("$.Reference.Annualized_Cash_Rate"));
		assertEquals("RPN", mergedRecord.getRecordRawData().getRawDataAttribute("$.Reference.Asset_Status_Code"));
		assertEquals("N", mergedRecord.getRecordRawData().getRawDataAttribute("$.Reference.Baby_Bond_Flag"));
	}
	
	@Test
	public void testMergeWithDbData_MissingJsonObject() {
		JSONObject feedJsonObj = JsonConverterUtil.convertToSimpleJson(FEED_JSON);
		String dbJson = "{	\"Asset_ID\": \"0x00102c59eb961008\"}";
		JSONObject dbJsonObj = JsonConverterUtil.convertToSimpleJson(dbJson);
		Record feedRecord = new Record();
		feedRecord.getRecordRawData().setRawData(feedJsonObj);
		Record mergedRecord = feedVsDbMergeService.merge(feedRecord, dbJsonObj);
		assertTrue(mergedRecord.getRecordRawData().getRawData().containsKey("Asset_ID"));
		assertTrue(mergedRecord.getRecordRawData().getRawData().containsKey("Reference"));
		assertEquals("20171207", mergedRecord.getRecordRawData().getRawDataAttribute("$.Reference.Asset_Effective_Date"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testMergeWithJsonArray_Rdsp() {
		JSONObject feedJsonObj = JsonConverterUtil.convertToSimpleJson(FEED_ARRAY_JSON);
		String dbJson = "{	\"Asset_ID\": \"0x00102c59eb961008\",	\"Symbol_Cross_Reference\": [		{			\"Action\": \"I\",			\"Change_Date\": \"\",			\"Current_Flag\": \"Y\",			\"Identifier_Characteristic_Code\": \"\",			\"Identifier_Scope\": \"\",			\"Identifier_Type_Code\": \"WPK\",			\"Identifier_Value\": \"A19S7U\",			\"Object_Identifier\": \"0x00102c59eb961008\",			\"Object_Type_Code\": \"GCBD\",			\"Perm_ID\": \"192772322821\"		}	]}";
		JSONObject dbJsonObj = JsonConverterUtil.convertToSimpleJson(dbJson);
		Record feedRecord = new Record();
		feedRecord.getRecordRawData().setRawData(feedJsonObj);
		feedVsDbMergeService.merge(feedRecord, dbJsonObj);
	}
}
