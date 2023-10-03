/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleEngine.java
 * Author:	Divya Bharadwaj
 * Date:	27-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.rules.Rule;

/**
 * @author Bharadwaj
 *
 */
public interface RuleEngine {
	/**
	 * 
	 */

	public abstract <T> Serializable executeRule(T inputRecord, Rule rule);
	
	public abstract void initializeRuleEngine(List<? extends Rule> rules, RduRuleContext context) ;
	
}
