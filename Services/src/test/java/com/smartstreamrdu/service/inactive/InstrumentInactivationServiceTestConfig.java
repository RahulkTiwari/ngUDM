/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : InstrumentInactivationServiceTestConfig.java
 * Author :SaJadhav
 * Date : 19-May-2021
 */
package com.smartstreamrdu.service.inactive;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.kafka.KafkaProducer;

/**
 * @author SaJadhav
 *
 */
@Profile("EmbeddedMongoTest")
public class InstrumentInactivationServiceTestConfig extends MongoConfig{

	@Bean("KafkaProducer")
	@Primary
	public KafkaProducer kafkaProducer(){
		return Mockito.mock(KafkaProducer.class);
	}
}
