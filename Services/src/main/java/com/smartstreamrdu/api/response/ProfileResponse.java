/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ProfileResponse.java
 * Author:  Padgaonkar
 * Date:    Jun 1, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.smartstreamrdu.api.request.ProfileRequest;
import com.smartstreamrdu.persistence.domain.Profile;

import lombok.Builder;
import lombok.Data;

/**
 * This class corresponding to response for profile request {@link ProfileRequest}
 * @author Padgaonkar
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class ProfileResponse implements Response{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9112352396312870063L;
	private String responseCode;
	private String responseString;
	private String profileName;
	private List<Profile> profile;
	
}
