package com.smartstreamrdu.api.response;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.smartstreamrdu.api.request.EnsPullRequestByDate;

public class EnsPullApiResponseWrapperTest {

	@Test
	public void test() {
		EnsPullApiResponseWrapper<EnsPullRequestByDate, EnsPullResponse> responseBuilder = EnsPullApiResponseWrapper
				.successBuilder().content( List.of(new EnsPullResponse())).build();

		assertEquals("S0000", responseBuilder.getResponseCode());

	}

}
