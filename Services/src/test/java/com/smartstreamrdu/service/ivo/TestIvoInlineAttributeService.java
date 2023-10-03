/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: TestIvoInlineAttributeService.java
 * Author : SaJadhav
 * Date : 03-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class TestIvoInlineAttributeService {
	
	@Autowired
	private IvoInlineAttributesService inlineAttributeService;
	
	@Test
	public void isInlineAttribute_true(){
		DataAttribute isin=DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
		Assert.assertTrue(inlineAttributeService.isInlineAttribute(isin));
		
		DataAttribute sedol=DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.SEC);
		Assert.assertTrue(inlineAttributeService.isInlineAttribute(sedol));
		
		DataAttribute exchangeCode=DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.SEC);
		Assert.assertTrue(inlineAttributeService.isInlineAttribute(exchangeCode));
		
		DataAttribute additionalXrfParameter=DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC);
		Assert.assertTrue(inlineAttributeService.isInlineAttribute(additionalXrfParameter));
		
		DataAttribute tradeCurrencyCode=DataAttributeFactory.getAttributeByNameAndLevel("tradeCurrencyCode", DataLevel.SEC);
		Assert.assertTrue(inlineAttributeService.isInlineAttribute(tradeCurrencyCode));
		
		DataAttribute instrumentStatus=DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
		Assert.assertTrue(inlineAttributeService.isInlineAttribute(instrumentStatus));
		
		DataAttribute securityStatus=DataAttributeFactory.getAttributeByNameAndLevel("securityStatus", DataLevel.SEC);
		Assert.assertTrue(inlineAttributeService.isInlineAttribute(securityStatus));
		
	}
	
	@Test
	public void isInlineAttribute_false(){
		DataAttribute ric=DataAttributeFactory.getAttributeByNameAndLevel("ric", DataLevel.SEC);
		Assert.assertFalse(inlineAttributeService.isInlineAttribute(ric));
		
		DataAttribute legalEntityName=DataAttributeFactory.getAttributeByNameAndLevel("legalEntityName", DataLevel.LE);
		Assert.assertFalse(inlineAttributeService.isInlineAttribute(legalEntityName));
	}
}
