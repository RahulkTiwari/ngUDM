/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleComponentEnum.java
 * Author:	Rushikesh Dedhia
 * Date:	03-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.rules.Rule;

public class RduRuleEngine implements RuleEngine {
	/**
	 * 
	 */
	private RduRuleExecutor ruleExecutor;
	

	@Override
	public <T> Serializable executeRule(T input, Rule rule) {
		//for new implementation we will be using input as a wrapper for record.
		if(input instanceof RduRuleServiceInput){
			return ruleExecutor.executeRule(rule, input);
		}
		
		RduRuleServiceInput ruleServiceInput = new RduRuleServiceInput();
		ruleServiceInput.setRecord((Record) input);
		return ruleExecutor.executeRule(rule, ruleServiceInput);
	}

	public void initializeRuleEngine(List<? extends Rule> rules, RduRuleContext context) {
		if (ruleExecutor == null) {
			ruleExecutor = new RduRuleExecutor();
		}
		ruleExecutor.initializeRuleExecutor(rules, context);
	}

	public void registerWithRuleEngine(String key, Object value) {
		if (ruleExecutor != null) {
			ruleExecutor.registerWithRuleExecutor(key, value);
		}
	}

}
