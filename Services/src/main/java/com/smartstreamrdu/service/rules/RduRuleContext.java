/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduRuleContext.java
 * Author:	Rushikesh Dedhia
 * Date:	03-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RduRuleContext implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7469488463270855233L;

	private Map<String, Serializable> ruleContext;

	public Map<String, Serializable> getRuleContext() {
		return ruleContext;
	}

	public void setRuleContext(Map<String, Serializable> ruleContext) {
		this.ruleContext = ruleContext;
	}

	public void addToRuleContext(String key, Serializable value) {
		if (ruleContext == null) {
			ruleContext = new HashMap<>();
		}
		ruleContext.put(key, value);
	}

	public void clearContext() {
		ruleContext.clear();
	}

}
