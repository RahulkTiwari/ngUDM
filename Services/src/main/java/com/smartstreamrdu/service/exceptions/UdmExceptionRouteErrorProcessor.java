/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionRouteErrorProcessor.java
 * Author:	Rushikesh Dedhia
 * Date:	25-Feb-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;

/**
 * If Exception occurred during route processing this class will again post the
 * message on UdmExceptionQueue for Re-processing.
 * 
 * @author Padgaonkar
 *
 */
@Component
public class UdmExceptionRouteErrorProcessor implements Processor {

	private static final Logger _logger = LoggerFactory.getLogger("exeptionLogger");


	@Override
	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn() != null) {
			Message message = exchange.getIn();
			Object data = message.getBody();
			UdmExceptionDataHolder exceptionDataHolder = JsonConverterUtil.convertFromJson(data.toString(),UdmExceptionDataHolder.class);
			
			_logger.error("Exception occured while raising service desk ticket for Following ExceptionDataHolder Object  : {}",exceptionDataHolder);
			_logger.error("Following error occured while processing exception on UdmExceptionRoute.", exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class));


		}
	}
}
