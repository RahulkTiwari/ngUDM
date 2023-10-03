/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerPostProcessorService.java
 * Author:	Rushikesh Dedhia
 * Date:	14-June-2019
 *
 ********************************************************************/
package com.smartstreamrdu.service.postprocess;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

public interface DataContainerPostProcessorService {
	
	/**
	 *  This method will apply the post processing logic on the supplied data sources as per the 
	 *  logic provided in the implementation. This method will check if the implementation is applicable
	 *  for the given data source and the data containers. If applicable, then only the post processor
	 *  implementation will be applied.
	 * @param dataSource
	 * @param dataContainers
	 * @throws UdmTechnicalException
	 */
	void postProcess(String dataSource, List<DataContainer> dataContainers) throws UdmTechnicalException;

}