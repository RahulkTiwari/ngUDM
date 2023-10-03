/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : VfsOpenFigiEligibleAttributesServiceTest.java
 * Author :SaJadhav
 * Date : 30-Nov-2021
 */
package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoInstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoSecurityAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MongoConfig.class)
public class VfsOpenFigiEligibleAttributesServiceTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private VfsOpenFigiEligibleAttributesService vfsOpenFigiEligibleAttributesService;

	@Test
	public void testGetXrfAttributesEligibleForVfsRequest() {
		List<DataAttribute> xrfAttributesEligibleForVfsRequest = vfsOpenFigiEligibleAttributesService
				.getXrfAttributesEligibleForVfsRequest();
		assertNotNull(xrfAttributesEligibleForVfsRequest);
		assertEquals(6, xrfAttributesEligibleForVfsRequest.size());
		assertTrue(xrfAttributesEligibleForVfsRequest.contains(InstrumentAttrConstant.ISIN));
		assertTrue(xrfAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.SEDOL));
		assertTrue(xrfAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.EXCHANGE_CODE));
		assertTrue(xrfAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.TRADE_CURRENCY_CODE));
		assertTrue(xrfAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE));

	}

	@Test
	public void testGetNonXrfAttributesEligibleForVfsRequest() {
		List<DataAttribute> xrfNonAttributesEligibleForVfsRequest = vfsOpenFigiEligibleAttributesService
				.getNonXrfAttributesEligibleForVfsRequest();
		assertNotNull(xrfNonAttributesEligibleForVfsRequest);
		assertEquals(6, xrfNonAttributesEligibleForVfsRequest.size());
		assertTrue(xrfNonAttributesEligibleForVfsRequest.contains(InstrumentAttrConstant.NAME_LONG));
		assertTrue(xrfNonAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.RIC));
		assertTrue(xrfNonAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.TICKER));
		assertTrue(xrfNonAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.EXCHANGE_TICKER));
		assertTrue(xrfNonAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.TRADING_SYMBOL));
		assertTrue(xrfNonAttributesEligibleForVfsRequest.contains(SecurityAttrConstant.ROUND_LOT_SIZE));
	}

	@Test
	public void testGetNonXrfIvoInsAttributesEligibleForVfsRequest() {
		List<DataAttribute> nonXrfIvoInsAttributesEligibleForVfsRequest = vfsOpenFigiEligibleAttributesService
				.getNonXrfIvoInsAttributesEligibleForVfsRequest();
		assertNotNull(nonXrfIvoInsAttributesEligibleForVfsRequest);
		assertEquals(1, nonXrfIvoInsAttributesEligibleForVfsRequest.size());
		assertTrue(nonXrfIvoInsAttributesEligibleForVfsRequest.contains(IvoInstrumentAttrConstant.NAME_LONG));
	}

	@Test
	public void testGetNonXrfIvoSecAttributesEligibleForVfsRequest() {
		List<DataAttribute> nonXrfIvoSecAttributesEligibleForVfsRequest = vfsOpenFigiEligibleAttributesService
				.getNonXrfIvoSecAttributesEligibleForVfsRequest();
		assertNotNull(nonXrfIvoSecAttributesEligibleForVfsRequest);
		assertEquals(5, nonXrfIvoSecAttributesEligibleForVfsRequest.size());
		assertTrue(nonXrfIvoSecAttributesEligibleForVfsRequest.contains(IvoSecurityAttrConstant.RIC));
		assertTrue(nonXrfIvoSecAttributesEligibleForVfsRequest.contains(IvoSecurityAttrConstant.TICKER));
		assertTrue(nonXrfIvoSecAttributesEligibleForVfsRequest.contains(IvoSecurityAttrConstant.EXCHANGE_TICKER));
		assertTrue(nonXrfIvoSecAttributesEligibleForVfsRequest.contains(IvoSecurityAttrConstant.TRADING_SYMBOL));
		assertTrue(nonXrfIvoSecAttributesEligibleForVfsRequest.contains(IvoSecurityAttrConstant.ROUND_LOT_SIZE));
	}
	
}
