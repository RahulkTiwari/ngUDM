/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    Validator.java
 * Author:  Padgaonkar
 * Date:    Jun 1, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.validator;

import com.smartstreamrdu.api.request.ApiRequestEnum;

/**
 * This is a general Validator interface for Visitor design pattern
 */
public interface Validator<E> {

	/**
	 * This method returns true or false based on validation condition
	 */
	public boolean isValid(E element);
	
	/**
	 * This method returns validator name
	 */
	public String getValidatorName();
	
	/**
	 * This method returns whether corresponding validator is applicable or not.
	 */
	public boolean isApplicable(ApiRequestEnum requestType);
	
}
