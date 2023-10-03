/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UTCLocalDateTimeTypeAdapter.java
 * Author:	GMathur
 * Date:	21-Nov-2019
 *
 *******************************************************************
 */

package com.smartstreamrdu.service.jsonconverter;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/*
 * This Converter is used to convert localDateTime with UTC Zone while writing into json file from proforma.
 */
public class UTCLocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

	private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").toFormatter();
	private static final String UTC_ZONE_OFFSET = "+00:00";

	@Override
	public void write(JsonWriter out, LocalDateTime value) throws IOException {
		if(value != null) {
			ZonedDateTime ldtZoned = value.atZone(ZoneId.systemDefault());
			ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
			LocalDateTime utcValue = utcZoned.toLocalDateTime();
			out.value(utcValue.format(FORMAT) + UTC_ZONE_OFFSET);
		} else {
			out.nullValue();
		}
		

	}

	@Override
	public LocalDateTime read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		String json = null;
		try {
			json = in.nextString();
			return LocalDateTime.from(FORMAT.parse(json));
		} catch (DateTimeException e) {
			throw new JsonSyntaxException("Could not parse Json to local date time: " + json, e);
		}
	}

}
