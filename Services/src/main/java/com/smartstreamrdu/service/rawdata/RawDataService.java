/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataHandlerService.java
 * Author:	Divya Bharadwaj
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.Record;

/**
 * @author Bharadwaj
 *
 */
public interface RawDataService extends Serializable {

	/**
	 * 
	 * @param batchOfRecords
	 * @param inactiveFilterService
	 * @param rawDataFilter
	 * @param rawDataFilterContext
	 * @param string 
	 * @return
	 */
	List<Record> handleRawData(List<RawRecordContextDetailsPojo> batchOfRecords,RawDataInactiveFilterService inactiveFilterService, 
			RawDataFilter rawDataFilter, RawDataFilterContext rawDataFilterContext, String feedVsDbRawDataMergeStrategy, String filePath);

}
