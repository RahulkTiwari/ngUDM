package com.smartstreamrdu.service.rules;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.rules.DisField;
import com.smartstreamrdu.rules.DisRuleOutput;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleOutput;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

import net.minidev.json.JSONArray;

@ContextConfiguration(classes = MongoConfig.class)
@RunWith(SpringRunner.class)
public class RuleCustomFunctionsTest extends AbstractEmbeddedMongodbJunitParent {
	
	public final static DataAttribute INS_Vendor_mapping = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings",
			DataLevel.DV_DOMAIN_MAP);
	
	private DataContainer container;
	private DataContainer containerSec;
	private Record record;
	private RduRule rduRule;
	private Map<String,DataContainer> containerMap;
	DisField rule;
	private RuleCustomFunctions functions;
	
	@Autowired
	private ReloadIgniteCache reloadCache;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	
	@Before
	public void setUp() throws Exception {
			
		functions = new RuleCustomFunctions();

		container = DataContainerTestUtil.getInstrumentContainer();
		containerSec = DataContainerTestUtil.getSecurityContainer();
		
		DataValue<String> dataValue = new DataValue<String>();
		
		dataValue.setValue(LockLevel.FEED, "TCSVSS");
		
		container.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);
		
		DataValue<DomainType> domainDataValue = new DataValue<DomainType>();
		DomainType domainTypeData = new DomainType();
		domainTypeData.setVal("BSE");
		domainDataValue.setValue(LockLevel.FEED, domainTypeData);
		
		containerSec.addAttributeValue(SecurityAttrConstant.EXCHANGE_CODE, domainDataValue);
		
		container.addDataContainer(containerSec, DataLevel.SEC);
		containerMap=new HashMap<>();
		containerMap.put("trdse", container);
		
		initRduRule();
		initDisRule();
		
	
	}

	/**
	 * 
	 */
	private void initDisRule() {
		
		
		rule = new DisField();
		DisRuleOutput ruleOutput = new DisRuleOutput();
		ruleOutput.setDisField("isin");
		rule.setRuleOutput(ruleOutput);
		
	}

	private void initRduRule() {
		record = new Record();
		record.setAttribute("trAssetId", "0xdd1230ASDES1");
		
		rduRule = new RduRule();
		RduRuleOutput ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName(SdAttributeNames.INS_TYPE_CODE);
		ruleOutput.setLockLevel(LockLevel.FEED.toString());
		rduRule.setRuleOutput(ruleOutput);
	}
	
	
	@Test
	public void testFeedValueRecordStringArraySerializable() {
		String[] keys = {"trAssetId"};
		Serializable value = functions.feedValue(rduRule, record, keys);
		Assert.assertNotNull(value);
		Assert.assertEquals("0xdd1230ASDES1", value);
	}
	
	@Test
	public void testFeedValueRecordStringArraySerializable_defaultValue() {
		String[] keys = {"trAssetId1"};
		Serializable value = functions.feedValue(rduRule, record, keys, "default");
		Assert.assertNotNull(value);
		Assert.assertEquals("default", value);
	}
	
	@Test
	public void testFeedValueRecordStringArraySerializable_negative() {
		String[] keys = {"trAssetId1"};
		Serializable value = functions.feedValue(rduRule, record, keys);
		Assert.assertNull(value);
		Assert.assertEquals(null, value);
	}
	
	@Test
	public void testFeedValueRecordStringArraySerializable_alternateKey() {
		String[] keys = {"trAssetId1", "trAssetId"};
		Serializable value = functions.feedValue(rduRule, record, keys);
		Assert.assertNotNull(value);
		Assert.assertEquals("0xdd1230ASDES1", value);
	}

	@Test
	public void testDomainLookup() {
		String[] keys = {"RDSE|Asset","trAssetId", "trAssetId1"};
		Serializable value = functions.domainLookup("trdse", rduRule, record, keys);
		Assert.assertNotNull(value);
		Assert.assertEquals("0xdd1230ASDES1", ((DomainType) value).getVal());
	}

	@Test
	public void testNormalizedValue() {
		String[] keys = {"ric","isin"};
		Serializable value = functions.normalizedValue("trdse",rduRule, container, keys);
		System.out.println(value);
		Assert.assertNotNull(value);
		Assert.assertEquals("TCSVSS", value);
	}
	
