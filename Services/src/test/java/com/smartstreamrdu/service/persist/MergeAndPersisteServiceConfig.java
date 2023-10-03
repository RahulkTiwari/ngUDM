/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergeAndPersisteServiceConfig.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.io.Serializable;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.cache.initializer.DataContainerCacheStore;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.events.DomainUpdateEventListener;
import com.smartstreamrdu.service.filter.DataFilterChainService;
import com.smartstreamrdu.service.id.generator.ObjectIdGenerator;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.lookup.LookupService;

/**
 * @author Jay Sangoi
 *
 */
@Profile("EmbeddedMongoTest")
public class MergeAndPersisteServiceConfig extends MongoConfig {

	@Bean
	@Primary
	public ListenerService listenerService() {
		return Mockito.mock(ListenerService.class);
	}
	
	@Bean
	@Primary
	public PersistenceService persistencService() {
		return Mockito.mock(PersistenceService.class);
	}
	
	@Bean
	@Primary
	public DomainUpdateEventListener domainUpdateEventListener() {
		return Mockito.mock(DomainUpdateEventListener.class);
	}
	
	@SuppressWarnings("unchecked")
	@Bean
	@Primary
	public ObjectIdGenerator<Serializable> generator() {
		return Mockito.mock(ObjectIdGenerator.class);
	}
	
	@Bean
	@Primary
	public LookupService lookupService() {
		return Mockito.mock(LookupService.class);
	}
	
	@Bean
	@Primary
	public DataFilterChainService filterSerivice() {
		return Mockito.mock(DataFilterChainService.class);
	}
	
	@Bean
	@Primary
	public DataContainerCacheStore ContainerCacheStore() {
		return Mockito.mock(DataContainerCacheStore.class);
	}
	
	@Bean
	@Primary
	public CacheDataRetrieval cacheDataRetrieval() {
		return Mockito.mock(CacheDataRetrieval.class);
	}
	
}
