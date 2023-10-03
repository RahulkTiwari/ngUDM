/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: TelemetryProvider.java
 * Author: Akshay Gehi
 * Date: 29-April-2022
 *
 *******************************************************************/
package com.smartstreamrdu.service.telemetry;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;

/**
 * Interface to define the beans required by the applications to run
 * 
 * @author Akshay Gehi
 *
 */
public interface TelemetryProvider {

	/**
	 * Provides services for tracing
	 * 
	 * @return tracer associated with the library initialized
	 */
	Tracer tracer();

	/**
	 * Used for propagating messages from one application to another through
	 * serialization and deserialization services on the context attributes
	 * 
	 * @return {@link ContextPropagators} generally in text readable format
	 */
	ContextPropagators contextPropagators();

}
