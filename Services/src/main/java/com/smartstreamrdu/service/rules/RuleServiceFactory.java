/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleServiceFactory.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;

public interface RuleServiceFactory extends Serializable{
	
	public RuleService getRuleService(RuleComponentEnum component);

}
