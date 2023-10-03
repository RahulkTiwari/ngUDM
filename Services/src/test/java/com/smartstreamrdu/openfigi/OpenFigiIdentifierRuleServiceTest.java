/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	OpenFigiRuleServiceTest.java
 * Author:	Shruti Arora
 * Date:	08-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.openfigi;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.openfigi.OpenFigiIdentifierRulesService;
import com.smartstreamrdu.util.Constant.OpenFigiContants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class OpenFigiIdentifierRuleServiceTest extends AbstractEmbeddedMongodbJunitParent {
	
	
	@Autowired 
	private OpenFigiIdentifierRulesService ruleService;
	
	@Test 
	public void testRuleService_sedolMic() {
		List<String> sedolMicRules= ruleService.getRuleAttributesForRuleEnum(OpenFigiRequestRuleEnum.SEDOL_MIC);
		
		Assert.assertNotNull(sedolMicRules);
		Assert.assertEquals(2,sedolMicRules.size());
		
		assertEquals(OpenFigiContants.SEDOL,sedolMicRules.get(0) );
		assertEquals(OpenFigiContants.EX_CODE,sedolMicRules.get(1) );
	}
	
	@Test 
	public void testRuleService_sedolMatchingMic() {
		List<String> sedolMicRules= ruleService.getRuleAttributesForRuleEnum(OpenFigiRequestRuleEnum.SEDOL_MATCHINGMIC);
		
		Assert.assertNotNull(sedolMicRules);
		Assert.assertEquals(2,sedolMicRules.size());
		
		assertEquals(OpenFigiContants.SEDOL,sedolMicRules.get(0) );
		assertEquals(OpenFigiContants.EX_CODE,sedolMicRules.get(1) );
	}
	
	@Test 
	public void testRuleService_isinCurrencyMic() {
		List<String> sedolMicRules= ruleService.getRuleAttributesForRuleEnum(OpenFigiRequestRuleEnum.ISIN_CURR_MIC);
		
		Assert.assertNotNull(sedolMicRules);
		Assert.assertEquals(3,sedolMicRules.size());
		
		assertEquals(OpenFigiContants.ISIN,sedolMicRules.get(0) );
		assertEquals(OpenFigiContants.EX_CODE,sedolMicRules.get(1) );
		assertEquals(OpenFigiContants.TRADE_CURR,sedolMicRules.get(2) );
	}
	
	@Test 
	public void testRuleService_isinCurrencyMatchingMic() {
		List<String> sedolMicRules= ruleService.getRuleAttributesForRuleEnum(OpenFigiRequestRuleEnum.ISIN_CURR_MATCHINGMIC);
		
		Assert.assertNotNull(sedolMicRules);
		Assert.assertEquals(3,sedolMicRules.size());
		assertEquals(OpenFigiContants.ISIN,sedolMicRules.get(0) );
		assertEquals(OpenFigiContants.EX_CODE,sedolMicRules.get(1) );
		assertEquals(OpenFigiContants.TRADE_CURR,sedolMicRules.get(2) );
	}
	
	@Test 
	public void testRuleService_isin() {
		List<String> sedolMicRules= ruleService.getRuleAttributesForRuleEnum(OpenFigiRequestRuleEnum.ISIN_ONLY);
		
		Assert.assertNotNull(sedolMicRules);
		Assert.assertEquals(1,sedolMicRules.size());
		assertEquals(OpenFigiContants.ISIN,sedolMicRules.get(0) );
	}
	

}
