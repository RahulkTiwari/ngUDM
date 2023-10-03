/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerCloneServiceTest.java
 * Author:	Jay Sangoi
 * Date:	15-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.domain;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.domain.DataContainerCloneService;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * Test class for {@link - DataContainerCloneService}
 * 
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DataContainerCloneServiceTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private DataContainerCloneService service;

	@Test
	public void test_createMinimumAttributeInactiveInstrumentDC() throws UdmBaseException {

		/**
		 * Test case 1 - Null Data container
		 */
		Assert.assertNull(service.createMinimumAttributeInactiveInstrumentDC(null));
		
		/**
		 * Test Case 2 - Container with minimum attribute
		 */
		DataContainer insContainer = createInstrumentDataContainer();
		
		DataContainer cloneDC = service.createMinimumAttributeInactiveInstrumentDC(insContainer);

		Assert.assertNotNull(cloneDC);

		DomainType status = (DomainType) cloneDC.getAttributeValueAtLevel(LockLevel.ENRICHED, DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS), DataLevel.INS));

		Assert.assertNotNull(status);

		Assert.assertEquals(DomainStatus.INACTIVE, status.getNormalizedValue());
		
		Assert.assertEquals(insContainer.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)),
				cloneDC.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));

	}
	
	@Ignore
	public void test_createMinimumAttributeActiveInstrumentDC() throws UdmBaseException {

		/**
		 * Test case 1 - Null Data container
		 */
		Assert.assertNull(service.createMinimumAttributeActiveInstrumentDC(null));
		
		/**
		 * Test Case 2 - Container with minimum attribute
		 */
		DataContainer insContainer = createInstrumentDataContainer();
		
		DataContainer cloneDC = service.createMinimumAttributeActiveInstrumentDC(insContainer);

		Assert.assertNotNull(cloneDC);

		DomainType status = (DomainType) cloneDC.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS), DataLevel.INS));

		Assert.assertNotNull(status);

		Assert.assertEquals(new String("1"), status.getVal());
		
		Assert.assertEquals(insContainer.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)),
				cloneDC.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));

	}

	@Test
	public void test_createMinimumAttributeInactiveSecurityDC() throws UdmBaseException {

		/**
		 * Test case 1 - Null Data container
		 */
		Assert.assertNull(service.createMinimumAttributeInactiveSecurityDC(null,null));
		
		/**
		 * Test Case 2 - Container with minimum attribute
		 */
		DataContainer secContainer = createSecurityDataContainer();
		
		DataContainer cloneDC = service.createMinimumAttributeInactiveSecurityDC(secContainer,getDataSource());

		Assert.assertNotNull(cloneDC);

		/*
		 * DomainType status = (DomainType)
		 * cloneDC.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory
		 * .getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(
		 * DataLevel.SEC), DataLevel.SEC));
		 * 
		 * Assert.assertNotNull(status);
		 * 
		 * Assert.assertEquals(new String("0"), status.getVal());
		 */
		
		Assert.assertEquals(secContainer.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC)),
				cloneDC.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC)));

	}
	
	private DomainType getDataSource() {
		DomainType dataSource = new DomainType();
		dataSource.setVal("trdse");
		return dataSource;
	}

	@Test
	public void test_createMinimumAttributeActiveSecurityDC() throws UdmBaseException {

		/**
		 * Test case 1 - Null Data container
		 */
		Assert.assertNull(service.createMinimumAttributeActiveSecurityDC(null,null));
		
		/**
		 * Test Case 2 - Container with minimum attribute
		 */
		DataContainer secContainer = createSecurityDataContainer();
		
		DataContainer cloneDC = service.createMinimumAttributeActiveSecurityDC(secContainer,getDataSource());

		Assert.assertNotNull(cloneDC);

		DomainType status = (DomainType) cloneDC.getAttributeValueAtLevel(LockLevel.ENRICHED, DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.SEC), DataLevel.SEC));

		Assert.assertNotNull(status);

		Assert.assertEquals(DomainStatus.ACTIVE, status.getNormalizedValue());
		
		Assert.assertEquals(secContainer.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC)),
				cloneDC.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC)));

	}
	
	@Test
	public void test_InvalidObjectForMinimumCLoning(){
		DataContainer secContainer = createSecurityDataContainer();
		
		try {
			service.createMinimumAttributeInactiveInstrumentDC(secContainer);
		} catch (UdmBaseException e) {
			Assert.assertEquals(String.format("Requested to craete Instrument DC, however the DC passed is %s",secContainer.getLevel()), e.getMessage());
			
		}
		
		DataContainer insContainer = createInstrumentDataContainer();
		
		try {
			service.createMinimumAttributeInactiveSecurityDC(insContainer,getDataSource());
		} catch (UdmBaseException e) {
			Assert.assertEquals(String.format("Requested to craete Security DC, however the DC passed is %s",insContainer.getLevel()), e.getMessage());
		}
		
	}

	
	private DataContainer createInstrumentDataContainer() {
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, UUID.randomUUID().toString());
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV);

		DomainType datasource = new DomainType("trdse");
		DataValue<DomainType> datasourceDV = new DataValue<>();
		datasourceDV.setValue(LockLevel.FEED, (DomainType) datasource);
		insContainer.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), datasourceDV);
		return insContainer;
	}
	
	private DataContainer createSecurityDataContainer() {
		DataContainerContext context = new DataContainerContext();
		context.setDataSource("trdse");
		DataContainer insContainer = DataContainerTestUtil.getSecurityContainer(context);
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, UUID.randomUUID().toString());
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV);

		DomainType datasource = new DomainType("trdse");
		DataValue<DomainType> datasourceDV = new DataValue<>();
		datasourceDV.setValue(LockLevel.FEED, (DomainType) datasource);
		insContainer.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.SEC), datasourceDV);
		return insContainer;
	}

}
