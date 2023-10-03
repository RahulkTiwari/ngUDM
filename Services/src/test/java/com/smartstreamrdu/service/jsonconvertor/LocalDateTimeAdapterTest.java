/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LocalDateTimeAdapterTest.java
 * Author:	S Padgaonakar
 * Date:	11-09-2018
 *
 *******************************************************************
 */

package com.smartstreamrdu.service.jsonconvertor;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;


/**
 * @author S Padgaonakar
 *
 */

public class LocalDateTimeAdapterTest {

	
	@Test
	public void test() {
		LocalDateTime dateTime = LocalDateTime.now();
		String json =JsonConverterUtil.convertToJson(dateTime);
		LocalDateTime dateTime2 =JsonConverterUtil.convertFromJson(json, LocalDateTime.class);
		Assert.assertNotNull(dateTime2);
	}

}
