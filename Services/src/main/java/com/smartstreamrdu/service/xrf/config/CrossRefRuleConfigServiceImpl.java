/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    CrossRefRuleConfigServiceImpl.java
 * Author:	Shruti Arora
 * Date:	09-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.xrf.config;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.xrf.XrfRefRuleDef;
import com.smartstreamrdu.commons.xrf.XrfRuleAttributeDef;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.CrossRefConstants;

/**
 * Services around XRF Rules configuration. This includes services like
 * attributes applicable for a rule, attributes used for persistence,
 * Attributes per level etc.
 *
 */
@Component
public class CrossRefRuleConfigServiceImpl implements CrossRefRuleConfigService {

	/**
	 * 
	 */
	
	private static final  Logger _logger = LoggerFactory.getLogger(CrossRefRuleConfigServiceImpl.class);

	@Autowired
	private  MongoTemplate mongoTemplate;

	private List<XrfRefRuleDef> rules = null;
	private List<XrfRuleAttributeDef> allAttributesList = null;
	private List<XrfRuleAttributeDef> mandatoryAndOptionalAttributesList = null;
	private List<XrfRuleAttributeDef> allAttributesListforPersist = null;
	private Map<String, List<XrfRuleAttributeDef>> priorityAttributeMap = null;
	private Map<String, XrfRefRuleDef> ruleConfigMap = new HashMap<>();

	private void ensureMongoTemplate() {
		if (mongoTemplate == null) {
			mongoTemplate = new AnnotationConfigApplicationContext(MongoConfig.class).getBean(MongoTemplate.class);
		}
	}

	@PostConstruct
	private void init() {
		populateConfiguration();
		populateRules();
		populateRuleMandatoryNullAttributeMap();
	}
	
	private void populateConfiguration() {
		if (ruleConfigMap.isEmpty()) {
			List<XrfRefRuleDef> docs = getAllRules();
			docs.forEach(x -> ruleConfigMap.put(x.getRuleName(), x));
		}
	}

	/**
	 * @return the ruleConfig
	 * 
	 */
	protected Map<String, XrfRefRuleDef> getRuleConfig() {
		populateConfiguration();
		return ruleConfigMap;
	}

	private void populateRules()  {
		if (rules == null) {
			ensureMongoTemplate();
			List<XrfRefRuleDef> crRules = mongoTemplate.findAll(XrfRefRuleDef.class, "crossReferenceRules");
			Collections.sort(crRules);
			this.rules = new ArrayList<>(crRules);
		}
	}

