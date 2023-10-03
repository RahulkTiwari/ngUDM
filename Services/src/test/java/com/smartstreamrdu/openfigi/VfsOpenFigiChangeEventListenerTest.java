/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: VfsOpenFigiChangeEventListenerTest.java
 * Author: Rushikesh Dedhia
 * Date: Jul 10, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.openfigi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.service.events.ChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.MergeCompleteChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.UpdateChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.VfsOpenFigiChangeEventListener;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.openfigi.FeedUpdatesVfsOpenFigiRequestService;
import com.smartstreamrdu.service.openfigi.IvoUpdatesVfsOpenFigiRequestService;

/**
 * @author Dedhia
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { VfsOpenFigiChangeEventListenerTestConfig.class })
public class VfsOpenFigiChangeEventListenerTest{

	ChangeEventInputPojo inputPojo = null;

	@Autowired
	private VfsOpenFigiChangeEventListener vfsOpenFigiChangeEventListener;
	
	@Autowired
	private IvoUpdatesVfsOpenFigiRequestService ivoUpdatesVfsOpenFigiRequestService;
	
	@Autowired
	private FeedUpdatesVfsOpenFigiRequestService feedUpdatesVfsOpenFigiRequestService;
	
	private boolean ivoUpdates=false;
	private boolean feedUpdates=false;
	
	@Before
	public void init() {
		Mockito.doAnswer(invocation -> ivoUpdates=true).when(ivoUpdatesVfsOpenFigiRequestService).sendRequestToVfsOpenFigi(Mockito.any());
		Mockito.doAnswer(invocation -> feedUpdates=true).when(feedUpdatesVfsOpenFigiRequestService).sendRequestToVfsOpenFigi(Mockito.any());
	}
	
	
	
	@Test
	public void testPropogateEvent_null() {
		vfsOpenFigiChangeEventListener.propogateEvent(null);
		assertFalse(ivoUpdates);
		assertFalse(feedUpdates);
	}
	
	@Test
	public void testPropogateEvent_nullDataContainer() {
		ChangeEventInputPojo changeEvent=new ChangeEventInputPojo();
		vfsOpenFigiChangeEventListener.propogateEvent(changeEvent);
		assertFalse(ivoUpdates);
		assertFalse(feedUpdates);
	}
	
	@Test
	public void testPropogateEvent_ivoDataContainer() {
		ChangeEventInputPojo changeEvent=new ChangeEventInputPojo();
		DataContainer postChangeContainer=new DataContainer(DataLevel.IVO_INS, null);
		changeEvent.setPostChangeContainer(postChangeContainer);
		vfsOpenFigiChangeEventListener.propogateEvent(changeEvent);
		assertTrue(ivoUpdates);
		assertFalse(feedUpdates);
	}
	
	@Test
	public void testPropogateEvent_insDataContainer() {
		ChangeEventInputPojo changeEvent=new ChangeEventInputPojo();
		DataContainer postChangeContainer=new DataContainer(DataLevel.INS, null);
		changeEvent.setPostChangeContainer(postChangeContainer);
		vfsOpenFigiChangeEventListener.propogateEvent(changeEvent);
		assertTrue(feedUpdates);
		assertFalse(ivoUpdates);
	}
	
	@Test
	public void testPropogateEvent_leDataContainer() {
		ChangeEventInputPojo changeEvent=new ChangeEventInputPojo();
		DataContainer postChangeContainer=new DataContainer(DataLevel.LE, null);
		changeEvent.setPostChangeContainer(postChangeContainer);
		vfsOpenFigiChangeEventListener.propogateEvent(changeEvent);
		assertFalse(feedUpdates);
		assertFalse(ivoUpdates);
	}
	
	@Test
	public void testIsEventApplicable() {
		assertTrue(vfsOpenFigiChangeEventListener.isEventApplicable(ListenerEvent.DataUpdate));
		assertFalse(vfsOpenFigiChangeEventListener.isEventApplicable(ListenerEvent.MergeComplete));
		assertFalse(vfsOpenFigiChangeEventListener.isEventApplicable(null));
	}
	
	@Test
	public void testCreateInput() {
		UpdateChangeEventListenerInputCreationContext inputCreationContext=new UpdateChangeEventListenerInputCreationContext();
		DataContainer dbDataContainer=new DataContainer(DataLevel.INS, null);
		inputCreationContext.setDbDataContainer(dbDataContainer);
		ChangeEventInputPojo input = vfsOpenFigiChangeEventListener.createInput(inputCreationContext);
		assertEquals(dbDataContainer, input.getPostChangeContainer());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateInput_invalid() {
		ChangeEventListenerInputCreationContext inputCreationContext=new MergeCompleteChangeEventListenerInputCreationContext();
		vfsOpenFigiChangeEventListener.createInput(inputCreationContext);
	}

	@After
	public void clearSession() {
		ivoUpdates=false;
		feedUpdates=false;
	}
}
