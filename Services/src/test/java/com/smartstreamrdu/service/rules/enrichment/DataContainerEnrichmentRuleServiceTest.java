/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerEnrichmentRuleServiceTest.java
 * Author :SaJadhav
 * Date : 26-Apr-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.domain.autoconstants.EnDataAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.rules.DataContainerEnrichmentRule;
import com.smartstreamrdu.rules.DataContainerEnrichmentRuleData;
import com.smartstreamrdu.rules.DataContainerEnrichmentRuleOutput;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MongoConfig.class)
public class DataContainerEnrichmentRuleServiceTest {
	
	@Autowired
	private DataContainerEnrichmentRuleService ruleService;
	
	@Test
	public void test_applyRules_simple() {
		EnrichmentRuleServiceInput ruleInput=new EnrichmentRuleServiceInput();
		DataContainer dataContainer=new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataValue<String> value=new DataValue<>();
		value.setValue(LockLevel.RDU, "Event Subject");
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_SUBJECT, value);
		ruleInput.setDataContainer(dataContainer);
		
		DataContainerEnrichmentRule rduRule=new DataContainerEnrichmentRule();
		DataContainerEnrichmentRuleData ruleData=new DataContainerEnrichmentRuleData();
		ruleData.setRuleScript("getAttributeValueFromDataContainer('eventSubject').toUpperCase()+'_'+'RDU'.toUpperCase();");
		rduRule.setRuleData(ruleData);
		DataContainerEnrichmentRuleOutput ruleOutput=new DataContainerEnrichmentRuleOutput();
		ruleOutput.setDataAttribute(EnDataAttrConstant.EVENT_SUMMARY_TEXT);
		ruleOutput.setLockLevel(LockLevel.RDU);
		
		rduRule.setRuleOutput(ruleOutput);
		
		List<DataContainerEnrichmentRule> rules=new ArrayList<>();
		rules.add(rduRule);
		ruleService.applyRules(ruleInput, rules, null);
		
		assertEquals("EVENT SUBJECT_RDU", dataContainer.getAttributeValueAtLevel(LockLevel.RDU, EnDataAttrConstant.EVENT_SUMMARY_TEXT));
	}
	
	@Test
	public void test_applyRules_nestedArray() {
		DataContainerEnrichmentRule rduRule=new DataContainerEnrichmentRule();
		DataContainerEnrichmentRuleData ruleData=new DataContainerEnrichmentRuleData();
		ruleData.setRuleScript("getNestedArrayCounter();");
		rduRule.setRuleData(ruleData);
		DataContainerEnrichmentRuleOutput ruleOutput=new DataContainerEnrichmentRuleOutput();
		ruleOutput.setDataAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings"));
		ruleOutput.setLockLevel(LockLevel.RDU);
		
		rduRule.setRuleOutput(ruleOutput);
		EnrichmentRuleServiceInput ruleInput=new EnrichmentRuleServiceInput();
		DataContainer dataContainer=new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		
		DataValue<ArrayList<DataRow>> dataValue=new DataValue<>();
		DataRow dataRow=new DataRow(EnDataAttrConstant.UNDERLYINGS,dataValue);
		
		ArrayList<DataRow> listDataRow=new ArrayList<>();
		DataRow row1=new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue=new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "TestIsin1");
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue);
		listDataRow.add(row1);
		
		DataRow row2=new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue2=new DataValue<>();
		isinValue2.setValue(LockLevel.RDU, "TestIsin2");
		row2.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue2);
		listDataRow.add(row2);
		dataValue.setValue(LockLevel.RDU, listDataRow);
		
		dataContainer.addAttributeValue(EnDataAttrConstant.UNDERLYINGS, dataRow);
		ruleInput.setDataContainer(dataContainer);
		
		List<DataContainerEnrichmentRule> rules=new ArrayList<>();
		rules.add(rduRule);
		ruleService.applyRules(ruleInput, rules, null);
		assertEquals(Long.valueOf(1l), (Long)row1.getAttributeValueAtLevel(LockLevel.RDU, DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings")));
		assertEquals(Long.valueOf(2l), (Long)row2.getAttributeValueAtLevel(LockLevel.RDU, DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings")));
	}

}
