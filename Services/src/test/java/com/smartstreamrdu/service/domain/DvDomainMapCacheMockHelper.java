/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DvDomainMapCacheMockHelper.java
 * Author:	Jay Sangoi
 * Date:	04-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class DvDomainMapCacheMockHelper {

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;
	
	public void createMockDataForStatus(String datasource, DataAttribute status) throws Exception{
		DomainType activeDt = new DomainType("1");
		DomainType inactiveDt = new DomainType("0");

		List<DomainType> activeStatusDomain = new ArrayList<>();
		activeStatusDomain.add(activeDt);

		List<DomainType> inactiveStatusDomain = new ArrayList<>();
		inactiveStatusDomain.add(inactiveDt);

		Mockito.when(cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource)).thenReturn("trds");
		String dsDomainSource = cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource);
		Mockito.when(cacheDataRetrieval.getVendorDomainValuesFromCache(
				DataAttributeFactory.getRduDomainForDomainDataAttribute(status), dsDomainSource,
				DomainStatus.ACTIVE)).thenReturn(activeStatusDomain);
		Mockito.when(cacheDataRetrieval.getVendorDomainValuesFromCache(
				DataAttributeFactory.getRduDomainForDomainDataAttribute(status), dsDomainSource,
				DomainStatus.INACTIVE)).thenReturn(inactiveStatusDomain);
	}

}
