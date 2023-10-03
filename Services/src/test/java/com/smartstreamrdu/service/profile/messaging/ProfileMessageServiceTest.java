/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ProfileMessageProcessingServiceTest.java
 * Author:	GMathur
 * Date:	29-Apr-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.profile.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.message.ProfileProcessingMessage;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.Profile;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes={ProducerFactoryMockConfig.class})
public class ProfileMessageServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private ProfileMessageService service;

	@Autowired
	private ProducerFactory producerFactory;
	
	private Message message;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() throws Exception {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Future sendMessage(Message message) throws Exception {
				ProfileMessageServiceTest.this.message = message;
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}
	
	@Test
	public void testSendMessage() throws Exception {
		ProfileProcessingMessage mssg = new ProfileProcessingMessage();
		mssg.setProfileName("EQUITY");
		mssg.setPrimaryDataSources(Arrays.asList("trdse"));
		
		service.sendMessage(mssg);
		
		assertNotNull(message);
		String data = message.getData();
		ProfileProcessingMessage profileMessage = JsonConverterUtil.convertFromJson(data, ProfileProcessingMessage.class);
		assertEquals("EQUITY", profileMessage.getProfileName());
		assertEquals(1, profileMessage.getPrimaryDataSources().size());
	}
	
	@Test
	public void testGenerateMessage() {
		Profile profile = new Profile();
		profile.setName("EQUITY");
		profile.setPrimaryDataSources(Arrays.asList("trdse"));
		profile.setClient("DnB");
		
		List<ProfileProcessingMessage> mssgList = service.generateProfileProcessingMessage(Arrays.asList(profile));
		
		assertEquals(1, mssgList.size());
		assertEquals("EQUITY", mssgList.get(0).getProfileName());
		assertEquals(1, mssgList.get(0).getPrimaryDataSources().size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGenerateEmptyMessage() {
		List<ProfileProcessingMessage> mssgList = service.generateProfileProcessingMessage(Collections.EMPTY_LIST);
		
		assertEquals(0, mssgList.size());
	}
	
	@Test
	public void testSendEmptyMessage() throws Exception {
		
		service.sendMessage(null);
		
		assertNull(message);
	}
}
