/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    RduTechnicalSecurityGenerationServiceTest.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.postprocess;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.SdDataAttributeConstant;


@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RduTechnicalSecurityGenerationServiceTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private RduTechnicalSecurityGenerationService rduTechnicalSecurityGenerationService;
	
	@Autowired
	private ReloadIgniteCache cacheReloader;
	
	private static final String DV_DOMAIN_MAP = DataLevel.DV_DOMAIN_MAP.getCollectionName();

	
	@Test
	public void testActiveRduTechnicalSecurityGenerationService() throws UdmTechnicalException {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);
	    DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		
		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType("1", null, null));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, activeDataValue);
		
		rduTechnicalSecurityGenerationService.postProcess("idcApex", Arrays.asList(insContainer));
		
		List<DataContainer> allChildDataContainers = insContainer.getAllChildDataContainers();
		Assert.assertEquals(1, allChildDataContainers.size());
		
		DataContainer technicalSecurity = getTechnicalSecurity(allChildDataContainers);
		Assert.assertNotNull(technicalSecurity);
		
		String technicalSecurityStatus = getTechnicalSecurityStatus(technicalSecurity);
		Assert.assertEquals("A", technicalSecurityStatus);
		
	}
	
	
	@Test
	public void testNoRduTechnicalSecurity() throws UdmTechnicalException {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);
	    DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		
		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType("1", null, null,"ListingTradingStatusTypeMap"));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, activeDataValue);
		
		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);
		secContainer1.addAttributeValue(SdDataAttributeConstant.SEC_STATUS, activeDataValue);
		
		rduTechnicalSecurityGenerationService.postProcess("idcApex", Arrays.asList(insContainer));
		
		List<DataContainer> allChildDataContainers = insContainer.getAllChildDataContainers();
		Assert.assertEquals(1, allChildDataContainers.size());
		
		DataContainer technicalSecurity = getTechnicalSecurity(Arrays.asList(insContainer));
		Assert.assertNull(technicalSecurity);
				
	}
	
	
	@Test
	public void testSecLevelDataRduTechnicalSecurityGenerationService_2() throws UdmTechnicalException {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);
		
		
		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType("1", null, null));
		
		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;
		secContainer1.addAttributeValue(SdDataAttributeConstant.SEC_STATUS, activeDataValue);
		
		rduTechnicalSecurityGenerationService.postProcess("idcApex", Arrays.asList(secContainer1));
		
		DataContainer technicalSecurity = getTechnicalSecurity(Arrays.asList(secContainer1));
		Assert.assertNull(technicalSecurity);
				
	}
	
	@Test
	public void testNoSecurityStatusRduTechnicalSecurityGenerationService() throws UdmTechnicalException {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);

		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());

		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType("1", null, null));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, activeDataValue);

		rduTechnicalSecurityGenerationService.postProcess("idcApex", Arrays.asList(insContainer));

		List<DataContainer> allChildDataContainers = insContainer.getAllChildDataContainers();
		Assert.assertEquals(1, allChildDataContainers.size());

		DataContainer technicalSecurity = getTechnicalSecurity(allChildDataContainers);
		Assert.assertNotNull(technicalSecurity);

		String technicalSecurityStatus = getTechnicalSecurityStatus(technicalSecurity);
		Assert.assertEquals("A", technicalSecurityStatus);

	}
	
	
	@Test
	public void testSecNormalizedStatusValue() throws UdmTechnicalException {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);

		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());

		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType("1", null, null));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, activeDataValue);

		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);
		
		DataValue<DomainType> val = new DataValue<>();
		val.setValue(LockLevel.RDU, new DomainType(null, null, "A"));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, activeDataValue);
		secContainer1.addAttributeValue(SdDataAttributeConstant.SEC_STATUS, val);

		rduTechnicalSecurityGenerationService.postProcess("idcApex", Arrays.asList(insContainer));

		List<DataContainer> allChildDataContainers = insContainer.getAllChildDataContainers();
		Assert.assertEquals(1, allChildDataContainers.size());

		DataContainer technicalSecurity = getTechnicalSecurity(allChildDataContainers);
		Assert.assertNull(technicalSecurity);


	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testTechnicalSecurityInActivation() throws UdmTechnicalException {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);

		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());

		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType("1", null, null));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, activeDataValue);

		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		DataValue<DomainType> val = new DataValue<>();
		val.setValue(LockLevel.ENRICHED, new DomainType(null, null, "A"));
		secContainer1.addAttributeValue(SdDataAttributeConstant.SEC_STATUS, val);
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);
		
	
		
		DataContainer secContainer2 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		DataValue<DomainType> type = new DataValue<>();
		type.setValue(LockLevel.ENRICHED, new DomainType(null, null, "Technical"));
		secContainer2.addAttributeValue(SdDataAttributeConstant.RDU_SEC_TYPE, type);
		secContainer2.addAttributeValue(SdDataAttributeConstant.SEC_STATUS, val);
		insContainer.addDataContainer(secContainer2, DataLevel.SEC);
		


		rduTechnicalSecurityGenerationService.postProcess("idcApex", Arrays.asList(insContainer));

		List<DataContainer> allChildDataContainers = insContainer.getAllChildDataContainers();
		Assert.assertEquals(2, allChildDataContainers.size());

		DataContainer technicalSecurity = getTechnicalSecurity(allChildDataContainers);
		Assert.assertNotNull(technicalSecurity);

		String technicalSecurityStatus = getTechnicalSecurityStatus(technicalSecurity);
		DataValue<Serializable> securityStatusDataValue = (DataValue<Serializable>) technicalSecurity.
				getAttributeValue(SdDataAttributeConstant.SEC_STATUS);
		
		Assert.assertEquals(true, securityStatusDataValue.hasValueChanged());
		Assert.assertEquals("I", technicalSecurityStatus);
	}
	
	
	private String getTechnicalSecurityStatus(DataContainer childContainer) {
		DomainType statusValue = (DomainType) childContainer.getHighestPriorityValue(SdDataAttributeConstant.SEC_STATUS);
		if (statusValue != null ) {
			return statusValue.getNormalizedValue();
		}
		return null;
	}

	private DataContainer getTechnicalSecurity(List<DataContainer> chlidContainers) {
		for (DataContainer container : chlidContainers) {
			if (isTechnicalSecurity(container)) {
				return container;
			}
		}
		return null;
	}
	
	private boolean isTechnicalSecurity(DataContainer container) {
		DomainType secType = (DomainType) container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		if (secType != null) {
			String secTypeVal = secType.getNormalizedValue();
			return StringUtils.isNotEmpty(secTypeVal) && secTypeVal.equals("Technical");
		}
		return false;
	}
}
