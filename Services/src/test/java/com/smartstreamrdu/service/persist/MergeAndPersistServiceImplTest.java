/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergeAndPersistServiceImplTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.util.ArrayList;
import java.util.List;

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
import com.smartstreamrdu.persistence.mock.config.DataRetrievalServiceMockConfig;
import com.smartstreamrdu.persistence.repository.FeedConfigurationRepository;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.filter.DataFilterChainService;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.lookup.LookupService;
import com.smartstreamrdu.service.lookup.LookupServiceImpl;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;
import com.smartstreamrdu.service.postprocess.DataContainerPostProcessorService;
import com.smartstreamrdu.service.util.MockUtil;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;

/**
 * @author Jay Sangoi
 *
 */
@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MergeAndPersisteServiceConfig.class, DataRetrievalServiceMockConfig.class })
public class MergeAndPersistServiceImplTest {
	
	@Autowired
	private MergeAndPersistService service;

	@Autowired
	private ListenerService listenerService;

	@Autowired
	private PersistenceService persistencService;

	@Autowired
	private LookupService lookupService;

	@Autowired
	private DataFilterChainService filterSerivice;
	
	@Autowired
	FeedConfigurationRepository repo;
	
	@Autowired
	private CacheDataRetrieval cache;
	
	@Autowired
    List<DataContainerPostProcessorService> dataContainerPostProcessors;

	/*
	 * @Autowired private DataContainerMergingService mergingService;
	 * 
	 */
	@Test
	public void test_lookupAndPersist_persist() throws Exception {
		Mockito.when(cache.getDataStorageFromDataSource("trdse")).thenReturn(DataStorageEnum.SD);
		MockUtil.mock_listenerService_newdataContainer(listenerService);
		MockUtil.mock_listenerService_dataContainerMerge(listenerService);
		MockUtil.mock_listenerService_mergeComplete(listenerService);
		MockUtil.mock_listenerService_dataContainerUpdated(listenerService);

		DomainType feedDs = new DomainType();
		feedDs.setVal("ds");

		// MockUtil.mock_mergingService_merge_empty(mergingService);

		// MockUtil.mock_mergingService_merge_val(mergingService);

		MockUtil.mock_persist_service_persist(persistencService);

		MockUtil.mock_filterserice_isPersist_true(filterSerivice);

		MockUtil.mock_lookupService_resolveLookup(lookupService);

		DataContainer inpContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);

		DataValue<String> sourceUniqueId = new DataValue<>();
		sourceUniqueId.setValue(LockLevel.FEED, "SourceUniqueId");

		DataValue<String> isinFeed = new DataValue<>();
		isinFeed.setValue(LockLevel.FEED, "Isin1");

		DataValue<String> isinDb = new DataValue<>();
		isinDb.setValue(LockLevel.FEED, "Isin2");

		inpContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(SdDataAttConstant.ISIN, DataLevel.INS), isinFeed);

		inpContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueId);
		
		DataAttribute domainAttr = DataAttributeFactory.getDatasourceAttribute(DataLevel.INS);
		DataValue<DomainType> value = new DataValue<>();
		DomainType domainVal = new DomainType();
		domainVal.setVal("trdse");
		value.setValue(LockLevel.FEED, domainVal);
		inpContainer.addAttributeValue(domainAttr, value);
		
		DataContainer dbContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);
		dbContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueId);
		dbContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(SdDataAttConstant.ISIN, DataLevel.INS), isinDb);

		List<DataContainer> dbContainers = new ArrayList<>();
		dbContainers.add(dbContainer);

		MockUtil.mock_lookupService_getDbDataContainer(lookupService, inpContainer, feedDs, dbContainers);

		DataContainer mergedDc = service.lookupAndPersist(inpContainer, new LookupAttributeInput(), "trdse",null);

		Assert.assertEquals(inpContainer, mergedDc);

	}

	@Test
	public void test_lookupAndPersist_no_db() throws Exception {
		MockUtil.mock_listenerService_newdataContainer(listenerService);
		MockUtil.mock_listenerService_dataContainerMerge(listenerService);
		MockUtil.mock_listenerService_mergeComplete(listenerService);
		MockUtil.mock_listenerService_dataContainerUpdated(listenerService);

		// MockUtil.mock_mergingService_merge_empty(mergingService);

		// MockUtil.mock_mergingService_merge_val(mergingService);

		MockUtil.mock_persist_service_persist(persistencService);

		DataContainer inpContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);

		DataValue<String> sourceUniqueId = new DataValue<>();
		sourceUniqueId.setValue(LockLevel.FEED, "SourceUniqueId");

		DataValue<String> isinFeed = new DataValue<>();
		isinFeed.setValue(LockLevel.FEED, "Isin1");

		DataValue<String> isinDb = new DataValue<>();
		isinDb.setValue(LockLevel.FEED, "Isin2");

		inpContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(SdDataAttConstant.ISIN, DataLevel.INS), isinFeed);

		inpContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueId);

		DomainType feedDs = new DomainType();
		feedDs.setVal("ds");

		MockUtil.mock_lookupService_getDbDataContainer(lookupService, inpContainer, feedDs, new ArrayList<>());

		MockUtil.mock_filterserice_isPersist_false(filterSerivice);

		MockUtil.mock_lookupService_resolveLookup(lookupService);

		service.lookupAndPersist(DataContainerTestUtil.getDataContainer(DataLevel.INS), new LookupAttributeInput(),
				"ds1",null);

	}
	
	@Test
	public void testUpdateOnlyFeed() throws Exception {

		LookupService lookupService = new LookupServiceImpl();
		DataContainer inpContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);

		DomainType feedDs = new DomainType();
		String ds = "rdsp";
		feedDs.setVal(ds);

		DataValue<String> sourceUniqueId = new DataValue<>();
		sourceUniqueId.setValue(LockLevel.FEED, "sourceUniqueId");

		DataValue<String> isin = new DataValue<>();
		isin.setValue(LockLevel.FEED, "isin");

		DataValue<DomainType> domainVal = new DataValue<>();
		domainVal.setValue(LockLevel.FEED, new DomainType("rdsp",null,null));
		inpContainer.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), domainVal);
		
		inpContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(SdDataAttConstant.ISIN, DataLevel.INS), isin);

		inpContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueId);

		service.mergeAndPersistSd(ds, inpContainer, null,
				repo.findByDatasourceAndFileType(ds, "CONSOLIDATED_LE|rawReprocessing"));
		List<DataContainer> containersBySourceUniqueId = lookupService.getDbDataContainersBySourceUniqueId(inpContainer,
				feedDs);
		Assert.assertEquals(0, containersBySourceUniqueId.size());
	}
	
	@Test
	public void getExecutionOrderTest() {
	    Assert.assertEquals("RduSecurityTypePopulationService", dataContainerPostProcessors.get(0).getClass().getSimpleName());
	    Assert.assertEquals("FigiAdditionalXrfParameterTypePostProcessServiceImpl", dataContainerPostProcessors.get(1).getClass().getSimpleName());
	    Assert.assertEquals("RduTechnicalSecurityGenerationService", dataContainerPostProcessors.get(2).getClass().getSimpleName());
	}
	
}
