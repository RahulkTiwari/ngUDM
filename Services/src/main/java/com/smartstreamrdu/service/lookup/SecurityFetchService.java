/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	SecurityFetchService.java
 * Author:	Jay Sangoi
 * Date:	17-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup;

import java.util.List;
import java.util.Map;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Jay Sangoi
 *
 */
public interface SecurityFetchService {
	/**
	 * This method is created for UDM-38439. Whenever the security is moved from one instrument to another instrument, we need to inactivate the previous security. 
	 * @return source and destination data container that needs to be merged and persisted
	 * @throws UdmTechnicalException
	 * @throws UdmBaseException 
	 */
	Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated(DataContainer feedContainer, List<DataContainer> dbDataContainers ) throws UdmBaseException;

}
