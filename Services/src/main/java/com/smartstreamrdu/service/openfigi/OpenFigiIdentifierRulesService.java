/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	OpenFigiIdentifierRules.java
 * Author:	Shruti Arora
 * Date:	05-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;

public interface OpenFigiIdentifierRulesService extends Serializable {
	
	/**
	 * Returns list of rules applicable for the ruleEnum
	 * 
	 * @param ruleEnum
	 * @return
	 */
	public List<String> getRuleAttributesForRuleEnum(OpenFigiRequestRuleEnum ruleEnum);

}
