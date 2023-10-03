/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedConfigurationTestConfig.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.feed;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.repository.FeedConfigurationRepository;

/**
 * @author Jay Sangoi
 *
 */
@Profile("test")
public class FeedConfigurationTestConfig extends MongoConfig {

	@Bean
	@Primary
	public FeedConfigurationRepository feedConfigurationRepository() {
		return Mockito.mock(FeedConfigurationRepository.class);
	}

}
