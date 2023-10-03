/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ProfileRequest.java
 * Author:  Padgaonkar
 * Date:    Jun 1, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.request;

import javax.validation.constraints.NotNull;

import com.smartstreamrdu.persistence.domain.Profile;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This is pojo class for profile request.
 * @author Padgaonkar
 *
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ProfileRequest implements Request {

	private static final long serialVersionUID = 1L;
	@NotNull
	@NonNull
	private Profile profile;
	
}
 