/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: EnrichmentLockRemoverTest.java
 * Author:Shreepad Padgaonkar
 * Date: Nov 20, 2020
 *
 *******************************************************************/
package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.SdDataAttributeConstant;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class EnrichmentLockRemoverTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private EnrichmentLockRemover enrichmentLockRemover;
	
	@Autowired
	private ReloadIgniteCache reloadIgniteCache;
	
	
	@Test
	public void noenrichmentLock() {
		
		DomainType dataSource = new DomainType("idcApex");
		DataAttribute secStatus = SdDataAttributeConstant.SEC_STATUS;
		
		DomainType domainVal = new DomainType("1");
		DataValue<Serializable> feedDataValue = new DataValue<>();
		feedDataValue.setValue(LockLevel.FEED, domainVal);
		
		DomainType dbdomainVal = new DomainType("1");
		DataValue<Serializable> dbDataValue = new DataValue<>();
		dbDataValue.setValue(LockLevel.FEED, dbdomainVal);
		enrichmentLockRemover.removeLockValue(secStatus, feedDataValue, dbDataValue, dataSource);
		
		assertNotNull(dbDataValue.getValue(LockLevel.FEED));
		assertEquals(domainVal, dbDataValue.getValue(LockLevel.FEED));
		
	}
	
	
	@Test
	public void enrichmentLockRemoval_1() {
		
		DomainType dataSource = new DomainType("idcApex");
		DataAttribute secStatus = SdDataAttributeConstant.SEC_STATUS;
		
		DomainType domainVal = new DomainType("1",null,null,"ListingTradingStatusTypeMap");
		DataValue<Serializable> feedDataValue = new DataValue<>();
		feedDataValue.setValue(LockLevel.FEED, domainVal);
		
		DomainType dbdomainVal = new DomainType(null,null,"A",null);
		DataValue<Serializable> dbDataValue = new DataValue<>();
		dbDataValue.setValue(LockLevel.ENRICHED, dbdomainVal);
		dbDataValue.setValue(LockLevel.FEED, domainVal);
		enrichmentLockRemover.removeLockValue(secStatus, feedDataValue, dbDataValue, dataSource);
		
		assertNotNull(dbDataValue.getValue(LockLevel.FEED));
		assertNull(dbDataValue.getValue(LockLevel.ENRICHED));
		assertEquals(domainVal, dbDataValue.getValue(LockLevel.FEED));
		
	}
	
	
	@Test
	public void enrichmentLockRemoval_day2Scenario_FeedDoesntProvideValue() {
		
		reloadIgniteCache.reloadCache(DataLevel.DV_DOMAIN_MAP.getCollectionName());
		DomainType dataSource = new DomainType("idcApex");
		DataAttribute secStatus = SdDataAttributeConstant.SEC_STATUS;
		
		DomainType domainVal = new DomainType(null,null,"A",null);
		DataValue<Serializable> feedDataValue = new DataValue<>();
		feedDataValue.setValue(LockLevel.ENRICHED, domainVal);
		
		DomainType feedDomainVal = new DomainType("1",null,null,"ListingTradingStatusTypeMap");
		DataValue<Serializable> dbDataValue = new DataValue<>();
		dbDataValue.setValue(LockLevel.FEED, feedDomainVal);
		dbDataValue.setValue(LockLevel.ENRICHED, domainVal);
		enrichmentLockRemover.removeLockValue(secStatus, feedDataValue, dbDataValue, dataSource);
		
		assertNotNull(dbDataValue.getValue(LockLevel.FEED));
		assertNull(dbDataValue.getValue(LockLevel.ENRICHED));
		assertEquals(feedDomainVal, dbDataValue.getValue(LockLevel.FEED));
		
	}
	
	
	@Test
	public void enrichmentLockRemoval_5() {
		
		DomainType dataSource = new DomainType("idcApex");
		DataAttribute secStatus = SdDataAttributeConstant.SEC_STATUS;
		
		DomainType domainVal = new DomainType(null,null,"A",null);
		DataValue<Serializable> feedDataValue = new DataValue<>();
		feedDataValue.setValue(LockLevel.ENRICHED, domainVal);
		
		DomainType feedDomainVal = new DomainType("1233",null,null,"ListingTradingStatusTypeMap");
		DataValue<Serializable> dbDataValue = new DataValue<>();
		dbDataValue.setValue(LockLevel.FEED, feedDomainVal);
		dbDataValue.setValue(LockLevel.ENRICHED, domainVal);
		enrichmentLockRemover.removeLockValue(secStatus, feedDataValue, dbDataValue, dataSource);
		
		assertNotNull(dbDataValue.getValue(LockLevel.FEED));
		assertNotNull(dbDataValue.getValue(LockLevel.ENRICHED));
		assertEquals(feedDomainVal, dbDataValue.getValue(LockLevel.FEED));
		assertEquals(domainVal, dbDataValue.getValue(LockLevel.ENRICHED));

	}
	
	
	@Test
	public void enrichmentLockRemoval_4() {
		
		DomainType dataSource = new DomainType("idcApex");
		DataAttribute secStatus = SdDataAttributeConstant.INS_ISIN;
		
		DataValue<Serializable> feedDataValue = new DataValue<>();
		feedDataValue.setValue(LockLevel.ENRICHED, "isin");
		
		DataValue<Serializable> dbDataValue = new DataValue<>();
		dbDataValue.setValue(LockLevel.ENRICHED, "isin");
		dbDataValue.setValue(LockLevel.FEED, "isin");
		enrichmentLockRemover.removeLockValue(secStatus, feedDataValue, dbDataValue, dataSource);
		
		assertNotNull(dbDataValue.getValue(LockLevel.FEED));
		assertNull(dbDataValue.getValue(LockLevel.ENRICHED));
		
	}
}
