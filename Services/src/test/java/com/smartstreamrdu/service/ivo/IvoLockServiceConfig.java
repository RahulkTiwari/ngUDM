/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoLockServiceConfig.java
 * Author : SaJadhav
 * Date : 08-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.listener.ListenerService;

/**
 * @author SaJadhav
 *
 */
public class IvoLockServiceConfig extends MongoConfig{
	
	@Bean
	@Primary
	public ListenerService listenerService() {
		return Mockito.mock(ListenerService.class);
	}

}
