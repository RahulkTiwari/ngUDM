/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StaticDataTestConfig.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.staticdata;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.cache.CacheRepository;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author Jay Sangoi
 *
 */
@Profile("test")
public class StaticDataTestConfig  extends MongoConfig {

	@Bean
	@Primary
	public CacheRepository cacheRepository() {
		return Mockito.mock(CacheRepository.class);
	}
	
}
