package com.smartstreamrdu.service.rules;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.RecordWrapper;

public class RuleExecutionStrategyFactoryImplTest {

	RecordWrapper wrapper;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetRuleExecutionStrategy_parentLevelScenario() {
		wrapper = new RecordWrapper();
		wrapper.setLevel(DataLevel.INS);
		RuleExecutionStrategyFactoryImpl factory = new  RuleExecutionStrategyFactoryImpl("test");
		RuleExecutionStrategy strategy = factory.getRuleExecutionStrategy(wrapper,"test_file","trdse");
		Assert.assertNotNull(strategy);
		Assert.assertEquals(ParentRecordRuleExecutionStrategy.class, strategy.getClass());
	}
	
	@Test
	public void testGetRuleExecutionStrategy_childLevelScenario() {
		wrapper = new RecordWrapper();
		wrapper.setLevel(DataLevel.SEC);
		RuleExecutionStrategyFactoryImpl factory = new  RuleExecutionStrategyFactoryImpl("test");
		RuleExecutionStrategy strategy = factory.getRuleExecutionStrategy(wrapper,"test_file","trdse");
		Assert.assertNotNull(strategy);
		Assert.assertEquals(ChildRecordRuleExecutionStrategy.class, strategy.getClass());
	}

	@Test(expected=NullPointerException.class)
	public void testGetRuleExecutionStrategy_nullWrapperScenario() {
		RuleExecutionStrategyFactoryImpl factory = new  RuleExecutionStrategyFactoryImpl("test");
		RuleExecutionStrategy strategy = null;
		try {
			strategy = factory.getRuleExecutionStrategy(wrapper,"test_file","trdse");
		} catch (IllegalArgumentException exception) {

		}
		Assert.assertNull(strategy);
	}

	@Test
	public void testGetRuleExecutionStrategy_nullDatalevelScenario() {
		wrapper = new RecordWrapper();
		RuleExecutionStrategyFactoryImpl factory = new  RuleExecutionStrategyFactoryImpl("test");
		RuleExecutionStrategy strategy = null;
		strategy = factory.getRuleExecutionStrategy(wrapper,"test_file","trdse");
		Assert.assertNull(strategy);
	}

}
