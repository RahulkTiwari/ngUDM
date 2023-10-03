/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : VfsOpenFigiRequestServiceFactoryTest.java
 * Author :SaJadhav
 * Date : 01-Dec-2021
 */
package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MongoConfig.class)
public class VfsOpenFigiRequestServiceFactoryTest {
	
	@Autowired
	private VfsOpenFigiRequestServiceFactory factory;
	
	@Autowired
	private FeedUpdatesVfsOpenFigiRequestService feedUpdatesVfsOpenFigiRequestService;
	
	@Autowired
	private IvoUpdatesVfsOpenFigiRequestService ivoUpdatesVfsOpenFigiRequestService;
	
	@Test
	public void testGetVfsOpenFigiRequestService_ivo() {
		VfsOpenFigiRequestService vfsOpenFigiRequestService = factory.getVfsOpenFigiRequestService(DataLevel.IVO_INS);
		assertEquals(ivoUpdatesVfsOpenFigiRequestService, vfsOpenFigiRequestService);
	}
	
	@Test
	public void testGetVfsOpenFigiRequestService() {
		VfsOpenFigiRequestService vfsOpenFigiRequestService = factory.getVfsOpenFigiRequestService(DataLevel.INS);
		assertEquals(feedUpdatesVfsOpenFigiRequestService, vfsOpenFigiRequestService);
	}
	
	@Test
	public void testGetVfsOpenFigiRequestService_levelLE() {
		VfsOpenFigiRequestService vfsOpenFigiRequestService = factory.getVfsOpenFigiRequestService(DataLevel.LE);
		assertNull(vfsOpenFigiRequestService);
	}

}
