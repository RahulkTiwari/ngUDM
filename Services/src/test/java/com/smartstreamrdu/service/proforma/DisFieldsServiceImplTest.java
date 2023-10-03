/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DisFieldsServiceImplTest.java
 * Author : VRamani
 * Date : Feb 25, 2020
 * 
 */
package com.smartstreamrdu.service.proforma;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DisDataType;
import com.smartstreamrdu.domain.proforma.ProformaDistributionServiceEnum;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.Field;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.rules.DisField;

/**
 * @author VRamani
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DisFieldsServiceImplTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private DisFieldsService disFieldsService;


	@Test
	public void testGetDisFieldByDisFieldName() {
		DisField disField = disFieldsService.getDisFieldByDisFieldNameAndParent("isin", null,ProformaDistributionServiceEnum.EQUITY);
		DisField disFieldUnderlyingSecurities = disFieldsService.getDisFieldByDisFieldNameAndParent("underlyingSecurities",
				"underlyings",ProformaDistributionServiceEnum.EQUITY);
		assertNotNull(disField);
		assertEquals("isin", disField.getRuleOutput().getDisField());
		assertNotNull(disFieldUnderlyingSecurities);
		assertEquals("underlyingSecurities", disFieldUnderlyingSecurities.getRuleOutput().getDisField());

	}

	@Test
	public void testGetAllDisFieldsForDistributionService() {
		List<DisField> disFields = disFieldsService
				.getAllDisFieldsForDistributionService(ProformaDistributionServiceEnum.EQUITY);
		assertNotNull(disFields);
	}

	@Test
	public void testGetDisFieldForProfileField() {
		Field profileField = new Field();
		profileField.setDisFieldName("isin");
		DisField disField = disFieldsService.getDisFieldForProfileField(profileField,
				ProformaDistributionServiceEnum.EQUITY);
		assertNotNull(disField);
		assertEquals("isin", disField.getRuleOutput().getDisField());
	}
	
	@Test
	public void testGetDataTypeByAttributeName() {
		DisDataType disDataType = disFieldsService.getDataTypeByAttributeName("isin", ProformaDistributionServiceEnum.EQUITY);
		DisDataType issuersDataDataType = disFieldsService.getDataTypeByAttributeName("issuers", ProformaDistributionServiceEnum.EQUITY);
		assertEquals(DisDataType.SIMPLE, disDataType);
		assertEquals(DisDataType.NESTED_ARRAY, issuersDataDataType);
	}
}
