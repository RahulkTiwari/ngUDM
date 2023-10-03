/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SimpleAttributeIvoAggregationHandlerTest.java
 * Author : SaJadhav
 * Date : 20-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;

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

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class SimpleAttributeIvoMergeHandlerTest {
	
	@Autowired
	private SimpleAttributeIvoMergeHandler simpleAttrIvoAggregationHandler;
	
	DataContainer sdContainer = null;
	DataContainer ivoContainer = null;
	
	DataAttribute cfiCodeAttribute = DataAttributeFactory.getAttributeByNameAndLevel("cfiCode", DataLevel.INS);
	DataAttribute cinsAttribute = DataAttributeFactory.getAttributeByNameAndLevel("cins", DataLevel.INS);
	
	DataAttribute ivoCfiCodeAttribute = DataAttributeFactory.getAttributeByNameAndLevel("cfiCode", DataLevel.IVO_INS);
	DataAttribute ivoCinsAttribute = DataAttributeFactory.getAttributeByNameAndLevel("cins", DataLevel.IVO_INS);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sdContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DataValue<String> feedCfiCodeValue = new DataValue<>();
		feedCfiCodeValue.setValue(LockLevel.FEED, "FEED_CFI_CODE", new RduLockLevelInfo());
		sdContainer.addAttributeValue(cfiCodeAttribute, feedCfiCodeValue);
		
		ivoContainer = new DataContainer(DataLevel.IVO_INS, DataContainerContext.builder().build());
		DataValue<String> rduCfiCodeValue = new DataValue<>();
		rduCfiCodeValue.setValue(LockLevel.RDU, "RDU_CFI_CODE");
		ivoContainer.addAttributeValue(ivoCfiCodeAttribute, rduCfiCodeValue);
		
		DataValue<String> rduCinsValue=new DataValue<>();
		rduCinsValue.setValue(LockLevel.RDU, "RDU_CINS");
		ivoContainer.addAttributeValue(ivoCinsAttribute, rduCinsValue);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_handleAttributeMerge(){
		
		simpleAttrIvoAggregationHandler.handleAttributeMerge(sdContainer, ivoContainer, ivoCfiCodeAttribute);
		
		assertEquals(2, ((DataValue<Serializable>)sdContainer.getAttributeValue(cfiCodeAttribute)).getLockData().size());
		assertEquals(ivoContainer.getAttributeValueAtLevel(LockLevel.RDU, ivoCfiCodeAttribute), sdContainer.getAttributeValueAtLevel(LockLevel.RDU, cfiCodeAttribute));
		
		simpleAttrIvoAggregationHandler.handleAttributeMerge(sdContainer, ivoContainer, ivoCinsAttribute);
		
		assertEquals(1, ((DataValue<Serializable>)sdContainer.getAttributeValue(cinsAttribute)).getLockData().size());
		assertEquals(ivoContainer.getAttributeValueAtLevel(LockLevel.RDU, ivoCinsAttribute), sdContainer.getAttributeValueAtLevel(LockLevel.RDU, cinsAttribute));
	}

}
