/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    Validatable.java
 * Author:  Padgaonkar
 * Date:    Jun 1, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.validator;

import java.util.Map;

import com.smartstreamrdu.api.response.ApiResponseCodes;

/**
 *  This is a general Validatable interface for Visitor design pattern
 *  Class which needs to validate implements this interface.This implementation
 *  is added on back on UDM-41972 where we need to validate multiple fields of 
 *  request object and based on validation we need to send respective error codes.
 */
public interface Validatable<E> {
	
	/**
	 * This method return true if validation pass else return false
	 */
	public boolean validate (Validator<E> validator);
	
	/**
	 * This map holds validator name & corresponding Api response.
	 */
	public Map<String,ApiResponseCodes> getResponseCodeMap();
}
