package com.smartstreamrdu.service.jsonconverter;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * {@link Timestamp} has intentionally been encoded in ISO_DATE_TIME for a
 * couple of reasons
 * <li>The database and messages have a single representation</li>
 * <li>We use {@link LocalDateTime} in database however in message,
 * {@link Timestamp} is used since {@link LocalDateTime} is not
 * {@link Serializable}</li>
 * <li>All test cases haveb een written in ISO_DATE_TIME format in the past and
 * changing 100s of test cases will not be easy</li>
 * 
 * @author gehi
 *
 */
public class TimestampTypeAdapter extends TypeAdapter<Timestamp> {

	final LocalDateTimeTypeAdapter adapter = new LocalDateTimeTypeAdapter();

	@Override
	public void write(JsonWriter out, Timestamp val) throws IOException {
		adapter.write(out, (val == null) ? null : val.toLocalDateTime());
	}

	@Override
	public Timestamp read(JsonReader in) throws IOException {
		LocalDateTime val = adapter.read(in);
		return (val == null) ? null : Timestamp.valueOf(val);
	}

}
