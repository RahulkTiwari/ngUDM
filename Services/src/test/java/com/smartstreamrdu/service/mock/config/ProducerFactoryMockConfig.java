/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: ProducerFactoryMockConfig.java
 * Author : AThanage
 * Date : Dec 10, 2021
 * 
 */
package com.smartstreamrdu.service.mock.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;

/**
 * @author AThanage
 *
 */
public class ProducerFactoryMockConfig extends MongoConfig{

	@Bean
	@Primary
	public ProducerFactory ProducerFactory() {
		return Mockito.mock(ProducerFactory.class);
	}
}
