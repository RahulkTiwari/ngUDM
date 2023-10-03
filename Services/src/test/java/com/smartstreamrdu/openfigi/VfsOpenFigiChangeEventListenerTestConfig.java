/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : VfsOpenFigiChangeEventListenerTestConfig.java
 * Author :SaJadhav
 * Date : 02-Dec-2021
 */
package com.smartstreamrdu.openfigi;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.openfigi.FeedUpdatesVfsOpenFigiRequestService;
import com.smartstreamrdu.service.openfigi.IvoUpdatesVfsOpenFigiRequestService;

/**
 * @author SaJadhav
 *
 */
public class VfsOpenFigiChangeEventListenerTestConfig extends MongoConfig {
	
	@Bean
	@Primary
	public FeedUpdatesVfsOpenFigiRequestService feedUpdatesVfsOpenFigiRequestService() {
		return Mockito.mock(FeedUpdatesVfsOpenFigiRequestService.class);
	}
	
	@Bean
	@Primary
	public IvoUpdatesVfsOpenFigiRequestService ivoUpdatesVfsOpenFigiRequestService() {
		return Mockito.mock(IvoUpdatesVfsOpenFigiRequestService.class);
	}

}
