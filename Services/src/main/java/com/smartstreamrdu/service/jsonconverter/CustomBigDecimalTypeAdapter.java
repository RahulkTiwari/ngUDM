/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	CustomBigDecimalTypeAdapter.java
 * Author:	Divya Bharadwaj
 * Date:	08-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonconverter;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.smartstreamrdu.domain.CustomBigDecimal;

/**
 * @author Bharadwaj
 *
 */
public class CustomBigDecimalTypeAdapter extends TypeAdapter<CustomBigDecimal>{

	/* (non-Javadoc)
	 * @see com.google.gson.TypeAdapter#write(com.google.gson.stream.JsonWriter, java.lang.Object)
	 */
	@Override
	public void write(JsonWriter out, CustomBigDecimal value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.value(new CustomBigDecimal(value.stripTrailingZeros().toPlainString()));
	}

	/* (non-Javadoc)
	 * @see com.google.gson.TypeAdapter#read(com.google.gson.stream.JsonReader)
	 */
	@Override
	public CustomBigDecimal read(JsonReader in) throws IOException {
		return null;
	}

	

	

}
