/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InsIdNotFoundFilteredInactivationService.java
 * Author:	Padgaonkar S
 * Date:	05-Jan-2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.enrichment.service;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.exception.UdmBaseException;

/**
 * This service is used for processing inactivation of filtered container.
 * 
 * @author Padgaonkar
 *
 */
public interface InsIdNotFoundFilteredInactivationService {

	/**
	 * Based on attributes specified in event message & corresponding dataLevel this
	 * method retrieves dataContainer & inactivate it.
	 * 
	 * @param eventMessage
	 * @param level
	 * @throws UdmBaseException
	 */
	public void inactivateContainer(EventMessage eventMessage, DataLevel level) throws UdmBaseException;

}
