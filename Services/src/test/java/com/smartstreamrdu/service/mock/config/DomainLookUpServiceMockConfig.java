/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DomainLookUpServiceMockConfig.java
 * Author :SaJadhav
 * Date : 14-Dec-2021
 */
package com.smartstreamrdu.service.mock.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.domain.DomainLookupService;

/**
 * @author SaJadhav
 *
 */
public class DomainLookUpServiceMockConfig extends MongoConfig {
	
	@Bean
	@Primary
	public DomainLookupService domainLookUpService(){
		return Mockito.mock(DomainLookupService.class);
	}
}
