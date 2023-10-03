/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LocalDateTypeAdapter.java
 * Author:	Divya Bharadwaj
 * Date:	08-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonconverter;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author Bharadwaj
 *
 */
public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gson.TypeAdapter#write(com.google.gson.stream.JsonWriter,
	 * java.lang.Object)
	 */
	@Override
	public void write(JsonWriter out, LocalDate value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.value(value.format(DateTimeFormatter.ISO_DATE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gson.TypeAdapter#read(com.google.gson.stream.JsonReader)
	 */
	@Override
	public LocalDate read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		String json = null;
		try {
			json = in.nextString();
			return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(json));
		} catch (DateTimeException e) {
			throw new JsonSyntaxException("Could not parse Json to local date time: " + json, e);
		}
	}

}
