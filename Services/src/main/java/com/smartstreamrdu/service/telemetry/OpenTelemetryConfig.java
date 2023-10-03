/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: OpenTelemetryConfig.java
 * Author: Akshay Gehi
 * Date: 29-April-2022
 *
 *******************************************************************/
package com.smartstreamrdu.service.telemetry;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation based on Jaeger
 * 
 * @see TraceUtils
 * @see TelemetryProvider
 * 
 * @author Akshay Gehi
 *
 */
@Slf4j
@Configuration
@Profile("jaeger")
public class OpenTelemetryConfig implements TelemetryProvider {

	private static final String TRACE_PROVIDER = "NgUDM";

	private static final AttributeKey<String> SERVICE_NAME = stringKey("service.name");

	@Setter
	@Value("${application.name}")
	private String componentName;

	@Setter
	@Value("${jaeger.endpoint}")
	private String jaegerEndpoint;

	@Autowired
	SdkTracerProvider tracerProvider;

	@Autowired
	ContextPropagators cp;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Bean("Jaeger_Trace_Provider")
	public Tracer tracer() {
		log.info("Initializing Jaeger");
		openTelemetry(tracerProvider, cp);
		return tracerProvider.get(TRACE_PROVIDER);
	}

	@Bean("__jaeger_sdk_trace_provider")
	protected SdkTracerProvider traceProvider() {
		JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder().setEndpoint(jaegerEndpoint)
				.setTimeout(30, TimeUnit.SECONDS).build();

		Resource serviceNameResource = Resource.getDefault()
				.merge(Resource.create(Attributes.of(SERVICE_NAME, componentName)));

		// Set to process the spans by the Jaeger Exporter
		SdkTracerProvider tp = SdkTracerProvider.builder()
				// .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).builder().build()
				.addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter)).setResource(serviceNameResource).build();

		// it's always a good idea to shut down the SDK cleanly at JVM exit.
		Runtime.getRuntime().addShutdownHook(new Thread(tp::close));

		return tp;
	}

	@Bean
	protected OpenTelemetrySdk openTelemetry(@Autowired SdkTracerProvider tracerProvider,
			@Autowired ContextPropagators cp) {
		return OpenTelemetrySdk.builder().setPropagators(cp).setTracerProvider(tracerProvider).build();
	}

	/**
	 * {@inheritDoc}
	 * @return W3C based serializer which can convert Context into W3C format data
	 *         which is then stored in a map within TraceInfo
	 */
	@Override
	@Bean("TextMap_W3CTraceContextPropagator")
	public ContextPropagators contextPropagators() {
		return ContextPropagators.create(TextMapPropagator.composite(W3CTraceContextPropagator.getInstance()));
	}

}