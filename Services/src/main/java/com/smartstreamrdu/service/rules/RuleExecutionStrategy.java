/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleExecutionStrategy.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.rules.RduRule;

public interface RuleExecutionStrategy extends Serializable {
	
	List<DataContainer> executeStrategy(RecordWrapper recordWrapper, RduRuleEngine ruleEngine, List<RduRule> rules);

}
