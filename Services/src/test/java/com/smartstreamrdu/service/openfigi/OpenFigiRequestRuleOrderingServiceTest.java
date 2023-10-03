/**
* Copyright (c) 2009-2023 The SmartStream Reference Data Utility
* All rights reserved.
*
* File : OpenFigiRequestRuleOrderingServiceTest.java
* Author :AGupta
* Date : Feb 2, 2023
*/
package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestOrderTypeEnum;
import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;


/**
 * @author AGupta
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class OpenFigiRequestRuleOrderingServiceTest extends AbstractEmbeddedMongodbJunitParent{

	@Autowired 
	private OpenFigiRequestRuleOrderingService requestOrderService;
	
	@Test
	public void test_sedol_Based() {
		OpenFigiRequestOrderTypeEnum orderTypeEnum=requestOrderService.getRequestOrderTypeForMic("BATE");
		Assert.assertNotNull(orderTypeEnum);
		assertEquals(OpenFigiRequestOrderTypeEnum.SEDOL_BASED,orderTypeEnum);
	}
	
	@Test
	public void test_Inavid_MIC_Value() {
		OpenFigiRequestOrderTypeEnum orderTypeEnum=requestOrderService.getRequestOrderTypeForMic("TEST");
		Assert.assertNotNull(orderTypeEnum);
		assertEquals(OpenFigiRequestOrderTypeEnum.DEFAULT_ISIN_BASED,orderTypeEnum);
	}
	
	@Test
	public void test_getNextRule_ISIN_BASED() {
		Optional<OpenFigiRequestRuleEnum> nextRuleEnum=requestOrderService.getNextRequestRule(OpenFigiRequestRuleEnum.ISIN_CURR_MATCHINGMIC,OpenFigiRequestOrderTypeEnum.DEFAULT_ISIN_BASED);
		Assert.assertNotNull(nextRuleEnum);
		assertEquals(OpenFigiRequestRuleEnum.SEDOL_MIC,nextRuleEnum.get());
	}


	@Test
	public void test_getNextRule_SEDOL_BASED() {
		Optional<OpenFigiRequestRuleEnum> nextRuleEnum=requestOrderService.getNextRequestRule(OpenFigiRequestRuleEnum.SEDOL_MATCHINGMIC,OpenFigiRequestOrderTypeEnum.SEDOL_BASED);
		Assert.assertNotNull(nextRuleEnum);
		assertEquals(OpenFigiRequestRuleEnum.ISIN_CURR_MIC,nextRuleEnum.get());
	}

}
