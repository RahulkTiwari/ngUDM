/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ProfileResponsePojoTest.java
 * Author:  Padgaonkar
 * Date:    Jan 12, 2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.response;

import org.junit.Assert;
import org.junit.Test;

public class ProfileResponsePojoTest {
	
	@Test
	public void testIt() {
		ProfileResponsePojo pojo = ProfileResponsePojo.failedBuilder().build();
		Assert.assertEquals(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR.getResponseCode(), pojo.getResponseCode());
		Assert.assertEquals("System error", pojo.getResponseString());

		ProfileResponsePojo pojo1 = ProfileResponsePojo.successBuilder().build();
		Assert.assertEquals(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseCode(), pojo1.getResponseCode());
		Assert.assertEquals("Success", pojo1.getResponseString());
	}
}
