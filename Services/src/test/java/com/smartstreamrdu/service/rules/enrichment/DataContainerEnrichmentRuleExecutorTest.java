/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerEnrichmentRuleExecutorTest.java
 * Author :SaJadhav
 * Date : 31-Mar-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.domain.autoconstants.EnDataAttrConstant;
import com.smartstreamrdu.rules.DataContainerEnrichmentRule;
import com.smartstreamrdu.rules.DataContainerEnrichmentRuleData;
import com.smartstreamrdu.rules.DataContainerEnrichmentRuleOutput;

/**
 * @author SaJadhav
 *
 */
public class DataContainerEnrichmentRuleExecutorTest {
	
	@Test
	public void test_getAttributeValueFromDataContainer() {
		DataContainerEnrichmentRuleExecutor ruleExecutor=new DataContainerEnrichmentRuleExecutor();
		
		DataContainerEnrichmentRule rduRule=new DataContainerEnrichmentRule();
		DataContainerEnrichmentRuleData ruleData=new DataContainerEnrichmentRuleData();
		ruleData.setRuleScript("getAttributeValueFromDataContainer('eventSubject').toUpperCase()+'_'+'RDU'.toUpperCase();");
		rduRule.setRuleData(ruleData);
		DataContainerEnrichmentRuleOutput ruleOutput=new DataContainerEnrichmentRuleOutput();
		ruleOutput.setDataAttribute(EnDataAttrConstant.CONTRACT_SIZE);
		
		rduRule.setRuleOutput(ruleOutput);
		EnrichmentRuleServiceInput ruleInput=new EnrichmentRuleServiceInput();
		DataContainer dataContainer=new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataValue<String> value=new DataValue<>();
		value.setValue(LockLevel.RDU, "Event Subject");
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_SUBJECT, value);
		ruleInput.setDataContainer(dataContainer);
		
		ruleExecutor.initializeRuleExecutor(List.of(rduRule), null);
		
