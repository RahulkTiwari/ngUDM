/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	BooleanTypeAdapter.java
 * Author:	GMathur
 * Date:	01-Jul-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonconverter;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * This adapter will be used to convert Boolean value.
 * If any value, other than 'true' and 'false', will come then it will throw an exception. 
 */
public class BooleanTypeAdapter extends TypeAdapter<Boolean>{

	@Override
	public void write(JsonWriter out, Boolean value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.value(value);
	}

	@Override
	public @Nullable Boolean read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null; // NOSONAR TypeAdapter specifies returning null 
		}
		return	in.nextBoolean();
	}

}
