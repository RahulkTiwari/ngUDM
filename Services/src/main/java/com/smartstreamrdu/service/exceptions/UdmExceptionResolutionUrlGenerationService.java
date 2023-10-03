/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionResolutionUrlGenerationService.java
 * Author:	Divya Bharadwaj
 * Date:	07-May-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.exceptions;

import java.net.URISyntaxException;

/**
 * @author Bharadwaj
 *
 */
public interface UdmExceptionResolutionUrlGenerationService {
	
	String populateResolutionUrl(String stormCriteriaValue, String exceptionType) throws URISyntaxException;
}
