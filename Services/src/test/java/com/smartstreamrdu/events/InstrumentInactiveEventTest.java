/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InstrumentInactiveEventTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.events;

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
import com.smartstreamrdu.service.domain.ServiceTestConfiguration;
import com.smartstreamrdu.service.events.DataContainerMergingChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.InstrumentInactiveEvent;
import com.smartstreamrdu.service.inactive.InactiveService;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.util.MockUtil;

/**
 * @author Jay Sangoi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfiguration.class })
public class InstrumentInactiveEventTest {

	@Autowired
	private InstrumentInactiveEvent event;
	
	@Autowired
	private InactiveService service;
	
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
	public void test_propogateEvent() throws Exception{
		MockUtil.mockInactiveService(service);
		
		event.propogateEvent(new InactiveBean());
	}
	
}
