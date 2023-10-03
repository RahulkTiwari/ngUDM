package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class RduRuleContextTest {
	
	RduRuleContext ruleContext;

	@Before
	public void setUp() throws Exception {
		ruleContext = new RduRuleContext();
		ruleContext.addToRuleContext("feedName", "Reuters");
	}

	@Test
	public void testGetRuleContext() {
		Map<String, Serializable> ruleContextMap = ruleContext.getRuleContext();
		Assert.assertNotNull(ruleContextMap);
	}

	@Test
	public void testSetRuleContext() {
		Map<String, Serializable> newRuleContextMap = new HashMap<>();
		ruleContext.setRuleContext(newRuleContextMap);
		Assert.assertEquals(0, ruleContext.getRuleContext().size());
	}

	@Test
	public void testAddToRuleContext() {
		ruleContext.addToRuleContext("newKey", "newKeyValue");
		Assert.assertNotNull(ruleContext.getRuleContext().get("newKey"));
		Assert.assertEquals("newKeyValue", ruleContext.getRuleContext().get("newKey"));
	}

	@Test
	public void testClearRuleContext() {
		ruleContext.addToRuleContext("newKey", "newKeyValue");
		ruleContext.clearContext();
		Assert.assertNull(ruleContext.getRuleContext().get("newKey"));
	}
}
