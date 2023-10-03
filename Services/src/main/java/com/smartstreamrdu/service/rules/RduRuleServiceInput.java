/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduRuleServiceInput.java
 * Author:	Divya Bharadwaj
 * Date:	27-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.rules.RuleServiceInput;

import lombok.Data;

/**
 * @author Bharadwaj
 *
 */
@Data
public class RduRuleServiceInput implements RuleServiceInput {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7431551218701548189L;
	
	//Wrapper object which holds parent as well as child records.
	private RecordWrapper recordWrapper;
	
	//ruleContext 
	private RduRuleContext rduRuleContext;
	
	//current record which is processing(applicable for feed Rules)
	private Record record;
	
	//current dataContainer which is processing(applicable for loaderRules)
	private DataContainer dataContainer;

	

}
