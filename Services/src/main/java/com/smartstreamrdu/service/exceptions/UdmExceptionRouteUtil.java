/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionRouteUtil.java
 * Author:	Rushikesh Dedhia
 * Date:	25-Feb-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdmExceptionRouteUtil {
	
	private static final Logger _logger = LoggerFactory.getLogger(UdmExceptionRouteUtil.class);
	
	private UdmExceptionRouteUtil () {
		// Private no-arg constructor.
	}
	
	/**
	 *  This method will return the UdmExceptionRouteContext object if set in the supplied
	 *  Exchange object.
	 * @param exchange
	 * @return
	 */
	public static UdmExceptionRouteContext getRouteContextFromExchange(Exchange exchange) {
		if (exchange == null || exchange.getIn() == null) {
			
			_logger.debug("Returning null as the supplied object is {}", exchange);
			return null;
		}
		
		return exchange.getIn().getBody(UdmExceptionRouteContext.class);
	}

}
