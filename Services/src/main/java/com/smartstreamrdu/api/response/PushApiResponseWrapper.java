/**
 * 
 */
package com.smartstreamrdu.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author ViKumar
 * 
 * This is wrapper class around {@link ApiResponseCodes}.
 * Response of push api (like where one way communication is needed ) needs to be send in this format.
 *
 */
@Data
@Builder
@AllArgsConstructor
public class PushApiResponseWrapper<O extends ResponseContent> {

	private String responseCode;

	private String responseString;
	//offset provided by client
	@JsonInclude(Include.NON_NULL)
	private Long offset;
	
	@JsonInclude(Include.NON_EMPTY)
	private final ResponseContent content;
	

	@SuppressWarnings("unchecked")
	public static <O extends ResponseContent> PushApiResponseWrapperBuilder<O> successBuilder(Long offset) {
		return (PushApiResponseWrapperBuilder<O>) builder()
				.responseCode(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_SUCCESS.getResponseString())
				.offset(offset);

	}

	@SuppressWarnings("unchecked")
	public static <O extends ResponseContent> PushApiResponseWrapperBuilder<O> successBuilderHeartBeat() {
		return (PushApiResponseWrapperBuilder<O>) builder()
				.responseCode(ApiResponseCodes.RESPONSE_CODE_HEARTBEAT.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_HEARTBEAT.getResponseString());

	}
	
	@SuppressWarnings("unchecked")
	public static <O extends ResponseContent> PushApiResponseWrapperBuilder<O> failedBuilder() {
		return (PushApiResponseWrapperBuilder<O>) builder()
				.responseCode(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR.getResponseCode())
				.responseString(ApiResponseCodes.RESPONSE_CODE_SYSTEM_ERROR.getResponseString());
	}

}