	/**
	 * Gets All CrossReference rules
	 * @return
	 */
	@Override
	public List<XrfRefRuleDef> getAllRules() {
		try {
			populateRules();
			return Collections.unmodifiableList(rules);
		} catch (Exception e) {
			_logger.error("Error in fetching Cross Reference Rules From DataBase ", e);
			return new ArrayList<>();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.smartstreamrdu.service.xrf.config.CrossRefRuleConfigService#getRules(com.smartstreamrdu.domain.DataLevel)
	 */
	@Override
	public List<XrfRefRuleDef> getRules(DataLevel level) {
		if (!DataLevel.XRF_LEVELS.contains(level)) {
			throw new IllegalArgumentException("Parameter level must be an XRF level but received: " + level);
		}
		return getAllRules().stream().filter(l -> l.getDataLevel() == level).collect(Collectors.toList());
	}

	/**
	 * Gets all XRF attributes - Mandatory, Optional and Validation attributes
	 * This contains no duplicate attributes
	 * @return Attribute list
	 */
	@Override
	public List<XrfRuleAttributeDef> getAllCrossRefAttributes() {
		createAllAttributesList();
		return allAttributesList;
	}
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.xrf.config.CrossRefRuleConfigService#getMandatoryAndOptionalCrossRefAttributes()
	 */
	@Override
	public List<XrfRuleAttributeDef> getMandatoryAndOptionalCrossRefAttributes() {
		createMandatoryAndOptionalAttributesList();
		return mandatoryAndOptionalAttributesList;
	}

	/**
	 * Create a list of all the Mandatory and Optional Attributes
	 */
	private void createMandatoryAndOptionalAttributesList() {
		if (mandatoryAndOptionalAttributesList != null) {
			return;
		}
		Set<XrfRuleAttributeDef> mandatoryAndOptionalAttributes = new HashSet<>();
		getAllRules().forEach(x -> {
			addMandatoryAttributes(mandatoryAndOptionalAttributes, x);
			addOptionalAttributes(mandatoryAndOptionalAttributes, x);
		});
		if (!mandatoryAndOptionalAttributes.isEmpty()) {
			mandatoryAndOptionalAttributesList = new ArrayList<>(mandatoryAndOptionalAttributes);
		}
	}

	/**
	 * Gets all XRF attributes - Mandatory and Optional used for Persistence
	 * This contains no duplicate attributes
	 * @return Attribute list
	 */
	@Override
	public List<XrfRuleAttributeDef> getAllAttributestoPersist() {
		createAllAttributesListToPersist();
		return allAttributesListforPersist;
	}

	private void createAllAttributesList() {
		if (allAttributesList != null) {
			return;
		}
		Set<XrfRuleAttributeDef> allAttributes = new HashSet<>();
		getAllRules().forEach(x -> {
			addMandatoryAttributes(allAttributes, x);
			addOptionalAttributes(allAttributes, x);
			addValidationalAttributes(allAttributes, x);
		});
		if (!allAttributes.isEmpty()) {
			allAttributesList = new ArrayList<>(allAttributes);
		}
	}

	private void addValidationalAttributes(Set<XrfRuleAttributeDef> allAttributes, XrfRefRuleDef x) {
		if (CollectionUtils.isNotEmpty(x.getValidationAttributes())) {
			allAttributes.addAll(x.getValidationAttributes());
		}
	}

	private void addOptionalAttributes(Set<XrfRuleAttributeDef> allAttributes, XrfRefRuleDef x) {
		if (CollectionUtils.isNotEmpty(x.getOptionalAttributes())) {
			allAttributes.addAll(x.getOptionalAttributes());
		}
	}

	private void addMandatoryAttributes(Set<XrfRuleAttributeDef> allAttributes, XrfRefRuleDef x) {
		if (CollectionUtils.isNotEmpty(x.getMandatoryAttributes())) {
			allAttributes.addAll(x.getMandatoryAttributes());
		}
	}

	private void createAllAttributesListToPersist() {
		if (allAttributesListforPersist != null) {
			return;
		}
		Set<XrfRuleAttributeDef> allAttributes = new HashSet<>();
		getAllRules().forEach(x -> {
			addMandatoryAttributes(allAttributes, x);
			addOptionalAttributes(allAttributes, x);
		});
		if (!allAttributes.isEmpty()) {
			allAttributesListforPersist = new ArrayList<>(allAttributes);
		}
	}

	@Override
	public List<XrfRuleAttributeDef> getAttributesBySdLevel(DataLevel sdlevel) {
		String sdLvl = sdlevel.name();
		List<XrfRuleAttributeDef> allRules = getAllCrossRefAttributes();
		if (allRules == null) {
			return new ArrayList<>();
		}

		List<XrfRuleAttributeDef> levelRules = new ArrayList<>();
		getAllCrossRefAttributes().forEach(x -> {
			if (x.getSdAttributeLevel() != null && x.getSdAttributeLevel().trim().equals(sdLvl)) {
				levelRules.add(x);
			}
		});
		return levelRules;
	}

	@Override
	public List<XrfRuleAttributeDef> getToBeNullAttributes(String ruleName) {
		populateRuleMandatoryNullAttributeMap();
		return priorityAttributeMap.get(ruleName);
	}

	/**
	 * Populates those attributes which should be null for a rule to match
	 * 
	 * For example:populateRuleMandatoryNullAttributeMap
	 * RULE 1 - SEDOL + EXCHANGE 
	 * RULE 2 - ISIN + EXCHANGE + CURRENCY
	 * SEDOL is mandatory null attribute for RULE 2
	 * 
	 * @throws Exception
	 */
	private void populateRuleMandatoryNullAttributeMap() {
		if (priorityAttributeMap != null) {
			return;
		}
		populateRules();
		
		// Rule vs Mandatory attributes not applicable to current rule
		// For example 
		// RULE 1 - SEDOL + EXCHANGE 
		// RULE 2 - ISIN + EXCHANGE + CURRENCY
		// SEDOL is mandatory null attribute for RULE 2
		Map<String, List<XrfRuleAttributeDef>> pMap = new HashMap<>(this.rules.size());
		
		// Collection which holds the mandatory attribute for the previous rule
		// This will be level specific
		Map<DataLevel, List<XrfRuleAttributeDef>> tmpPrev = new EnumMap<>(DataLevel.class);
		
		// Sort the rules by their priority since the attribute level is important
		// For example for ISIN + EXCHANGE + CURRENCY,
		// SEDOL in the the matched document must be null
		Collections.sort(this.rules);

		for (XrfRefRuleDef rule : this.rules) {
			DataLevel level = rule.getDataLevel();
			List<XrfRuleAttributeDef> prevSecAttrList = tmpPrev.getOrDefault(level, new ArrayList<>());
			
			// Retain current mandatory attributes but remove the
			// ones which were mandatory in a higher priority rule
			// but not present in the current rule
			prevSecAttrList.removeAll(rule.getMandatoryAttributes());
			
			// Clone it because the list is about to Change
			ArrayList<XrfRuleAttributeDef> clone = new ArrayList<>(prevSecAttrList);
			pMap.put(rule.getRuleName(), Collections.unmodifiableList(clone));
			
			// Always append to the previous list
			prevSecAttrList.addAll(rule.getMandatoryAttributes());
			
			// Keep tmp ready for next rule
			tmpPrev.put(level, prevSecAttrList);
		}
		
		this.priorityAttributeMap = pMap;
	}

	@Override
	public XrfRefRuleDef getRuleConfig(String name) {
		populateConfiguration();
		XrfRefRuleDef crossRefRule = ruleConfigMap.get(name);
		Objects.requireNonNull(crossRefRule, () -> "No rule found with name: " + name);
		return crossRefRule;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.xrf.config.CrossRefRuleConfigService#getAdditionalAttribute()
	 */
	@Deprecated
	@Override
	public List<XrfRuleAttributeDef> getAdditionalAttribute() {
		return asList(
				create(CrossRefConstants.XR_SEC_LINK_STATUS, "securityStatus", DataLevel.XRF_SEC, DataLevel.SEC),
				create(CrossRefConstants.XR_INS_LINK_STATUS, "instrumentStatus", DataLevel.XRF_INS, DataLevel.INS));
	}
	
	private XrfRuleAttributeDef create(String xrfAttrName, String sdAttributeName, DataLevel xrfLevel , DataLevel sdLevel) {
		XrfRuleAttributeDef crAttr = new XrfRuleAttributeDef();
		crAttr.setAttributeLevel(xrfLevel.name());
		crAttr.setAttributeName(xrfAttrName);
		crAttr.setSdAttributeLevel(sdLevel.name());
		crAttr.setSdAttributeName(sdAttributeName);
		return crAttr;
	}
	
	@Override
	public  List<XrfRuleAttributeDef> getApplicableAttributesForLevel(DataLevel level){
		List<XrfRuleAttributeDef> attributesBySdLevel = getAttributesBySdLevel(level);
		
		Iterator<XrfRuleAttributeDef> iterator = attributesBySdLevel.iterator();
		//TODO : Remove asset type from 
		while(iterator.hasNext()){
			XrfRuleAttributeDef next = iterator.next();
			if(Constant.CrossRefConstants.XRF_INS_TYPE_CODE.equals(next.getAttributeName())){
				iterator.remove();
			}
		}
		
		return attributesBySdLevel;
	}

}
