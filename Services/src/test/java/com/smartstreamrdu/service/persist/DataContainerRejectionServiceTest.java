/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ExpirationDateBasedDataContainerRejectionService.java
 * Author:  Padgaonkar
 * Date:    Feb 10, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.SpringUtil;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DataContainerRejectionServiceTest {
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Autowired
	private RduInactivationDateBasedDataContainerRejectionService rejectionService;
	
	@Test
	public void test_InvalidContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"DataContainerRejectionServiceTest/testInValidContainer/sdData.json", SdData.class);
		rejectionService.validateAndRemoveInvalidContainerFromList(containers);
		Assert.assertEquals(0, containers.size());
	}
	
	@Test
	public void test_ValidContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"DataContainerRejectionServiceTest/testValidContainer/sdData.json", SdData.class);

		rejectionService.validateAndRemoveInvalidContainerFromList(containers);
		Assert.assertEquals(1, containers.size());
	}
	
	@Test
	public void test_ValidContainer_RduInactivationDate() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"DataContainerRejectionServiceTest/testInavlidContainerWithoutDate/sdData.json", SdData.class);

		rejectionService.validateAndRemoveInvalidContainerFromList(containers);
		Assert.assertEquals(1, containers.size());
	}
	
	
	@Test
	public void test_InValidLevelContainer() throws UdmTechnicalException, IOException {	
		DataContainer container = new DataContainer(DataLevel.IVO_INS, null);
		List<DataContainer> containerList = Arrays.asList(container);
		rejectionService.validateAndRemoveInvalidContainerFromList(Arrays.asList(container));
		Assert.assertEquals(1, containerList.size());
	}
	
	@Test
	public void test_InValidNullLevelContainer() throws UdmTechnicalException, IOException {	
		DataContainer container = new DataContainer(DataLevel.IVO_INS, null);
		container.setLevel(null);
		List<DataContainer> containerList = Arrays.asList(container);
		rejectionService.validateAndRemoveInvalidContainerFromList(Arrays.asList(container));
		Assert.assertEquals(1, containerList.size());
	}
	
	@Test
	public void test_InValidNotNewLevelContainer() throws UdmTechnicalException, IOException {	
		DataContainer container = new DataContainer(DataLevel.IVO_INS, null);
		container.setNew(false);
		List<DataContainer> containerList = Arrays.asList(container);
		rejectionService.validateAndRemoveInvalidContainerFromList(Arrays.asList(container));
		Assert.assertEquals(1, containerList.size());
	}
	
	
	@Test
	public void test_InValidContainerSecurityLevel() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"DataContainerRejectionServiceTest/testRejectSecLevelDataContainer/sdData.json", SdData.class);

		DataContainerRejectionService service = SpringUtil.getBean(InsInactivationLevelBasedDataContainerRejectionService.class);
		service.validateAndRemoveInvalidContainerFromList(containers);
		Assert.assertEquals(0, containers.size());
	}
	
	@Test
	public void test_ValidContainerSecurityLevel() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"DataContainerRejectionServiceTest/testValidSecLevelDataContainer/sdData.json", SdData.class);

		DataContainerRejectionService service = SpringUtil.getBean(InsInactivationLevelBasedDataContainerRejectionService.class);
		service.validateAndRemoveInvalidContainerFromList(containers);
		Assert.assertEquals(1, containers.size());
	}
	
	

}
