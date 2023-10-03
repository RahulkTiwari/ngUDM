/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawdataFeedContainerUpdateStrategy.java
 * Author:	Dedhia
 * Date:	Mar 17, 2023
 *
 *******************************************************************/
package com.smartstreamrdu.service.rawdata;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.Record;

/**
 *  This interface provides the functionality to update the feed RAW DataContainer
 *  with the record object porvided.
 *  
 *  Implementations of this class will be data source specific.
 * 
 * @author Dedhia
 *
 */
public interface RawdataFeedContainerUpdateStrategy {
	
	/**
	 *  Updates and returns the feed data container object with the 
	 *  record object provided.
	 * 
	 * @param rawDataRecord
	 * @param pojo
	 * @param feedContainer
	 * @return
	 */
	DataContainer updatedFeedDataContainer(Record rawDataRecord, RawRecordContextDetailsPojo pojo, DataContainer feedContainer);

}
