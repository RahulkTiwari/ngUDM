/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StaticDataServiceImplTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.staticdata;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author Jay Sangoi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class StaticDataServiceImplTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private StaticDataService service;

	@Autowired
	private ReloadIgniteCache cacheReloader;
	
	@Test
	public void test_getDataByCode() throws Exception {
		
		cacheReloader.reloadCache("CountryCodes");
		
		String input = "AN";

		DataLevel level = DataLevel.COUNTRY_CODES;

		String countryCodeSource = "ISO 3166-1 alpha-2";
		
		DataAttribute att = DataAttributeFactory.getAttributeByNameAndLevel("countryCodeSource", DataLevel.COUNTRY_CODES);
		
		Serializable val2 = service.getDataByCode(level, att, input);
		
		Assert.assertNotNull(val2);
		
		Assert.assertEquals(countryCodeSource, val2);
		
		Serializable val3 = service.getDataByCode(level, att.getAttributeName(), input);
		
		Assert.assertNotNull(val3);
		
		Assert.assertEquals(countryCodeSource, val3);
		
	}

}
