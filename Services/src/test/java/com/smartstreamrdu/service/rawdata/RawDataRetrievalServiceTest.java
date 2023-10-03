/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataRetrievalServiceTest.java
 * Author:  Padgaonkar
 * Date:    Jul 19, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author Padgaonkar
 *
 */
@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RawDataRetrievalServiceTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private RawDataRetrievalService retrievalService;
	
	@InputCollectionsPath(paths={"RawDataRetrievalServiceTest/input"})
	@ModifiedCollections(collections={"enRawData"})
	@Test
	public void testRawDataRetrievalServiceValid() throws UdmTechnicalException {
		
		JSONObject rawDataJson = retrievalService.retrieveRawDataJSON("60eda1f526bd361d6483d318","rduEns");
		Assert.assertNotNull(rawDataJson);	
	}	
		
	@Test
	public void testRawDataRetrievalServiceInValid() throws UdmTechnicalException {		
		JSONObject rawDataJson = retrievalService.retrieveRawDataJSON("123","rduEns");
		Assert.assertNull(rawDataJson);
		
	}	
	

}
