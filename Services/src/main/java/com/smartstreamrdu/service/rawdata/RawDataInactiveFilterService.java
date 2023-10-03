/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataInactiveFilterService.java
 * Author:	Shruti Arora
 * Date:	21-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.Record;

public interface RawDataInactiveFilterService extends Serializable{
	
	boolean shouldPersistRawData(Record record, List<DataContainer> dbContainers);

}
