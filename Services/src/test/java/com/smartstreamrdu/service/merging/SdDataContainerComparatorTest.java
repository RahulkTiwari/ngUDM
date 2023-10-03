/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: SdDataContainerComparatorTest.java
 * Author: Rushikesh Dedhia
 * Date: Sep 25, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.domain.DomainService;

/**
 * @author Dedhia
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class SdDataContainerComparatorTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private transient DomainService domainService;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Autowired
	private ContainerComparatorFactoryImpl containerComparatorFactory;
	
	
	DataAttribute insSourceUniqueId = DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS);
	DataAttribute dataSourceAttribute = DataAttributeFactory.getDatasourceAttribute(DataLevel.INS);
	DataAttribute statusAttribute = InstrumentAttrConstant.INSTRUMENT_STATUS;
	
	DataContainer feedContainer = null;
	DataContainer dbContainer1 = null;
	DataContainer dbContainer2 = null;
	DataContainer dbContainer3 = null;
	
	List<DomainType> activeStatusValueForVendor = null;
	
	DataValue<String> insSourceUniqueId1 = new DataValue<>();
	
	List<DataContainer> dbDataContainers = new ArrayList<>();

	private DataValue<DomainType> dataSourceValue;

	private DataValue<DomainType> statusActiveValue;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		feedContainer = new DataContainer(DataLevel.INS, emptyContext());
		dbContainer1 = new DataContainer(DataLevel.INS, emptyContext());
		dbContainer2 = new DataContainer(DataLevel.INS, emptyContext());
		dbContainer3 = new DataContainer(DataLevel.INS, emptyContext());
		
		dbDataContainers.add(dbContainer1);
		dbDataContainers.add(dbContainer2);
		
		
		insSourceUniqueId1.setValue(LockLevel.FEED, "SOURCE_UNIQUE_ID_VAL_1");
		DataValue<String> insSourceUniqueId2 = new DataValue<>();
		insSourceUniqueId2.setValue(LockLevel.FEED, "SOURCE_UNIQUE_ID_VAL_2");
		
		dataSourceValue = new DataValue<>();
		DomainType dataSource = new DomainType("trdse");
		dataSourceValue.setValue(LockLevel.FEED, dataSource);
		
		activeStatusValueForVendor = domainService.getActiveStatusValueForVendor((DomainType) dataSource, statusAttribute);
		List<DomainType> inactiveStatusValueForVendor = domainService.getInActiveStatusValueForVendor((DomainType) dataSource, statusAttribute);
		
		statusActiveValue = new DataValue<>();
		statusActiveValue.setValue(LockLevel.FEED, activeStatusValueForVendor.get(0));
		
		DataValue<DomainType> statusInactiveValue = new DataValue<>();
		statusInactiveValue.setValue(LockLevel.FEED, inactiveStatusValueForVendor.get(0));
		
		feedContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);
		dbContainer1.addAttributeValue(dataSourceAttribute, dataSourceValue);
		dbContainer2.addAttributeValue(dataSourceAttribute, dataSourceValue);
		dbContainer3.addAttributeValue(dataSourceAttribute, dataSourceValue);
		
		feedContainer.addAttributeValue(statusAttribute, statusActiveValue);
		dbContainer1.addAttributeValue(statusAttribute, statusInactiveValue);
		dbContainer2.addAttributeValue(statusAttribute, statusActiveValue);
		dbContainer3.addAttributeValue(statusAttribute, statusInactiveValue);
		
		feedContainer.addAttributeValue(insSourceUniqueId, insSourceUniqueId1);
		dbContainer1.addAttributeValue(insSourceUniqueId, insSourceUniqueId2);
		dbContainer2.addAttributeValue(insSourceUniqueId, insSourceUniqueId1);
		dbContainer3.addAttributeValue(insSourceUniqueId, insSourceUniqueId2);
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.merging.SdDataContainerComparator#compareContainersAndReturn(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer, java.util.List)}.
	 * @throws Exception 
	 */
	@Test
	public void testCompareContainersAndReturn() throws Exception {
		
		ParentContainerComparator comparator = containerComparatorFactory.getParentContainerComparator(feedContainer);
		
		DataContainer outCome = comparator.compareDataContainer(feedContainer, dbDataContainers);
		
		Assert.assertNotEquals(null, outCome);
		
		Serializable outComeSourceUniqueId = outCome.getAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(outCome.getLevel()));
		Assert.assertEquals(insSourceUniqueId1, outComeSourceUniqueId);
		
		Serializable attributeValueAtLevel = outCome.getAttributeValueAtLevel(LockLevel.FEED, statusAttribute);
		Assert.assertEquals(activeStatusValueForVendor.get(0), attributeValueAtLevel);
		
	}
	
	@Test
	public void testCompareContainersAndReturnNull() throws Exception {
		
		feedContainer = new DataContainer(DataLevel.SEC, emptyContext());
		feedContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);
		
		DataAttribute statusAttribute = SecurityAttrConstant.SECURITY_STATUS;
		feedContainer.addAttributeValue(statusAttribute, statusActiveValue);
		dbContainer1.addAttributeValue(statusAttribute, statusActiveValue);
		dbContainer2.addAttributeValue(statusAttribute, statusActiveValue);
		dbContainer3.addAttributeValue(statusAttribute, statusActiveValue);
		
		ParentContainerComparator comparator = containerComparatorFactory.getParentContainerComparator(feedContainer);
		
		DataContainer outCome = comparator.compareDataContainer(feedContainer, dbDataContainers);
		
		Assert.assertNull(outCome);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCompareContainersAndReturn_1() throws Exception {
		ParentContainerComparator comparator = containerComparatorFactory.getParentContainerComparator(feedContainer);
		comparator.compareDataContainer(null, dbDataContainers);
	}
	
	@Test
	public void testCompareContainersAndReturn_2() {
		
		ParentContainerComparator comparator = containerComparatorFactory.getParentContainerComparator(feedContainer);
		
		DataContainer outCome = null;
		
		List<DataContainer> dbContainersList = new ArrayList<>();
		
		try {
			 outCome = comparator.compareDataContainer(feedContainer, dbContainersList);
		} catch (Exception e) {
			Assert.assertNull("As expected", outCome);
			Assert.assertEquals(true, e instanceof IllegalArgumentException);
			Assert.assertEquals("Exception while merging SD data containers. Neither the feed container nor the database containers can be empty", e.getMessage());
			return;
		}
		Assert.fail("Something went wrong here.");
	}
	
	
	protected static DataContainerContext emptyContext() {
		return DataContainerContext.builder().build();
	}
	
	
	@Test
	public void testCompareContainersBasedOnObjectIdentifier() throws Exception {
		
		List<DataContainer> dbContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"SdDataContainerComparatorTest/testCompareContainers/dbContainer/sdData.json", SdData.class);
		
		List<DataContainer> feedContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"SdDataContainerComparatorTest/testCompareContainers/feedContainer/sdData.json", SdData.class);
		
		List<DataContainer> allFeedChildDataContainers = feedContainers.get(0).getAllChildDataContainers();
		
		List<DataContainer> alldbChildDataContainers = dbContainers.get(0).getAllChildDataContainers();
		
		
		ChildContainerComparator comparator = containerComparatorFactory.getChildContainerComparator(allFeedChildDataContainers.get(0));
		
		for(DataContainer child : allFeedChildDataContainers) {
			DataContainer container = comparator.compare(feedContainers.get(0),child, alldbChildDataContainers);
			
			Serializable feedAttributeValue = child.getAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC));
			Serializable dbAttributeValue = container.getAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC));

			Assert.assertEquals(feedAttributeValue, dbAttributeValue);
		}
		
	}

}
