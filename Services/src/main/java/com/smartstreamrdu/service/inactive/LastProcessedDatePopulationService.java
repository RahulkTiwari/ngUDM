/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LastProcessedDatePopulationService.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.domain.EventConfiguration;

public interface LastProcessedDatePopulationService {

	/**
	 * This method check whether input rawDatacontainer is applicable based on provided config & if yes then
	 * it retrieve corresponding normalized container using rawDataId  & populates lastProcessedDate in it.
	 * 
	 * @param fileName
	 * @param rawDbDataContainer
	 * @param config
	 */
	public void populateLastProcessedDateBasedOnRawContainer(String fileName, DataContainer rawDbDataContainer, EventConfiguration config);

	
	/**
	 * This method checks if provided container is applicable based on config & if its applicable then it populates
	 * lastProcessedDate in it.
	 * @param feedDataContainer
	 * @param config
	 */
	public void populateLastProcessedDateInFeedContainer(DataContainer feedDataContainer, EventConfiguration config);

	
	/**
	 * Based on provided level it populates lastProcessedDate in dataContainer.
	 * @param dc
	 * @param level
	 */
	public void populateLastProcessedDate(DataContainer dc,DataLevel level);

}
