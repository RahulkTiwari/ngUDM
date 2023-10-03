/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergingDataContainerTest.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class MergingDataContainerTest {

	@Autowired
	private MergingDataContainer mergingService;
	
	@Test
	public void test_Security_Merging(){
		
		String insSourceUnqueId = UUID.randomUUID().toString();
		
		DataContainer dbContainer = createDbDataContainer(insSourceUnqueId);

		DataContainer feedContainer = createFeedDataContainer(insSourceUnqueId);

		DataContainer result = mergingService.merge(feedContainer, dbContainer);

		Assert.assertNotNull(result);
		
		Assert.assertEquals(dbContainer.get_id(), result.get_id());
		Assert.assertTrue(result.getChildDataContainers(DataLevel.SEC).size() == 2);
		
	}
	
	private DataContainer createDbDataContainer(String unique){
		DataContainer dbInsContainer = DataContainerTestUtil.getInstrumentContainer();
		DataContainer dbSecContainer = DataContainerTestUtil.getSecurityContainer();
		dbInsContainer.set_id(UUID.randomUUID().toString());
		DataValue<Serializable> insSourceUniqueIdVal = new DataValue<>();
		insSourceUniqueIdVal.setValue(LockLevel.FEED, unique);
		dbInsContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), insSourceUniqueIdVal);
		
		
		dbInsContainer.addDataContainer(dbSecContainer, DataLevel.SEC);

		DataValue<Serializable> secSourceUniqueIdVal = new DataValue<>();
		secSourceUniqueIdVal.setValue(LockLevel.FEED, UUID.randomUUID().toString());
		dbSecContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC), secSourceUniqueIdVal);

		
		return dbInsContainer;

	}
	
	
	private DataContainer createFeedDataContainer(String unique){
		DataContainer dbInsContainer = DataContainerTestUtil.getInstrumentContainer();
		DataContainer dbSecContainer = DataContainerTestUtil.getSecurityContainer();
		
		DataValue<Serializable> insSourceUniqueIdVal = new DataValue<>();
		insSourceUniqueIdVal.setValue(LockLevel.FEED, unique);
		dbInsContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), insSourceUniqueIdVal);
		
		
		dbInsContainer.addDataContainer(dbSecContainer, DataLevel.SEC);

		DataValue<Serializable> secSourceUniqueIdVal = new DataValue<>();
		secSourceUniqueIdVal.setValue(LockLevel.FEED, UUID.randomUUID().toString());
		dbSecContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC), secSourceUniqueIdVal);

		
		return dbInsContainer;

	}
	
}
