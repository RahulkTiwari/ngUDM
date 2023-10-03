package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.service.PersistenceEntityRepository;
import com.smartstreamrdu.persistence.service.PersistenceServiceImpl;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleFilter;
import com.smartstreamrdu.rules.RduRuleOutput;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = { MongoConfig.class })
public class JavascriptRuleExecutorTestBA {

	
	public JavascriptRuleExecutorTestBA() {
		// TODO Auto-generated constructor stub
	}

	@Autowired
	private PersistenceServiceImpl persistenceService;

	@Autowired
	private MongoTemplate mongo;
	
	@Autowired
	private PersistenceEntityRepository repo;
	
	public final static DataAttribute INS_Vendor_mapping = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings",
			DataLevel.DV_DOMAIN_MAP);

	Record record;
	RduRule rule;
	RduRuleFilter ruleFilter;
	RduRuleData ruleData;
	RduRuleOutput ruleOutput;

	DataContainer container = DataContainerTestUtil.getSecurityContainer();

	RduRuleExecutor executor;

	List<RduRule> rules = null;
	RduRuleContext ruleContext;
//	@Before
	public void setUp() throws Exception {
		ruleContext=new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "trdse");
		record = new Record();
/*		record.addAttribute("Exchange Code", "XBAH");
		record.addAttribute("Segment", "");
		record.addAttribute("thomsonReutersClassificationScheme", "NCRCNPPRasdasd");
*/
		record.setAttribute("Exchange Code", "ABAH");
		record.setAttribute("Segment", "");
		record.setAttribute("thomsonReutersClassificationScheme", "NCRCNPPR");

		
		rule = new RduRule();

		ruleData = new RduRuleData();
		ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName("exchangeTicker");
		ruleFilter = (RduRuleFilter.builder()).feedName("Reuters").build();

		rule = new RduRule();
		rule.setRuleData(ruleData);
		rule.setRuleFilter(ruleFilter);
		rule.setRuleOutput(ruleOutput);

		rules = Arrays.asList(rule);

		DataValue<String> value = new DataValue<>();
		String exchangeVal = "CME";
		value.setValue(LockLevel.FEED, exchangeVal);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("exchangeTicker", DataLevel.SEC), value);
		
		DataValue<DomainType> domainDataValue = new DataValue<DomainType>();
		DomainType domainTypeData = new DomainType();
		domainTypeData.setVal("BSE");
		domainDataValue.setValue(LockLevel.FEED, domainTypeData);
		
		container.addAttributeValue(SecurityAttrConstant.EXCHANGE_CODE, domainDataValue);

		mongo.remove(new Query(), repo.getRootClassForLevel(DataLevel.DV_DOMAIN_MAP));


		DataContainer container1 = DataContainerTestUtil.getDataContainer(DataLevel.DV_DOMAIN_MAP);

		DataRow dataRow = new DataRow(INS_Vendor_mapping);

		DomainType domainType = new DomainType();
		domainType.setVal("BSE");
		DataValue<DomainType> domainTypeValue = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		domainTypeValue.setValue(LockLevel.RDU, domainType, rduLockLevelInfo);
		dataRow.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("domainValue", DataLevel.DV_DOMAIN_MAP), domainTypeValue);

		DataValue<String> domainNameValue = new DataValue<>();
		domainNameValue.setValue(LockLevel.RDU, "NSE|Exchange", rduLockLevelInfo);
		dataRow.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("domainName", DataLevel.DV_DOMAIN_MAP), domainNameValue);

		DataValue<String> domainSourceValue = new DataValue<>();
		domainSourceValue.setValue(LockLevel.RDU, "trds", rduLockLevelInfo);
		dataRow.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("domainSource", DataLevel.DV_DOMAIN_MAP), domainSourceValue);
		
		ArrayList<DataRow> dataRowList = new ArrayList<>();
		dataRowList.add(dataRow);

		DataValue<ArrayList<DataRow>> dataVal = new DataValue<>();
		dataVal.setValue(LockLevel.RDU, dataRowList, rduLockLevelInfo);

		DataRow dataRowOuter = new DataRow(INS_Vendor_mapping, dataVal);
		
		DataValue<String> rduDomainValue = new DataValue<>();
		rduDomainValue.setValue(LockLevel.RDU, "exchangeCodes", rduLockLevelInfo);
		
		DataValue<String> normalizedValue = new DataValue<>();
		normalizedValue.setValue(LockLevel.RDU, "XBAH", rduLockLevelInfo);
		
		container1.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("normalizedValue", DataLevel.DV_DOMAIN_MAP), normalizedValue);
		container1.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("rduDomain", DataLevel.DV_DOMAIN_MAP), rduDomainValue);
		container1.addAttributeValue(INS_Vendor_mapping, dataRowOuter);
		
		persistenceService.persist(container1);
	}
	
