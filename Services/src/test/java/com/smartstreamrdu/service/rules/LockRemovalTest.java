/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: LockRemovalTest.java
 * Author:Shreepad Padgaonkar
 * Date: Nov 20, 2020
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleFilter;
import com.smartstreamrdu.rules.RduRuleOutput;
import com.smartstreamrdu.util.SdDataAttributeConstant;

@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class LockRemovalTest extends AbstractEmbeddedMongodbJunitParent{

	private static final String DV_DOMAIN_MAP = DataLevel.DV_DOMAIN_MAP.getCollectionName();

	@Autowired
	private ReloadIgniteCache cacheReloader;
	
	List<RduRule> rules;
	RduRuleEngine ruleEngine = new RduRuleEngine();
	ChildRecordRuleExecutionStrategy executionStrategy = new ChildRecordRuleExecutionStrategy("test_file","test_pgm", LocalDateTime.now(),"idcApex");

	Record record;
	RduRule rule;
	RduRuleFilter ruleFilter;
	RduRuleData ruleData;
	RduRuleOutput ruleOutput;
	RecordWrapper wrapper;

	Record record1;
	RduRule rule1;
	RduRuleFilter ruleFilter1;
	RduRuleData ruleData1;
	RduRuleOutput ruleOutput1;
	
	RduRule rule2;
	RduRuleFilter ruleFilter2;
	RduRuleData ruleData2;
	RduRuleOutput ruleOutput2;

	RduRuleContext ruleContext;
	
	
	@Before
	public void setUp() throws Exception {
		ruleContext=new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "idcApex");
		record = new Record();
		record.setAttribute("$.master_information.market_master.market.listing_trading_status._VALUE",1,true);

		rule = new RduRule();

		ruleData = new RduRuleData();
		ruleData.setRuleScript("domainLookup(['ListingTradingStatusTypeMap','$.master_information.market_master.market.listing_trading_status._VALUE']);");
		ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName("securityStatus");
		ruleOutput.setLockLevel("feed");
		ruleFilter = (RduRuleFilter.builder()).feedName("idcApex").ruleType("feed").build();

		rule = new RduRule();
		rule.setRuleData(ruleData);
		rule.setRuleFilter(ruleFilter);
		rule.setRuleOutput(ruleOutput);


		rule1 = new RduRule();

		ruleData1 = new RduRuleData();
		ruleData1.setRuleScript("domainLookup(['ListingTradingStatusTypeMap','$.master_information.market_master.market.listing_trading_status._VALUE']);");
		ruleOutput1 = new RduRuleOutput();
		ruleOutput1.setAttributeName("securityStatus");
		ruleOutput1.setLockLevel("enriched");
		ruleFilter1 = (RduRuleFilter.builder()).feedName("idcApex").ruleType("dependent").build();

		rule1 = new RduRule();
		rule1.setRuleData(ruleData1);
		rule1.setRuleFilter(ruleFilter1);
		rule1.setRuleOutput(ruleOutput1);
		
		

		ruleData2 = new RduRuleData();
		ruleData2.setRuleScript("normalizedDomainValue('A')");
		ruleOutput2 = new RduRuleOutput();
		ruleOutput2.setAttributeName("securityStatus");
		ruleOutput2.setLockLevel("enriched");
		ruleFilter2 = (RduRuleFilter.builder()).feedName("idcApex").ruleType("dependent").build();

		rule2 = new RduRule();
		rule2.setRuleData(ruleData2);
		rule2.setRuleFilter(ruleFilter2);
		rule2.setRuleOutput(ruleOutput2);

		record1 = new Record();
		record1.setAttribute("$.master_information.market_master.market.listing_trading_status._VALUE",1,true);
		record1.getRecordRawData().setRawDataLevel("SEC");
		record1.getRecordRawData().setRawDataId("TestRawDataId2");

		wrapper = new RecordWrapper();
		wrapper.setParentRecord(record);
		wrapper.setChildRecords(Arrays.asList(record1));
		wrapper.setLevel(DataLevel.SEC);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteStrategy() {
		RduRuleEngine ruleEngine = new RduRuleEngine();
		List<RduRule> rules;
		rules = new ArrayList<RduRule>(Arrays.asList(rule,rule1));
		ruleEngine.initializeRuleEngine(rules, ruleContext);
		ArrayList<DataContainer> dataContainers = executionStrategy.executeStrategy(wrapper, ruleEngine,rules);
		Assert.assertNotNull(dataContainers);
		DataContainer parentDataContainer = dataContainers.get(0);
		List<DataContainer> childDataContainers = parentDataContainer.getChildDataContainers(DataLevel.SEC);
		DataContainer record1Container = childDataContainers.get(0);
		assertNotNull(record1Container.getAttributeValue(SdDataAttributeConstant.SEC_STATUS));
		DataValue<DomainType> attributeValue = (DataValue<DomainType>) record1Container.getAttributeValue(SdDataAttributeConstant.SEC_STATUS);
		
		DomainType enrichValue = attributeValue.getValue(LockLevel.ENRICHED);
		Assert.assertNull(enrichValue);

		DomainType feedValue = attributeValue.getValue(LockLevel.FEED);
		Assert.assertNotNull(feedValue);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteStrategy_1() {
		cacheReloader.reloadCache(DV_DOMAIN_MAP);
		RduRuleEngine ruleEngine = new RduRuleEngine();
		List<RduRule> rules;
		rules = new ArrayList<RduRule>(Arrays.asList(rule,rule2));
		ruleEngine.initializeRuleEngine(rules, ruleContext);
		ArrayList<DataContainer> dataContainers = executionStrategy.executeStrategy(wrapper, ruleEngine,rules);
		Assert.assertNotNull(dataContainers);
		DataContainer parentDataContainer = dataContainers.get(0);
		List<DataContainer> childDataContainers = parentDataContainer.getChildDataContainers(DataLevel.SEC);
		DataContainer record1Container = childDataContainers.get(0);
		assertNotNull(record1Container.getAttributeValue(SdDataAttributeConstant.SEC_STATUS));
		DataValue<DomainType> attributeValue = (DataValue<DomainType>) record1Container.getAttributeValue(SdDataAttributeConstant.SEC_STATUS);
		
		DomainType enrichValue = attributeValue.getValue(LockLevel.ENRICHED);
		Assert.assertNull(enrichValue);

		DomainType feedValue = attributeValue.getValue(LockLevel.FEED);
		Assert.assertNotNull(feedValue);
	}
}