		Serializable executeRule = ruleExecutor.executeRule(rduRule, ruleInput);
		assertEquals("EVENT SUBJECT_RDU", executeRule);
	}
	
	@Test
	public void test_getNestedArrayCounter() {

		DataContainerEnrichmentRuleExecutor ruleExecutor=new DataContainerEnrichmentRuleExecutor();
		
		DataContainerEnrichmentRule rduRule=new DataContainerEnrichmentRule();
		DataContainerEnrichmentRuleData ruleData=new DataContainerEnrichmentRuleData();
		ruleData.setRuleScript("getNestedArrayCounter();");
		rduRule.setRuleData(ruleData);
		DataContainerEnrichmentRuleOutput ruleOutput=new DataContainerEnrichmentRuleOutput();
		ruleOutput.setDataAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings"));
		
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
		
		ruleExecutor.initializeRuleExecutor(List.of(rduRule), null);
		
		Serializable executeRule = ruleExecutor.executeRule(rduRule, ruleInput);
		assertEquals(1, executeRule);
		
		DataValue<Long> counterDataVal=new DataValue<>();
		counterDataVal.setValue(LockLevel.RDU, 1l);
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings"), counterDataVal);
		assertEquals(2, ruleExecutor.executeRule(rduRule, ruleInput));
	}
	
	@Test
	public void test_enrichEventFileNames() {

		DataContainerEnrichmentRuleExecutor ruleExecutor = new DataContainerEnrichmentRuleExecutor();

		DataContainerEnrichmentRule rduRule = new DataContainerEnrichmentRule();
		DataContainerEnrichmentRuleData ruleData = new DataContainerEnrichmentRuleData();
		ruleData.setRuleScript(
				"enrichEventFileNames(formatDate(getAttributeValueFromDataContainer('eventInsertDate'),'yyyy-MM-dd')+'_'+getNormalizedAttributeValueFromDataContainer('exchangeSourceName')\n+'_'+getNormalizedAttributeValueFromDataContainer('eventType').replace(/\\//g, \"_\").toUpperCase()+'_', '_'+'RDU');");
		rduRule.setRuleData(ruleData);
		DataContainerEnrichmentRuleOutput ruleOutput = new DataContainerEnrichmentRuleOutput();
		ruleOutput.setDataAttribute(EnDataAttrConstant.EVENTS);

		rduRule.setRuleOutput(ruleOutput);
		EnrichmentRuleServiceInput ruleInput = new EnrichmentRuleServiceInput();
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());

		DataValue<ArrayList<String>> eventsValue = new DataValue<>();
		ArrayList<String> list = new ArrayList<>();
		list.add("sameer.com.pdf");
		list.add("eventypes_enz.jpeg");
		eventsValue.setValue(LockLevel.RDU, list);
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENTS, eventsValue);

		DataValue<LocalDate> insertDateval = new DataValue<>();
		insertDateval.setValue(LockLevel.RDU, LocalDate.of(2022, 3, 10));
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_INSERT_DATE, insertDateval);

		DataValue<DomainType> exchangeSourceVal = new DataValue<>();
		exchangeSourceVal.setValue(LockLevel.RDU, new DomainType(null, null, "TestSource"));
		dataContainer.addAttributeValue(EnDataAttrConstant.EXCHANGE_SOURCE_NAME, exchangeSourceVal);

		DataValue<DomainType> eventTypeVal = new DataValue<>();
		eventTypeVal.setValue(LockLevel.RDU, new DomainType(null, null, "Acceleration Of Maturity/Expiration"));
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_TYPE, eventTypeVal);

		ruleInput.setDataContainer(dataContainer);

		ruleExecutor.initializeRuleExecutor(List.of(rduRule), null);

		ArrayList<String> executeRule = (ArrayList<String>) ruleExecutor.executeRule(rduRule, ruleInput);
		assertEquals("2022-03-10_TestSource_ACCELERATION OF MATURITY_EXPIRATION_sameer.com_RDU.pdf",
				executeRule.get(0));
		assertEquals("2022-03-10_TestSource_ACCELERATION OF MATURITY_EXPIRATION_eventypes_enz_RDU.jpeg",
				executeRule.get(1));
	}
	
	@Test
	public void test_getCurrentDateInUTC() {
		DataContainerEnrichmentRuleExecutor ruleExecutor=new DataContainerEnrichmentRuleExecutor();
		
		DataContainerEnrichmentRule rduRule=new DataContainerEnrichmentRule();
		DataContainerEnrichmentRuleData ruleData=new DataContainerEnrichmentRuleData();
		ruleData.setRuleScript("getCurrentDateInUTC();");
		rduRule.setRuleData(ruleData);
		DataContainerEnrichmentRuleOutput ruleOutput=new DataContainerEnrichmentRuleOutput();
		ruleOutput.setDataAttribute(EnDataAttrConstant.EVENT_INSERT_DATE);
		
		rduRule.setRuleOutput(ruleOutput);
		EnrichmentRuleServiceInput ruleInput=new EnrichmentRuleServiceInput();
		DataContainer dataContainer=new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		ruleInput.setDataContainer(dataContainer);
		
		ruleExecutor.initializeRuleExecutor(List.of(rduRule), null);
		
		LocalDate ruleResult = (LocalDate) ruleExecutor.executeRule(rduRule, ruleInput);
		LocalDate expectedDate= LocalDate.now(ZoneId.of("Z"));
		assertEquals(expectedDate.getYear(), ruleResult.getYear());
		assertEquals(expectedDate.getMonth(), ruleResult.getMonth());
		assertEquals(expectedDate.getDayOfMonth(), ruleResult.getDayOfMonth());
		
	}
	
	@Test
	public void test_domainValue() {
		DataContainerEnrichmentRuleExecutor ruleExecutor=new DataContainerEnrichmentRuleExecutor();
		
		DataContainerEnrichmentRule rduRule=new DataContainerEnrichmentRule();
		DataContainerEnrichmentRuleData ruleData=new DataContainerEnrichmentRuleData();
		ruleData.setRuleScript("domainValue('rduEns');");
		rduRule.setRuleData(ruleData);
		DataContainerEnrichmentRuleOutput ruleOutput=new DataContainerEnrichmentRuleOutput();
		ruleOutput.setDataAttribute(EnDataAttrConstant.DATA_SOURCE);
		
		rduRule.setRuleOutput(ruleOutput);
		EnrichmentRuleServiceInput ruleInput=new EnrichmentRuleServiceInput();
		DataContainer dataContainer=new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		ruleInput.setDataContainer(dataContainer);
		
		ruleExecutor.initializeRuleExecutor(List.of(rduRule), null);
		
		DomainType ruleResult = (DomainType) ruleExecutor.executeRule(rduRule, ruleInput);
		
		assertEquals(new DomainType("rduEns"), ruleResult);
		
	}
	
}
