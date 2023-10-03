/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ApiEventsTest.java
 * Author:	S Padgaonkar
 * Date:	06-Sept-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.validation.events;

import org.junit.Assert;
import org.junit.Test;

public class ApiEventsTest {

	@Test
	public void testApiEvent() {
		ApiEvents event = ApiEvents.MISSING_ISIN;
		Assert.assertEquals("E1001", event.getStatusCode());
		Assert.assertEquals("ISIN not specified", event.getStatusMessage());

		ApiEvents event1 = ApiEvents.MISSING_MIC;
		Assert.assertEquals("E6006", event1.getStatusCode());
		Assert.assertEquals("MIC not specified", event1.getStatusMessage());

		ApiEvents event2 = ApiEvents.INVALID_ISIN;
		Assert.assertEquals("E1002", event2.getStatusCode());
		Assert.assertEquals("Invalid ISIN specified", event2.getStatusMessage());

		ApiEvents event3 = ApiEvents.INVALID_MIC;
		Assert.assertEquals("E6002", event3.getStatusCode());
		Assert.assertEquals("Invalid MIC specified", event3.getStatusMessage());

		ApiEvents event4 = ApiEvents.SYSTEM_ERROR;
		Assert.assertEquals("E1000", event4.getStatusCode());
		Assert.assertEquals("System error", event4.getStatusMessage());

		ApiEvents event5 = ApiEvents.MISSING_CLIENT;
		Assert.assertEquals("E1009", event5.getStatusCode());
		Assert.assertEquals("Client id not specified", event5.getStatusMessage());

		ApiEvents event6 = ApiEvents.NO_RECORDS_FOUND;
		Assert.assertEquals("W6002", event6.getStatusCode());
		Assert.assertEquals("Record Not Found", event6.getStatusMessage());

		ApiEvents event7 = ApiEvents.SUCCESS;
		Assert.assertEquals("S0000", event7.getStatusCode());
		Assert.assertEquals("Success", event7.getStatusMessage());
		
	}
	
	@Test
	public void getStatusMessageCodeTest()
	{
		Assert.assertEquals("ISIN not specified", ApiEvents.getStatusMessageCode("E1001"));
		Assert.assertEquals("MIC not specified", ApiEvents.getStatusMessageCode("E6006"));
		Assert.assertEquals("Invalid ISIN specified", ApiEvents.getStatusMessageCode("E1002"));
		Assert.assertEquals("Invalid MIC specified", ApiEvents.getStatusMessageCode("E6002"));
		Assert.assertEquals("System error", ApiEvents.getStatusMessageCode("E1000"));
		Assert.assertEquals("Client id not specified", ApiEvents.getStatusMessageCode("E1009"));
		Assert.assertEquals("Record Not Found", ApiEvents.getStatusMessageCode("W6002"));
		Assert.assertEquals("Success", ApiEvents.getStatusMessageCode("S0000"));
	}
	

	@Test
	public void getEventByCodeTest()
	{
		Assert.assertEquals(ApiEvents.MISSING_ISIN, ApiEvents.getEventByCode("E1001"));
		Assert.assertEquals(ApiEvents.MISSING_MIC, ApiEvents.getEventByCode("E6006"));
		Assert.assertEquals(ApiEvents.INVALID_ISIN, ApiEvents.getEventByCode("E1002"));
		Assert.assertEquals(ApiEvents.INVALID_MIC, ApiEvents.getEventByCode("E6002"));
		Assert.assertEquals(ApiEvents.SYSTEM_ERROR, ApiEvents.getEventByCode("E1000"));
		Assert.assertEquals(ApiEvents.MISSING_CLIENT, ApiEvents.getEventByCode("E1009"));
		Assert.assertEquals(ApiEvents.NO_RECORDS_FOUND, ApiEvents.getEventByCode("W6002"));
		Assert.assertEquals(ApiEvents.SUCCESS, ApiEvents.getEventByCode("S0000"));
	
	}
}
