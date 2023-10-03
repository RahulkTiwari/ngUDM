package com.smartstreamrdu.service.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.UdmErrorCodes;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleFilter;
import com.smartstreamrdu.rules.RduRuleOutput;
import com.smartstreamrdu.rules.Rule;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class JavascriptRuleExecutorTest extends AbstractEmbeddedMongodbJunitParent{
	
	

	Record record;
	RduRule rule;
	RduRuleFilter ruleFilter;
	RduRuleData ruleData;
	RduRuleOutput ruleOutput;
	RduRuleContext ruleContext;
	RduRuleServiceInput input;

	DataContainer container = DataContainerTestUtil.getSecurityContainer();

	JavascriptRuleExecutor executor;

	List<Rule> rules = null;

	@Before
	public void setUp() throws Exception {
		ruleContext=new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "trdse");
	    input = new RduRuleServiceInput();
		
		input.setRduRuleContext(ruleContext);
		record = new Record();
		record.setAttribute("Exchange_Code", "XBAH");
		record.setAttribute("Segment", "");
		record.setAttribute("assetRatioFor", 10);
		record.setAttribute("assetRatioAgainst", 2);
		record.setAttribute("warrantIssueDate", LocalDate.now());
		record.setAttribute("marketSector", "Equity");
	    record.setAttribute("exchCode", "abc");

	    input.setRecord(record);
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
		
		/*DataValue<LocalDate> issueDateValue = new DataValue<>();
		LocalDate issueDateVal = LocalDate.now();
		issueDateValue.setValue(LockLevel.FEED, issueDateVal);
		container.addAttributeValue(DataAttributeFactory.getDataAttributeByNameForSdData("warrantIssueDate"), issueDateValue);*/

		/*mongo.dropCollection(repo.getRootClassForLevel(DataLevel.DV_DOMAIN_MAP));


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
		
		persistenceService.persist(container1);*/
	}
	
	@Test
	public void testExecuteRule_PositiveScenario() {
		ruleData.setRuleScript("feedValue(['Exchange_Code'])");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("XBAH", ruleResult);
	}

	@Test
	public void testExecuteRule_PositiveScenario_DomainType() {
		ruleData.setRuleScript("domainLookup(['exchangeCodeMap','Exchange_Code'],['marketSegmentMicMap','Exchange_Code'])");
		ruleOutput.setAttributeName("exchangeCode");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(ruleResult.getClass(), DomainType.class);
		Assert.assertEquals("XBAH", ((DomainType) ruleResult).getVal());
	}

	@Test
	public void testExecuteRule_PositiveScenario_DefaultValue() {
		ruleData.setRuleScript("'Default_Value';");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("Default_Value", ruleResult);
	}

	@Test
	public void testExecuteRule_PositiveScenario_Slice() {
		ruleData.setRuleScript("feedValue(['Exchange_Code']).slice(1)");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("BAH", ruleResult);
	}


	@Test
	public void testExecuteRule_PositiveScenario_normalizedValue() {
		ruleData.setRuleScript("normalizedValue(['exchangeTicker'])");
		executor = new RduRuleExecutor();
		executor.registerWithRuleExecutor("dataContainer", container);
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("CME", ruleResult);
	}

	@Test
	public void testExecuteRule_PositiveScenario_nullRuleScenario() {
		executor = new RduRuleExecutor();
		Serializable ruleResult = executor.executeRule(null, input);
		Assert.assertNull(ruleResult);
		Assert.assertEquals(ruleResult, null);
	}

	@Test
	public void testExecuteRule_NegativeScenario_nonCompiledRules() {
		ruleData.setRuleScript("normalizedValue(['exchangeTicker'])");
		executor = new RduRuleExecutor();
		executor.registerWithRuleExecutor("dataContainer", container);
		executor.initializeRuleExecutor(new ArrayList<Rule>(), ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("CME", ruleResult);
	}

//	@Test
	public void testExecuteRule_normalizedDomainValue() {
		ruleData.setRuleScript("normalizedDomainLookup(['NSE|Exchange','exchangeCode'])");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<Rule>(), ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals("XBAH", ruleResult);
	}

	@Test
	public void testExecuteRule_nullValues() {
		ruleData.setRuleScript("Values.NULL_VALUE;");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<Rule>(), ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
		Assert.assertEquals(ValueWrapper.NULL_VALUE, ruleResult);
	}
	
	@Test
	public void testIssueDateRule() {
		ruleData.setRuleScript("feedValue(['warrantIssueDate']);");
		ruleOutput.setAttributeName("issueDate");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<Rule>(), ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
	}
	
	@Test
	public void testExecuteRule_NegativeScenario_DRValues() {
		DataValue<DomainType> domainDataValue = new DataValue<DomainType>();
		DomainType domainTypeData = new DomainType();
		domainTypeData.setNormalizedValue("0");
		domainDataValue.setValue(LockLevel.FEED, domainTypeData);
		ruleData.setRuleScript("if (feedValue(['assetRatioFor']) != null && feedValue(['assetRatioAgainst']) != null && normalizedDomainLookup(['trcsMap','instrumentTypeCode']) == '5106') {    feedValue(['assetRatioAgainst']) / feedValue(['assetRatioFor']);}");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<Rule>(),  ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNull(ruleResult);
	}
	
	@Test
	public void testExecuteRule_PositiveScenario_DRValues() {
		DataValue<DomainType> domainDataValue = new DataValue<DomainType>();
		DomainType domainTypeData = new DomainType();
		domainTypeData.setNormalizedValue("5106");
		domainDataValue.setValue(LockLevel.FEED, domainTypeData);
		
		container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_TYPE_CODE, domainDataValue);
		ruleData.setRuleScript("if (feedValue(['assetRatioFor']) != null && feedValue(['assetRatioAgainst']) != null && normalizedDomainLookup(['trcsMap','instrumentTypeCode']) == '5106') {    feedValue(['assetRatioAgainst']) / feedValue(['assetRatioFor']);}");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(new ArrayList<Rule>(),  ruleContext);
		executor.registerWithRuleExecutor("dataContainer", container);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNotNull(ruleResult);
	}
	
	@Test
	public void test_getFromCodeForMap() {
		ruleData.setRuleScript("if (feedValue(['marketSector']) == 'Equity') { getFromCodeForMap([feedValue(['exchCode']),'figiMap','bbgExchange2CompositeExchangeCodeMap']); }");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		Assert.assertNull(ruleResult);
	}
	
	@Test
	public void test_errorCode_TBA() {
		ruleData.setRuleScript("UdmErrorCodes.TBA");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		assertEquals(UdmErrorCodes.TBA, ruleResult);
	}
	
	@Test
	public void test_errorCode_101() {
		ruleData.setRuleScript("UdmErrorCodes.getByErrorCode(101);");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		assertEquals(UdmErrorCodes.DOMAIN_MISSING, ruleResult);
	}
	
	@Test
	public void test_errorCode_invalid() {
		ruleData.setRuleScript("UdmErrorCodes.ABC");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		assertNull(ruleResult);
	}
	
	@Test
	public void test_getErrorCodeFromMap() {
		ruleData.setRuleScript("getErrorCodeFromMap(['dummyErrorVal','rduEnsMap','allString2EffectiveDateLogic']);");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(rules, ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		assertNull(ruleResult);
	}
	

	@Test
	public void test_getErrorCodeFromMap_1() {
		
		RuleCustomFunctions functions = spy(RuleCustomFunctions.class);
		String[] array = {"dummyErrorVal","rduEnsMap","allString2EffectiveDateLogic"};
		Mockito.when(functions.getFromCodeForMap(array)).thenReturn("DummyerrorCode108-newError");
		Serializable errorCodeFromMap =functions.getErrorCodeFromMap(array);
		assertNull(errorCodeFromMap);
	}
	
	@Test
	public void test_getErrorCodeFromMap_2() {
		
		RuleCustomFunctions functions = spy(RuleCustomFunctions.class);
		String[] array = {"dummyErrorVal","rduEnsMap","allString2EffectiveDateLogic"};
		Mockito.when(functions.getFromCodeForMap(array)).thenReturn("DummyerrorCode1a08-newError");
		Serializable errorCodeFromMap =functions.getErrorCodeFromMap(array);
		assertNull(errorCodeFromMap);
	}

	@Test
	public void test_convertJsonArrayToListOfStrings() {
		HashMap<String, String> map = new HashMap<>();
		HashMap<String, String> map1 = new HashMap<>();
		HashMap<String, Object> map2 = new HashMap<>();
		HashMap<String, Object> map3 = new HashMap<>();
		HashMap<String, Object> map4 = new HashMap<>();
		HashMap<String, Object> map5 = new HashMap<>();

		map.put("country", "NO");
		map.put("_VALUE", "Oslo");
		map1.put("_VALUE1", "2 DAY Holiday");

		JSONArray array = new JSONArray();
		JSONArray array1 = new JSONArray();
		JSONArray array2 = new JSONArray();
		JSONArray array3 = new JSONArray();
		JSONArray array4 = new JSONArray();
		array.add(map);
		array1.add(map1);
		array2.addAll(array);
		array2.addAll(array1);

		map2.put("holiday_calendar", array2);
		array3.add(map2);
		map3.put("coupon_date_rules", array3);
		map5.put("coupon_date_rules", array3);
		array4.add(map3);
		array4.add(map5);
		map4.put("coupon_payment_feature", array4);

		Record record = new Record();
		RduRule rule = new RduRule();
		RduRuleData ruleData = new RduRuleData();
		rule.setRuleData(ruleData);
		RduRuleOutput output = new RduRuleOutput();
		output.setAttributeName("holiday_calender");
		output.setLockLevel(LockLevel.FEED.toString());
		rule.setRuleOutput(output);

		record.setAttribute("$.debt", map4, true);
		
		input.setRecord(record);
		ruleData.setRuleScript("convertJsonArrayToListOfStrings(feedValue(['$.debt.coupon_payment_feature[*].coupon_date_rules[*].holiday_calendar[*]..*']))");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(Arrays.asList(rule), ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		assertNotNull(ruleResult);
		Assert.assertEquals(true, ruleResult instanceof List);
	}
	
	@Test
	public void test_convertJsonArrayToListOfStrings1() {
		HashMap<String, String> map = new HashMap<>();
		HashMap<String, Integer> map1 = new HashMap<>();
		HashMap<String, Object> map2 = new HashMap<>();
		HashMap<String, Object> map3 = new HashMap<>();
		HashMap<String, Object> map4 = new HashMap<>();
		HashMap<String, Object> map5= new HashMap<>();

		map.put("country", "NO");
		map1.put("_VALUE", 8);

		JSONArray array = new JSONArray();
		JSONArray array2 = new JSONArray();
		JSONArray array3 = new JSONArray();
		JSONArray array4 = new JSONArray();
		array.add(map);
		array2.addAll(array);

		map2.put("holiday_calendar", array2);
		map5.put("rate",map1);
		array3.add(map2);
		array3.add(map5);
		map3.put("coupon_date_rules", array3);
		array4.add(map3);
		map4.put("coupon_payment_feature", array4);

		Record record = new Record();
		RduRule rule = new RduRule();
		RduRuleData ruleData = new RduRuleData();
		rule.setRuleData(ruleData);
		RduRuleOutput output = new RduRuleOutput();
		output.setAttributeName("holiday_calender");
		output.setLockLevel(LockLevel.FEED.toString());
		rule.setRuleOutput(output);

		record.setAttribute("$.debt", map4, true);
		
		input.setRecord(record);
		ruleData.setRuleScript("convertJsonArrayToListOfStrings(feedValue(['$.debt.coupon_payment_feature[*].coupon_date_rules[*].holiday_calendar[*]..*']))");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(Arrays.asList(rule), ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		assertNotNull(ruleResult);
		Assert.assertEquals(true, ruleResult instanceof List);
	}
	
	@Test
	public void test_initiateArrayMerge() {
		JSONObject map1 = new JSONObject();
		JSONObject map2 = new JSONObject();
		JSONObject map3 = new JSONObject();
		JSONObject object1 = new JSONObject();
		JSONObject object2 = new JSONObject();
		JSONObject object3 = new JSONObject();
		JSONObject object4 = new JSONObject();
		JSONArray array1 = new JSONArray();
		JSONArray array2 = new JSONArray();
		JSONArray array3 = new JSONArray();
		JSONArray array4 = new JSONArray();

		JSONArray array5 = new JSONArray();
		map1.put("_start_date", "2015-12-29");
		map1.put("contingent_interest_ind", "false");
		array1.add(map1);

		map2.put("_start_date", "2043-12-29");
		map2.put("_end_date", "2042-03-15");
		array1.add(map2);

		map3.put("_start_date", "2015-03-29");
		map3.put("_end_date", "2042-03-15");
		array1.add(map3);
		object1.put("coupon_payment_feature", array1);

		array2.add(map3);
		array2.add(map2);
		object2.put("step_schedule", array2);

		array3.add(map1);
		array3.add(map2);

		object3.put("test_tag", array3);

		array4.add(map2);
		object4.put("test_date", array4);

		array5.add(object1);
		array5.add(object2);
		array5.add(object3);
		array5.add(object4);
		Record record=new Record();
		record.setAttribute("$.debt.fixed_income",array5,true);
		input.setRecord(record);
		
		RduRule rule = new RduRule();
		RduRuleData ruleData = new RduRuleData();
		rule.setRuleData(ruleData);
		ruleData.setRuleScript(""
				+ "initiateArrayMerge(feedValue(['$.debt.fixed_income[*].coupon_payment_feature[*]']), ['_start_date'], 'coupon_payment_feature')"
				+ ".merge(feedValue(['$.debt.fixed_income[*].step_schedule[*]']), ['_start_date'], 'step_schedule')"
				+ ".merge(feedValue(['$.debt.fixed_income[*].test_tag[*]']), ['_start_date'], 'test_tag')"
				+ ".merge(feedValue(['$.debt.fixed_income[*].test_date[*]']), ['_start_date'], 'test_date')"
				+ ".getJsonArray()");
		executor = new RduRuleExecutor();
		executor.initializeRuleExecutor(Arrays.asList(rule), ruleContext);
		Serializable ruleResult = executor.executeRule(rule, input);
		assertNotNull(ruleResult);
	}
}
