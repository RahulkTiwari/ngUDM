/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StaticDataServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	08-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.staticdata;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.cache.CacheRepository;
import com.smartstreamrdu.util.Constant.StaticDataConstant;

/**
 * Implementation for Static Data Service
 * 
 * @author Jay Sangoi
 *
 */
@Component
public class StaticDataServiceImpl implements StaticDataService {

	@Autowired
	private CacheRepository cacheService;
	
	@Autowired
	private CacheDataRetrieval dataRetrievalService;
	
	private static final Logger _logger = LoggerFactory.getLogger(StaticDataServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.staticdata.StaticDataService#getDataByCode(com
	 * .smartstreamrdu.domain.DataLevel)
	 */
	@Override
	public Serializable getDataByCode(DataLevel level, String requiredAttributeName, Serializable value) throws UdmBaseException {
		
		return dataRetrievalService.getDataFromCache(requiredAttributeName, level.getCollectionName(), StaticDataConstant.CODE_ATT_NAME,(String)value);

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.staticdata.StaticDataService#getDataByCode(com
	 * .smartstreamrdu.domain.DataLevel, com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public Serializable getDataByCode(DataLevel level, DataAttribute requiredAttributeName, Serializable value)	throws UdmBaseException {
		
		return dataRetrievalService.getDataFromCache(requiredAttributeName.getAttributeName(), level.getCollectionName(), StaticDataConstant.CODE_ATT_NAME,(String)value);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.xrf.config.CrossRefAttributeConfigService#
	 * getXrfSourcePriority(java.lang.String)
	 */
	@Override
	public Optional<Integer> getXrfSourcePriority(String dataSourceName) throws UdmTechnicalException {
		String dsPriority = cacheService.getXrfSourcePriority(dataSourceName);
		Integer result = StringUtils.isNotEmpty(dsPriority) ? Integer.parseInt(dsPriority) : null;
		return Optional.ofNullable(result);
	}
	
	public List<String> getFieldsForExceptionType(String exceptionType) throws UdmTechnicalException{
		String fields = cacheService.getExceptionFields(exceptionType);
		return Arrays.asList(fields.split(","));		
	}
	
	public String getExceptionSubCategoryExceptionType(String exceptionType) {
		try {
			return cacheService.getSubCategoryForExceptionType(exceptionType);
		} catch (UdmTechnicalException e) {
			_logger.error("Following error occured.",e);
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 *  This method return the DataContainer object for the given exception type from the 
	 *  UdmExcecptionConfig collection.
	 * @param exceptionType
	 * @return
	 * @throws UdmTechnicalException
	 */
	public DataContainer getExceptionConfig(String exceptionType) throws UdmTechnicalException{
		return cacheService.getUdmExceptionConfig(exceptionType);
	}

	@Override
	public Boolean getIsExceptionAutoClosable(String exceptionType) throws UdmTechnicalException {
		return Boolean.valueOf(cacheService.getIsExceptionAutoClosable(exceptionType));
	}

	@Override
	public boolean getIsExceptionValidForReprocessing(String exceptionType) throws UdmTechnicalException {
		return Boolean.valueOf(cacheService.getIsExceptionValidForReporcessing(exceptionType));
	}
}
