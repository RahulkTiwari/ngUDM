/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedConfigurationServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	04-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.feed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.UdlFeedConfigSearch;
import com.smartstreamrdu.persistence.repository.FeedConfigurationRepository;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class FeedConfigurationServiceImpl implements FeedConfigurationService{

	@Autowired
	private FeedConfigurationRepository repo;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.feed.FeedConfigurationService#fetchFeedConfigurationForFileLoad(com.smartstreamrdu.domain.UdlFeedConfigSearch)
	 */
	@Override
	public FeedConfiguration fetchFeedConfigurationForFileLoad(UdlFeedConfigSearch search) {
		return repo.findByDatasourceAndFileType(search.getDatasource(), search.getFileType());
	}
}
