/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    ReactorKafkaReceiverImplTest.java
 * Author:	Padgaonkar
 * Date:	08-April-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.reactor.kafka;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.kafka.KafkaConfigurationImpl;

import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ReactorKafkaReceiverImplTestConfig.class })
public class ReactorKafkaReceiverImplTest {

	@Autowired
	private ReactorKafkaReceiverImpl kafkaReceiver;

	@SuppressWarnings("rawtypes")
	@Autowired
	KafkaReceiver receiver;

	@Test
	public void test() {
		KafkaConfigurationImpl consumer = SpringUtil.getBean(KafkaConfigurationImpl.class);
		ReactorKafkaReceiverInput input = new ReactorKafkaReceiverInput("newTopic",consumer.createConsumerConfiguration());
		Flux<Serializable> fluxMap = kafkaReceiver.receive(input);
		Assert.assertNotNull(fluxMap);
	}
}
