/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: RduLockRemoverTest.java
* Author : VRamani
* Date : Mar 5, 2019
* 
*/
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

import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.SdDataAttributeConstant;

/**
* @author VRamani
*
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RduLockRemoverTest {
	
	private static final String OPS_VALUE = "OPS_VALUE";
	private static final String FEED_VALUE = "FEED_VALUE";
	
	@Autowired
	RduLockRemover rduLockRemover;
	
	@Test
	public void testRduLockRemover() {
		DataValue<Serializable> isinDataValue = new DataValue<>();
		isinDataValue.setValue(LockLevel.FEED, FEED_VALUE);
		isinDataValue.setValue(LockLevel.RDU, FEED_VALUE);
		rduLockRemover.removeLockValue(SdDataAttributeConstant.INS_ISIN,null,isinDataValue,null);
		assertNull(isinDataValue.getValue(LockLevel.RDU));
	}
	
	@Test
	public void testRduLockRemover2() {
		DataValue<Serializable> isinDataValue = new DataValue<>();
		isinDataValue.setValue(LockLevel.FEED, FEED_VALUE);
		isinDataValue.setValue(LockLevel.RDU, OPS_VALUE);
		rduLockRemover.removeLockValue(SdDataAttributeConstant.INS_ISIN,null,isinDataValue,null);
		assertNotNull(isinDataValue.getValue(LockLevel.RDU));
		assertEquals(OPS_VALUE, isinDataValue.getValue(LockLevel.RDU));
	}
	
	@Test
	public void testRemoveLockValue_feedNormalizedValue() {
		DataValue<Serializable> additionalXrfDataValue = new DataValue<>();
		additionalXrfDataValue.setValue(LockLevel.FEED, new DomainType(null, null, "Default"));
		additionalXrfDataValue.setValue(LockLevel.RDU, new DomainType(null,null,"1 Day Repo"));
		
		DataValue<Serializable> additionalXrfFeedDataValue = new DataValue<>();
		additionalXrfFeedDataValue.setValue(LockLevel.FEED, new DomainType(null, null, "Default"));
		
		rduLockRemover.removeLockValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE,additionalXrfFeedDataValue,additionalXrfDataValue,new DomainType("trdse"));
		assertNotNull(additionalXrfDataValue.getValue(LockLevel.RDU));
		assertEquals(new DomainType(null,null,"1 Day Repo"), additionalXrfDataValue.getValue(LockLevel.RDU));
	}

}
