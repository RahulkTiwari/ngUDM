/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : IvoQueryServiceTest.java
 * Author :SaJadhav
 * Date : 30-Nov-2021
 */
package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdIvo;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MongoConfig.class)
public class IvoQueryServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private IvoQueryService ivoQueryService;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	
	@Test
	@InputCollectionsPath(paths = "IvoQueryServiceTest/input/xrData.json")
	@ModifiedCollections(collections = {"xrData"})
	public void testGetXrfDataContainerFromIvoDataContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> listSdIvoDocs= bsonConverter.getListOfDataContainersFromFilePath("IvoQueryServiceTest/input/sdIvo.json", SdIvo.class);
		DataContainer ivoDataContainer=listSdIvoDocs.get(0);
		DataContainer xrData = ivoQueryService.getXrfDataContainerFromIvoDataContainer(ivoDataContainer);
		assertNotNull(xrData);
		assertEquals("5e2012fb21ec0916e88bb9c5", xrData.get_id());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetXrfDataContainerFromIvoDataContainer_invalidInput() throws UdmTechnicalException, IOException {
		DataContainer ivoDataContainer=new DataContainer(DataLevel.INS, null);
		ivoQueryService.getXrfDataContainerFromIvoDataContainer(ivoDataContainer);
	}
	
	@Test(expected = NullPointerException.class)
	public void testGetXrfDataContainerFromIvoDataContainer_nullInput() throws UdmTechnicalException, IOException {
		ivoQueryService.getXrfDataContainerFromIvoDataContainer(null);
	}

}
