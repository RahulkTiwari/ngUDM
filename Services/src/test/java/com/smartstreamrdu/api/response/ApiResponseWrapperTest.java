/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ApiResponseWrapperTest.java
 * Author:  Padgaonkar
 * Date:    Jun 3, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.response;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.api.request.ProfileRequest;
import com.smartstreamrdu.api.response.ApiResponseWrapper.ApiResponseWrapperBuilder;

/**
 * @author Padgaonkar
 *
 */
public class ApiResponseWrapperTest {

	@Test
	public void TestApiResponseWrapper() {

		List<ProfileResponse> contentList = Arrays.asList(ProfileResponse.builder().build());
		
		ProfileRequest request = new ProfileRequest();
		
		ApiResponseWrapperBuilder<ProfileRequest, ProfileResponse> builder = ApiResponseWrapper.builder();
		
		ApiResponseWrapper<ProfileRequest, ProfileResponse> wrapper = builder.responseCode("E0001").responseString("Failture").request(request).content(contentList).build();

		Assert.assertEquals("E0001", wrapper.getResponseCode());
		Assert.assertEquals("Failture", wrapper.getResponseString());
		Assert.assertEquals(contentList, wrapper.getContent());
		Assert.assertEquals(request, wrapper.getRequest());
		
		builder = ApiResponseWrapper.successBuilder();
		ApiResponseWrapper<ProfileRequest, ProfileResponse> successWrapper = builder.build();
		Assert.assertEquals("S0000", successWrapper.getResponseCode());
		Assert.assertEquals("Success", successWrapper.getResponseString());
	}
}
