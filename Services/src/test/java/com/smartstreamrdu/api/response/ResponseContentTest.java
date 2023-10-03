/**
 * 
 */
package com.smartstreamrdu.api.response;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

/**
 * @author Akshay Gehi
 *
 */
public class ResponseContentTest {

	@Test
	public void testConstruct() {
		Response ctx = new ResponseContent();
		assertTrue(ctx instanceof Map);
	}

}
