/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: NoopTelementaryConfig.java
 * Author: Akshay Gehi
 * Date: 29-April-2022
 *
 *******************************************************************/
package com.smartstreamrdu.service.telemetry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import lombok.extern.slf4j.Slf4j;

/**
 * Default no operations for disabling tracing
 * 
 * @author Akshay Gehi
 *
 */
@Slf4j
@Profile("!jaeger")
@Configuration
public class NoopTelementaryConfig implements TelemetryProvider {
	
	@Value("${spring.profiles.active:}")
	private String activeProfile;
	

	@Override
	@Bean("noopTracer")
	public Tracer tracer() {
		log.info("Currently active profile - " + activeProfile + ". NoOp Initialized");
		return TracerProvider.noop().get("Default");
	}
	
	@Override
	@Bean("noopContextPropagators")
	public ContextPropagators contextPropagators() {
		return ContextPropagators.create(TextMapPropagator.composite(TextMapPropagator.noop()));
	}
}
