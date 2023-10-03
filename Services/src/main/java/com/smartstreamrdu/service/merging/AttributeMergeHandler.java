/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: MergeHandler.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DomainType;

/**
 * @author Dedhia
 *
 */
public interface AttributeMergeHandler{

	
	/**
	 *  This method takes input as a data attribute and dataAttribute and a Map of DataAttribute VS dataValue. 
	 *  It takes into consideration the data type of the data attribute and then merges the contents accordingly.
	 * @param sourceDataContainer
	 * @param destinationDataContainer
	 * @param dataAttribute
	 */
	void handleAttributeMerge(DataContainer feedContainer, DataContainer dbContainer, DataAttribute dataAttribute);
	
	void handleAttributeMerge(DataRow feedDataRow, DataRow dbDataRow, DataAttribute dataAttribute,DomainType dataSource);
	
}
