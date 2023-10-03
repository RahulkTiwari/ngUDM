/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ServiceTestConfiguration.java
 * Author:	Jay Sangoi
 * Date:	16-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.feed.FeedConfigurationService;
import com.smartstreamrdu.service.inactive.InactiveService;
import com.smartstreamrdu.service.normalized.NormalizedValueService;

/**
 * @author Jay Sangoi
 *
 */
@Profile("test")
public class ServiceTestConfiguration extends MongoConfig {

	@Bean
	@Primary
	public CacheDataRetrieval dvDomainMapCache() {
		return Mockito.mock(CacheDataRetrieval.class);
	}

	@Bean
	@Primary
	public NormalizedValueService normalizedValueService() {
		return Mockito.mock(NormalizedValueService.class);
	}

	@Bean
	@Primary
	public  DomainLookupService domainService(){
		return Mockito.mock(DomainLookupService.class);
	}
	
	@Bean
	@Primary
	public FeedConfigurationService feedConfigurationService() {
		return Mockito.mock(FeedConfigurationService.class);
	}
	
	@Bean
	@Primary
	public  InactiveService inactiveService(){
		return Mockito.mock(InactiveService.class);
	}
	
	
	@Bean
	@Primary
	public UdmSystemPropertiesCache udmSystemPropertiesCache() {
		return Mockito.mock(UdmSystemPropertiesCache.class);
	}
	 
	
}
