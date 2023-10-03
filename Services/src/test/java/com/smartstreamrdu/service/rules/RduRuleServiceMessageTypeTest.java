package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleFilter;
import com.smartstreamrdu.rules.RduRuleFilter.RduRuleFilterBuilder;
import com.smartstreamrdu.rules.RduRuleOutput;


public class RduRuleServiceMessageTypeTest {
	
	RduRuleServiceImpl service = new RduRuleServiceImpl("test");

	RduRule rule;
	RduRuleFilter ruleFilter;
	RduRuleData ruleData;
	RduRuleOutput ruleOutput;
	RecordWrapper wrapper = new RecordWrapper();

	Record record;

	List<RduRule> rules = new ArrayList<RduRule>();
	RduRuleContext ruleContext;

	
	@Before
	public void setUp() throws Exception {
		ruleContext = new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "idcApex");
		Map<String,String> attributes=new HashMap<String,String>();
		attributes.put("cusip","INS");
		setUpRuleData(attributes);

		record = new Record();
		record.setAttribute("cusip", "001983204");
		record.setAttribute("exchangeTicker", "CME|Ticker");
		record.getRecordRawData().setRawDataLevel("INS");
		record.setMessageType("myMessageType");
		wrapper.setParentRecord(record);
		
		wrapper.setLevel(DataLevel.INS);
	}

	public void setUpRuleData(Map<String,String> attributes){
		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			setUpRuleData(attribute.getKey(), attribute.getValue());
		}
	}
	public void setUpRuleData(String attribute,String level){
		ruleData = new RduRuleData();
		String ruleScript="feedValue(['"+attribute+"'])";
		ruleData.setRuleScript(ruleScript);
		ruleOutput = new RduRuleOutput();
		ruleData.setPreConditions(new HashMap<String, List<Serializable>>(){
			private static final long serialVersionUID = 1L;
				{
					put("assetType", Arrays.asList("EQTY"));
				}});
		
		ruleOutput.setAttributeName(attribute);
		ruleOutput.setLockLevel("feed");
		ruleFilter = defaultRuleFilter().build();
		ruleFilter.setMessageTypes(Arrays.asList("myMessageType","yourMessageType"));
		persistRule();
	}

	private RduRuleFilterBuilder defaultRuleFilter() {
		return (RduRuleFilter.builder()).feedName("idcApex").ruleType("feed");
	}
	
	private void persistRule() {
		rule = new RduRule();
		rule.setRuleData(ruleData);
		rule.setRuleFilter(ruleFilter);
		rule.setRuleOutput(ruleOutput);

		rules.add(rule);
	}
	
	@Test
	public void testApplyRules() {
		List<DataContainer> result = service.applyRules(wrapper, rules, "idcApex","test_file");
		DataLevel level = result.get(0).getLevel();
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(level);
	}

	@Test
	public void testApplyRulesWithNoMessageTypes() {
		record.setMessageType(null);
		List<DataContainer> result = service.applyRules(wrapper, rules, "idcApex","test_file");
		DataLevel level = result.get(0).getLevel();
		//rules fired because no venderfields and no messagetype was there
		Assert.assertNotNull(result);
		Assert.assertNotNull(level);
	}

	@Test
	public void testApplyRulesWithInvalidMessageTypes() {
		record.setMessageType("NotMatchingWithRule");
		List<DataContainer> result = service.applyRules(wrapper, rules, "idcApex","test_file");
		Assert.assertNotNull(result);
	}
	
	@Test
	public void testApplyRulesWithNoMessageTypesAndVendorFieldAdded() {
		record.setMessageType(null);
		List<String> vf = Arrays.asList("a","b");
		RduRuleFilter filter = defaultRuleFilter().vendorFields(vf).build();
		rule.setRuleFilter(filter);

		List<DataContainer> result = service.applyRules(wrapper, rules, "idcApex","test_file");
		DataLevel level = result.get(0).getLevel();
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(level);
	}
}
