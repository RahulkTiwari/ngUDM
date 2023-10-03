/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ProfileDeleteRequest.java
 * Author:  Aakash Gupta
 * Date:    Oct 11, 2021
 *
 *******************************************************************/
package com.smartstreamrdu.api.request;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * Pojo class for Profile delete Request.
 * @author AGupta
 *
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ProfileDeleteRequest implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NotNull
	@NonNull
	private String profileName;

}
