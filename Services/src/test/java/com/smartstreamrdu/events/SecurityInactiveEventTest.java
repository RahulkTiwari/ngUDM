/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    SecurityInactiveEventTest.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.events.DataContainerMergingChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.SecurityInActiveEvent;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.SdDataAttributeConstant;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class SecurityInactiveEventTest extends AbstractEmbeddedMongodbJunitParent{

	@Autowired
	private SecurityInActiveEvent event;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Autowired
	private NormalizedValueService normalizedValService;
	
	@Test
	public void test_isEventApplicable(){
		Assert.assertTrue(event.isEventApplicable(ListenerEvent.DataContainerMerging));
		Assert.assertFalse(event.isEventApplicable(ListenerEvent.DataUpdate));
	}
	
	@Test
	public void test_createInput(){
		DataContainer dc = DataContainerTestUtil.getDataContainer(DataLevel.INS);
		DataContainerMergingChangeEventListenerInputCreationContext input = new DataContainerMergingChangeEventListenerInputCreationContext();
		input.setDataContainer(dc);
		input.setDataSource("ds");
		InactiveBean inp = event.createInput(input);
		Assert.assertNotNull(inp);
		Assert.assertNotNull(inp.getDatasource());
		Assert.assertEquals("ds", inp.getDatasource());
		Assert.assertNotNull(inp.getDbContainer());
		Assert.assertEquals(dc, inp.getDbContainer());
	}
	
	@Test
	public void test_propogateEvent_Active() throws Exception{
		DataContainer container=bsonConverter.getListOfDataContainersFromFilePath("SecurityInactivationEventTest/TestActive/sdData.json", SdData.class).get(0);
		InactiveBean bean = new InactiveBean();
		bean.setDatasource("idcApex");
		bean.setDbContainer(container);
		event.propogateEvent(bean);
		
		DataContainer dbContainer = bean.getDbContainer();
		DomainType insStatusValue = (DomainType) container.getHighestPriorityValue(SdDataAttributeConstant.INS_STATUS);
		
		Assert.assertEquals("1",insStatusValue.getVal());
		
		List<DataContainer> allChildDataContainers = dbContainer.getAllChildDataContainers();
		
		List<String> statusList = new ArrayList<>();
		for(DataContainer child : allChildDataContainers ) {
			String feedSecurityStatus = getFeedSecurityStatus(child,"idcApex");
			statusList.add(feedSecurityStatus);
		}
		
 		Assert.assertEquals(false, statusList.contains("I"));
	}
	
	
	@Test
	public void test_propogateEvent_InActive() throws Exception{
		DataContainer container=bsonConverter.getListOfDataContainersFromFilePath("SecurityInactivationEventTest/TestInActive/sdData.json", SdData.class).get(0);
		InactiveBean bean = new InactiveBean();
		bean.setDatasource("idcApex");
		bean.setDbContainer(container);
		event.propogateEvent(bean);
		
		DataContainer dbContainer = bean.getDbContainer();
		DomainType insStatusValue = (DomainType) dbContainer.getHighestPriorityValue(SdDataAttributeConstant.INS_STATUS);
		
		Assert.assertEquals("2",insStatusValue.getVal());
		
		List<DataContainer> allChildDataContainers = dbContainer.getAllChildDataContainers();
		
		List<String> statusList = new ArrayList<>();
		for(DataContainer child : allChildDataContainers ) {
			String feedSecurityStatus = getFeedSecurityStatus(child,"idcApex");
			statusList.add(feedSecurityStatus);
		}
		
		Assert.assertEquals(false, statusList.contains("A"));
	}
	
	private String getFeedSecurityStatus(DataContainer childContainer, String dataSource) {
		DomainType statusValue = (DomainType) childContainer
				.getHighestPriorityValue(SdDataAttributeConstant.SEC_STATUS);

		if (statusValue == null) {
			return null;
		}

		if (statusValue.getNormalizedValue() != null) {
			return statusValue.getNormalizedValue();
		}

		Serializable normalizedValueForDomainValue = normalizedValService
				.getNormalizedValueForDomainValue(SdDataAttributeConstant.SEC_STATUS, statusValue, dataSource);
		return (String) normalizedValueForDomainValue;
	}
}
