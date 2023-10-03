/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainMergeAndPersistServiceTest.java
 * Author : SaJadhav
 * Date : 22-Aug-2019
 * 
 */
package com.smartstreamrdu.service.persist;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.cache.CacheException;

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
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.DataContainerCacheStore;
import com.smartstreamrdu.persistence.mock.config.DataRetrievalServiceMockConfig;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.service.events.DomainUpdateEventListener;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author SaJadhav
 *
 */

@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MergeAndPersisteServiceConfig.class, DataRetrievalServiceMockConfig.class} )
public class DomainMergeAndPersistServiceTest extends AbstractEmbeddedMongodbJunitParent {
	@Autowired
	private DomainMergeAndPersistService mergeAndPersistService;

	@Autowired
	private DataContainerCacheStore dataContainerCacheStore;

	@Autowired
	private DataRetrievalService retrievalService;
	
	@Autowired
	private DomainUpdateEventListener domainUpdateEventListener;

	@Test
	public void test_merge() throws DataContainerMergeException{

		LocalDateTime updDate=LocalDateTime.now();
		DataContainer dataContainer=getUAECurrencyContainer("saJadhav",updDate);
		DataContainer dbDataContainer=getUAECurrencyDBContainer();
		Mockito.doNothing().when(domainUpdateEventListener).propogateEvent(Mockito.any());
		Mockito.doNothing().when(dataContainerCacheStore).updateCacheEntry(Mockito.any());
		mergeAndPersistService.merge(dataContainer, dbDataContainer);

		assertEquals("UAE DRHM", dbDataContainer.getHighestPriorityValue(DataAttributeFactory.getAttributeByNameAndLevel("name", DataLevel.CURRENCY_CODES)));
		assertEquals("saJadhav", dbDataContainer.getHighestPriorityValue(DataAttributeFactory.getAttributeByNameAndLevel("updUser", DataLevel.CURRENCY_CODES)));
		assertEquals(updDate, dbDataContainer.getHighestPriorityValue(DataAttributeFactory.getAttributeByNameAndLevel("updDate", DataLevel.CURRENCY_CODES)));
	}


	@Test(expected = CacheException.class)
	public void testMrgeAndretryCacheUpdate() throws Exception {
		Mockito.doThrow(CacheException.class).when(dataContainerCacheStore).updateCacheEntry(Mockito.any());

		DataContainer dbDataContainer=getUAECurrencyDBContainer();

		List<DataContainer> dataContainers = new ArrayList<>();
		dataContainers.add(dbDataContainer);
		Mockito.when(retrievalService.retrieve(Mockito.any(), Mockito.any())).thenReturn(dataContainers);
		
		LocalDateTime updDate=LocalDateTime.now();
		DataContainer dataContainer=getUAECurrencyContainer("saJadhav",updDate);

		mergeAndPersistService.merge(dataContainer, dbDataContainer);
	}


	/**
	 * @return
	 */
	private DataContainer getUAECurrencyDBContainer() {
		DataAttribute code =DataAttributeFactory.getAttributeByNameAndLevel("code", DataLevel.CURRENCY_CODES);
		DataAttribute name =DataAttributeFactory.getAttributeByNameAndLevel("name", DataLevel.CURRENCY_CODES);
		DataAttribute flagActive = DataAttributeFactory.getAttributeByNameAndLevel("status", DataLevel.CURRENCY_CODES);
		DataValue<String> codeVal = new DataValue<String>();
		codeVal.setValue(LockLevel.RDU, "AED");
		DataValue<String> nameVal = new DataValue<String>();
		nameVal.setValue(LockLevel.RDU, "UAE Dirham");
		DataContainer dataContainer = new DataContainer(DataLevel.CURRENCY_CODES,null);
		dataContainer.addAttributeValue(code, codeVal);
		dataContainer.addAttributeValue(name, nameVal);
		dataContainer.set_id("5e1ec1c64d5d17af719b2994");
		DataValue<String> statusActiveVal=new DataValue<>();
		statusActiveVal.setValue(LockLevel.RDU, "A");
		dataContainer.addAttributeValue(flagActive, statusActiveVal);
		DataContainerUtil.populateNewFlagAndHasChanged(Arrays.asList(new DataContainer[]{dataContainer}));
		return dataContainer;
	}

	private DataContainer getUAECurrencyContainer(String userName, LocalDateTime updDate) {
		DataAttribute code = DataAttributeFactory.getAttributeByNameAndLevel("code", DataLevel.CURRENCY_CODES);
		DataAttribute name = DataAttributeFactory.getAttributeByNameAndLevel("name", DataLevel.CURRENCY_CODES);
		DataAttribute flagActive = DataAttributeFactory.getAttributeByNameAndLevel("status", DataLevel.CURRENCY_CODES);
		DataValue<String> codeVal = new DataValue<String>();
		codeVal.setValue(LockLevel.RDU, "AED");
		DataValue<String> nameVal = new DataValue<String>();
		nameVal.setValue(LockLevel.RDU, "UAE DRHM");
		DataContainer dataContainer = new DataContainer(DataLevel.CURRENCY_CODES,
				DataContainerContext.builder().withUpdateBy(userName).withUpdateDateTime(updDate).build());
		dataContainer.addAttributeValue(code, codeVal);
		dataContainer.addAttributeValue(name, nameVal);
		dataContainer.setLevel(DataLevel.CURRENCY_CODES);
		dataContainer.set_id("5e1ec1c64d5d17af719b2994");
		DataValue<String> statusActiveVal = new DataValue<>();
		statusActiveVal.setValue(LockLevel.RDU, "A");
		dataContainer.addAttributeValue(flagActive, statusActiveVal);
		return dataContainer;
	}
}
