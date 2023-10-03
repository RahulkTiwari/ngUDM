/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MatchingGroupMicLookupServiceTest.java
 * Author:	Divya Bharadwaj
 * Date:	22-Oct-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.openfigi;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.ignite.Ignite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.cache.IgniteHelperService;
import com.smartstreamrdu.persistence.cache.initializer.CacheOperation;
import com.smartstreamrdu.persistence.cache.initializer.DataContainerCacheStore;
import com.smartstreamrdu.persistence.domain.ExchangeCodes;
import com.smartstreamrdu.persistence.mongodb.EmbeddedMongoConfigTest;
import com.smartstreamrdu.service.openfigi.MatchingGroupMicLookupService;


/**
 * @author Bharadwaj
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { EmbeddedMongoConfigTest.class })
public class MatchingGroupMicLookupServiceTest extends AbstractEmbeddedMongodbJunitParent {
	@Autowired
	MatchingGroupMicLookupService service;
	
	@Autowired
	Ignite ignite;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Autowired
	private DataContainerCacheStore dataContainerCacheStore;
	
	@Autowired
	private IgniteHelperService igniteHelperService;

	@Test	
	public void lookupOperatingMic() throws Exception {
		ignite.getOrCreateCache("ExchangeCodes").clear();
		
		List<DataContainer> exchangeCodesContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"MatchingGroupMicLookupServiceTest/lookupOperatingMic/exchangeCodes.json", ExchangeCodes.class);
		
		exchangeCodesContainers.forEach(dc -> {
			dataContainerCacheStore.updateCacheEntry(igniteHelperService.getCacheStoreArguments(CacheOperation.INSERT, dc));
		});
		
		
		
		List<String> result = service.fetchFallbackMics("XFRA");
		List<String> expected =Arrays.asList(new String[]{"FRAA", "XDBC", "XDBV"});
		assertEquals(expected, result);
	}

}
