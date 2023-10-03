/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerReprocessingServiceFactory.java
 * Author :SaJadhav
 * Date : 07-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.service.events.XrfEligibilityEvaluator;

import lombok.Setter;

/**
 * Factory for getting instance of DataContainerReprocessingService
 * 
 * @author SaJadhav
 *
 */
@Component
public class DataContainerReprocessingServiceFactory {
	
	@Autowired
	@Setter
	private XrfEligibilityEvaluator xrfEligibilityEvaluator;
	
	@Autowired
	@Setter
	private XrfDataContainerReprocessingService xrfDataContainerReprocessingService;
	
	@Autowired
	@Setter
	private NonXrfDataContainerReprocessingService nonXrfDataContainerReprocessingService;
	
	
	/**
	 * Factory method for getting DataContainerReprocessingService instance based on xrfSourcePriority for the dataSource
	 * 
	 * @param dataContainer
	 * @return
	 * @throws UdmBaseException
	 */
	public DataContainerReprocessingService getDataContainerReprocessingService(DataContainer dataContainer) throws UdmBaseException {
		
		if(xrfEligibilityEvaluator.isDataContainerEligibleForXrf(dataContainer)) {
			return xrfDataContainerReprocessingService;
		}else {
			return nonXrfDataContainerReprocessingService;
		}
	}

}
