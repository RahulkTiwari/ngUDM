/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataInputPojoTest.java
 * Author:	Divya Bharadwaj
 * Date:	10-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Bharadwaj
 *
 */
/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })*/
public class RawDataInputPojoTest {
	
	@Test
	public void testPojo(){
		RawDataInputPojo obj = Mockito.mock(RawDataInputPojo.class);
		Mockito.when(obj.getCodeHash()).thenReturn("abcd");
		Mockito.when(obj.getDataSourceValue()).thenReturn("trdse");
		Mockito.when(obj.getFeedExecutionDetailId()).thenReturn("1");
		Mockito.when(obj.getFileType()).thenReturn("XE");
		JSONObject map=new JSONObject();
		map.put("isin", "testIsin");
		Mockito.when(obj.getRawData()).thenReturn(map);
		Mockito.when(obj.getRawDataLevelValue()).thenReturn("SEC");
		Mockito.when(obj.getUniqueColumnValue()).thenReturn("abcd");


		Assert.assertEquals(obj.getCodeHash(),"abcd");
		Assert.assertEquals(obj.getDataSourceValue(),"trdse");
		Assert.assertEquals(obj.getFeedExecutionDetailId(),"1");
		Assert.assertEquals(obj.getFileType(),"XE");
		Assert.assertEquals(obj.getRawData(),map);
		Assert.assertEquals(obj.getRawDataLevelValue(),"SEC");
		Assert.assertEquals(obj.getUniqueColumnValue(),"abcd");
	}
	
}
