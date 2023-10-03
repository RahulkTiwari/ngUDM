/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DomainServiceImplTest.java
 * Author:	Jay Sangoi
 * Date:	04-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;

/**
 * @author Jay Sangoi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfiguration.class })
public class DomainServiceImplTest {

	@Autowired
	private DomainService domainService;

	@Autowired
	private DvDomainMapCacheMockHelper helper;

	@Test
	public void test_getActiveStatusValueForVendor() throws Exception {
		String datasource = "trdse";
		DataAttribute insStatus = InstrumentAttrConstant.INSTRUMENT_STATUS;
		helper.createMockDataForStatus(datasource, insStatus);
		DomainType ds = new DomainType(datasource);
		DomainType activeDt = new DomainType("1");
		DomainType inactiveDt = new DomainType("0");

		Assert.assertEquals(activeDt, domainService.getActiveStatusValueForVendor(ds, insStatus).get(0));

		Assert.assertEquals(inactiveDt, domainService.getInActiveStatusValueForVendor(ds, insStatus).get(0));

	}

}