//	@Test
	public void testExecuteRule_PositiveScenario_DefaultValue() {
		ruleData.setRuleScript("'Default Value';");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(ruleResult, "Default Value");
	}

//	@Test
	public void testExecuteRule_PositiveScenario_Slice() {
		ruleData.setRuleScript("feedValue(['Exchange Code']).slice(1)");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, record);
		System.out.println("Actual:"+ruleResult);
		System.out.println("Expected:BAH");
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(ruleResult, "BAH");
	}

//	@Test
	public void testExecuteRule_PositiveScenario() {
		ruleData.setRuleScript("if(feedValue(['thomsonReutersClassificationScheme']) != null && ['NCICNPPRF', 'NCICPPRF', 'NCRCNPPRF','NCRCPPRF'].indexOf(feedValue(['thomsonReutersClassificationScheme'])) > -1) {\n    true\n}");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(true, ruleResult);
	}
	
//	@Test
	public void testExecuteRule_normalizedDomainValue() {
		ruleData.setRuleScript("normalizedDomainLookup(['NSE|Exchange','exchangeCode'])");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<RduRule>(), ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("XBAH", ruleResult);
	}


	
	

/*	@Test
	public void testExecuteRule_PositiveScenario() {
		ruleData.setRuleScript("feedValue(['Exchange Code'])");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("XBAH", ruleResult);
	}

	@Test
	public void testExecuteRule_PositiveScenario_DomainType() {
		ruleData.setRuleScript("domainLookup(['exchangeCodeMap','Exchange Code'],['marketSegmentMicMap','Exchange Code'])");
		ruleOutput.setAttributeName("exchangeCode");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		System.out.println(ruleResult);
		Assert.assertEquals(ruleResult.getClass(), DomainType.class);
		Assert.assertEquals("XBAH", ((DomainType) ruleResult).getVal());
	}
*/
	

/*
	@Test
	public void testExecuteRule_PositiveScenario_normalizedValue() {
		ruleData.setRuleScript("normalizedValue(['exchangeTicker'])");
		executor = new RduRuleExecutor();
		executor.registerWithRuleExecutor("dataContainer", container);
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(ruleResult, "CME");
	}

	@Test
	public void testExecuteRule_PositiveScenario_nullRuleScenario() {
		executor = new RduRuleExecutor();
		Serializable ruleResult = executor.executeRule(null, null);
		Assert.assertNull(ruleResult);
		Assert.assertEquals(ruleResult, null);
	}

	@Test
	public void testExecuteRule_NegativeScenario_nonCompiledRules() {
		ruleData.setRuleScript("normalizedValue(['exchangeTicker'])");
		executor = new RduRuleExecutor();
		executor.registerWithRuleExecutor("dataContainer", container);
		executor.initializeRuleExecutor(new ArrayList<RduRule>(), ruleContext);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(ruleResult, "CME");
	}*/

	/*@Test
	public void testExecuteRule_normalizedDomainValue() {
		ruleData.setRuleScript("normalizedDomainLookup(['NSE|Exchange','exchangeCode'])");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<RduRule>(), ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("XBAH", ruleResult);
	}

	@Test
	public void testExecuteRule_nullValues() {
		ruleData.setRuleScript("Values.NULL_VALUE;");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<RduRule>(), ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, record);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(ValueWrapper.NULL_VALUE, ruleResult);
	}
*/
}
