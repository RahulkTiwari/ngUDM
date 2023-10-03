/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainSearchServiceTest.java
 * Author : VRamani
 * Date : Jan 28, 2020
 * 
 */
package com.smartstreamrdu.service.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.AssetClassifications;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author VRamani
 *
 */
@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DomainSearchServiceTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private BsonConverter converter;
	
	@Autowired
	private DomainSearchService domainSearchService;

	@Test
	public void testFindVendorMappingsFromRduDomain() throws UdmTechnicalException, IOException, ClassNotFoundException {
		DataContainer assetClassificationsContainer = converter.getListOfDataContainersFromFilePath("junit/base/assetClassifications.json", AssetClassifications.class).get(0);
		
		DataContainer dbDvDomainMapContainer = domainSearchService.findVendorMappingsFromRduDomain(assetClassificationsContainer);
		List<DataContainer> dvContainers = new ArrayList<>();
		dvContainers.add(dbDvDomainMapContainer);
		compareExpectedOutput("DomainSearchServiceTest/testFindVendorMappiongsFromRduDomain/output/dvDomainMap.json", dvContainers);
		assertTrue(true);
	}
	
	@Test
	public void testGetRduDomainFromDataLevel() {
		assertEquals("assetClassifications", domainSearchService.getRduDomainFromDataLevel(DataLevel.ASSET_CLASSIFICATIONS));
		assertEquals("exchangeCodes", domainSearchService.getRduDomainFromDataLevel(DataLevel.EXCHANGE_CODES));
	}
}
