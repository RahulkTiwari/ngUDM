/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleComponentEnum.java
 * Author:	Rushikesh Dedhia
 * Date:	03-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

public enum RuleComponentEnum {
	
	LOADER("feedName"),
	DIS("profileId"),
	SEN("");
	
	RuleComponentEnum(String filterAttribute) {
		this.filterAttribute = filterAttribute;
	}
	
	private String filterAttribute;
	
	public String getFilterAttribute() {
		return this.filterAttribute;
	}

}
