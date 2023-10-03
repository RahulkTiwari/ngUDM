/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergingDecoratorBuilderTest.java
 * Author:	Jay Sangoi
 * Date:	05-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.FallbackLockLevelInfo;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;


/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DataContainerMergingDecoratorBuilderTest {

	@Autowired
	private  DataContainerMergingDecoratorBuilder builder;
	
	@Test
	public void test_DecoratorCreation(){
		
		//Default Decorator with only attribite
		DataContainer feedContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		
		
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FALLBACK, UUID.randomUUID().toString(), new FallbackLockLevelInfo());
		
		
		feedContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		
		
		DataContainerMergingDecorator decorator = builder.createDataContainerMergingDecorator(feedContainer, null);
		
		Assert.assertEquals(DefaultDataContainerMergingDecorator.class, decorator.getClass());
		
		DataContainer dbContainer1 = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		dbContainer1.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		
		List<DataContainer> dbContainers = new ArrayList<>();
		
		decorator = builder.createDataContainerMergingDecorator(feedContainer, dbContainers);
		
		Assert.assertEquals(DefaultDataContainerMergingDecorator.class, decorator.getClass());
		
		DataContainer childContainer = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		childContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC), value);
		
		feedContainer.addDataContainer(childContainer, DataLevel.SEC);
		dbContainer1.addDataContainer(childContainer, DataLevel.SEC);
		
		
		decorator = builder.createDataContainerMergingDecorator(feedContainer, dbContainers);
		Assert.assertEquals(ParentChildMergingDecorator.class, decorator.getClass());
		
		dbContainers.add(dbContainer1);
		dbContainers.add(dbContainer1);
		decorator = builder.createDataContainerMergingDecorator(feedContainer, dbContainers);
		Assert.assertEquals(MultipleContainerMergingDecorator.class, decorator.getClass());
		
		
	}
	
}
