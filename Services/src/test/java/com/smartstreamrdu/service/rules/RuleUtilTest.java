/*******************************************************************
*
* Copyright (c) 2009-2021 The SmartStream Reference Data Utility
* All rights reserved. 
*
* File:    RuleUtilTest.java
* Author:  Padgaonkar
* Date:    March 03,2021
*
*******************************************************************
*/
package com.smartstreamrdu.service.rules;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleOutput;


public class RuleUtilTest {

	@Test
	public void testRule_WithPArentAttribute() {
		RduRule rduRule = getRduRule("referenceId","instrumentRelations",DataLevel.INS.name());
		DataAttribute ruleDataAttibute = RuleUtil.getRuleDataAttibute(rduRule, "referenceId");
		DataAttribute parent = DataAttributeFactory.getAttributeByNameAndLevel( "instrumentRelations", DataLevel.INS);
		DataAttribute attributeByNameAndLevelAndParent = DataAttributeFactory.getAttributeByNameAndLevelAndParent("referenceId", DataLevel.INS,parent);
		Assert.assertEquals(attributeByNameAndLevelAndParent,ruleDataAttibute);
		
		RduRule rduRule1 = getRduRule("referenceId","instrumentLegalEntityRelations",DataLevel.INS.name());
		DataAttribute ruleDataAttibute1 = RuleUtil.getRuleDataAttibute(rduRule1, "referenceId");
		DataAttribute parent1 = DataAttributeFactory.getAttributeByNameAndLevel( "instrumentLegalEntityRelations", DataLevel.INS);
		DataAttribute attributeByNameAndLevelAndParent1 = DataAttributeFactory.getAttributeByNameAndLevelAndParent("referenceId", DataLevel.INS,parent1);
		Assert.assertEquals(attributeByNameAndLevelAndParent1,ruleDataAttibute1);
		
		
		RduRule rduRule2 = getRduRule("legalEntitySourceUniqueId","instrumentLegalEntityRelations",DataLevel.LE.name());
		DataAttribute ruleDataAttibute2 = RuleUtil.getRuleDataAttibute(rduRule2, "legalEntitySourceUniqueId");
		DataAttribute attributeByNameAndLevelAndParent2 = DataAttributeFactory.getAttributeByNameAndLevel("legalEntitySourceUniqueId", DataLevel.LE);
		Assert.assertEquals(attributeByNameAndLevelAndParent2,ruleDataAttibute2);
	}
	
	@Test
	public void testRule_withoutParentAttribute() {
		RduRule rduRule = getRduRule("isin",null,DataLevel.INS.name());
		DataAttribute ruleDataAttibute =RuleUtil.getRuleDataAttibute(rduRule, "isin");
		Assert.assertEquals( InstrumentAttrConstant.ISIN,ruleDataAttibute);

	}
	
	@Test
	public void testRule_withParent_SameAsAttribute() {
		RduRule rduRule = getRduRule("isin","isin",DataLevel.INS.name());
		DataAttribute ruleDataAttibute =RuleUtil.getRuleDataAttibute(rduRule, "isin");
		Assert.assertEquals(InstrumentAttrConstant.ISIN,ruleDataAttibute);
	}

	@Test
	public void testRule_withnullLevel() {
		RduRule rduRule = getRduRule("isin",null,null);
		DataAttribute ruleDataAttibute =RuleUtil.getRuleDataAttibute(rduRule, "isin");
		Assert.assertEquals(InstrumentAttrConstant.ISIN,ruleDataAttibute);
	}

	private RduRule getRduRule(String attribute, String parentAttribute,String dataLevel) {
		RduRule rule = new RduRule();
		RduRuleOutput output = new RduRuleOutput();
		 output.setAttributeName(attribute);
		 output.setParentAttributeName(parentAttribute);
		 output.setDataLevel(dataLevel);
		 rule.setRuleOutput(output);
		 return rule;
	}
}
