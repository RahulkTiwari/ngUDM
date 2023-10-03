/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleExecutor.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.rules.Rule;

public interface RuleExecutor {

	/**
	 *  This method will execute the rule passed with the given data in the record object.
	 *  The execution of the rule will be based as per which ruleExecutor is being used.
	 * @param <T>
	 * @param rduRule
	 * @param record
	 * @return
	 */
	<T> Serializable executeRule(Rule rduRule, T record);
	
	/**
	 *  This method will initialize the ruleExecutor.
	 *  Things such as initializing the underlying script engine, compiling the rules with the engine,
	 *  registration of the prerequisite script language functions etc will happen in this method. 
	 * @param rules
	 */
	void initializeRuleExecutor (List<? extends Rule> rules, RduRuleContext context);

	void registerWithRuleExecutor(String key, Object value);
	
}
