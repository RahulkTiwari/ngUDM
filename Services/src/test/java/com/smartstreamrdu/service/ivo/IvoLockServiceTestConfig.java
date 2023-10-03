/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoLockServiceTestConfig.java
 * Author : SaJadhav
 * Date : 01-Oct-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.listener.ListenerService;

/**
 * @author SaJadhav
 *
 */
@Profile("EmbeddedMongoTest")
public class IvoLockServiceTestConfig extends MongoConfig{
	@Bean
	@Primary
	public ListenerService listenerService() {
		return Mockito.mock(ListenerService.class);
	}
}
