/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawdataFeedContainerUpdateStrategyFactoryTest.java
 * Author:	Dedhia
 * Date:	Mar 17, 2023
 *
 *******************************************************************/
package com.smartstreamrdu.service.rawdata;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RawdataFeedContainerUpdateStrategyFactoryTest {
	
	@Autowired
	private RawdataFeedContainerUpdateStrategyFactory rawdataFeedContainerUpdateStrategyFactory;

	@Test
	public void testGetStrategy() {
		RawdataFeedContainerUpdateStrategy strategy = rawdataFeedContainerUpdateStrategyFactory.getStrategy("rdsp");
		assertEquals(null, strategy);
		strategy = rawdataFeedContainerUpdateStrategyFactory.getStrategy("rdso");
		assertEquals(true, strategy instanceof DsosRawdataFeedContainerUpdateStrategy);
	}

}
