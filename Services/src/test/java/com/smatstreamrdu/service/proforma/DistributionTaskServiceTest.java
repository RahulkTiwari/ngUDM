/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DistributionTaskServiceTest.java
 * Author :SaJadhav
 * Date : 06-Feb-2020
 */
package com.smatstreamrdu.service.proforma;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.ProformaDistributionTask;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.proforma.DistributionTaskService;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DistributionTaskServiceTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private DistributionTaskService disTaskService;

	@Test
	public void testGetDistributionTaskFromName() {
		ProformaDistributionTask disTask = disTaskService.getDistributionTaskFromNameAndStatus("ESMA_FULL", "A");
		assertNotNull(disTask);
		assertEquals("ESMA_FULL", disTask.getName());
		assertEquals("ESMA", disTask.getProfile());
		assertEquals("FULL", disTask.getFileType().name());
	}

	@Test
	public void testGetDeltaDistributionTaskNameForProfileName() {
		String disTask = disTaskService.getDeltaDistributionTaskNameForProfileName("ANNA");
		assertNotNull(disTask);
		assertEquals("ANNA_DELTA", disTask);
	}
	
	@Test
	public void testGetDeltaDistributionTaskNameForProfileName_NoResult() {
		String disTask = disTaskService.getDeltaDistributionTaskNameForProfileName("ANNADELTA");
		assertNull(disTask);
	}

	@Test
	@InputCollectionsPath(paths = { "DistributionTaskServiceTest/input" })
	@ModifiedCollections(collections = { "proformaDistributionTask" })
	public void testinactivateDisTasksForProfileName() {
		ProformaDistributionTask distributionTask = disTaskService
				.getDistributionTaskFromNameAndStatus("ESMA_FULL_DELETE_API", "I");
		assertNull(distributionTask);

		distributionTask = disTaskService.getDistributionTaskFromNameAndStatus("ESMA_FULL_DELETE_API", "A");
		assertNotNull(distributionTask);

		disTaskService.inactivateDisTasksForProfileName("ESMA", "ashok.thanage@smartstreamrdu.com");
		ProformaDistributionTask expectedDistributionTask = disTaskService.getDistributionTaskFromNameAndStatus("ESMA_FULL_DELETE_API", "I");
		assertNotNull(expectedDistributionTask);
		assertEquals("ESMA", expectedDistributionTask.getProfile());
	}
}
