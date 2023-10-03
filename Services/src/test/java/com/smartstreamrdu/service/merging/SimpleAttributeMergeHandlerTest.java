/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: SimpleAttributeMergeHandlerTest.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author Dedhia
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class SimpleAttributeMergeHandlerTest {
	
	@Autowired
	SimpleAttributeMergeHandler handler;
	
	
	DataContainer dbContainer = null;
	
	DataAttribute isinAttribute = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
	DataAttribute cusipAttribute = DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.INS);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dbContainer = new DataContainer(DataLevel.INS, emptyContext());
		DataValue<String> dbIsinValue = new DataValue<>();
		dbIsinValue.setValue(LockLevel.FEED, "FEED_ISIN_VALUE");
		dbContainer.addAttributeValue(isinAttribute, dbIsinValue);
		DataContainerUtil.populateNewFlagAndHasChanged(Arrays.asList(dbContainer));
	}

	protected static DataContainerContext emptyContext() {
		return DataContainerContext.builder().build();
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.merging.SimpleAttributeMergeHandler#handleAttributeMerge(java.util.Map, java.util.Map, com.smartstreamrdu.domain.DataAttribute)}.
	 */
	@Test
	public void testHandleAttributeMerge_changed() {
		DataContainer feedContainer = new DataContainer(DataLevel.INS, emptyContext());
		feedContainer = new DataContainer(DataLevel.INS, emptyContext());
		DataValue<String> feedIsinValue = new DataValue<>();
		feedIsinValue.setValue(LockLevel.RDU, "OPS_VALUE", new RduLockLevelInfo());
		feedContainer.addAttributeValue(isinAttribute, feedIsinValue);
		
		DataValue<String> feedCusipValue = new DataValue<>();
		feedCusipValue.setValue(LockLevel.FEED, "FEED_CUSIP_VALUE");
		feedContainer.addAttributeValue(cusipAttribute, feedCusipValue);
		
		Map<DataAttribute, DataValue<Serializable>> feedRecordData = feedContainer.getRecordData();
		
		Set<DataAttribute> dataAttributes = feedRecordData.keySet();
		
		for (DataAttribute dataAttribute : dataAttributes) {
			handler.handleAttributeMerge(feedContainer, dbContainer, dataAttribute);
		}
		
		assertTrue(dbContainer.hasContainerChanged());
		Assert.assertEquals(feedContainer.getAttributeValueAtLevel(LockLevel.RDU, isinAttribute), dbContainer.getAttributeValueAtLevel(LockLevel.RDU, isinAttribute));
		
		Assert.assertEquals(feedContainer.getAttributeValueAtLevel(LockLevel.FEED, cusipAttribute), dbContainer.getAttributeValueAtLevel(LockLevel.FEED, cusipAttribute));
		
	}
	
	@Test
	public void testHandleAttributeMerge_UnChanged() {
		DataContainer feedContainer = new DataContainer(DataLevel.INS, emptyContext());
		DataValue<String> feedIsinValue = new DataValue<>();
		feedIsinValue.setValue(LockLevel.FEED, "FEED_ISIN_VALUE");
		feedContainer.addAttributeValue(isinAttribute, feedIsinValue);
		
		Map<DataAttribute, DataValue<Serializable>> feedRecordData = feedContainer.getRecordData();
		
		Set<DataAttribute> dataAttributes = feedRecordData.keySet();
		
		for (DataAttribute dataAttribute : dataAttributes) {
			handler.handleAttributeMerge(feedContainer, dbContainer, dataAttribute);
		}
		
		assertFalse(dbContainer.hasContainerChanged());
		DataValue<String> attributeValue = (DataValue<String>) dbContainer.getAttributeValue(isinAttribute);
		assertFalse(attributeValue.hasValueChanged());
	}

}
