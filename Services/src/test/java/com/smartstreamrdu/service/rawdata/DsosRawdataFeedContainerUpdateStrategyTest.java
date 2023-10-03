/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DsosRawdataFeedContainerUpdateStrategyTest.java
 * Author:	Dedhia
 * Date:	Mar 17, 2023
 *
 *******************************************************************/
package com.smartstreamrdu.service.rawdata;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerContext.DataContainerContextBuilder;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordRawData;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.Constant;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DsosRawdataFeedContainerUpdateStrategyTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private DsosRawdataFeedContainerUpdateStrategy updateStrategy;

	@Test
	@SuppressWarnings("unchecked")
	public void testUpdatedFeedDataContainer() {
		RawRecordContextDetailsPojo recordPojo = new RawRecordContextDetailsPojo();
		recordPojo.setDataSource("rdso");
		DataContainerContext ctx = DataContainerContext.builder().build();
		DataContainer feedContainer = new DataContainer(DataLevel.SD_RAW_DATA, ctx);
		Record rawDataRecord = new Record();
		
		JSONObject object = new JSONObject();
		object.put("key", "value");
		RecordRawData recordRawData = new RecordRawData(object);
		rawDataRecord.setRecordRawData(recordRawData);
		
		DataContainer feedContainerUpdated = updateStrategy.updatedFeedDataContainer(rawDataRecord, recordPojo, feedContainer);
		
		DataAttribute rawData = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_RECORD_ATTRIBUTE, DataLevel.SD_RAW_DATA);
		Serializable value = feedContainerUpdated.getAttributeValueAtLevel(LockLevel.FEED, rawData);
		
		assertEquals("{\"key\":\"value\"}", value);
		
		
	}

}
