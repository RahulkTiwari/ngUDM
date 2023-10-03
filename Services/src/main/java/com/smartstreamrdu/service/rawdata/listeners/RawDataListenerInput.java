/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataListenerInput.java
 * Author:	Padgaonkar S
 * Date:	29-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata.listeners;

import com.smartstreamrdu.domain.DataContainer;

import lombok.Builder;
import lombok.Data;

/**
 * Input class for rawDataListeners
 * @author Padgaonkar
 *
 */
@Data
@Builder
public class RawDataListenerInput {

	private String filePath;
	
	//rawData dbConatiner
	private DataContainer dbDataContainer;
	
	private String dataSource;
	
}
