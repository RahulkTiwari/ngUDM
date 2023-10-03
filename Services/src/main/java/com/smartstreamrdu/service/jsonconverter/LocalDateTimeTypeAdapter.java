package com.smartstreamrdu.service.jsonconverter;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_DATE_TIME;

	private String zoneOffSet;

	/**
	 * @param string
	 */
	public LocalDateTimeTypeAdapter(String zoneOffSet) {
		this.zoneOffSet = zoneOffSet;
	}

	public LocalDateTimeTypeAdapter() {
	}

	@Override
	public void write(JsonWriter out, LocalDateTime value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		if (StringUtils.isNotEmpty(this.zoneOffSet)) {
			out.value(value.format(FORMAT) + zoneOffSet);
		} else {
			out.value(value.format(FORMAT));
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
