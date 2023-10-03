/*******************************************************************
 *
 * Copyright (c) 2009-2016 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	XrfEligibilityEvaluatorTest.java
 * Author:	AThanage
 * Date:	Jun 22, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.events;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class XrfEligibilityEvaluatorTest extends AbstractEmbeddedMongodbJunitParent {

	
	private DataContainer dataContainer = null;

	private DomainType domainValue = null;
	
	@Autowired
	private XrfEligibilityEvaluator xrfEligibilityEvaluator;
	
	private static final DataAttribute dataSourceAttribute = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		domainValue = new DomainType();
		dataContainer = new DataContainer(DataLevel.INS, null);
		dataContainer.set_id("5d5ead25fd03c713883bdedd");
	}

	/*
	 * DataSource is eligible for xrf that means when xrfSourcePriority of dataSource is +ve.
	 * In this case message will be send from sd to xrf and from xrf to proforma.
	 */
	@Test
	public void testIsDataContainerEligibleForXrf_Scenario1() throws UdmBaseException {
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		domainValue.setVal("trdse");
		dataSourceValue.setValue(LockLevel.RDU, domainValue);
		dataContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);		
		Assert.assertTrue(xrfEligibilityEvaluator.isDataContainerEligibleForXrf(dataContainer));
	}
	
	/*
	 * DataSource is not eligible for xrf that means when xrfSourcePriority of dataSource is -ve. ( proforma only dataSource) 
	 * In this case message will be directly send from sd to proforma.
	 */
	@Test
	public void testIsDataContainerEligibleForXrf_Scenario2() throws UdmBaseException {
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		domainValue.setVal("asbIsin");
		dataSourceValue.setValue(LockLevel.RDU, domainValue);
		dataContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);		
		Assert.assertFalse(xrfEligibilityEvaluator.isDataContainerEligibleForXrf(dataContainer));
	}
	
	/*
	 * dataSource = null.
	 * For IVO containers dataSource value will be populated as null.
	 */
	@Test
	public void testIsDataContainerEligibleForXrfFor_Scenario3() throws UdmBaseException {
		Assert.assertFalse(xrfEligibilityEvaluator.isDataContainerEligibleForXrf(dataContainer));
	}
}