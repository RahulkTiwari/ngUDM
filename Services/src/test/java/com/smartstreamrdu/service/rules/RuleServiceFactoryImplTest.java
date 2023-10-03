package com.smartstreamrdu.service.rules;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RuleServiceFactoryImplTest {
	
	RuleServiceFactory factory;

	@Before
	public void setUp() throws Exception {
		
		factory = new RuleServiceFactoryImpl();
		
	}

	@Test
	public void testGetRuleService() {
		RuleService service = factory.getRuleService(RuleComponentEnum.LOADER);
		
		Assert.assertNotNull(service);
	}
	
	
	@Test
	public void testGetRuleService_negative() {
		RuleService service = null;
		
		try {
			service = factory.getRuleService(null);
		} catch (Exception e) {
			
		}
		
		Assert.assertNull(service);
	}

}
