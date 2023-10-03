/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawdataFeedContainerUpdateStrategyFactory.java
 * Author:	Dedhia
 * Date:	Mar 17, 2023
 *
 *******************************************************************/
package com.smartstreamrdu.service.rawdata;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  Factory class that for creating RawdataFeedContainerUpdateStrategy
 *  objects.
 * 
 * @author Dedhia
 *
 */
@Component
public class RawdataFeedContainerUpdateStrategyFactory {
	
	private static final String RAWDATA_FEED_CONTAINER_UPDATE_STRATEGY = "RawdataFeedContainerUpdateStrategy";
	
	@Autowired
	Map<String, RawdataFeedContainerUpdateStrategy> strategies;
	
	/**
	 *  Returns the RawdataFeedContainerUpdateStrategy configured for the provided 
	 *  data source. 
	 *  
	 *  If for a given data source there is no RawdataFeedContainerUpdateStrategy available
	 *  it returns null.
	 * 
	 * @param dataSource
	 * @return
	 */
	RawdataFeedContainerUpdateStrategy getStrategy(String dataSource) {
		return strategies.get(dataSource+RAWDATA_FEED_CONTAINER_UPDATE_STRATEGY);
	}

}
