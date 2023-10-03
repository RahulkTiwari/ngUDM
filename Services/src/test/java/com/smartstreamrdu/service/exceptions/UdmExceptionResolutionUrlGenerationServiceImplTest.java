/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionResolutionUrlGenerationServiceImplTest.java
 * Author:	Divya Bharadwaj
 * Date:	07-May-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.exceptions;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author Bharadwaj
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class UdmExceptionResolutionUrlGenerationServiceImplTest extends AbstractEmbeddedMongodbJunitParent{
	@Autowired
	UdmExceptionResolutionUrlGenerationService service;
	
	@Test
	public void testPopulateResolutionUrl() throws URISyntaxException, UnsupportedEncodingException{
		String stormCriteriaValue = "RDUNIN000000000003|2520195-XNAS-Default|Source filter - Security";
		String exceptionType ="Source filter - Security";
		String url = service.populateResolutionUrl(stormCriteriaValue, exceptionType);
		Assert.assertNotNull(url);
		URI uri = new URI(url);
		Assert.assertEquals("http", uri.getScheme());
		Assert.assertTrue(URLDecoder.decode(uri.toString(), "UTF-8").contains("RDUNIN000000000003|2520195-XNAS-Default|Source filter - Security"));
	}
}
