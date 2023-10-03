/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduRuleService.java
 * Author:	Divya Bharadwaj
 * Date:	26-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.rules.Rule;
import com.smartstreamrdu.rules.RuleServiceInput;

/**
 * @author Bharadwaj
 * @param <T>
 *
 */
public interface RuleService<S extends RuleServiceInput> {

	<T extends Serializable> T applyRules(S input, List<? extends Rule> rules, String fileName);

}
