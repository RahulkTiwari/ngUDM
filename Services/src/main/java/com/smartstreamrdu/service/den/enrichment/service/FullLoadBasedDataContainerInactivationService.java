/*******************************************************************
*
* Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	FullLoadBasedDataContainerInactivationService.java
* Author:	Padgaonkar S
* Date:	26-Oct-2021
*
*******************************************************************
*/
package com.smartstreamrdu.service.den.enrichment.service;

import java.time.LocalDateTime;
import java.util.Iterator;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmBaseException;

/**
* This service is responsible for inactivating instrument/security based on
* full file load.
* 
* @author Padgaonkar
*
*/
public interface FullLoadBasedDataContainerInactivationService {

	/**
	 * This service inactivates securities based on fullFileLoadStartDate.So if
	 * security lastProcessedDate is less than fullFileLoadCompletitionDate then it
	 * inactivates those securities.
	 * 
	 * @param dataContainerIterator
	 * @param fullLoadStartDateTime
	 * @throws UdmBaseException
	 */
	public void inactivateApplicableSecurity(Iterator<DataContainer> dataContainerIterator,
			LocalDateTime fullLoadStartDateTime) throws UdmBaseException;

}
