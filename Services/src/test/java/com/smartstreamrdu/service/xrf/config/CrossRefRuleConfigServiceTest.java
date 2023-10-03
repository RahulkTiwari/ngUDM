/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: fileCrossRefRuleConfigServiceTest.java
 * Author: Shruti Arora
 * Date: 27-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.xrf.XrfRefRuleDef;
import com.smartstreamrdu.commons.xrf.XrfRuleAttributeDef;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.SpringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class CrossRefRuleConfigServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Inject
	CrossRefRuleConfigService ruleService;	
	
	@Test
	public void test_RuleConfigService() throws Exception {
		
		CrossRefRuleConfigService service= SpringUtil.getBean(CrossRefRuleConfigService.class);
		assertNotNull(service.getAllCrossRefAttributes());
		assertEquals(7, service.getAllCrossRefAttributes().size());
		assertNotNull(service.getAllAttributestoPersist());
		assertNotNull(service.getAllAttributestoPersist());
		
		List<XrfRefRuleDef> allRules = service.getAllRules();
		assertNotNull(allRules);
		assertNotNull(service.getAttributesBySdLevel(DataLevel.INS));
		assertNotNull(service.getAttributesBySdLevel(DataLevel.SEC));
	//	assertNotNull(service.getRuleConfig());
		
		assertNotNull(service.getRuleConfig("Rule_Sec_Sedol_EC"));
		assertNotNull(service.getRuleConfig("Rule_Sec_Isin_EC_TC"));
		assertNotNull(service.getRuleConfig("Rule_Ins_Isin"));
		
		assertTrue(service.getToBeNullAttributes("Rule_Sec_Sedol_EC").isEmpty());
		assertTrue(service.getToBeNullAttributes("Rule_Ins_Isin").isEmpty());
		assertFalse(service.getToBeNullAttributes("Rule_Sec_Isin_EC_TC").isEmpty());

		List<XrfRefRuleDef> allRules2 = service.getAllRules();
		assertEquals( allRules, allRules2);
		
		List<XrfRuleAttributeDef> mandatoryAndOptionalAttributes = service.getMandatoryAndOptionalCrossRefAttributes();
		
		assertNotNull(mandatoryAndOptionalAttributes);
		assertEquals(6, mandatoryAndOptionalAttributes.size());
	}

	
	@Test
	public void testGetRulesByLevelInstrument() {
		List<XrfRefRuleDef> rules = ruleService.getRules(DataLevel.XRF_INS);
		assertEquals(2, rules.size());
	}
	
	@Test
	public void testGetRulesByLevelSecurity() {
		List<XrfRefRuleDef> rules = ruleService.getRules(DataLevel.XRF_SEC);
		assertEquals(3, rules.size());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetRulesByLevelInvalid() {
		ruleService.getRules(DataLevel.INS);
	}
}
