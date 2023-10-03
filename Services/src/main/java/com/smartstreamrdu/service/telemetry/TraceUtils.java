/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: TraceUtils.java
 * Author: Akshay Gehi
 * Date: 29-April-2022
 *
 *******************************************************************/
package com.smartstreamrdu.service.telemetry;

import com.smartstreamrdu.domain.TraceInfo;
import com.smartstreamrdu.persistence.service.SpringUtil;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Utility methods to enable transfer of OpenTelemetry context between Threads,
 * VMs and Components and even Spark Jobs
 * 
 * <ul>
 * <li>Tracer is the starting point for creating telemetry</li>
 * <li>TextMapPropagator are serializers but not transporters</li>
 * <li>Context allows for information to be transported between VMs. This is
 * required by Span related APIs to do the linkages</li>
 * </ul>
 * 
 * SpanBuilder APIs require Context object in order to link with Parent. This is
 * done by translating TraceInfo to {@link io.opentelemetry.context.Context}
 * since Context is not Serializable and cannot be directly transferred
 * 
 * @see OpenTelemetryConfig
 * @see TelemetryProvider
 * @see TextMapPropagator
 * @see TraceInfo
 * @see Context
 * 
 * @author Akshay Gehi
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TraceUtils {

	/** Deserializer - Helps convert TraceInfo to Context **/
	private static final TextMapGetterImplementation TEXT_MAP_GETTER = new TextMapGetterImplementation();

	/** Serializer - Helps convert Context to TraceInfo **/
	private static final TextMapSetter<TraceInfo> TEXT_MAP_SETTER = TraceInfo::put;

	private static Tracer tracer = null;

	public static Tracer getTracer() {
		if (tracer == null) {
			tracer = SpringUtil.getBean(Tracer.class);
		}
		return tracer;
	}

	private static TextMapPropagator textMapPropagator = null;

	/**
	 * Required for converting Trace Id and Parent span id into text
	 * 
	 * @see OpenTelemetryConfig#contextPropagators()
	 */
	public static TextMapPropagator getTextMapPropagator() {
		if (textMapPropagator == null) {
			textMapPropagator = SpringUtil.getBean(ContextPropagators.class).getTextMapPropagator();
		}
		return textMapPropagator;
	}

	/**
	 * Convert from Context to TraceInfo
	 * 
	 * @return TraceInfo converted
	 */
	public static TraceInfo contextToTraceInfo() {
		TraceInfo traceInfo = new TraceInfo();
		getTextMapPropagator().inject(Context.current(), traceInfo, TEXT_MAP_SETTER);
		return traceInfo;
	}

	/**
	 * TraceInfo has a mechanism to transfer {@link Context} linkage info from one
	 * node to another. This method translates from TraceInfo to Context
	 * 
	 * @param traceInfo
	 * @return
	 */
	public static Context traceInfoToContext(@NonNull TraceInfo traceInfo) {
		return getTextMapPropagator().extract(Context.current(), traceInfo, TEXT_MAP_GETTER);
	}

	protected static final class TextMapGetterImplementation implements TextMapGetter<TraceInfo> {

		@Override
		public Iterable<String> keys(@NonNull TraceInfo carrier) {
			return carrier.keys();
		}

		@Override
		public String get(TraceInfo carrier, String key) {
			return carrier.get(key);
		}
	}
}
