/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    ReactorKafkaReceiverImplTestConfig.java
 * Author:	Padgaonkar
 * Date:	08-April-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.reactor.kafka;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;

import reactor.kafka.receiver.KafkaReceiver;

@ActiveProfiles("testEns")
public class ReactorKafkaReceiverImplTestConfig extends MongoConfig{

	@SuppressWarnings("rawtypes")
	@Bean
	@Primary
	public KafkaReceiver getKafkaReceiver() {
		return Mockito.mock(KafkaReceiver.class);
	}
}

