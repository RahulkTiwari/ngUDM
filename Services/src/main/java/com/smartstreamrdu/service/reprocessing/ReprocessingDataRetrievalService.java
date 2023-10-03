/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ReprocessingDataRetrievalService.java
 * Author:	GMathur
 * Date:	23-Jan-2020
 *
 *******************************************************************/

package com.smartstreamrdu.service.reprocessing;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.spark.api.java.JavaRDD;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.message.ReprocessingData;
import com.smartstreamrdu.exception.UdmTechnicalException;
/**
 * Service to get list of DataContainer to reprocess. 
 */
public interface ReprocessingDataRetrievalService {

	/**
	 * Returns RDD of DataContainers based on Criteria where <pre>
	 * { $OR : [{
	 * 				$AND : [{
	 * 							$IS : {DataSource, value},
	 * 							$OR : [{
	 * 										$IN : (DataAttribute,DomainType)
	 * 								  }]
	 * 					   }]
	 * 		   }]
	 * }.
	 * </pre>
	 * 
	 * @param rduDomain rduDomain changed 
	 * @param mapOfDomainSourceVsListOfReprocessingData map of domainSourc vs list of changed mappings for that domain source
	 * @param mapDataStorageVsDomainSources 
	 * @param normalizedValue normalizedValue of the changed domain mapping
	 * @return
	 * @throws UdmTechnicalException
	 */
	public Optional<JavaRDD<DataContainer>> getDataContainersToReprocess(String rduDomain,
			Map<String, List<ReprocessingData>> mapOfDomainSourceVsListOfReprocessingData,
			Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources,String normalizedValue) throws UdmTechnicalException;

}
