/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ApiResponseCodesTest.java
 * Author:  Padgaonkar
 * Date:    Jun 3, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.response;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;


/**
 * @author Padgaonkar
 *
 */
public class ApiResponseCodesTest {

	@Test
	public void TestResponseCodes() {
	
		validateResponseCode(ApiResponseCodes.RESPONSE_CODE_UNAUTHORIZED,"E0001","User is not authorized to perform this action",HttpStatus.UNAUTHORIZED);
		validateResponseCode(ApiResponseCodes.RESPONSE_CODE_SUCCESS,"S0000", "Success", HttpStatus.OK);
		validateResponseCode(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR,"E1000","System error", HttpStatus.INTERNAL_SERVER_ERROR);
		validateResponseCode(ApiResponseCodes.RESPONSE_CODE_INVALID_REQUEST,"E1021", "Request JSON is incomplete or invalid", HttpStatus.BAD_REQUEST);
		validateResponseCode(ApiResponseCodes.RESPONSE_CODE_INVALID_OFFSET,"E1021", "", HttpStatus.BAD_REQUEST);
		validateResponseCode(ApiResponseCodes.RESPONSE_CODE_EXCEPTION,"E1022", "", HttpStatus.BAD_REQUEST);
		validateResponseCode(ApiResponseCodes.RESPONSE_CODE_METHOD_ARG_TYPE_MISMATCH,"E1023", "Request parameter type mismatch parameter: %s, value: %s, required type: %s", HttpStatus.BAD_REQUEST);

	}

	private void validateResponseCode(ApiResponseCodes responseCode, String responseCodeVal,String responseCodeString,HttpStatus status) {
		
		Assert.assertEquals(responseCodeVal, responseCode.getResponseCode());
		Assert.assertEquals(responseCodeString, responseCode.getResponseString());
		Assert.assertEquals(status, responseCode.getHttpStatus());
	}
}
