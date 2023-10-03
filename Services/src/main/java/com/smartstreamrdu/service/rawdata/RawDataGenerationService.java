/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataGenerationService.java
 * Author:	Divya Bharadwaj
 * Date:	07-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author Bharadwaj
 *
 */
public interface RawDataGenerationService extends Serializable {

	/**
	 * @param obj
	 * @return
	 */
	DataContainer generateRawData(RawDataInputPojo obj,String dataSource);
}
