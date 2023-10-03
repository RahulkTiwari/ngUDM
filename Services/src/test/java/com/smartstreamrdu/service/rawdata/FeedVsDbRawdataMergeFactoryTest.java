/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedVsDbRawdataMergeFactoryTest.java
 * Author:	GMathur
 * Date:	08-Jan-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class FeedVsDbRawdataMergeFactoryTest {

	@Autowired
	private FeedVsDbRawdataMergeFactory feedVsDbRawdataMergeFactory;
	
	@Test
	public void testFactory() {
		FeedVsDbRawdataMergeService obj = feedVsDbRawdataMergeFactory.getFeedVsDbMergeStrategy("DefaultFeedVsDbRawdataMergeService");
		assertEquals("FeedVsDbRawdataMergeServiceImpl", obj.getClass().getSimpleName());
	}
}
