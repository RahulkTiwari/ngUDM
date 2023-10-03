/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    LoaderRuleExecutionStrategy.java
 * Author:  Padgaonkar
 * Date:    Feb 02, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import com.smartstreamrdu.rules.RduRule;

/**
 * This is base interface to execute various loaderRule strategies.
 * @author Padgaonkar
 *
 */
public interface LoaderRuleExecutionStrategy {

	/**
	 * This method execute rules & accordingly set correct output in dataContainer.
	 * @param input
	 * @param ruleEngine
	 * @param rule
	 */
	public void executeStrategy(RduRuleServiceInput input, RduRuleEngine ruleEngine, RduRule rule);

}
