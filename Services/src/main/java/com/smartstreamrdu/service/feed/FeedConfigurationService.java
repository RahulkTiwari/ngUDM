/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedConfigurationService.java
 * Author:	Jay Sangoi
 * Date:	04-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.feed;

import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.UdlFeedConfigSearch;

/**
 * @author Jay Sangoi
 *
 */
public interface FeedConfigurationService {
	/**
	 * Fetch the feed configuration for Udl
	 * @param search
	 * @return
	 */
	FeedConfiguration fetchFeedConfigurationForFileLoad(UdlFeedConfigSearch search);
}
