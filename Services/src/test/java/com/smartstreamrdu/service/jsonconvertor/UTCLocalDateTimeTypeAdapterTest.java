/*******************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    UTCLocalDateTimeTypeAdapterTest.java
 * Author:    Padgaonkar
 * Date:    28-Nov-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonconvertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.smartstreamrdu.service.jsonconverter.UTCLocalDateTimeTypeAdapter;

/**
 * @author Padgaonkar
 *
 */
public class UTCLocalDateTimeTypeAdapterTest {
	
	private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").toFormatter();
	private static final String UTC_ZONE_OFFSET = "+00:00";
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new  UTCLocalDateTimeTypeAdapter()).create();
	 
	@Test
	public void test_write() {
		
		LocalDateTime localDate=LocalDateTime.of(2019, 12, 11, 17, 42, 12);
		ZonedDateTime ldtZoned = localDate.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
		LocalDateTime utcValue = utcZoned.toLocalDateTime();
		String value = utcValue.format(FORMAT) + UTC_ZONE_OFFSET;
		
		String json = gson.toJson(localDate);
		assertEquals("\""+value+"\"", json);
	
	}
	
	@Test
	public void test_read() {
		String json="\"2019-11-28T12:56:33.849\"";
		LocalDateTime localDate = gson.fromJson(json, LocalDateTime.class);
		LocalDateTime expecteddate=LocalDateTime.of(2019, 11, 28, 12, 56, 33,849000000);
		assertEquals(expecteddate, localDate);
		
		json="\"201911-28T12:56:33.849\"";
		try {
			localDate = gson.fromJson(json, LocalDateTime.class);
		}catch(JsonSyntaxException e) {
			assertEquals(JsonSyntaxException.class, e.getClass());
		}
	}
	
	@Test
	public void test_readPeek() {
		String json="null";
		LocalDateTime localDate = gson.fromJson(json, LocalDateTime.class);
		assertNull(localDate);
	}
}
