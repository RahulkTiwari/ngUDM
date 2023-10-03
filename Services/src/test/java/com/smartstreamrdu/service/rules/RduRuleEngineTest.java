package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleFilter;
import com.smartstreamrdu.rules.RduRuleOutput;

public class RduRuleEngineTest {

	RduRuleEngine ruleEngine = new RduRuleEngine();

	RduRule rule;
	RduRuleFilter ruleFilter;
	RduRuleData ruleData;
	RduRuleOutput ruleOutput;
	
	Record record;
	RduRuleContext ruleContext;

	@Before
	public void setUp() throws Exception {
		ruleContext=new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", "trdse");
		ruleData = new RduRuleData();
		ruleData.setRuleScript("feedValue(['exchangeTicker'])");
		ruleOutput = new RduRuleOutput();
		ruleOutput.setAttributeName("exchangeTicker");
		ruleFilter = (RduRuleFilter.builder()).feedName("Reuters").build();
		
		rule = new RduRule();
		rule.setRuleData(ruleData);
		rule.setRuleFilter(ruleFilter);
		rule.setRuleOutput(ruleOutput);
		
		record = new Record();
		record.setAttribute("exchangeTicker", "CME|Ticker");
	}

	@Test
	public void testExecuteRule() {
		
		ruleEngine.initializeRuleEngine(Arrays.asList(rule), ruleContext);
		Serializable value = ruleEngine.executeRule(record, rule);
		
		Assert.assertNotNull(value);
		Assert.assertEquals("CME|Ticker", value);
	}

}
