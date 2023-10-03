/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleExecutionStrategyFactory.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;

import com.smartstreamrdu.domain.RecordWrapper;

public interface RuleExecutionStrategyFactory extends Serializable {
	
	public RuleExecutionStrategy getRuleExecutionStrategy(RecordWrapper recordWrapper, String fileName, String dataSource);

}
