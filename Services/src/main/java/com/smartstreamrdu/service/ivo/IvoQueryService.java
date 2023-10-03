/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoQueryService.java
* Author : Padgaonkar
* Date : April 10, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * This interface is used by IvoServices to query on Sd and SdIvo Collection to get MatchingDataContainers.
 */
public interface IvoQueryService {

	
	List<DataContainer> getMatchingDataContainerByDocumentId(DataContainer sdContainer) throws UdmTechnicalException;
	
	List<DataContainer> getMatchingLegalEntityDataContainerFromSdData(DataContainer sdLeContainer) throws UdmTechnicalException;

	List<DataContainer> getMatchingDataContainerFromSdIvo(DataContainer sdIvoContainer) throws UdmTechnicalException;
	
	List<DataContainer> getXrfDataContainer(String instrumentId,String sdDocumentId) throws UdmTechnicalException;
	
	/**
	 * Retrieves XRF dataContainer from input {@code ivoDataContainer}.
	 * It queries the xrData collection based on xrf documentId present in the instrumentRelations.
	 * 
	 * @param ivoDataContainer
	 * @return
	 * @throws UdmTechnicalException 
	 */
	DataContainer getXrfDataContainerFromIvoDataContainer(DataContainer ivoDataContainer) throws UdmTechnicalException;

}
