/*******************************************************************
*
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	EnsPullApiResponseWrapper.java
* Author:	Ravi Kaithwas
* Date:	18-Aug-2021
*
*******************************************************************
*/
package com.smartstreamrdu.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.smartstreamrdu.api.request.EnsPullRequestByDate;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * This is wrapper class around {@link ApiResponseCodes}. Response needs to be
 * send in this format.
 * 
 * @author RKaithwas
 */
@Data
@Builder()
@JsonInclude(Include.NON_NULL)
public class EnsPullApiResponseWrapper<I extends EnsPullRequestByDate, K extends EnsPullResponse> {

	private String responseCode;

	private String responseString;

	@JsonInclude(Include.NON_NULL)
	private I request;
	
	@Singular("with")
	@JsonInclude(Include.NON_EMPTY)
	private final List<K> content;

	@SuppressWarnings("unchecked")
	public static <I extends EnsPullRequestByDate, K extends EnsPullResponse> EnsPullApiResponseWrapperBuilder<I, K> successBuilder() {
		return (EnsPullApiResponseWrapperBuilder<I, K>) builder()
				.responseCode(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseString());

	}

}
