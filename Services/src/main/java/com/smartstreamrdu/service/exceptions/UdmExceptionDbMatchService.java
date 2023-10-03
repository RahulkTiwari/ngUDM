/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionDbMatchService.java
 * Author:	Padgaonkar
 * Date:	17-April-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.util.Optional;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

public interface UdmExceptionDbMatchService {
	
	/**
	 * This method will search in UdmExceptionData using  stormCriteriaVale and return 
	 * Matching Container
	 * @param stormCriteriaVale
	 * @return
	 * @throws UdmTechnicalException 
	 */
	Optional<DataContainer> getExceptionDataContainer(UdmExceptionDataHolder  udmExceptionDataHolder) throws UdmTechnicalException;

}
