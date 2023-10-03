package com.smartstreamrdu.service.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.domain.UdmErrorCodes;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleFilter;
import com.smartstreamrdu.rules.RduRuleOutput;
import com.smartstreamrdu.util.DataAttributeConstant;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class ParentRecordRuleExecutionStrategyTest {
	
	private static final DataAttribute INS_RAW_DATA_ID = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeConstant.INS_RAW_DATA_ID, DataLevel.INS);
	private static final DataAttribute LE_RAW_DATA_ID = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeConstant.LE_RAW_DATA_ID, DataLevel.LE);
	List<RduRule> rules;
	RduRuleEngine ruleEngine = new RduRuleEngine();
	ParentRecordRuleExecutionStrategy executionStrategy = new ParentRecordRuleExecutionStrategy("test_file","test_pgm", LocalDateTime.now(),"trdse");
	
	Record record;
	RduRule rule;
	RduRuleFilter ruleFilter;
	RduRuleData ruleData;
	RduRuleOutput ruleOutput;
	RecordWrapper wrapper;
	
	RduRule rule1;
	RduRuleFilter ruleFilter1;
	RduRuleData ruleData1;
	RduRuleOutput ruleOutput1;
	RecordWrapper wrapper1;
	
	RduRule rule2;
	RduRuleFilter ruleFilter2;
	RduRuleData ruleData2;
	RduRuleOutput ruleOutput2;
	
	RduRule rule3;
	RduRuleFilter ruleFilter3;
	RduRuleData ruleData3;
	RduRuleOutput ruleOutput3;
	
	RduRule rule4;
	RduRuleFilter ruleFilter4;
	RduRuleData ruleData4;
	RduRuleOutput ruleOutput4;
	
	RduRuleContext ruleContext;

	@Before
	public void setUp() throws Exception {
		ruleContext=new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "trdse");
		record = new Record();
		record.setAttribute("isin", "TCSLMESS");
		record.setAttribute("Segment", "PIT");
		record.setAttribute("securityNameLong", "securityNameLong");
		record.getRecordRawData().setRawDataLevel(DataLevel.INS.name());
		record.getRecordRawData().setRawDataId("testInsRawDataId");
		
		ruleData = new RduRuleData();
		ruleData.setRuleScript("feedValue(['isin'])");
		ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName("isin");
		/*ruleOutput.setParentAttributeName("instrumentRelations");
		ruleOutput.setRelationshipType("Underlying");*/
		ruleOutput.setLockLevel("feed");
		ruleFilter = (RduRuleFilter.builder()).feedName("Reuters").ruleType("feed").build();
		
		rule = new RduRule();
		rule.setRuleData(ruleData);
		rule.setRuleFilter(ruleFilter);
		rule.setRuleOutput(ruleOutput);
		
		ruleData1 = new RduRuleData();
		ruleData1.setRuleScript("feedValue(['securityNameLong'])");
		ruleOutput1 = new RduRuleOutput();
		ruleOutput1.setAttributeName("securityNameLong");
		ruleOutput1.setParentAttributeName("instrumentRelations");
		ruleOutput1.setRelationshipType("Underlying");
		ruleOutput1.setLockLevel("feed");
		ruleFilter1 = (RduRuleFilter.builder()).feedName("Reuters").ruleType("feed").build();

		rule1 = new RduRule();
		rule1.setRuleData(ruleData1);
		rule1.setRuleFilter(ruleFilter1);
		rule1.setRuleOutput(ruleOutput1);
		
		wrapper = new RecordWrapper();
		wrapper.setParentRecord(record);
		wrapper.setLevel(DataLevel.INS);
		
		rule2 = new RduRule();
		ruleData2 = new RduRuleData();
		ruleData2.setRuleScript("normalizedValue(['isin'])");
		ruleFilter2  = (RduRuleFilter.builder()).feedName("Reuters").ruleType("dependent").build();
		ruleOutput2 = new RduRuleOutput();
		ruleOutput2.setAttributeName("cusip");
		ruleOutput2.setLockLevel("feed");
		rule2.setRuleData(ruleData2);
		rule2.setRuleFilter(ruleFilter2);
		rule2.setRuleOutput(ruleOutput2);
		
		rule3 = new RduRule();
		ruleData3 = new RduRuleData();
		ruleData3.setRuleScript("normalizedValue(['isin'])");
		ruleFilter3  = (RduRuleFilter.builder()).feedName("Reuters").ruleType("bootstrap").build();
		ruleOutput3 = new RduRuleOutput();
		ruleOutput3.setAttributeName("cusip");
		ruleOutput3.setLockLevel("feed");
		rule3.setRuleData(ruleData3);
		rule3.setRuleFilter(ruleFilter3);
		rule3.setRuleOutput(ruleOutput3);
		
		rule4 = getNestedArrayRuleWithoutRelationType();
		
		rules = Arrays.asList(rule, rule1, rule2, rule3, rule4);
		
		ruleEngine.initializeRuleEngine(rules, ruleContext);
		
		executionStrategy.getBoorstrapRulesContext().put("criteriaAttribute", "criteriaValue");
	}

	private RduRule getNestedArrayRuleWithoutRelationType() {
		RduRule rule = new RduRule();
		
		ruleFilter4 = (RduRuleFilter.builder()).feedName("Reuters").ruleType("feed").build();
		
		ruleData4 = new RduRuleData();
		ruleData4.setRuleScript("feedValue(['sampleFeild'])");
		
		ruleOutput4 = new RduRuleOutput();
		ruleOutput4.setAttributeName("insSnpRatingSymbol");
		ruleOutput4.setLockLevel("feed");
		ruleOutput4.setParentAttributeName("instrumentSnpRatings");
		
		rule.setRuleData(ruleData4);
		rule.setRuleFilter(ruleFilter4);
		rule.setRuleOutput(ruleOutput4);
		return rule;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteStrategy() {
		record.setLevel(DataLevel.INS.name());
		List<DataContainer> dataContainers = executionStrategy.executeStrategy(wrapper, ruleEngine, rules);
		assertNotNull(dataContainers);
		DataContainer dataContainer = dataContainers.get(0);
		assertNotNull(dataContainer.getAttributeValue(INS_RAW_DATA_ID));
		assertEquals("testInsRawDataId",
				((DataValue<String>) dataContainer.getAttributeValue(INS_RAW_DATA_ID)).getValue());
	}
	
	@Test
	public void testExecutionStrategy2() {
		record.getRecordRawData().setRawDataLevel(DataLevel.LE.name());
		record.setLevel(DataLevel.LE.name());
		List<DataContainer> dataContainers = executionStrategy.executeStrategy(wrapper, ruleEngine, rules);
		assertNull(dataContainers.get(0).getAttributeValue(INS_RAW_DATA_ID));
		
		record.getRecordRawData().setRawDataId("123");
		dataContainers = executionStrategy.executeStrategy(wrapper, ruleEngine, rules);
		assertNull(dataContainers.get(0).getAttributeValue(INS_RAW_DATA_ID));
		List<DataContainer> leCOntainer = dataContainers.stream().filter(dc -> dc.getLevel().equals(DataLevel.LE)).collect(Collectors.toList());
		assertNotNull(leCOntainer.get(0).getAttributeValue(LE_RAW_DATA_ID));
		// Not checking Raw Data ID for Security level since child containers will not be created.
	}

	@Test
	public void testAddDataAttributeAndDataValueToContainer() {
		Serializable securityNameLong = "someNameLong";
		rule1.getRuleOutput().setParentAttributeName(null);
		rule1.getRuleOutput().setRelationshipType(null);
		executionStrategy.addDataAttributeAndDataValueToContainer(DataLevel.SEC, rule1, securityNameLong);
		Serializable fromContainer = executionStrategy.getDataContainerForLevel(DataLevel.SEC).getAttributeValue(SecurityAttrConstant.SECURITY_NAME_LONG);
		Assert.assertNotNull(fromContainer);
		Assert.assertEquals("someNameLong", ((DataValue<?>)fromContainer).getValue(LockLevel.FEED));
	}
	
	@Test
	public void testAddDataAttributeAndDataValueToContainer_errorCode_CANC() {
		Serializable errorCodeCanc = UdmErrorCodes.CANC;
		rule1.getRuleOutput().setParentAttributeName(null);
		rule1.getRuleOutput().setRelationshipType(null);
		executionStrategy.addDataAttributeAndDataValueToContainer(DataLevel.SEC, rule1, errorCodeCanc);
		DataContainer fromContainer = executionStrategy.getDataContainerForLevel(DataLevel.SEC);
		Assert.assertNotNull(fromContainer);
		Optional<UdmErrorCodes> attributeErrorCodeAtLevel = fromContainer.getAttributeErrorCodeAtLevel(LockLevel.FEED,
				SecurityAttrConstant.SECURITY_NAME_LONG);

		Assert.assertEquals(errorCodeCanc, attributeErrorCodeAtLevel.get());
	}
	
	@Test
	public void testAddDataAttributeAndDataValueToContainer_errorCode_NULL_VALUE() {
		Serializable value = null;
		rule1.getRuleOutput().setParentAttributeName(null);
		rule1.getRuleOutput().setRelationshipType(null);
		executionStrategy.addDataAttributeAndDataValueToContainer(DataLevel.SEC, rule1, value);
		DataContainer fromContainer = executionStrategy.getDataContainerForLevel(DataLevel.SEC);
		Assert.assertNotNull(fromContainer);
		Optional<UdmErrorCodes> attributeErrorCodeAtLevel = fromContainer.getAttributeErrorCodeAtLevel(LockLevel.FEED,
				SecurityAttrConstant.SECURITY_NAME_LONG);

		Assert.assertEquals(UdmErrorCodes.NULL_VALUE, attributeErrorCodeAtLevel.get());
	}
	
	/**
	 * Test method for {@link com.smartstreamrdu.service.rules.AbstractRuleExecutionStrategy#isRuleApplicableAsPerBootstrapContext(com.smartstreamrdu.service.rules.RduRule)}.
	 */
	@Test
	public void testIsRuleApplicableAsPerBootstrapContext_Positive() {

		RduRule rule = new RduRule();

		RduRuleData ruleData = new RduRuleData();
		ArrayList<Serializable> valueList = new ArrayList<>();
		valueList.add("criteriaValue");
		Map<String, List<Serializable>> preConditionMap = new HashMap<>();
		preConditionMap.put("criteriaAttribute", valueList);
		ruleData.setPreConditions(preConditionMap);
		
		rule.setRuleData(ruleData);

		boolean isApplicable = executionStrategy.isRuleApplicableAsPerBootstrapContext(rule);
		
		Assert.assertEquals(true, isApplicable);
	}
	
	/**
	 * Test method for {@link com.smartstreamrdu.service.rules.AbstractRuleExecutionStrategy#isRuleApplicableAsPerBootstrapContext(com.smartstreamrdu.service.rules.RduRule)}.
	 */
	@Test
	public void testIsRuleApplicableAsPerBootstrapContext_Negative() {

		RduRule rule = new RduRule();

		RduRuleData ruleData = new RduRuleData();
		ArrayList<Serializable> valueList = new ArrayList<>();
		valueList.add("criteriaValue1");
		Map<String, List<Serializable>> preConditionMap = new HashMap<>();
		preConditionMap.put("criteriaAttribute", valueList);
		ruleData.setPreConditions(preConditionMap);
		rule.setRuleData(ruleData);

		boolean isApplicable = executionStrategy.isRuleApplicableAsPerBootstrapContext(rule);
		
		Assert.assertEquals(false, isApplicable);
	}
	
	@Test
	public void testExecuteDependentRules() {
		executionStrategy.executeDependentRules(record, ruleEngine, rules, DataLevel.INS);
		DataContainer container = executionStrategy.getContainers().get(DataLevel.INS);
		
		System.out.println(container.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS)));
		System.out.println(container.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.INS)));
		
		
	}
	
	@Test
	public void test_IterativeRule() {
		
		record.setAttribute("$.instrumentRatings[0].DurationText", "durationText", true);
		
		RduRule bootstrapRule = getBootstrapRule();
		RduRule iterativeRule = getIterativeRule();
		
		List<RduRule> ruleList = new ArrayList<>();
		ruleList.add(bootstrapRule);
		ruleList.add(iterativeRule);
		
		RecordWrapper recordWrapper = new RecordWrapper();
		recordWrapper.setParentRecord(record);
		recordWrapper.setLevel(DataLevel.INS);
		
		executionStrategy.executeStrategy(recordWrapper, ruleEngine, ruleList);
		
		DataContainer container = executionStrategy.getContainers().get(DataLevel.INS);
		DataAttribute parentAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentMoodysRatings", DataLevel.INS);
		DataAttribute attribute = DataAttributeFactory.getAttributeByNameAndLevelAndParent("insMoodysRatingDurationText", DataLevel.INS, parentAttribute);
		
		DataRowIterator dataRowIterator = new DataRowIterator(container, parentAttribute);
		
		while (dataRowIterator.hasNext()) {
			DataRow dataRow = dataRowIterator.next();
			Assert.assertEquals("durationText", dataRow.getAttributeValue(attribute).getValue());
		}
	}

	
	@Test
	public void testRedemptionRules() {
		record.setAttribute("$.callRedemptions[0].callId", "callId1", true);
		record.setAttribute("$.callRedemptions[0].callSchedules[0].callScheduleId", "scheduleId1", true);
		record.setAttribute("$.callRedemptions[0].callSchedules[1].callScheduleId", "scheduleId1_1", true);
		record.setAttribute("$.callRedemptions[1].callId", "callId2", true);
		record.setAttribute("$.callRedemptions[1].callSchedules[0].callScheduleId", "scheduleId2", true);
		record.setAttribute("$.callRedemptions[1].callSchedules[1].callScheduleId", "scheduleId2_2", true);

		RduRule bootstrapRule = getRedemptionRule();
		RduRule iterativeRule = getRedemptionRule_1();
		RduRule rule3 = getCallScheduleIdRule();
		RduRule rule4 = getScheduleBaseRule();

		List<RduRule> ruleList = new ArrayList<>();
		ruleList.add(bootstrapRule);
		ruleList.add(iterativeRule);
		ruleList.add(rule3);
		ruleList.add(rule4);

		RecordWrapper recordWrapper = new RecordWrapper();
		recordWrapper.setParentRecord(record);
		recordWrapper.setLevel(DataLevel.INS);

		executionStrategy.executeStrategy(recordWrapper, ruleEngine, ruleList);
		DataContainer container = executionStrategy.getContainers().get(DataLevel.INS);
		
		DataAttribute parentAttribute = DataAttributeFactory.getAttributeByNameAndLevel("callRedemptions", DataLevel.INS);
		DataRowIterator dataRowIterator = new DataRowIterator(container, parentAttribute);
		
		Assert.assertEquals(2, dataRowIterator.stream().count());
		
		while (dataRowIterator.hasNext()) {
			DataRow redemption = dataRowIterator.next();
			DataRowIterator rowIterator = new DataRowIterator(redemption, parentAttribute);
			Assert.assertEquals(2, rowIterator.stream().count());
		}
	}
	
	
	private RduRule getIterativeRule() {
		RduRule iterativeRule = new RduRule();
		RduRuleFilter iterativeRuleFilter = RduRuleFilter.builder().ruleType("feed").build();
		RduRuleData iterativeRuleData = new RduRuleData();
		RduRuleOutput iterativeRuleOutput = new RduRuleOutput();
		iterativeRule.setRuleFilter(iterativeRuleFilter);
		iterativeRule.setRuleData(iterativeRuleData);
		iterativeRule.setRuleOutput(iterativeRuleOutput);
		iterativeRuleOutput.setParentAttributeName("instrumentMoodysRatings");
		iterativeRuleOutput.setAttributeName("insMoodysRatingDurationText");
		iterativeRuleOutput.setLockLevel(LockLevel.FEED.getLockLevelName());
		iterativeRuleData.setRuleScript("feedValue(['$.RDU_instrumentMoodysRatings.DurationText'])");
		
		return iterativeRule;
	}

	private RduRule getBootstrapRule() {
		RduRule bootstrapRule = new RduRule();
		RduRuleFilter bootStrapRuleFilter = RduRuleFilter.builder().ruleType("feed").build();
		RduRuleData bootstrapRuleData = new RduRuleData();
		RduRuleOutput bootstrapRuleOutput = new RduRuleOutput();
		bootstrapRule.setRuleFilter(bootStrapRuleFilter);
		bootstrapRule.setRuleData(bootstrapRuleData);
		bootstrapRule.setRuleOutput(bootstrapRuleOutput);
		bootstrapRuleData.setRuleScript("$.instrumentRatings");
		bootstrapRuleOutput.setParentAttributeName("instrumentMoodysRatings");
		bootstrapRuleOutput.setVariableName("RDU_instrumentMoodysRatings");
		return bootstrapRule;
	}
	
	private RduRule getRedemptionRule() {
		RduRule iterativeRule = new RduRule();
		RduRuleFilter iterativeRuleFilter = RduRuleFilter.builder().ruleType("feed").build();
		RduRuleData iterativeRuleData = new RduRuleData();
		RduRuleOutput iterativeRuleOutput = new RduRuleOutput();
		iterativeRule.setRuleFilter(iterativeRuleFilter);
		iterativeRule.setRuleData(iterativeRuleData);
		iterativeRule.setRuleOutput(iterativeRuleOutput);
		iterativeRuleOutput.setParentAttributeName("callRedemptions");
		iterativeRuleOutput.setVariableName("RDU_callRedemptions");
		iterativeRuleOutput.setLockLevel(LockLevel.FEED.getLockLevelName());
		iterativeRuleData.setRuleScript("$.callRedemptions");
		return iterativeRule;
	}
	
	private RduRule getScheduleBaseRule() {
		RduRule iterativeRule = new RduRule();
		RduRuleFilter iterativeRuleFilter = RduRuleFilter.builder().ruleType("feed").build();
		RduRuleData iterativeRuleData = new RduRuleData();
		RduRuleOutput iterativeRuleOutput = new RduRuleOutput();
		iterativeRule.setRuleFilter(iterativeRuleFilter);
		iterativeRule.setRuleData(iterativeRuleData);
		iterativeRule.setRuleOutput(iterativeRuleOutput);
		iterativeRuleOutput.setParentAttributeName("callSchedules");
		iterativeRuleOutput.setVariableName("RDU_callSchedules");
		iterativeRuleOutput.setLockLevel(LockLevel.FEED.getLockLevelName());
		iterativeRuleData.setRuleScript("$.RDU_callRedemptions.callSchedules");
		return iterativeRule;
	}
	
	private RduRule getRedemptionRule_1() {
		RduRule iterativeRule = new RduRule();
		RduRuleFilter iterativeRuleFilter = RduRuleFilter.builder().ruleType("feed").build();
		RduRuleData iterativeRuleData = new RduRuleData();
		RduRuleOutput iterativeRuleOutput = new RduRuleOutput();
		iterativeRule.setRuleFilter(iterativeRuleFilter);
		iterativeRule.setRuleData(iterativeRuleData);
		iterativeRule.setRuleOutput(iterativeRuleOutput);
		iterativeRuleOutput.setParentAttributeName("callRedemptions");
		iterativeRuleOutput.setAttributeName("callId");
		iterativeRuleOutput.setLockLevel(LockLevel.FEED.getLockLevelName());
		iterativeRuleData.setRuleScript("feedValue(['$.RDU_callRedemptions.callId'])");
		return iterativeRule;
	}
	
	private RduRule getCallScheduleIdRule() {
		RduRule iterativeRule = new RduRule();
		RduRuleFilter iterativeRuleFilter = RduRuleFilter.builder().ruleType("feed").build();
		RduRuleData iterativeRuleData = new RduRuleData();
		RduRuleOutput iterativeRuleOutput = new RduRuleOutput();
		iterativeRule.setRuleFilter(iterativeRuleFilter);
		iterativeRule.setRuleData(iterativeRuleData);
		iterativeRule.setRuleOutput(iterativeRuleOutput);
		iterativeRuleOutput.setParentAttributeName("callSchedules");
		iterativeRuleOutput.setAttributeName("callScheduleId");
		iterativeRuleOutput.setLockLevel(LockLevel.FEED.getLockLevelName());
		iterativeRuleData.setRuleScript("feedValue(['$.RDU_callSchedules.callScheduleId'])");
		return iterativeRule;
		}

}
