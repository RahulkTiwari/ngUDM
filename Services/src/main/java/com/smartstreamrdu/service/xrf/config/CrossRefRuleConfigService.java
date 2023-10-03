/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	CrossRefRuleConfigService.java
 * Author:	Jay Sangoi
 * Date:	04-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.xrf.config;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import com.smartstreamrdu.commons.xrf.XrfRefRuleDef;
import com.smartstreamrdu.commons.xrf.XrfRuleAttributeDef;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.CacheService;

/**
 * Service for working with Cross Reference Rules
 * 
 * @author Jay Sangoi
 *
 */
public interface CrossRefRuleConfigService {

	/**
	 * Get all the rules
	 * 
	 * @return
	 */
	@Cacheable(sync=true, cacheManager = CacheService.CACHE_MANAGER, value = CacheService.CACHE_CROSS_REF_RULES)
	List<XrfRefRuleDef> getAllRules();
	
	/**
	 * Get all the rules for an XRF data level
	 * 
	 * @return
	 */
	@Cacheable(sync=true, cacheManager = CacheService.CACHE_MANAGER, value = CacheService.CACHE_CROSS_REF_RULES)
	List<XrfRefRuleDef> getRules(DataLevel level);

	/**
	 * Get all the cross reference attributes
	 * 
	 * @return
	 */
	@Cacheable(sync=true, cacheManager = CacheService.CACHE_MANAGER, value = CacheService.CACHE_CROSS_REF_ALL_FIELDS)
	List<XrfRuleAttributeDef> getAllCrossRefAttributes();
	
	/**
	 * Get Mandatory and Optional cross reference attributes;
	 * @return
	 */
	@Cacheable(sync=true, cacheManager = CacheService.CACHE_MANAGER, value = CacheService.CACHE_CROSS_REF_FIELDS)
	List<XrfRuleAttributeDef> getMandatoryAndOptionalCrossRefAttributes();

	/**
	 * @return
	 */
	@Cacheable(sync=true, cacheManager = CacheService.CACHE_MANAGER, value = CacheService.CACHE_CROSS_REF_PERSISTENCE_FIELDS)
	List<XrfRuleAttributeDef> getAllAttributestoPersist();

	/**
	 * @param sdlevel
	 * @return
	 */
	@Cacheable(sync=true, cacheManager = CacheService.CACHE_MANAGER, value = CacheService.CACHE_CROSS_REF_RULES_BY_LEVEL)
	List<XrfRuleAttributeDef> getAttributesBySdLevel(DataLevel sdlevel);

	/**
	 * @param ruleName
	 * @return
	 */
	List<XrfRuleAttributeDef> getToBeNullAttributes(String ruleName);

	/**
	 * @param name
	 * @return
	 */
	XrfRefRuleDef getRuleConfig(String name);
	
	List<XrfRuleAttributeDef> getAdditionalAttribute();
	
	List<XrfRuleAttributeDef> getApplicableAttributesForLevel(DataLevel level);

}