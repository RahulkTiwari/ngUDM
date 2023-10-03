/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    RduSecurityTypePopulationServiceTest.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.postprocess;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.SdDataAttributeConstant;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RduSecurityTypePopulationServiceTest {

	@Autowired
	private RduSecurityTypePopulationService rduSecurityTypePopulationService;
	
	@Test
	public void testSecurityTypePopulation() throws UdmTechnicalException {
		DataContainer container = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;		
		boolean serviceApplicable = rduSecurityTypePopulationService.isServiceApplicable("idcApex", Arrays.asList(container));
        Assert.assertTrue(serviceApplicable);
		rduSecurityTypePopulationService.populateSecurityType("Regular", container);
		DomainType highestPriorityValue = container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		Assert.assertEquals("Regular",highestPriorityValue.getNormalizedValue());
	}
	
	@Test
	public void testSecurityTypePopulation_1() {
		DataContainer container = new DataContainer(DataLevel.INS, DataContainerContext.builder().build()) ;		
		rduSecurityTypePopulationService.populateSecurityType("Regular", container);
		DomainType highestPriorityValue = container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		Assert.assertNull(highestPriorityValue);
	}
	
	@Test
	public void testSecurityTypePopulation_2() {
		DataContainer container = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;		
		rduSecurityTypePopulationService.populateSecurityType("Regular", container);
		DomainType highestPriorityValue = container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		Assert.assertEquals("Regular",highestPriorityValue.getNormalizedValue());
		
		container.setHasChanged(false);
		rduSecurityTypePopulationService.populateSecurityType("Regular", container);
		Assert.assertEquals(false, container.hasContainerChanged());
	}
	
	@Test
	public void testSecurityTypePopulation_3() {
		DataContainer container = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;		
		rduSecurityTypePopulationService.populateSecurityType("Regular", container);
		DomainType highestPriorityValue = container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		Assert.assertEquals("Regular",highestPriorityValue.getNormalizedValue());
		
		rduSecurityTypePopulationService.populateSecurityType("Technical", container);
		Assert.assertEquals("Technical",highestPriorityValue.getNormalizedValue());
	}
}
