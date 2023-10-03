/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	NormalizedValueServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.normalized;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.cache.RuleNormalizationStrategyCache;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.rules.RuleNormalizationStrategy;
import com.smartstreamrdu.service.domain.DomainLookupService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jay Sangoi
 *
 */
@Component
@Slf4j
public class NormalizedValueServiceImpl implements NormalizedValueService{

	/**
	 * 
	 */
	private static final String DEFAULT = "Default";

	/**
	 * 
	 */
	private static final long serialVersionUID = -5644412851532912435L;

	private transient DomainLookupService domainService ;
	
	private static final Logger logger = LoggerFactory.getLogger(NormalizedValueServiceImpl.class);
	

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;
	
	@Autowired
	private RuleNormalizationStrategyCache strategyCache;
	
	public void initialize(){
		if(domainService == null){
			domainService = SpringUtil.getBean(DomainLookupService.class);
		}
		if(cacheDataRetrieval == null ){
			cacheDataRetrieval = SpringUtil.getBean(CacheDataRetrieval.class);
		}
		if(strategyCache == null ){
			strategyCache = SpringUtil.getBean(RuleNormalizationStrategyCache.class);
		}
	}
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.normalized.NormalizedValueService#getNormalizedValue(com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DomainType, java.lang.String)
	 */
	@Override
	public Serializable getNormalizedValueForDomainValue(DataAttribute dataAttribute, DomainType domainData,
			String dataSource) {
		initialize();
		String domainSource = null;
		try {
			domainSource = cacheDataRetrieval.getDataSourceDomainSourceFromCode(dataSource);
		} catch (Exception e) {
			logger.error("Following error occured while fetching domainSource for dataSource : {}", dataSource, e);
			return null;
		}

		RuleNormalizationStrategy strategy = strategyCache.getStrategy(dataSource, dataAttribute.getAttributeName());	
		
		if(strategy == null ) {
			log.error("No normalization strategy found for dataSource : {} , dataAttribte : {}",dataSource,dataAttribute.getAttributeName());
			return null;
		}
		String rduDomain = DataAttributeFactory.getRduDomainForDomainDataAttribute(dataAttribute);
		
		switch (strategy) {
		case DOMAIN_REGEX_RESOLVER:
			return getNormalizedValue(domainData, domainSource, rduDomain);
		case DEFAULT:
			return domainService.getNormalizedValueForDomainValue(domainData, domainSource, null, rduDomain);
		default:
			log.error("Specified ruleNormalizationStrategy : {} is not configured in system:", strategy);
			return null;
		}
	}
	/**
	 * @param domainData
	 * @param domainSource
	 * @param rduDomain
	 * @return
	 */
	private Serializable getNormalizedValue(DomainType domainData, String domainSource, String rduDomain) {
		Serializable normValue = domainService.getPatternMatchNormalizedValueForDomainValue(domainData, domainSource,
				rduDomain);
		if (normValue == null) {
			DomainType defaultDomainData = new DomainType(DEFAULT, DEFAULT, null,domainData.getDomain());
			return domainService.getPatternMatchNormalizedValueForDomainValue(defaultDomainData, domainSource,
					rduDomain);
		} 
		return normValue;
	}
	

}
