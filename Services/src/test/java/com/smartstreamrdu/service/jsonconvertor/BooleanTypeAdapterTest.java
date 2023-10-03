/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	BooleanTypeAdapterTest.java
 * Author:	GMathur
 * Date:	02-Jul-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonconvertor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.smartstreamrdu.service.jsonconverter.BooleanTypeAdapter;

public class BooleanTypeAdapterTest {

	private static final Gson gson = new GsonBuilder().registerTypeAdapter(Boolean.class, new BooleanTypeAdapter()).create();
	
	@Test
	public void test_Write() {
		Boolean val = Boolean.FALSE;
		String json = gson.toJson(val);
		assertEquals(val.toString(), json);
	}
	
	@Test
	public void test_Read() {
		String val = Boolean.TRUE.toString();
		Boolean json = gson.fromJson(val,Boolean.class);
		assertEquals(Boolean.valueOf(val), json);
	}
	
	@Test
	public void test_ReadNull() {
		String val = null;
		Boolean json = gson.fromJson(val,Boolean.class);
		assertEquals(val, json);
	}
	
	@Test(expected = JsonSyntaxException.class)
	public void test_ReadInvalid() {
		String val = "truee";
		gson.fromJson(val,Boolean.class);
	}
}
