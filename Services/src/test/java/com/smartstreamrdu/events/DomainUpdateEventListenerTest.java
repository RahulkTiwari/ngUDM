/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainUpdateEventListenerTest.java
 * Author : VRamani
 * Date : Jan 23, 2020
 * 
 */
package com.smartstreamrdu.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataAttributeFactoryFieldPojo;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.message.ReprocessingData;
import com.smartstreamrdu.domain.message.ReprocessingMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.AssetClassifications;
import com.smartstreamrdu.persistence.domain.CountryCodes;
import com.smartstreamrdu.persistence.domain.DvDomainMap;
import com.smartstreamrdu.service.events.DomainContainerChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.DomainUpdateEventListener;
import com.smartstreamrdu.service.events.MergeCompleteChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant.Component;
import com.smartstreamrdu.util.DataAttributeConstant;

/**
 * @author VRamani
 *
 */
@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class DomainUpdateEventListenerTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private DomainUpdateEventListener domainUpdateEventListener;
	
	@Autowired
	private BsonConverter bsonConverter;
		
	private Message reprocessingMessage;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks() {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				reprocessingMessage = message;
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}
	
	@Test
	public void testIsEventApplicable() {
		assertTrue(domainUpdateEventListener.isEventApplicable(ListenerEvent.DomainContainerUpdate));
	}
	
	@Test
	public void testNegativeIsEventApplicable() {
		assertFalse(domainUpdateEventListener.isEventApplicable(ListenerEvent.DomainContainerMerged));
	}
	
	@Test
	public void testCreateInput() throws UdmTechnicalException, IOException {
		List<DataContainer> dataContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"DomainUpdateEventListenerTest/testCreateInput/dvDomainMap.json", DvDomainMap.class);
		DomainContainerChangeEventListenerInputCreationContext context = new DomainContainerChangeEventListenerInputCreationContext();
		context.setDomainContainer(dataContainers.get(0));
		
		DataContainer inputContainer = domainUpdateEventListener.createInput(context);
		
		assertEquals(dataContainers.get(0), inputContainer);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNegativeCreateInput() throws UdmTechnicalException, IOException {
		DataContainer dataContainer = bsonConverter.getListOfDataContainersFromFilePath(
				"DomainUpdateEventListenerTest/testCreateInput/dvDomainMap.json", DvDomainMap.class).get(0);
		MergeCompleteChangeEventListenerInputCreationContext inputContext = new MergeCompleteChangeEventListenerInputCreationContext();
		inputContext.setDataContainer(dataContainer);
		domainUpdateEventListener.createInput(inputContext);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPropogateEvent_DVDomainMap() throws UdmTechnicalException, IOException {
		DataContainer dataContainer = bsonConverter.getListOfDataContainersFromFilePath(
				"DomainUpdateEventListenerTest/testPropogateEvent/dvDomainMap.json", DvDomainMap.class).get(0);
		LocalDateTime updDateTime = LocalDateTime.now();
		dataContainer.updateDataContainerContext(DataContainerContext.builder().withUpdateDateTime(updDateTime).build());
		domainUpdateEventListener.propogateEvent(dataContainer);
		DataValue<String> rduDomain = (DataValue<String>) dataContainer.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeConstant.RDU_DOMAIN, DataLevel.DV_DOMAIN_MAP));
		
		ReprocessingMessage message = JsonConverterUtil.convertFromJson(reprocessingMessage.getData(), ReprocessingMessage.class);
		
		assertNotNull(reprocessingMessage);
		ReprocessingMessage convertedMessage=JsonConverterUtil.convertFromJson(reprocessingMessage.getData(), ReprocessingMessage.class);
		assertNull(convertedMessage.getNormalizedValue());
		assertEquals(rduDomain.getValue(), message.getRduDomain());
		assertEquals(Component.DATA_ENRICHMENT, reprocessingMessage.getTarget());
		
		assertEquals(updDateTime, message.getUpdDate());
		assertEquals(2, message.getMapOfDomainSourceVsListOfReprocessingData().size());
	}
	
	@Test
	@InputCollectionsPath(paths={"DomainUpdateEventListenerTest/testPropogateEvent"})
	@ModifiedCollections(collections={"countryCodes"})
	public void testPropogateEvent_NonDvDomain_CountryCodes() throws UdmTechnicalException, IOException {
		DataContainer dataContainer = bsonConverter.getListOfDataContainersFromFilePath(
				"DomainUpdateEventListenerTest/testPropogateEvent/countryCodes.json", CountryCodes.class).get(0);
 		LocalDateTime updDateTime = LocalDateTime.now();
		dataContainer.updateDataContainerContext(DataContainerContext.builder().withUpdateDateTime(updDateTime).build());
		
		domainUpdateEventListener.propogateEvent(dataContainer);
		
		ReprocessingMessage message = JsonConverterUtil.convertFromJson(reprocessingMessage.getData(), ReprocessingMessage.class);
		
		assertNotNull(reprocessingMessage);
		assertEquals(Component.DATA_ENRICHMENT, reprocessingMessage.getTarget());
		ReprocessingMessage convertedMessage=JsonConverterUtil.convertFromJson(reprocessingMessage.getData(), ReprocessingMessage.class);
		assertEquals("AD", convertedMessage.getNormalizedValue());
		assertEquals(updDateTime, message.getUpdDate());
		assertEquals(1, message.getMapOfDomainSourceVsListOfReprocessingData().size());
	}
	
	/**
	 * In this case the rduDomain assetClassificationsTest has 
	 * figiRequest and reprocessign as false;
	 */
	@Test
	@InputCollectionsPath(paths={"DomainUpdateEventListenerTest/testPropogateEvent/assetClassifications.json"})
	@ModifiedCollections(collections={"assetClassifications"})
	public void testPropogateEventForDomainHasFigiAndReprocessingFalse() throws UdmTechnicalException, IOException     {
		
		DataContainer dataContainer = bsonConverter.
				getListOfDataContainersFromFilePath("DomainUpdateEventListenerTest/testPropogateEvent/assetClassifications.json", 
						AssetClassifications.class).get(0);
 		LocalDateTime updDateTime = LocalDateTime.now();
		dataContainer.updateDataContainerContext(DataContainerContext.builder().withUpdateDateTime(updDateTime).build());
		
		domainUpdateEventListener.propogateEvent(dataContainer);
		assertNull(reprocessingMessage);
	}
	
	@Test
	public void testDomainNotPresentInDomainMaintenance() throws Exception {
		
		String rduDomain = "testDomain";
		String attributeName = "code";
		Boolean isPresent = (Boolean) WhiteboxImpl.invokeMethod(domainUpdateEventListener, "isReprocessable", rduDomain,  attributeName);
		assertFalse(isPresent);
	}
	
	@Test
	public void testCheckStatusDvDomain() throws Exception {
		DataContainer dataContainer = bsonConverter.
				getListOfDataContainersFromFilePath("DomainUpdateEventListenerTest/testPropogateEvent/assetClassifications.json", 
						AssetClassifications.class).get(0);
		dataContainer.setLevel(DataLevel.DV_DOMAIN_MAP);
 		Boolean isPresent = (Boolean) WhiteboxImpl.invokeMethod(domainUpdateEventListener, "checkStatus",dataContainer);
		assertFalse(isPresent);
	}
	
	
	@Test
	public void testDomainPresentInDomainMaintenance() throws Exception {
		
		String rduDomain = "exchangeCodes";
		String attributeName = "code";
		Boolean isPresent = (Boolean) WhiteboxImpl.invokeMethod(domainUpdateEventListener, "isReprocessable", rduDomain,  attributeName);
		assertTrue(isPresent);

	}
	
	@Test
	public void testNoValueChangedForReprocessing() throws Exception {
		List<ReprocessingData> reprocessingDataList = new ArrayList<>();
		DataAttribute da = new DataAttribute(DataAttributeFactoryFieldPojo.builder().attributeName("status").build());
		DataRow row = new DataRow(da);
		WhiteboxImpl.invokeMethod(domainUpdateEventListener, "addToReprocessingDataList", false,  reprocessingDataList, row);
		boolean isEmpty = reprocessingDataList.isEmpty();
		assertTrue(isEmpty);
	}
	
	@Test
	public void testInactivationPropogateEvent() throws UdmTechnicalException, IOException {
		DataContainer dataContainer = bsonConverter.getListOfDataContainersFromFilePath(
				"DomainUpdateEventListenerTest/testInactivationPropogateEvent/assetClassifications.json",
				AssetClassifications.class).get(0);
		
		domainUpdateEventListener.propogateEvent(dataContainer);
		
		assertNull(reprocessingMessage);
	}
}
