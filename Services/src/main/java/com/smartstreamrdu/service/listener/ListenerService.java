/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ListnerService.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.listener;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.FeedConfiguration;

/**
 * @author Jay Sangoi
 *
 */
public interface ListenerService {

	/**
	 * Whenever the data in db and feed has changed
	 * @param oldContainer
	 * @param newContainer
	 * @param fileType 
	 */
	void dataContainerUpdated(String datasource, DataContainer oldContainer, DataContainer newContainer,FeedConfiguration feedConfiguration);
	
	/**
	 * Whenever data container merge is done
	 * @param feedConfig - feed configuration
	 * @param feedContainer - feed container 
	 * @param dbContainer - dbcontainer
	 */
	void dataContainerMerge(String datasource, DataContainer feedContainer, DataContainer dbContainer);
	
	
	/**
	 * Whenever the new data Container is Added
	 * @param newContainer
	 */
	void newdataContainer(DataContainer container);
	
	
	void mergeComplete(String datasource, DataContainer feedContainer, DataContainer dbContainer);
}
