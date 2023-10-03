/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataFilterService.java
 * Author:	Jay Sangoi
 * Date:	17-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import java.io.Serializable;

import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Jay Sangoi
 *
 */
public interface DataFilterChainService extends Serializable{
	/**
	 * Apply the available filter. If the output of one filter is don't persist, then it will not be forwarded to  another filter, else it will be forwarded to another filter
	 * @param input
	 * @return
	 * @throws Exception 
	 */
	FilterOutput applyFilterChain(FilterInput input) throws UdmTechnicalException;
}
