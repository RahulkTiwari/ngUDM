/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: RecordTelemetry.java
 * Author: Akshay Gehi
 * Date: 29-April-2022
 *
 *******************************************************************/
package com.smartstreamrdu.service.telemetry;

import static com.smartstreamrdu.service.telemetry.TraceUtils.contextToTraceInfo;
import static com.smartstreamrdu.service.telemetry.TraceUtils.getTracer;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.json.simple.JSONObject;

import com.smartstreamrdu.domain.Record;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * OpenTelemetry for Record object while processing NgUDL
 * 
 * Since we are using Spark to process the {@link Record}s, it is quite possible
 * that one JavaRDD set is processed on one node, and the next task is processed
 * on another node. This requires Serialization of all the data that exchanged
 * between the nodes including Telemetry information, {@see Span} itself is not
 * {@link Serializable} so we cannot use the same span across Spark tasks
 * 
 * @see TraceUtils
 * 
 * @author Akshay Gehi
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordTelemetry {

	private static final String EVENT_RECORD_START = "Record Processing";

	/**
	 * Publish parsing related telemetry
	 * 
	 * @see RecordTelemetry#addParsingSpanWithParent(Record, String)
	 * 
	 * @param records    List of records for which Telemetry needs to be published
	 * @param dataSource DataSource for which the records are being processed
	 */
	public static void addSpans(List<Record> records, String dataSource) {
		records.forEach(r -> RecordTelemetry.addParsingSpanWithParent(r, dataSource));
	}

	/**
	 * Publish parsing related telemetry. Spans are ended immediately because they
	 * are not serializable and cannot be sent to the next Spark task
	 * 
	 * @param r          Record for which telemetry needs to be published
	 * @param datasource DataSource for which the records are being processed
	 */
	public static void addParsingSpanWithParent(Record r, String datasource) {
		SpanBuilder sb = getTracer().spanBuilder(EVENT_RECORD_START + ": " + datasource);
		Span startSpan = sb.startSpan();
		try (Scope s = startSpan.makeCurrent()) {
			Span parseSpan = addParsingSpan(r, datasource);
			r.setTraceInfo(contextToTraceInfo());
			parseSpan.end();
		}
		startSpan.end();
	}

	protected static Span addParsingSpan(Record r, String ds) {
		SpanBuilder sbp = getTracer().spanBuilder("Parsing");
		Span parseSpan = sbp.startSpan();
		if (parseSpan.isRecording()) {
			Attributes attr = toAttributes(r.getRecordRawData().getRawData(), ds);
			parseSpan.addEvent("Record parsed", attr);
			parseSpan.setStatus(StatusCode.OK);
		}
		return parseSpan;
	}

	/**
	 * Publishes all the attributes in the record
	 * 
	 * @param rawData RawData to be published as a part of telemetry
	 * @param ds      Data source
	 * @return Attributes format for Telemetry publishing
	 */
	private static Attributes toAttributes(JSONObject rawData, String ds) {
		Objects.requireNonNull(rawData, "RawData cannot be null");
		AttributesBuilder b = Attributes.builder();
		b.put("Datasource", ds);
		@SuppressWarnings("unchecked")
		Set<Entry<String, ?>> entrySet = rawData.entrySet();
		entrySet.stream().forEach(c -> {
			Object v = c.getValue();
			if (v != null) {
				String s = String.valueOf(v).trim();
				if (!s.isEmpty()) {
					b.put(c.getKey(), s);
				}
			}
		});

		return b.build();
	}

	/**
	 * Publishes decoded information
	 * 
	 * @param r          Record object which is decoded
	 * @param datasource DataSource
	 */
	public static void addDecodedSpan(@Nullable Record r, String datasource) {
		if (r == null) {
			return;
		}

		SpanBuilder sb = getTracer().spanBuilder("Decoded");
		sb.setParent(TraceUtils.traceInfoToContext(r.getTraceInfo()));
		Span decodedSpan = sb.startSpan();
		if (decodedSpan.isRecording()) {
			Attributes attr = toAttributes(r.getData(), datasource);
			decodedSpan.addEvent("Record decoded", attr);
			decodedSpan.setStatus(StatusCode.OK);
		}
		decodedSpan.end();
	}

	/**
	 * When records are delta rejected
	 * 
	 * @param r Record
	 */
	public static void addDeltaRejectEvent(Record r) {
		if (r == null) {
			return;
		}

		SpanBuilder sb = getTracer().spanBuilder("Delta Processing");
		sb.setParent(TraceUtils.traceInfoToContext(r.getTraceInfo()));
		Span span = sb.startSpan();
		if (span.isRecording()) {
			span.addEvent("Record filtered by delta processing");
			span.setAttribute("Unique ID", r.getUniqueId());
			span.setStatus(StatusCode.OK);
		}
		span.end();
	}

}
