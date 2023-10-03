/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: AttributeMergeHandlerFactoryImplTest.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;


/**
 * @author Dedhia
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class AttributeMergeHandlerFactoryImplTest extends AbstractEmbeddedMongodbJunitParent{
	
	AttributeMergeHandler handler;
	
	@Autowired
	AttributeMergeHandlerFactoryImpl factory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.merging.AttributeMergeHandlerFactoryImpl#getMergeHandler(com.smartstreamrdu.domain.DataAttribute)}.
	 */
	@Test
	public void testGetMergeHandler() {

		DataAttribute insLeLink = DataAttributeFactory.getAttributeByNameAndLevel("instrumentLegalEntityRelations",
				DataLevel.INS);
		
		DataAttribute nestedArrayDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
		handler = factory.getMergeHandler(nestedArrayDataAttribute);
		Assert.assertEquals(NestedArrayAttributeMergeHandler.class, handler.getClass());
		
		DataAttribute nestedDataAttribute = DataAttributeFactory.getAttributeByNameAndLevelAndParent("refData", DataLevel.INS, insLeLink);
		handler = factory.getMergeHandler(nestedDataAttribute);
		Assert.assertEquals(NestedAttributeMergeHandler.class, handler.getClass());
		
		DataAttribute simpleDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
		handler = factory.getMergeHandler(simpleDataAttribute);
		Assert.assertEquals(SimpleAttributeMergeHandler.class, handler.getClass());
		
	}

}
