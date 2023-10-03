/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ApiResponseCodes.java
 * Author:  Padgaonkar
 * Date:    Jun 1, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.response;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This class stores all response codes sends as response in ApiRequest.
 */
@AllArgsConstructor
public enum ApiResponseCodes {

	RESPONSE_CODE_SUCCESS("S0000", "Success", HttpStatus.OK),
	RESPONSE_CODE_UNAUTHORIZED("E0001", "User is not authorized to perform this action", HttpStatus.UNAUTHORIZED),
	RESPONSE_CODE_SYSTEM_ERROR("E1000", "System error", HttpStatus.INTERNAL_SERVER_ERROR),
	RESPONSE_CODE_INVALID_REQUEST("E1021", "Request JSON is incomplete or invalid", HttpStatus.BAD_REQUEST),
	RESPONSE_CODE_HEARTBEAT("S0002", "Heartbeat notification at : ", HttpStatus.OK),
	NO_RECORDS_FOUND("W6002", "Record Not Found", HttpStatus.NO_CONTENT),
	RESPONSE_CODE_INVALID_OFFSET("E1021", "", HttpStatus.BAD_REQUEST),
	RESPONSE_CODE_EXCEPTION("E1022", "", HttpStatus.BAD_REQUEST),
	RESPONSE_CODE_METHOD_ARG_TYPE_MISMATCH("E1023", "Request parameter type mismatch parameter: %s, value: %s, required type: %s", HttpStatus.BAD_REQUEST),
	REQUESTED_DATE_ALLIGNMENT_INVALID("E1024", "Requested fromEventEffectiveDate should not be greater than toEventEffectiveDate",  HttpStatus.BAD_REQUEST);


	@Getter
	private String responseCode;

	@Getter
	private String responseString;

	@Getter
	private HttpStatus httpStatus;

}
