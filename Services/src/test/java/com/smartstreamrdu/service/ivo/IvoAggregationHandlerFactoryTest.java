/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoAggregationHandlerFactoryTest.java
 * Author : SaJadhav
 * Date : 20-Mar-2019
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
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class IvoAggregationHandlerFactoryTest {
	
	@Autowired
	private IvoAggregationHandlerFactory factory;
	
	@Test
	public void test_getIvoAggregationHandler(){
		DataAttribute isinDataAttribute=InstrumentAttrConstant.ISIN;
		Assert.assertEquals(SimpleAttributeIvoMergeHandler.class, factory.getIvoAggregationHandler(isinDataAttribute).getClass());
		
		DataAttribute insToInsRelAttr=DataAttributeFactory.getIvoRelationAttributeForInsAndIns();
		Assert.assertEquals(NestedArrayAttributeIvoAggregationHandler.class, factory.getIvoAggregationHandler(insToInsRelAttr).getClass());
		
		DataAttribute refDataAttr=DataAttributeFactory.getRelationRefDataAttribute(insToInsRelAttr);
		Assert.assertEquals(NestedAttributeIvoAggregationHandler.class, factory.getIvoAggregationHandler(refDataAttr).getClass());
	}
}
