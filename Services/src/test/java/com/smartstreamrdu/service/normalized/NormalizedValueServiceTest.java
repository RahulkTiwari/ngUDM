/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    NormalizedValueServiceTest.java
 * Author:  Padgaonkar
 * Date:    Mar 29, 2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.normalized;

import java.io.Serializable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author Padgaonkar
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class NormalizedValueServiceTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private NormalizedValueService normService;

	@Test
	public void testgetNormalizedValue_RegexBasedAttribute() {
		DataAttribute additionalXrfParameterType = DataAttributeFactory
				.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC);

		DomainType domainObj = new DomainType("Ab.BCO", "BCO", null, "xrfAdditionalTypesMap");
		Serializable normalizedValueForDomainValue = normService
				.getNormalizedValueForDomainValue(additionalXrfParameterType, domainObj, "trdse");
		Assert.assertEquals("XBRU", normalizedValueForDomainValue);
		System.out.println(normalizedValueForDomainValue);
	}

	@Test
	public void testgetNormalizedValue_ExactMatchAttribute() {
		DataAttribute additionalXrfParameterType = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus",
				DataLevel.INS);

		DomainType domainObj = new DomainType("0", null, null, "tradingStatusMap");
		Serializable normalizedValueForDomainValue = normService
				.getNormalizedValueForDomainValue(additionalXrfParameterType, domainObj, "figi");
		Assert.assertEquals("I", normalizedValueForDomainValue);
		System.out.println(normalizedValueForDomainValue);
	}
	
	@Test
	public void testgetNormalizedValue_ExactMatchAttribute_WrongDataSource() {
		DataAttribute additionalXrfParameterType = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus",
				DataLevel.INS);

		DomainType domainObj = new DomainType("0", null, null, "tradingStatusMap");
		Serializable normalizedValueForDomainValue = normService
				.getNormalizedValueForDomainValue(additionalXrfParameterType, domainObj,null);
		Assert.assertNull(normalizedValueForDomainValue);
	}
	
	@Test
	public void testgetNormalizedValue_ExactMatchAttribute_RuleNotConfiguredForAttribute() {
		DataAttribute additionalXrfParameterType = DataAttributeFactory.getAttributeByNameAndLevel("fttFlag",
				DataLevel.INS);

		DomainType domainObj = new DomainType("0", null, null, "tradingStatusMap");
		Serializable normalizedValueForDomainValue = normService
				.getNormalizedValueForDomainValue(additionalXrfParameterType, domainObj, "figiaa");
		Assert.assertNull(normalizedValueForDomainValue);
	}
}
