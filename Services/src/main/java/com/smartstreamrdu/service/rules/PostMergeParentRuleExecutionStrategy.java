/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    PostMergeParentRuleExecutionStrategy.java
 * Author:  Padgaonkar
 * Date:    Feb 02, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.rules.RduRule;

/**
 * This strategy is applicable for parent level attributes.This strategy execute
 * rules & set output in dataContainer.
 * 
 * @author Padgaonkar
 *
 */
public class PostMergeParentRuleExecutionStrategy extends ParentRecordRuleExecutionStrategy
		implements LoaderRuleExecutionStrategy {

	private static final long serialVersionUID = 1L;
	
	public PostMergeParentRuleExecutionStrategy(String fileName, String prgm, LocalDateTime date, String dataSource) {
		super(fileName, prgm, date, dataSource);
	}

	/**
	 * execute rule for an attribute and then add the rule output the dataContainer 
	 */
	@Override
	public void executeStrategy(RduRuleServiceInput input, RduRuleEngine ruleEngine, RduRule rule) {
		Serializable ruleResult = ruleEngine.executeRule(input, rule);
		
		
		String attributeName = rule.getRuleOutput().getAttributeName();
		String level = rule.getRuleOutput().getLockLevel();
		DataAttribute dataAttribute = RuleUtil.getRuleDataAttibute(rule,attributeName);
		LockLevel lockLevel = LockLevel.getLevelForName(level);
		
		
		// Since merging of data containers has already been completed before this point, this rule service 
		// will keep on adding data values to the data container creating unnecessary changes to the data
		// container that later reflect as additional Audits. Refer JIRA UDM-50741
		// To solve this issue, we will be adding the value to the data container only if there is
		// no same value available in the data container already for the same lock level.
		if (!checkIfSameValeAlreadyExists(ruleResult, dataAttribute, lockLevel, input.getDataContainer())) {
			addValueToDataContainer(rule, ruleResult, input.getDataContainer());
		}
		
	}
	
	
	/**
	 *  This method checks if there is already a value available for the same lock level
	 *  for the given data attribute.
	 *  
	 *  If yes then check if the values are same.
	 *  
	 *  Returns true if values match. Else returns false.
	 *  
	 * @param ruleResult
	 * @param dataAttribute
	 * @param lockLevel
	 * @param dataContainer
	 * @return
	 */
	private boolean checkIfSameValeAlreadyExists(Serializable ruleResult, DataAttribute dataAttribute,
			LockLevel lockLevel, DataContainer dataContainer) {
		Serializable value = dataContainer.getAttributeValueAtLevel(lockLevel, dataAttribute);
		return value != null && validateValue(dataAttribute, ruleResult, value, dataSource);
	}
}