/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DomainServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	04-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class DomainServiceImpl implements DomainService {

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.domain.DomainService#
	 * getActiveStatusValueForVendor(com.smartstreamrdu.domain.DomainType)
	 */
	@Override
	public List<DomainType> getActiveStatusValueForVendor(DomainType datasource, DataAttribute statusAtt) throws UdmTechnicalException {
		return getStatusValueForVendor(datasource, statusAtt, DomainStatus.ACTIVE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.domain.DomainService#
	 * getInActiveStatusValueForVendor(com.smartstreamrdu.domain.DomainType,
	 * com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public List<DomainType> getInActiveStatusValueForVendor(DomainType datasource, DataAttribute statusAtt) throws UdmTechnicalException {
		return getStatusValueForVendor(datasource, statusAtt, DomainStatus.INACTIVE);
	}

	private List<DomainType> getStatusValueForVendor(DomainType datasource, DataAttribute statusAtt, String statusValue)
			throws UdmTechnicalException {
		String dsDomainSource = cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource.getVal());
		List<DomainType> status = cacheDataRetrieval.getVendorDomainValuesFromCache(
				DataAttributeFactory.getRduDomainForDomainDataAttribute(statusAtt), dsDomainSource, statusValue);
		return (status == null || status.isEmpty()) ? null : status;
	}

}
