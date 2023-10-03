/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergeAndPersistService.java
 * Author:	Jay Sangoi
 * Date:	13-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;
import com.smartstreamrdu.service.merging.DataContainerMergeException;

/**
 * 
 * Merge and Persist Service. This has methods like merge and persist, merge etc
 * @author Jay Sangoi
 *
 */
public interface MergeAndPersistService {

	/**
	 * This will merge the feed and db container and persist the merged result into db.
	 * It will also call the respective listener
	 * @param feedContainer
	 * @param dbContainers
	 * @throws DataContainerMergeException 
	 */
	void mergeAndPersistSd(String datasource, DataContainer feedContainer, List<DataContainer> dbContainers,FeedConfiguration feedConfiguration) throws DataContainerMergeException;
	
	/**
	 * Performs the lookup, merge and perisst the data container
	 * @param container
	 * @param input1
	 * @param datasource
	 * @param feedConfiguration 
	 * @return
	 * @throws Exception
	 */
	DataContainer lookupAndPersist(DataContainer container, LookupAttributeInput input1, String datasource, FeedConfiguration feedConfiguration)
			throws UdmBaseException;
	
	
}
