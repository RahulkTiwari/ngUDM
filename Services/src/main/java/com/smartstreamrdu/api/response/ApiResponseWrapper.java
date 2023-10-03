/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ApiResponseWrapper.java
 * Author:  Padgaonkar
 * Date:    Jun 1, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.smartstreamrdu.api.request.Request;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * This is wrapper class around {@link ApiResponseCodes}.
 * Response needs to be send in this format.
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class ApiResponseWrapper<I extends Request, O extends Response> {

	private String responseCode;

	private String responseString;

	@JsonInclude(Include.NON_NULL)
	private I request;

	@Singular("with")
	@JsonInclude(Include.NON_EMPTY)
	private final List<O> content;

	@SuppressWarnings("unchecked")
	public static <I extends Request, O extends Response> ApiResponseWrapperBuilder<I, O> successBuilder() {
		return (ApiResponseWrapperBuilder<I, O>) builder().responseCode(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseString());

	}
	
	@SuppressWarnings("unchecked")
	public static <I extends Request, O extends Response> ApiResponseWrapperBuilder<I, O>  failedBuilder() {
		return (ApiResponseWrapperBuilder<I, O>) builder().responseCode(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR.getResponseString());
	}
}
