/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    TestInstrumentInActiveUtil.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

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
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.SdDataAttributeConstant;

@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class TestInstrumentInActiveUtil extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private ReloadIgniteCache cacheReloader;
	
	private static final String DV_DOMAIN_MAP = DataLevel.DV_DOMAIN_MAP.getCollectionName();

	
	@Test
	public void verifyInsStatus() {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);
		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		boolean instrumentStatusInActive = InstrumentInactiveUtil.isInstrumentStatusInActive("idcApex", insContainer);
		Assert.assertFalse(instrumentStatusInActive);
		
		DataValue<DomainType> inactiveDataValue = new DataValue<>();
		inactiveDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.INACTIVE));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, inactiveDataValue);
		boolean instrumentStatusInActive1 = InstrumentInactiveUtil.isInstrumentStatusInActive("idcApex", insContainer);
		Assert.assertTrue(instrumentStatusInActive1);
		
	}
	
	@Test
	public void verifyInsStatus_1() {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);
		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		
		DataValue<DomainType> inactiveDataValue = new DataValue<>();
		inactiveDataValue.setValue(LockLevel.RDU, new DomainType("2", null, null,"BondIssueStatusMap"));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, inactiveDataValue);
		boolean instrumentStatusInActive1 = InstrumentInactiveUtil.isInstrumentStatusInActive("idcApex", insContainer);
		Assert.assertTrue(instrumentStatusInActive1);
		
	}
	
	@Test
	public void verifyInsStatus_2() {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);
		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		
		DataValue<DomainType> inactiveDataValue = new DataValue<>();
		inactiveDataValue.setValue(LockLevel.RDU, new DomainType("999", null, null));
		insContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, inactiveDataValue);
		boolean instrumentStatusInActive1 = InstrumentInactiveUtil.isInstrumentStatusInActive("idcApex", insContainer);
		Assert.assertFalse(instrumentStatusInActive1);	
	}
		
}
