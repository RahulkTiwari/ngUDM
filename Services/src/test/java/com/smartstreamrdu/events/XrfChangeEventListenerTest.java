/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	XrfChangeEventListenerTest.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
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
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.events.XrfChangeEventListener;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class XrfChangeEventListenerTest extends AbstractEmbeddedMongodbJunitParent{
	
	private static final DataAttribute CUSIP_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.INS);

	private static final DataAttribute RIC_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("ric", DataLevel.SEC);

	@Autowired
	private XrfChangeEventListener listener;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	private static Method methodToTest;
	
	@BeforeClass
	public static void init() throws NoSuchMethodException, SecurityException {
		 methodToTest = XrfChangeEventListener.class.getDeclaredMethod("isDataContainerEligibleToSendForXrf",
				DataContainer.class,FeedConfiguration.class);
		 methodToTest.setAccessible(true);
	}
	
	@Test
	public void test_isEventApplicable(){
		Assert.assertTrue(listener.isEventApplicable(ListenerEvent.DataUpdate));
	}
	
	@Test
	public void testisDataContainerEligibleToSendForXrf_fromUI()
			throws NoSuchMethodException, SecurityException, UdmTechnicalException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("XrfChangeEventListenerTest/sdData.json", SdData.class).get(0);
		instrumentContainer.updateDataContainerContext(
				DataContainerContext.builder().withProgram("NG-EquityWeb").withUpdateBy("saJadhav").build());
		boolean isEligible = (boolean) methodToTest.invoke(listener, instrumentContainer,null);
		assertTrue(isEligible);
	}
	
	@Test
	public void testIsDataContainerEligibleToSendForXrf_Unchanged() throws UdmTechnicalException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("XrfChangeEventListenerTest/sdData.json", SdData.class).get(0);
		DataContainerUtil.populateNewFlagAndHasChanged(Arrays.asList(instrumentContainer));
		boolean isEligible = (boolean) methodToTest.invoke(listener, instrumentContainer,null);
		assertFalse(isEligible);
	}
	
	@Test
	public void testIsDataContainerEligibleToSendForXrf_InsLevelAttributeChanged() throws UdmTechnicalException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("XrfChangeEventListenerTest/sdData.json", SdData.class).get(0);
		DataContainerUtil.populateNewFlagAndHasChanged(Arrays.asList(instrumentContainer));
		
		DataValue<String> cusipValue=new DataValue<>();
		cusipValue.setValue(LockLevel.FEED, "testCusip");
		instrumentContainer.addAttributeValue(CUSIP_ATTR, cusipValue);
		
		boolean isEligible = (boolean) methodToTest.invoke(listener, instrumentContainer,null);
		assertTrue(isEligible);
	}
	
	@Test
	public void testIsDataContainerEligibleToSendForXrf_SecLevelAttributeChanged() throws UdmTechnicalException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("XrfChangeEventListenerTest/sdData.json", SdData.class).get(0);
		DataContainerUtil.populateNewFlagAndHasChanged(Arrays.asList(instrumentContainer));
		
		DataContainer secDataContainer = instrumentContainer.getChildDataContainers(DataLevel.SEC).get(0);
		
		DataValue<String> ricValue=new DataValue<>();
		ricValue.setValue(LockLevel.FEED, "testRic");
		secDataContainer.addAttributeValue(RIC_ATTR, ricValue);
		
		boolean isEligible = (boolean) methodToTest.invoke(listener, instrumentContainer,null);
		assertTrue(isEligible);
	}
	
	@Test
	public void testIsDataContainerEligibleToSendForXrf_new() throws UdmTechnicalException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("XrfChangeEventListenerTest/sdData.json", SdData.class).get(0);
		instrumentContainer.setNew(true);
		boolean isEligible = (boolean) methodToTest.invoke(listener, instrumentContainer,null);
		assertTrue(isEligible);
	}
	
	@Test
	public void testIsDataContainerEligibleToSendForXrf_LE_container() throws UdmTechnicalException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DataContainer leContainer = new DataContainer(DataLevel.LE, DataContainerContext.builder().build());
		boolean isEligible = (boolean) methodToTest.invoke(listener, leContainer,null);
		assertFalse(isEligible);
	}
	
	@Test
	public void testIsDataContainerEligibleToSendForXrf_IVO_container() throws UdmTechnicalException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DataContainer leContainer = new DataContainer(DataLevel.IVO_INS, DataContainerContext.builder().build());
		boolean isEligible = (boolean) methodToTest.invoke(listener, leContainer,null);
		assertFalse(isEligible);
	}
	
	@Test
	public void testIsDataContainerEligibleToSendForXrf_annaDataSource() throws UdmTechnicalException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("XrfChangeEventListenerTest/sdData_anna.json", SdData.class).get(0);
		instrumentContainer.setNew(true);
		boolean isEligible = (boolean) methodToTest.invoke(listener, instrumentContainer,null);
		assertFalse(isEligible);
	}
}
