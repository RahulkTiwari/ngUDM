/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StatusFilterTest.java
 * Author:	Jay Sangoi
 * Date:	17-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.domain.ServiceTestConfiguration;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * @author Jay Sangoi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfiguration.class })
public class StatusFilterTest {

	@Autowired
	private StatusFilter filter;

	private String datasource = "trdse";

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;

	@Autowired
	private NormalizedValueService normalizedValueService;
	
	private DataContainer createDataContainer(DataLevel level, boolean active) throws Exception {
		DataContainer container = DataContainerTestUtil.getDataContainer(level);
		DataValue<String> sourceUniqueId = new DataValue<>();
		sourceUniqueId.setValue(LockLevel.FEED, UUID.randomUUID().toString());
		container.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(level), sourceUniqueId);

		
		DomainType activeDt = new DomainType("1");
		DomainType inactiveDt = new DomainType("0");
		
		Mockito.when(cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource)).thenReturn("trds");
		Mockito.when(cacheDataRetrieval.getDataStorageFromDataSource(datasource)).thenReturn(DataStorageEnum.SD);

		DataAttribute stAt = DataAttributeFactory.getAttributeByNameAndLevel(
				DataAttributeFactory.getStatusFlagForLevel(container.getLevel()), container.getLevel());
		Mockito.when(normalizedValueService.getNormalizedValueForDomainValue(stAt, activeDt, datasource)).thenReturn(DomainStatus.ACTIVE);
		
		Mockito.when(normalizedValueService.getNormalizedValueForDomainValue(stAt, inactiveDt, datasource)).thenReturn(DomainStatus.INACTIVE);
		
		String dsDomainSource = cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource);
		DataAttribute attribute = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(level),level);

		List<DomainType> activeStatusDomain = new ArrayList<>();
		activeStatusDomain.add(activeDt);

		List<DomainType> inactiveStatusDomain = new ArrayList<>();
		inactiveStatusDomain.add(inactiveDt);

		Mockito.when(cacheDataRetrieval.getVendorDomainValuesFromCache(DataAttributeFactory.getRduDomainForDomainDataAttribute(attribute), dsDomainSource, DomainStatus.ACTIVE )).thenReturn(activeStatusDomain);
		Mockito.when(cacheDataRetrieval.getVendorDomainValuesFromCache(DataAttributeFactory.getRduDomainForDomainDataAttribute(attribute), dsDomainSource, DomainStatus.INACTIVE )).thenReturn(inactiveStatusDomain);

		List<DomainType> status = cacheDataRetrieval.getVendorDomainValuesFromCache(
				DataAttributeFactory.getRduDomainForDomainDataAttribute(attribute), dsDomainSource,
				active ? DomainStatus.ACTIVE : DomainStatus.INACTIVE);

		DataValue<DomainType> domainValue = new DataValue<>();
		domainValue.setValue(LockLevel.FEED, status.get(0));
		container.addAttributeValue(DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(level), level), domainValue);

		return container;
	}

	@Test
	public void test_InactiveInstrument() throws Exception {
		DataContainer createInstrument = createDataContainer(DataLevel.INS, false);

		FilterOutput doPersist = filter.doPersist(createInstrument, null, datasource);

		Assert.assertNotNull(doPersist);
		Assert.assertTrue(doPersist.getFilteredInactiveSecurities().isEmpty());

		Assert.assertEquals(false, doPersist.isPersist());

		Assert.assertNotNull(doPersist.getMessage());

	}

	@Test
	public void test_ActiveInstrument() throws Exception {
		DataContainer createInstrument = createDataContainer(DataLevel.INS, true);

		FilterOutput doPersist = filter.doPersist(createInstrument, null, datasource);

		Assert.assertNotNull(doPersist);
		Assert.assertTrue(doPersist.getFilteredInactiveSecurities().isEmpty());

		Assert.assertEquals(true, doPersist.isPersist());

	}

	@Test
	public void test_AllSecurityInactive() throws Exception {
		DataContainer instrument = createDataContainer(DataLevel.INS, true);

		DataContainer securitu1 = createDataContainer(DataLevel.SEC, false);

		instrument.addDataContainer(securitu1, DataLevel.SEC);

		DataContainer securitu2 = createDataContainer(DataLevel.SEC, false);

		instrument.addDataContainer(securitu2, DataLevel.SEC);

		FilterOutput doPersist = filter.doPersist(instrument, null, datasource);

		Assert.assertNotNull(doPersist);
		Assert.assertEquals(2, doPersist.getFilteredInactiveSecurities().size());

		Assert.assertEquals(false, doPersist.isPersist());

		Assert.assertNotNull(doPersist.getMessage());

		Assert.assertEquals(0, instrument.getChildDataContainers(DataLevel.SEC).size());

	}

	@Test
	public void test_SomeSecurityInactive() throws Exception {
		DataContainer instrument = createDataContainer(DataLevel.INS, true);

		DataContainer securitu1 = createDataContainer(DataLevel.SEC, false);

		instrument.addDataContainer(securitu1, DataLevel.SEC);

		DataContainer securitu2 = createDataContainer(DataLevel.SEC, true);

		instrument.addDataContainer(securitu2, DataLevel.SEC);

		FilterOutput doPersist = filter.doPersist(instrument, null, datasource);

		Assert.assertNotNull(doPersist);
		Assert.assertEquals(1, doPersist.getFilteredInactiveSecurities().size());

		Assert.assertEquals(true, doPersist.isPersist());

		Assert.assertNotNull(doPersist.getMessage());

		Assert.assertEquals(1, instrument.getChildDataContainers(DataLevel.SEC).size());

	}
}
