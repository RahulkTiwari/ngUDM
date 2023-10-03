/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataFilter.java
 * Author:	Jay Sangoi
 * Date:	16-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * Common interface for performing data filter. Example, we want to add a filter, where if status is inactive, we should not save it.
 * @author Jay Sangoi
 *
 */
public interface DataFilter extends Serializable{
	
	/**
	 * Is the filter defined for the feed
	 * @param feedName
	 * @return
	 */
	boolean isApplicationForFeed(String feedName);
	
	/**
	 * Should the data container be persisted. 
	 * @param feedContainer
	 * @param dbContainer
	 * @return true, if it needs to be persisted, else false
	 * @throws UdmTechnicalException
	 */
	FilterOutput doPersist(DataContainer feedContainer, List<DataContainer> dbContainer, String dataSource) throws UdmTechnicalException;
	
}
