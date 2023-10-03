/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : SdDataReprocessingService.java
 * Author :SaJadhav
 * Date : 22-Jan-2020
 */
package com.smartstreamrdu.service.reprocessing;

import java.time.LocalDateTime;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmBaseException;

/**
 * Service for reprocessing the DataContainers after static data change
 * 
 * @author SaJadhav
 *
 */
public interface DataContainerReprocessingService {

	/**
	 * Sends the message to downstream components (XRF or Proforma) for the
	 * dataContainer affected by static data change.
	 * 
	 * If dataSource for the dataContainer is xrf-dataSource(xrfSourcePriority >0 ) then sends message to XRF
	 * otherwise sends message to proforma
	 * 
	 * @param dataContainer
	 * @param staticDataUpdateDate
	 * @param rduDomain 
	 * @param changedDomainFieldNames 
	 * @throws UdmBaseException 
	 */
	public void reprocesDataContainer(DataContainer dataContainer, LocalDateTime staticDataUpdateDate, String rduDomain, List<String> changedDomainFieldNames) throws UdmBaseException;
}
