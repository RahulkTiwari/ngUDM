package com.smartstreamrdu.service.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.LegalEntityAttrConstant;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleFilter;
import com.smartstreamrdu.rules.RduRuleOutput;
import com.smartstreamrdu.util.DataAttributeConstant;

public class ChildRecordRuleExecutionStrategyTest {

	private static final DataAttribute LEGALENTITY_SOURCEUNIQUEID = LegalEntityAttrConstant.LEGAL_ENTITY_SOURCE_UNIQUE_ID;
	private static final DataAttribute INSTRUMENT_LE_RELATIONS = InstrumentAttrConstant.INSTRUMENT_LEGAL_ENTITY_RELATIONS;
	List<RduRule> rules;
	RduRuleEngine ruleEngine = new RduRuleEngine();
	ChildRecordRuleExecutionStrategy executionStrategy = new ChildRecordRuleExecutionStrategy("test_file","test_pgm", LocalDateTime.now(),"trdse");
	private static final DataAttribute SEC_RAW_DATA_ID = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeConstant.SEC_RAW_DATA_ID, DataLevel.SEC);

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
	
	RduRule rule3;
	RduRuleFilter ruleFilter3;
	RduRuleData ruleData3;
	RduRuleOutput ruleOutput3;
	RduRuleContext ruleContext;

	RduRule apexRule;
	RduRuleFilter apexRuleFilter;
	RduRuleData apexRuleData;
	RduRuleOutput apexRuleOutput;
	
	
	@Before
	public void setUp() throws Exception {
		ruleContext=new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "trdse");
		record = new Record();
		record.setAttribute("$.isin", "TCSLMEEEX",true);
		record.setAttribute("$.master_information.organization_master._id",Long.valueOf(12345),true);
		

		rule = new RduRule();

		ruleData = new RduRuleData();
		ruleData.setRuleScript("feedValue(['isin'])");
		ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName("isin");
		ruleOutput.setLockLevel("feed");
		ruleFilter = (RduRuleFilter.builder()).feedName("Reuters").ruleType("feed").build();

		rule = new RduRule();
		rule.setRuleData(ruleData);
		rule.setRuleFilter(ruleFilter);
		rule.setRuleOutput(ruleOutput);

		rules = new ArrayList<RduRule>(Arrays.asList(rule));

		rule1 = new RduRule();

		ruleData1 = new RduRuleData();
		ruleData1.setRuleScript("feedValue(['Exchange Code'])");
		ruleOutput1 = new RduRuleOutput();
		ruleOutput1.setAttributeName("exchangeTicker");
		ruleOutput1.setLockLevel("feed");
		ruleFilter1 = (RduRuleFilter.builder()).feedName("Reuters").ruleType("feed").build();

		rule1 = new RduRule();
		rule1.setRuleData(ruleData1);
		rule1.setRuleFilter(ruleFilter1);
		rule1.setRuleOutput(ruleOutput1);

		rule2 = new RduRule();

		ruleData2 = new RduRuleData();
		ruleData2.setRuleScript("feedValue(['isin'])");
		ruleOutput2 = new RduRuleOutput();
		ruleOutput2.setAttributeName("isin");
		ruleOutput2.setParentAttributeName("instrumentRelations");
		ruleOutput2.setRelationshipType("Underlying");
		ruleOutput2.setLockLevel("feed");
		ruleFilter2 = (RduRuleFilter.builder()).feedName("Reuters").ruleType("Feed").build();

		rule2.setRuleData(ruleData2);
		rule2.setRuleFilter(ruleFilter2);
		rule2.setRuleOutput(ruleOutput2);
		
		rule3 = new RduRule();
		
		ruleData3 = new RduRuleData();
		ruleData3.setRuleScript("feedValue(['lotSize'])");
		ruleOutput3 = new RduRuleOutput();
		ruleOutput3.setAttributeName("roundLotSize");
		ruleOutput3.setLockLevel("feed");
		ruleFilter3 = (RduRuleFilter.builder()).feedName("Reuters").ruleType("feed").build();
		
		rule3.setRuleData(ruleData3);
		rule3.setRuleFilter(ruleFilter3);
		rule3.setRuleOutput(ruleOutput3);

		
		apexRule = new RduRule();
		apexRuleFilter =  (RduRuleFilter.builder()).feedName("Reuters").ruleType("feed").build();
		apexRuleData = new RduRuleData();
		apexRuleOutput = new RduRuleOutput();
		
		apexRuleData.setRuleScript("feedValue(['$.master_information.organization_master._id']);");
		apexRuleOutput.setAttributeName("legalEntitySourceUniqueId");
		apexRuleOutput.setParentAttributeName("instrumentLegalEntityRelations");
		apexRuleOutput.setRelationshipType("Issuer");
		apexRuleOutput.setLockLevel("feed");
		
		apexRule.setRuleFilter(apexRuleFilter);
		apexRule.setRuleData(apexRuleData);
		apexRule.setRuleOutput(apexRuleOutput);

		
		
		record1 = new Record();
		record1.setAttribute("Exchange Code", "XCME");
		record1.setAttribute("lotSize", Long.valueOf(100));
		record1.getRecordRawData().setRawDataLevel("SEC");
		record1.getRecordRawData().setRawDataId("TestRawDataId");
		Record record2 = new Record();
		record2.setAttribute("Exchange Code", "XCME1");
		record2.setAttribute("lotSize", Long.valueOf(100));
		record2.getRecordRawData().setRawDataLevel("SEC");
		record2.getRecordRawData().setRawDataId("TestRawDataId2");

		wrapper = new RecordWrapper();
		wrapper.setParentRecord(record);
		wrapper.setChildRecords(Arrays.asList(record1, record2));
		wrapper.setLevel(DataLevel.SEC);

		rules.add(rule1);
		rules.add(rule2);
		rules.add(rule3);
		rules.add(apexRule);
		
		
		ruleEngine.initializeRuleEngine(rules, ruleContext);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteStrategy() {
		ArrayList<DataContainer> dataContainers = executionStrategy.executeStrategy(wrapper, ruleEngine, rules);
		Assert.assertNotNull(dataContainers);
		DataContainer parentDataContainer = dataContainers.get(0);
		List<DataContainer> childDataContainers = parentDataContainer.getChildDataContainers(DataLevel.SEC);
		DataContainer record1Container = childDataContainers.get(0);
		assertNotNull(record1Container.getAttributeValue(SEC_RAW_DATA_ID));
		assertEquals("TestRawDataId", ((DataValue<String>)record1Container.getAttributeValue(SEC_RAW_DATA_ID)).getValue());
		DataContainer record2Container = childDataContainers.get(1);
		assertNotNull(record2Container);
		assertEquals("TestRawDataId2", ((DataValue<String>)record2Container.getAttributeValue(SEC_RAW_DATA_ID)).getValue());
	
		DataRow instrumentLegalEntityRelations =(DataRow) parentDataContainer.getAttributeValue(INSTRUMENT_LE_RELATIONS);
		DataValue<ArrayList<DataRow>> relationDataValue = instrumentLegalEntityRelations.getValue();
		ArrayList<DataRow> instrumentLegalEntityRelationsDataRowList = relationDataValue.getValue();
		DataRow instrumentLegalEntityRelationshipRow = instrumentLegalEntityRelationsDataRowList.get(0);
		DataRow refDataAttribute =(DataRow) instrumentLegalEntityRelationshipRow.getAttributeValue(DataAttributeFactory.getRelationRefDataAttribute(INSTRUMENT_LE_RELATIONS));
		DataValue<Serializable> legalEntitySourceUniqueIdDataValue = refDataAttribute.getRowData().get(LEGALENTITY_SOURCEUNIQUEID);

		// Even though we map this attribute as a Integer in our parser Xml. In sdData
		// we neeD to map it to the DataType specified in the json Schemas for sdData.
		assertEquals(LEGALENTITY_SOURCEUNIQUEID.getDataType().getTypeClass(), legalEntitySourceUniqueIdDataValue.getValue().getClass());
		assertEquals("12345",legalEntitySourceUniqueIdDataValue.getValue());
	}

	@Test
	public void testNullScenario1() {
		ArrayList<DataContainer> resultContainers = executionStrategy.executeStrategy(null, ruleEngine, null);
		Assert.assertNull(resultContainers);
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullScenario2() {
		//Here NullPointerException is expected
		executionStrategy.executeStrategy(new RecordWrapper(), null, rules);		
	}	
}
