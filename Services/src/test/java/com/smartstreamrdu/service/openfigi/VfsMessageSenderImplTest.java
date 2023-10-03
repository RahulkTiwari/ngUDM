/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: VfsMessageSenderImplTest.java
 * Author: AThanage
 * Date: Dec 02, 2021
 *
 *******************************************************************/
package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant.Process;

/**
 * @author AThanage
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class VfsMessageSenderImplTest {

	@Autowired
	private VfsMessageSenderImpl vfsMessageSenderImpl;

	@Autowired
	private ProducerFactory producerFactory;

	private List<Message> listMessageForVfsRequestQueue = new ArrayList<>();

	private VfsFigiRequestMessage identifiers;

	/**
	 * @throws java.lang.Exception
	 */
	public void setUpData() throws Exception {
		identifiers = new VfsFigiRequestMessage();
		identifiers.setCusip("ABC");
		identifiers.setMicIndex(-1);
		identifiers.setIsin("US4592001014");
		identifiers.setExchangeCode("XCSE");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks() {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				listMessageForVfsRequestQueue.add(message);
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}

	/**
	 * Test method for
	 * {@link com.smartstreamrdu.service.openfigi.VfsMessageSenderImpl#sendMessage(com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage, com.smartstreamrdu.util.Constant.Process, java.lang.Integer)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testSendMessage() throws Exception {
		setUpData();
		vfsMessageSenderImpl.sendMessage(identifiers, Process.FigiRequest, 10);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(1, listMessageForVfsRequestQueue.size());
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil
				.convertFromJson(listMessageForVfsRequestQueue.get(0).getData(), VfsFigiRequestMessage.class);
		assertEquals("ABC", openFigiIdentifiers.getCusip());
		assertEquals(-1, openFigiIdentifiers.getMicIndex());
		assertEquals("US4592001014", openFigiIdentifiers.getIsin());
	}

	/**
	 * Test method for
	 * {@link com.smartstreamrdu.service.openfigi.VfsMessageSenderImpl#sendMessage(com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage, com.smartstreamrdu.util.Constant.Process, java.lang.Integer)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testSendMessage_NullIdentifiersOrProcessOrPartition() throws Exception {
		setUpData();
		vfsMessageSenderImpl.sendMessage(null, Process.FigiRequest, 10);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(0, listMessageForVfsRequestQueue.size());
		vfsMessageSenderImpl.sendMessage(identifiers, null, 10);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(0, listMessageForVfsRequestQueue.size());
		vfsMessageSenderImpl.sendMessage(identifiers, Process.FigiRequest, null);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}

	
	/**
	 * Test method for {@link com.smartstreamrdu.service.openfigi.VfsMessageSenderImpl#sendMessage(com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage, com.smartstreamrdu.util.Constant.Process, java.lang.Integer)}.
	 * @throws Exception 
	 */
	@Test
	public final void testSendMessage_ExceptionWhileSendingMessage() throws Exception {
		mockProducer();
		setUpData();
		vfsMessageSenderImpl.sendMessage(identifiers, Process.FigiRequest, 10);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void mockProducer() {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				Exception e= new NullPointerException();
				throw new UdmTechnicalException("Technical exception", e);
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}
}
