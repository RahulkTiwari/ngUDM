/**
 * 
 */
package com.smartstreamrdu.api.response;

import java.util.LinkedHashMap;

/**
 * Helper class to get a {@link Response} object compatible with
 * {@link ApiResponseWrapper}. EquityResponse makes use of this class to return
 * a Response object which can be set in ApiResponseWrapper
 * 
 * @author Akshay Gehi
 *
 */
public class ResponseContent extends LinkedHashMap<String, Object> implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
