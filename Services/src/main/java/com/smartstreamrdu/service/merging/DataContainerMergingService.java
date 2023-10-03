/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergingService.java
 * Author:	Jay Sangoi
 * Date:	24-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * Service to perform data continer merge
 * @author Jay Sangoi
 *
 */
public interface DataContainerMergingService extends Serializable{

	/**
	 * Perform data container merege
	 * @param feedContainer - feed container
	 * @param dbContainers - db containers
	 * @return - merged data container
	 * @throws DataContainerMergeException - Any exception while performing merging
	 */
	List<DataContainer> merge(DataContainer feedContainer, List<DataContainer> dbContainers)
			throws DataContainerMergeException;
	
	/**
	 * Get the feed equivalent db container from list of db containers 
	 * @param feedContainer
	 * @param dbContainers
	 * @return
	 * @throws UdmTechnicalException
	 */
	DataContainer getFeedEquivalentParentDbContainer(DataContainer feedContainer, List<DataContainer> dbContainers) throws UdmTechnicalException;

	
	/**
	 * Get the feed equivalent db container from list of db containers 
	 * @param feedContainer
	 * @param dbContainers
	 * @return
	 * @throws UdmTechnicalException
	 * @throws Exception 
	 */
	DataContainer getFeedEquivalentChildDbContainer(DataContainer feedParentContainer,DataContainer feedChildContainer, List<DataContainer> dbContainers) throws UdmTechnicalException;


}
