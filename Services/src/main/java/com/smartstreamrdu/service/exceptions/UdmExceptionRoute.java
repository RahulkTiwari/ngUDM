/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionRoute.java
 * Author:	Rushikesh Dedhia
 * Date:	25-Feb-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.util.UdmExceptionConstant;
import com.smartstreamrdu.util.UdmSystemPropertiesConstant;


/**
 *  This is a Camel route extension to manage the work-flow of raising exceptions to 
 *  the ticketing system.
 * @author Dedhia
 *
 */
@Component
public class UdmExceptionRoute extends RouteBuilder {
	
	@Autowired
	private UdmExceptionRouteErrorProcessor udmExceptionRouteErrorProcessor;
	
	@Autowired
	private UdmExceptionTransferProcessor udmExceptionTransferProcessor;
	
	@Autowired
	private UdmExceptionFileOutputProcessor udmEceptionFileOutputProcessor;
	
	@Autowired
	private ExceptionEvaluator exceptionEvaluator;
	
	
	private static final Logger _logger = LoggerFactory.getLogger(UdmExceptionRoute.class);
	
	@Autowired
	UdmSystemPropertiesCache systemCache;
	
	private String udmExceptionKafkaRoute = null;
	
	@PostConstruct
	public void initialize() {
		Optional<String> kafkaRouteOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.EXCEPTION_KAFKA_ROUTE,UdmSystemPropertiesConstant.COMPONENT_XRF,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(kafkaRouteOptional.isPresent()) {
			udmExceptionKafkaRoute =kafkaRouteOptional.get();
	   }
	}

	@Override
	public void configure() throws Exception {	

		
	from(udmExceptionKafkaRoute)
	.process(exchange -> _logger.info("Exchange : {}", exchange)).id("Exchange Logger")	
	.onException(Exception.class)
		.process(udmExceptionRouteErrorProcessor).id("Technical Exception Handler")
		.end()
    	  .choice().id("RaiseException")
			.when().body(exceptionEvaluator).to(UdmExceptionConstant.EXCEPTION_ROUTE)
			.otherwise()
			.to(UdmExceptionConstant.FILE_ROUTE)
			.end();
	
			from(UdmExceptionConstant.EXCEPTION_ROUTE).process(udmExceptionTransferProcessor).errorHandler(noErrorHandler()) .end();
	
			from(UdmExceptionConstant.FILE_ROUTE).process(udmEceptionFileOutputProcessor).errorHandler(noErrorHandler()) .end();		
}
	
}