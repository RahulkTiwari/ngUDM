/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMerging.java
 * Author:	Jay Sangoi
 * Date:	24-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * Interface for Data Container Merging. It takes feed data container and db data container, performs merged and returns Merged data container
 * @author Jay Sangoi
 *
 */
public interface DataContainerMerging {
	/**
	 * Merge the feed data container with db data container
	 * @param feedContainer - feed data container 
	 * @param dbContainers - List of db data containers
	 * @throws DataContainerMergeException - Any exception while performing data container merging
	 * @return
	 * @throws Exception 
	 */
	void merge(DataContainer feedContainer, List<DataContainer> dbContainers) throws DataContainerMergeException, Exception;
}
