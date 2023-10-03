/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DomainLookupServiceImpl.java
 * Author:	Rushikesh Dedhia
 * Date:	07-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.domain;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.repository.LookupWrapper;

@Component
public class DomainLookupServiceImpl implements DomainLookupService {


	private static final Logger _logger = LoggerFactory.getLogger(DomainLookupServiceImpl.class);

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval ;

	private static final String EXCEPTION_MESSAGE = "Following error occured while getting value from cache";
	
	@Override
	public Serializable getNormalizedValueForDomainValue(DomainType domainData, String domainSource, String domainName, String rduDomainName) {

		LookupWrapper lookupKey = new LookupWrapper(domainSource, domainName, rduDomainName, domainData);

		Serializable value = null;
		try {
			value = cacheDataRetrieval.getNormalizedValFromCache(lookupKey);
		} catch (Exception e) {
			_logger.error(EXCEPTION_MESSAGE, e);
		}

		return value;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.domain.DomainLookupService#getDomainSourceFromDataSource(java.lang.String)
	 */
	@Override
	public String getDomainSourceFromDataSource(String datasource) throws UdmTechnicalException {
		return cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource);
	}

	@Override
	public Integer getNormalizedErrorCodeForDomainValue(DomainType domainData, String domainSource, String domainName,
			String rduDomainName) {
		LookupWrapper lookupKey = new LookupWrapper(domainSource, domainName, rduDomainName, domainData);

		Integer value = null;
		try {
			value = cacheDataRetrieval.getNormalizedErrorCodeFromCache(lookupKey);
		} catch (Exception e) {
			_logger.error(EXCEPTION_MESSAGE, e);
		}

		return value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getPatternMatchNormalizedValueForDomainValue(DomainType domainData, String domainSource,
			String rduDomainName) {
		DomainType domainValue = cacheDataRetrieval.getDomainValueFromCacheByPatternMatch(domainData.getDomain(),
				domainData.getVal(), domainData.getVal2(), domainSource, rduDomainName);

		if (domainValue == null) {
			return null;
		}
		return domainValue.getNormalizedValue();
	}
}
