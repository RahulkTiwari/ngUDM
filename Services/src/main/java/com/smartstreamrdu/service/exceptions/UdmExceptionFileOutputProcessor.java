/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionFileOutputProcessor.java
 * Author:	Padgaonkar
 * Date:	23-March-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;

@Component
public class UdmExceptionFileOutputProcessor implements Processor {

	private static final Logger _logger = LoggerFactory.getLogger("exeptionLogger");
	
	@Autowired
	UdmSystemPropertiesCache systemCache;

	@Override
	public void process(Exchange exchange) throws Exception {
		Object data = null;
		if (exchange.getIn() != null) {
			Message message = exchange.getIn();
			data = message.getBody();
			_logger.info("following  exceptionDataHolderObject is written into file : {}",data);
		}
	}

}
