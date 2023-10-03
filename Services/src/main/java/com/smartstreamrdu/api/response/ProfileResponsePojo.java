/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ProfileResponsePojo.java
 * Author:	RKaithwas
 * Date:	05-May-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * this holds the details on which profile is processed successfully and which
 * does not with responseCode and profile name
 * 
 * @author RKaithwas
 *
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class ProfileResponsePojo implements Response{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2744220273756365574L;

	private String responseCode;

	private String responseString;

	private String profileName;

	public static ProfileResponsePojoBuilder successBuilder() {
		return builder().responseCode(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseString());
	}

	public static ProfileResponsePojoBuilder failedBuilder() {
		return builder().responseCode(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR.getResponseString());
	}

}
