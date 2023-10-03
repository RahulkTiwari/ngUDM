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

import java.util.Arrays;
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
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes={ProducerFactoryMockConfig.class})
public class ProfileMessageServiceExceptionTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private ProfileMessageService service;

	@Autowired
	private ProducerFactory producerFactory;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() throws Exception {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Future sendMessage(Message message) throws Exception {
				throw new Exception("Test error message");
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}
	
	@Test(expected = Exception.class)
	public void testSendMessage() throws Exception {
		ProfileProcessingMessage mssg = new ProfileProcessingMessage();
		mssg.setProfileName("EQUITY");
		mssg.setPrimaryDataSources(Arrays.asList("trdse"));
		
		service.sendMessage(mssg);
	}
}
