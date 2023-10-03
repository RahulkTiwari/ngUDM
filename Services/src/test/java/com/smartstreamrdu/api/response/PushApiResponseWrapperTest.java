/**
 * 
 */
package com.smartstreamrdu.api.response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author ViKumar
 *
 */
public class PushApiResponseWrapperTest {
	
	@Test
	public void TestPushApiResponseWrapper() {
		
		PushApiResponseWrapper<ResponseContent> responseBuilder = PushApiResponseWrapper.successBuilder(null).content(null).build();
		
		assertEquals("S0000",responseBuilder.getResponseCode());

		PushApiResponseWrapper<ResponseContent> responseFailBuilder = PushApiResponseWrapper.failedBuilder().content(null).build();
		
		assertEquals("E1000",responseFailBuilder.getResponseCode());

		PushApiResponseWrapper<ResponseContent> responseHeartBeatBuilder = PushApiResponseWrapper.successBuilderHeartBeat().content(null).build();
		
		assertEquals("S0002",responseHeartBeatBuilder.getResponseCode());

	}

}
