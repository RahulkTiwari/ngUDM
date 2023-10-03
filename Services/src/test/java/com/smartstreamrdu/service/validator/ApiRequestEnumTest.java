/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ApiRequestEnumTest.java
 * Author:  Padgaonkar
 * Date:    Jun 3, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.validator;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.api.request.ApiRequestEnum;

/**
 * @author Padgaonkar
 *
 */
public class ApiRequestEnumTest {

	@Test
	public void testIt() {
		ApiRequestEnum createProfile = ApiRequestEnum.CREATE_PROFILE;
		Assert.assertNotNull(createProfile);
		
		ApiRequestEnum updateProfile = ApiRequestEnum.UPDATE_PROFILE;
		Assert.assertNotNull(updateProfile);
	}
}