//	@Autowired
//	private DomainLookupService domainService;
	
	@Test
	public void testNormalizedDomainValue() {
		
		String[] keys = {"NSE|Exchange","exchangeCode"};
		DataAttribute dataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(keys[1],DataLevel.SEC);
		
		containerSec.getAttributeValueAtLevel(LockLevel.FEED, dataAttribute);
		
	/*	Mockito.when(domainService.getNormalizedValueForDomainValue((DomainType) containerSec.getAttributeValueAtLevel(LockLevel.FEED, dataAttribute), 
				"trds", keys[0], DataAttributeFactory.getRduDomainForDomainDataAttribute(dataAttribute))).thenReturn("XBAH");
*/
		rduRule.getRuleOutput().setAttributeName("exchangeCode");
		Serializable value = functions.normalizedDomainLookup("trdse", rduRule, containerSec, keys);
//		Assert.assertNotNull("Lookup returned no result", value);
//		Assert.assertEquals("XBAH", value);
	}
	
	@Test
	public void test_GetDomainSourceForDomainValue() {
		String dataSource = "trdse";
		String domainSource = functions.getDomainSourceFromDataSource(dataSource);
		Assert.assertEquals("trds", domainSource);
	}
	
	
	@Test
	public void testRawFeedValueRecord() throws UdmTechnicalException {
		String[] keys = {"test1","exchangeCode"};
		Record record = new Record();
		//TODO:need to confirmed with @Ravikant is this a valid case in case of SnpXfr feed??
		//record.setRawAttribute("test1", "");
		record.getRecordRawData().setRawAttribute("exchangeCode", "MAEL");

		Serializable value = functions.rawFeedValue(rduRule, record, keys);
		Assert.assertNotNull(value);
		Assert.assertEquals("MAEL", value);
	}
	
	//get rule feed value when record is null
	@Test
	public void testRawFeedValueRecord_1() throws UdmTechnicalException {
		String[] keys = {"test1","exchangeCode"};
		Serializable value = functions.rawFeedValue(rduRule, null, keys);
		Assert.assertNull(value);
	}
	
	//get rule feed value when keys are null
	@Test
	public void testRawFeedValueRecord_2() throws UdmTechnicalException {
		String[] keys = {};
		Record record = new Record();
		record.getRecordRawData().setRawAttribute("exchangeCode", "MAEL");

		Serializable value = functions.rawFeedValue(rduRule, record, keys);
		Assert.assertNull(value);
	}
		
	//get rule feed value in case of jsonArray
	@Test
	public void testRawFeedValueRecord_jsonArray_1() throws UdmTechnicalException {
		String[] keys = {"test1","$.spRatingData[*].securitySymbolValue"};
		JSONArray array = new JSONArray();
		array.add("MAEL");
		
		Record record = new Record();
		record.getRecordRawData().setRawAttribute("$.spRatingData.abc.securitySymbolValue", array,true);

		
		Serializable value = functions.rawFeedValue(rduRule, record, keys);
		Assert.assertNotNull(value);
		Assert.assertEquals("MAEL", value);
	}
	
	//get rule feed value in case of jsonArray with multiple values
	@Test(expected = UdmTechnicalException.class)
	public void testRawFeedValueRecord_jsonArray_2() throws UdmTechnicalException {
		String[] keys = {"test1","$.spRatingData[*].securitySymbolValue"};
		JSONArray array = new JSONArray();
		array.add("MAEL");
		array.add("MAL");
		
		Record record = new Record();
		record.getRecordRawData().setRawAttribute("$.spRatingData.abc.securitySymbolValue", array,true);
		functions.rawFeedValue(rduRule, record, keys);
	}
	
	@Test
	public void testRawFeedValueRecord_jsonArray() throws UdmTechnicalException {
		String[] keys = {"test1","$.spRatingData[*].securitySymbolValue"};
		Record record = new Record();
		record.getRecordRawData().setRawAttribute("$.spRatingData.abc.securitySymbolValue", "MAEL",true);

		Serializable value = functions.rawFeedValue(rduRule, record, keys);
		Assert.assertNotNull(value);
		Assert.assertEquals("MAEL", value);
	}
	
	
	@Test
	public void testFeedValueWithDefaultDateFallback() {
		record = new Record();
		record.setAttribute("maturityExpirationDate", "R",true);
		
		rduRule = new RduRule();
		RduRuleOutput ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName("maturityExpirationDate");
		ruleOutput.setLockLevel(LockLevel.FEED.toString());
		rduRule.setRuleOutput(ruleOutput);

		String[] keys = {"$.maturityExpirationDate"};
		LocalDate date = (LocalDate)functions.feedValueWithDefaultDateFallback(rduRule, record, keys, "P|Q|R", "yyyyMMdd", "99991231");

		Assert.assertNotNull(date);
		Assert.assertEquals(9999, date.getYear());
		Assert.assertEquals(12, date.getMonthValue());
		Assert.assertEquals(31, date.getDayOfMonth());
		
		record.setAttribute("maturityExpirationDate", "",true);
		
		LocalDate date1 = (LocalDate)functions.feedValueWithDefaultDateFallback(rduRule, record, keys, "P|Q|R", "yyyyMMdd", "99991231");
		Assert.assertNull(date1);//set error code
		
	}
	
	@Test
	public void testFeedValueWithDefaultDateFallback_RegEx() {
		record = new Record();
		record.setAttribute("maturityExpirationDate", "20121231",true);
		
		rduRule = new RduRule();
		RduRuleOutput ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName("maturityExpirationDate");
		ruleOutput.setLockLevel(LockLevel.FEED.toString());
		rduRule.setRuleOutput(ruleOutput);

		String[] keys = {"$.maturityExpirationDate"};
		LocalDate date = (LocalDate)functions.feedValueWithDefaultDateFallback(rduRule, record, keys, "P|Q|R", "yyyyMMdd", "99991231");

		Assert.assertNotNull(date);
		Assert.assertEquals(2012, date.getYear());
		Assert.assertEquals(12, date.getMonthValue());
		Assert.assertEquals(31, date.getDayOfMonth());
	}
	

	@Test
	public void test() throws UdmTechnicalException {
	
		reloadCache.reloadCache("CountryCodes");
		
		RduRule rule = new RduRule();
		
		RduRuleOutput output = new RduRuleOutput();
		output.setAttributeName("incorporationCountryCode");
		rule.setRuleOutput(output);
		
		Serializable ruleResult = functions.normalizedDomainValue(rule, "AD");
		
		Assert.assertTrue(ruleResult instanceof DomainType);
	}
	
	@Test(expected = IllegalArgumentException.class )
	public void testnormalizedDomainValue_exception() throws UdmTechnicalException {
		RduRule rule = new RduRule();

		RduRuleOutput output = new RduRuleOutput();
		output.setAttributeName("attributeOutOfDataRepository");
		rule.setRuleOutput(output);

		functions.normalizedDomainValue(rule, DomainStatus.ACTIVE);
	}	
	
	
	@Test
	public void testDomainLookup_1() {
		String[] keys = {"RDSE|Asset","trAssetId", "trAssetId1"};
		Serializable value = functions.domainLookup("trdse", rduRule, record, keys);
		Assert.assertNotNull(value);
		Assert.assertEquals("0xdd1230ASDES1", ((DomainType) value).getVal());
	}
	
	@Test
	public void testDomainLookup_withEmptyVal() {
		String[] keys = {"RDSE|Asset","quoteId","trAssetId1"};
		Serializable value = functions.domainLookup("trdse", rduRule, record, keys);
		Assert.assertNotNull(value);
		
		DomainType domainOb = (DomainType)value;
		Assert.assertEquals(null,domainOb.getVal());
	}
	/**
	 * sort an array and then return first value. 
	 */
	@Test
	public void testFistValue() {
		JSONArray keys = new JSONArray();
		keys.add("ABC");
		keys.add("123");
		keys.add("xyz");
		Serializable value = functions.firstValue(keys);
		Assert.assertEquals("123",  value);
		
		Assert.assertEquals("abc",functions.firstValue("abc"));
	}
	
	
	@Test
	public void testInactivationBasedOnAttribute() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/InactivateBasedOnAttribute/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivateBasedOnAttribute("21", "expirationDate",container);
		Assert.assertEquals(inactivationDate,LocalDate.of(RuleConstants.MAX_YEAR, RuleConstants.MAX_MONTH, RuleConstants.MAX_DATE));
	}
	
	
	@Test
	public void testInactivationBasedOnAttribute_ExpiryDate() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/InactivateBasedOnAttribute_ExpiryDate/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivateBasedOnAttribute("21", "expirationDate",container);
		Assert.assertNotNull(inactivationDate);
	}
	
	@Test
	public void testInactivationBasedOnAttribute_ActiveAndInactiveSec() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationBasedOnAttribute_ActiveAndInactiveSec/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivateBasedOnAttribute("90", "expirationDate",container);
		Assert.assertNotNull(inactivationDate);
	}
	
	@Test
	public void testInactivationBasedOnAttribute_MultipleActive() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationBasedOnAttribute_ActiveAndInactiveSec/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivateBasedOnAttribute("90", "expirationDate",container);
		Assert.assertNotNull(inactivationDate);
	}
	
	@Test
	public void testInactivationBasedOnAttribute_MultipleActive_normalizedStatus() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationBasedOnAttribute_ActiveAndInactiveSec/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivateBasedOnAttribute("90", "expirationDate",container);
		Assert.assertNotNull(inactivationDate);
	}
	
	@Test
	public void testInactivationBasedOnAttribute_MultipleInactive_normalizedStatus() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationBasedOnAttribute_MultipleInactive_normalizedStatus/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivateBasedOnAttribute("90", "expirationDate",container);
		Assert.assertNotNull(inactivationDate);
	}
	
	@Test
	public void testInactivationBasedOnSecurity() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationBasedOnSecurity/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivationBasedOnSecurity("90",null,container);
		Assert.assertNotNull(inactivationDate);
	}
	
	@Test
	public void testInactivationBasedOnSecurityWithTechnicalSec() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationBasedOnSecurityWithTechnicalSec/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivationBasedOnSecurity("90",null,container);
		Assert.assertEquals(inactivationDate, LocalDate.now().plusDays(Long.valueOf("90")));
	}
	
	@Test
	public void testInactivationInsLevelDataSource() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationInsLevelDataSource/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivationBasedOnSecurity("90",null,container);
		Assert.assertEquals(inactivationDate,LocalDate.of(9999,12,31));
	}
	
	@Test
    public void testInactivationBasedOnSecurityFigiDataSource() throws UdmTechnicalException, IOException {
        DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationBasedOnSecurityFigiDataSource/sdData.json", SdData.class).get(0);
        LocalDate inactivationDate = (LocalDate) functions.inactivationBasedOnSecurity("0","figi",container);
        Assert.assertEquals(inactivationDate,LocalDate.now());
    }
	
	@Test
	public void testInactivationInsLevelDataSource_1() throws UdmTechnicalException, IOException {
		DataContainer container = bsonConverter.getListOfDataContainersFromFilePath("RuleCustomFunction/testInactivationInsLevelDataSource/sdData.json", SdData.class).get(0);
		LocalDate inactivationDate = (LocalDate) functions.inactivateBasedOnAttribute("90","expirationDate",container);
		Assert.assertEquals(inactivationDate,LocalDate.of(9999,12,31));
	}
	
	@Test
	public void testGetEarliest() throws UdmTechnicalException, IOException {
		LocalDate earliestDate = (LocalDate) functions.getEarliest(null, LocalDate.MIN);
		Assert.assertEquals(LocalDate.MIN,earliestDate);
		
		LocalDate earliestDate1 = (LocalDate) functions.getEarliest(LocalDate.MIN,null);
		Assert.assertEquals(LocalDate.MIN,earliestDate1);
		
		LocalDate earliestDate2 = (LocalDate) functions.getEarliest(LocalDate.MAX, LocalDate.MIN);
		Assert.assertEquals(LocalDate.MIN,earliestDate2);
		
		LocalDate earliestDate3 = (LocalDate) functions.getEarliest(LocalDate.MIN, LocalDate.MAX);
		Assert.assertEquals(LocalDate.MIN,earliestDate3);
	}
	
	@Test
	public void testConvertToListOfStrings() {
		String[] jsonPath = { "$.debt.coupon_payment_feature[*].coupon_date_rules[*].holiday_calendar[*]..*" };
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> map1 = new HashMap<String, String>();
		HashMap<String, Object> map2 = new HashMap<String, Object>();
		HashMap<String, Object> map3 = new HashMap<String, Object>();
		HashMap<String, Object> map4 = new HashMap<String, Object>();
		HashMap<String, Object> map5 = new HashMap<String, Object>();

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
		RduRuleOutput output = new RduRuleOutput();
		output.setAttributeName("holiday_calender");
		output.setLockLevel(LockLevel.FEED.toString());
		rule.setRuleOutput(output);

		record.setAttribute("$.debt", map4, true);
		JSONArray Fvalues = (JSONArray) functions.feedValue(rule, record, jsonPath);
		List<String> values = functions.convertJsonArrayToListOfStrings(Fvalues);

		Assert.assertEquals("NO", values.get(0));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_negativeConvertToListOfStrings() {
		String[] jsonPath = { "$.debt.coupon_payment_feature[*].coupon_date_rules[*].holiday_calendar[*]..*" };
		HashMap<String, String> map = new HashMap<>();
		HashMap<String, Integer> map1 = new HashMap<>();
		HashMap<String, Object> map2 = new HashMap<>();
		HashMap<String, Object> map3 = new HashMap<>();
		HashMap<String, Object> map4 = new HashMap<>();

		map.put("country", "NO");
		map.put("_VALUE", "Oslo");
		map1.put("_VALUE1", 1);

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
		array4.add(map3);
		map4.put("coupon_payment_feature", array4);

		Record record = new Record();
		RduRule rule = new RduRule();
		RduRuleOutput output = new RduRuleOutput();
		output.setAttributeName("holiday_calender");
		output.setLockLevel(LockLevel.FEED.toString());
		rule.setRuleOutput(output);

		record.setAttribute("$.debt", map4, true);
		JSONArray Fvalues = (JSONArray) functions.feedValue(rule, record, jsonPath);
		functions.convertJsonArrayToListOfStrings(Fvalues);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_negativeConvertToListOfStrings2() {
		String[] jsonPath = { "$.debt.coupon_payment_feature[*].coupon_date_rules[*].holiday_calendar[*]..*" };
		HashMap<String, Serializable> map = new HashMap<>();
		HashMap<String, Object> map2 = new HashMap<>();
		HashMap<String, Object> map3 = new HashMap<>();
		HashMap<String, Object> map4 = new HashMap<>();
		HashMap<String, Object> map5 = new HashMap<>();

		map.put("country", "NO");
		map.put("_VALUE", "Oslo");
		map.put("_VALUE1", 1);

		JSONArray array3 = new JSONArray();
		JSONArray array4 = new JSONArray();
		
		map2.put("holiday_calendar", map);
		array3.add(map2);
		map3.put("coupon_date_rules", array3);
		map5.put("coupon_date_rules", array3);
		array4.add(map3);
		array4.add(map5);
		map4.put("coupon_payment_feature", array4);

		Record record = new Record();
		RduRule rule = new RduRule();
		RduRuleOutput output = new RduRuleOutput();
		output.setAttributeName("holiday_calender");
		output.setLockLevel(LockLevel.FEED.toString());
		rule.setRuleOutput(output);

		record.setAttribute("$.debt", map4, true);
		JSONArray Fvalues = (JSONArray) functions.feedValue(rule, record, jsonPath);
		functions.convertJsonArrayToListOfStrings(Fvalues);
	}
}
