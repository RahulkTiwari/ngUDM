/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedConfigurationServiceImplTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.feed;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.UdlFeedConfigSearch;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.repository.FeedConfigurationRepository;
import com.smartstreamrdu.service.util.MockUtil;


/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class FeedConfigurationServiceImplTest extends AbstractEmbeddedMongodbJunitParent{

	@InjectMocks
	private FeedConfigurationServiceImpl service;

	@Mock
	private FeedConfigurationRepository repo;

	@Test
	public void test_fetchFeedConfigurationForFileLoad() {

		MockitoAnnotations.initMocks(this);
		
		MockUtil.mock_FeedConfigurationRepository(repo);

		UdlFeedConfigSearch search = new UdlFeedConfigSearch();
		
		search.setDatasource("trdse");
		search.setFileType("XE");
		FeedConfiguration feedConfig = service.fetchFeedConfigurationForFileLoad(search);
		
		Assert.assertNotNull(feedConfig);
		Assert.assertEquals("trdse", feedConfig.getDatasource());
		
		Assert.assertEquals("XE", feedConfig.getFileType().get(0));
		
		
		
	}

}
