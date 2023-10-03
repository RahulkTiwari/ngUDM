/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MessageFilterationService.java
 * Author:	S Padgaonkar
 * Date:	04-April-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonobject.filter;

import org.json.simple.JSONObject;

import com.jayway.jsonpath.Criteria;

/**
 * This service is used to validate JSONObject based on provided Criteria.
 * @author Padgaonkar
 *
 */
public interface MessageFilterationService {

	/**
	 * If object is applicable based on provided criteria then this returns true.
	 * else it returns false.
	 * @param jsonObject
	 * @param criteria
	 * @return
	 */
	public boolean isApplicable(JSONObject jsonObject,Criteria criteria);
}
