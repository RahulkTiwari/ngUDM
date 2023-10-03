/*******************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: IvoLockService.java
 * Author: Varun Ramani
 * Date: Feb 12, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.ivo;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author VRamani
 *
 */
public interface IvoLockService {

	/**
	 * This method receives List of editedDataContainer at INS and LE level as
	 * input. For each DataContainer based on LockLevel and xrf/non-xrf
	 * attribute, it segregates the DataContainer into centralized(sdIVO) and
	 * inline containers. Segragated DataContainers are merged and persisted in
	 * the respective collections.
	 * 
	 * This is not transactional .Updates in SdData and SdIvo are in separate
	 * transactions.
	 * 
	 * @param editedDataContainers
	 * @throws UdmBaseException 
	 * @throws Exception
	 */
	void persist(List<DataContainer> editedDataContainers) throws  UdmBaseException;

	/**
	 * Combines IVO locks from {@code sdIvoDataContainer} into
	 * {@code sdDataContainer}
	 * 
	 * @param sdInsDataContainer A pre-poulated instance of {@link DataContainer} fetched from SD
	 * @param sdIvoInsDataContainer A pre-poulated instance of {@link DataContainer} fetched from SD IVO 
	 * @param xrDataContainer
	 * @return
	 */
	DataContainer mergeContainers(DataContainer sdInsDataContainer, DataContainer sdIvoInsDataContainer,
			DataContainer xrDataContainer) throws UdmTechnicalException;

	
	/**
	 * This method will take list of dataContainer.And find Equivalent DataContainer from db from that
	 * it will removes value at Lock level for Each DataAttribute value mentioned in deletedDataContainerList 
	 * @param deletedDataContainerList
	 * @throws UdmTechnicalException
	 * @throws UdmBaseException 
	 */
	 void removeIvoLocks(List<DataContainer> deletedDataContainerList) throws  UdmBaseException ;
	
}
