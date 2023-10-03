/**
 * *****************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionRouteUtilTest.java
 * Author:	Padgaonkar
 * Date:	05-Mar-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.exceptions;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class UdmExceptionRouteUtilTest {

	@Autowired
	UdmExceptionRouteContext udmExceptionRouteContext;
	
	private CamelContext camelContext;

	@Test
	public void TestRouteUtil() {
		camelContext = new DefaultCamelContext();
		Exchange exchange = new DefaultExchange(camelContext);
		exchange.getIn().setBody(udmExceptionRouteContext, UdmExceptionRouteContext.class);

		Assert.assertNull(UdmExceptionRouteUtil.getRouteContextFromExchange(null));
		Assert.assertNotNull(UdmExceptionRouteUtil.getRouteContextFromExchange(exchange));
		Assert.assertEquals(udmExceptionRouteContext, UdmExceptionRouteUtil.getRouteContextFromExchange(exchange));
	}
}
