/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleServiceImpl.java
 * Author:	Rushikesh Dedhia
 * Date:	03-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.Rule;
import com.smartstreamrdu.rules.RuleServiceInput;

public class RduRuleServiceImpl implements RuleService<RuleServiceInput> {

	private static final Logger _logger = LoggerFactory.getLogger(RduRuleServiceImpl.class);

	private RduRuleEngine ruleEngine = new RduRuleEngine();

	private RuleExecutionStrategyFactory ruleExecutionStrategyFactory ;

	
	/**
	 * @param appName
	 */
	public RduRuleServiceImpl(String appName) {
		super();
		ruleExecutionStrategyFactory = new RuleExecutionStrategyFactoryImpl(appName);
	}


	public List<DataContainer> applyRules(RecordWrapper recordWrapper, List<RduRule> rules, String dataSource,  String fileName) {

		List<DataContainer> dataContainers = null;
		if (recordWrapper == null){
			return null;
		}
		
		RuleExecutionStrategy ruleExecutionStrategy = ruleExecutionStrategyFactory
				.getRuleExecutionStrategy(recordWrapper, fileName,dataSource);

		if (ruleExecutionStrategy != null && CollectionUtils.isNotEmpty(rules)) {

			dataContainers = ruleExecutionStrategy.executeStrategy(recordWrapper, ruleEngine,
					filterRules(recordWrapper, rules));
			if (dataContainers.isEmpty()) {
				_logger.debug("Unable to create data containers for RecordWrapper : {} and rules : {}", recordWrapper,
						rules);
			}
		} else {
			_logger.warn("Could not applyRules for RecordWrapper: {} and rules : {}", recordWrapper, rules);
		}

		return dataContainers;
	}

	

	private boolean isRuleAplicableUsingFeedAttribute(RecordWrapper recordWrapper, List<String> vendorFields, List<String> messageTypes) {

		if (CollectionUtils.isEmpty(vendorFields) && CollectionUtils.isEmpty(messageTypes)) {
			return true;
		}

		// Check for Parent record
		boolean isApplicable = isRuleApplicableForRecord(recordWrapper.getParentRecord(), vendorFields, messageTypes);
		if (isApplicable) {
			// Parent has the attribute, hence we need to apply the rule
			return true;
		}
		List<Record> childRecords = recordWrapper.getChildRecords();

		if (CollectionUtils.isEmpty(childRecords)) {
			return false;
		}

		for (Record record : childRecords) {
			if (isRuleApplicableForRecord(record, vendorFields, messageTypes)) {
				return true;
			}
		}

		return false;

	}

	/**
	 * If the messageType passed in record object matches rduRules.messageTypes then fire the rule.
	 * <br>if messageType is not set and fallback to vendorFields comparison to fire the rule. 
	 * <br>We do not expect messageType and vendorFields to be present together 
	 * @param recordWrapper
	 * @param vendorFields
	 * @return
	 */
	private boolean isRuleApplicableForRecord(Record record, List<String> vendorFields, List<String> messageTypes) {
		String recordMessageType = record.getMessageType();
		if (recordMessageType != null && CollectionUtils.isNotEmpty(messageTypes)) {
			return messageTypes.contains(recordMessageType);
		}else if(CollectionUtils.isEmpty(vendorFields)) {
			return true;
		}
		
		for (String key : vendorFields) {
			if(record.containsDataAttribute(key)) {
				
				return true;
			}
		}
		return false;
	}

	/**
	 * @param v1
	 * @param rules
	 */
	private List<RduRule> filterRules(RecordWrapper v1, List<RduRule> rules) {
		List<RduRule> filterRules = new ArrayList<>();
		Iterator<RduRule> it = rules.iterator();

		while (it.hasNext()) {
			RduRule next = it.next();
			boolean applicable = isRuleAplicableUsingFeedAttribute(v1, next.getRuleFilter().getVendorFields(), next.getRuleFilter().getMessageTypes());
			if(applicable){
				filterRules.add(next);
			}

		}
		return filterRules;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.rules.RduRuleService#applyRules(com.smartstreamrdu.service.rules.RuleServiceInput, java.util.List)
	 */
	@Override
	public <T extends Serializable>T applyRules(RuleServiceInput input, List<? extends Rule> rules, String fileName) {
		if(input!=null){
			RduRuleContext context=((RduRuleServiceInput)input).getRduRuleContext();
				ruleEngine.initializeRuleEngine(rules, context);
			RecordWrapper recordWrapper=((RduRuleServiceInput)input).getRecordWrapper();
			return  (T) applyRules(recordWrapper, (List<RduRule>) rules, (String)context.getRuleContext().get("dataSource"), fileName);
			
		}
		return null;
	}


}
