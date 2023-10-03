package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.smartstreamrdu.rules.RduRuleOutput;

public class RduRuleServiceTest {
	
	RduRuleServiceImpl service = new RduRuleServiceImpl("test");

	RduRule rule;
	RduRuleFilter ruleFilter;
	RduRuleData ruleData;
	RduRuleOutput ruleOutput;
	RecordWrapper wrapper = new RecordWrapper();

	Record record;

	List<RduRule> rules = new ArrayList<RduRule>();
	RduRuleContext ruleContext;

	public void setUpRuleData(Map<String,String> attributes){
		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			setUpRuleData(attribute.getKey(), attribute.getValue());
		}
	}
	public void setUpRuleData(String attribute,String level){
		ruleData = new RduRuleData();
		String ruleScript="feedValue(['"+attribute+"'])";
		ruleData.setRuleScript(ruleScript);
		ruleData.setPreConditions(new HashMap<String, List<Serializable>>(){/**
		 * 
		 */
			private static final long serialVersionUID = 1L;
			{
				put("assetType", Arrays.asList("EQTY"));
			}});
		ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName(attribute);
		ruleOutput.setLockLevel("Feed");
		ruleFilter = (RduRuleFilter.builder()).feedName("Reuters").ruleType("Feed").build();
		persistRule();
	}
	
	
	@Before
	public void setUp() throws Exception {
		ruleContext = new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "trdse");
		Map<String,String> attributes=new HashMap<String,String>();
		//attributes.put("expirationDate","SEC");
		attributes.put("trAssetId","INS");
		//attributes.put("ric","SEC");
		//attributes.put("trQuoteId","SEC");
		attributes.put("putCallIndicator","INS");
		setUpRuleData(attributes);

		record = new Record();
		record.setAttribute("exchangeTicker", "CME|Ticker");
		record.setAttribute("strikePriceCurrencyCode", "CME|Ticker");
		record.setAttribute("ric", "RCI123");
		record.setAttribute("putCallIndicator", "CME|Ticker");
		record.getRecordRawData().setRawDataLevel("INS");
		
		wrapper.setParentRecord(record);
		
		wrapper.setLevel(DataLevel.INS);
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
		Collection<DataContainer> result = service.applyRules(wrapper, rules, "trdse","test_file");
		Assert.assertNotNull(result);
	}

}
