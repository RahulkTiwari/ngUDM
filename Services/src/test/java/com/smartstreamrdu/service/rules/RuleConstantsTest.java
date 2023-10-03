package com.smartstreamrdu.service.rules;

import org.junit.Assert;
import org.junit.Test;

public class RuleConstantsTest {

	@Test
	public void test() {
		Assert.assertEquals("feedName",RuleConstants.FEED_NAME);
		Assert.assertEquals("context",RuleConstants.CONTEXT);
		Assert.assertEquals("record",RuleConstants.RECORD);
		Assert.assertEquals("functions",RuleConstants.FUNCTIONS);
		Assert.assertEquals("bootstrap",RuleConstants.BOOTSTRAP);
		Assert.assertEquals("feed",RuleConstants.FEED);
		Assert.assertEquals("dependent",RuleConstants.DEPENDENT);
		Assert.assertEquals("nestedAttributes",RuleConstants.NESTED_ATTRIBUTES);
		Assert.assertEquals("nonNestedAttributes",RuleConstants.NON_NESTED_ATTRIBUTES);
		Assert.assertEquals("isInitialized", RuleConstants.IS_INITIALIZED);
		Assert.assertNotNull(RuleConstants.NULL_VALUE_CHECK_OBJECT);
		Assert.assertNotNull(RuleConstants.FEED_NAME);
		Assert.assertNotNull(RuleConstants.BOOTSTRAP);
		Assert.assertNotNull(RuleConstants.CONTEXT);
		Assert.assertNotNull(RuleConstants.FEED);
		Assert.assertNotNull(RuleConstants.FUNCTIONS);
		Assert.assertNotNull(RuleConstants.DEPENDENT);
		Assert.assertNotNull(RuleConstants.NESTED_ATTRIBUTES);
		Assert.assertNotNull(RuleConstants.NON_NESTED_ATTRIBUTES);
		Assert.assertNotNull(RuleConstants.RECORD);
	}

}