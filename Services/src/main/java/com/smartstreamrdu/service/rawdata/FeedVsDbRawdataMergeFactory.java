/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedVsDbRawdataMergeFactory.java
 * Author:	GMathur
 * Date:	29-Oct-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * Factory to get instance of FeedVsDbRawdataMergeService based on feedConfiguration property 'feedVsDbRawdataMergeStrategy'.
 */
@Component
public class FeedVsDbRawdataMergeFactory {

	@Autowired
	@Setter
	private Map<String, FeedVsDbRawdataMergeService> mergeServiceMap;
	
	/**
	 * It will return an instance of FeedVsDbRawdataMergeService based on feedConfiguration property 'feedVsDbRawdataMergeStrategy'.
	 * Returns 'null', if input parameter is null or empty.
	 * 
	 * @param feedVsDbRawdataMergeStrategy
	 * @return
	 */
	public FeedVsDbRawdataMergeService getFeedVsDbMergeStrategy(String feedVsDbRawdataMergeStrategy) {
		if(!StringUtils.isEmpty(feedVsDbRawdataMergeStrategy)) {
			return mergeServiceMap.get(feedVsDbRawdataMergeStrategy);
		}
		return null;
	}
}
