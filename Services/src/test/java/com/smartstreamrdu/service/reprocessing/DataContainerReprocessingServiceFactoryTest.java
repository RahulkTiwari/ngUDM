/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerReprocessingServiceFactoryTest.java
 * Author :SaJadhav
 * Date : 07-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DataContainerReprocessingServiceFactoryTest {
	
	@Autowired
	private DataContainerReprocessingServiceFactory factory;
	
	@Autowired
	private XrfDataContainerReprocessingService xrfReprocessingService;
	
	@Autowired
	private NonXrfDataContainerReprocessingService nonXrfReprocessingService;
	
	@Test
	public void testGetDataContainerReprocessingService_xrfDataSource() throws UdmBaseException {
		DataContainer dataContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		dataSourceValue.setValue(LockLevel.FEED, new DomainType("trdse"));
		dataContainer.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), dataSourceValue);
		DataContainerReprocessingService dataContainerReprocessingService = factory
				.getDataContainerReprocessingService(dataContainer);
		assertEquals(xrfReprocessingService, dataContainerReprocessingService);
	}
	
	@Test
	public void testGetDataContainerReprocessingService_nonXrfDataSource() throws UdmBaseException {
		DataContainer dataContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		dataSourceValue.setValue(LockLevel.FEED, new DomainType("rduEns"));
		dataContainer.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.EN), dataSourceValue);
		DataContainerReprocessingService dataContainerReprocessingService = factory
				.getDataContainerReprocessingService(dataContainer);
		assertEquals(nonXrfReprocessingService, dataContainerReprocessingService);
	}

}
