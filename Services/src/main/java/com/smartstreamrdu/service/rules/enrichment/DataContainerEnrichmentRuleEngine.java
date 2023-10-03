/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerEnrichmentRuleEngine.java
 * Author :SaJadhav
 * Date : 31-Mar-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.rules.Rule;
import com.smartstreamrdu.service.rules.RduRuleContext;
import com.smartstreamrdu.service.rules.RuleEngine;
import com.smartstreamrdu.service.rules.RuleExecutor;

/**
 * Rule engine for executing DataContainer enrichment rules
 * 
 * @author SaJadhav
 *
 */
public class DataContainerEnrichmentRuleEngine implements RuleEngine {
	
	private RuleExecutor ruleExecutor;

	@Override
	public <T> Serializable executeRule(T inputRecord, Rule rule) {
		if(ruleExecutor!=null) {
			return ruleExecutor.executeRule(rule, inputRecord);
		}
		return null;
		
	}

	@Override
	public void initializeRuleEngine(List<? extends Rule> rules, RduRuleContext context) {
		if (ruleExecutor == null) {
			ruleExecutor = new DataContainerEnrichmentRuleExecutor();
		}
		ruleExecutor.initializeRuleExecutor(rules, context);
	}

}
