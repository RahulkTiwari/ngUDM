/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : IvoUpdatesVfsOpenFigiRequestServiceTest.java
 * Author :SaJadhav
 * Date : 02-Dec-2021
 */
package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdIvo;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoInstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoSecurityAttrConstant;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.service.spark.SparkUtil;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ProducerFactoryMockConfig.class)
public class IvoUpdatesVfsOpenFigiRequestServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private IvoUpdatesVfsOpenFigiRequestService service;
	
	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	private List<Message> listMessageForVfsRequestQueue=new ArrayList<>();
	
	@Autowired
	private SparkUtil sparkUtil;
	
	/**
	 * @throws java.lang.Exception
	 */
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks() {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				listMessageForVfsRequestQueue.add(message);
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}
	
	@Test(expected = NullPointerException.class)
	public void testSendRequestToVfsOpenFigi_nullValue() {
		service.sendRequestToVfsOpenFigi(null);
	}
	
	@Test
	@InputCollectionsPath(paths = {"VfsOpenFigiChangeEventListenerTest/ivo"})
	@ModifiedCollections(collections = {"sdData","xrData","sdIvo"})
	public void testPropogateEvent_nameLong_IVOLock() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdIvoDocuments = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiChangeEventListenerTest/ivo/sdIvo.json", SdIvo.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdIvoDocuments);
		
		DataContainer existingIvoContainer=existingSdIvoDocuments.get(0);
		DataValue<String> nameLongVal=new DataValue<>();
		nameLongVal.setValue(LockLevel.RDU, "testNameLong");
		existingIvoContainer.addAttributeValue(IvoInstrumentAttrConstant.NAME_LONG, nameLongVal);
		service.sendRequestToVfsOpenFigi(existingIvoContainer);
		assertEquals(6, listMessageForVfsRequestQueue.size());
		Message firstMessage = listMessageForVfsRequestQueue.get(0);
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(firstMessage.getData(), VfsFigiRequestMessage.class);
		assertEquals("AU0000001869", openFigiIdentifiers.getIsin());
//		assertEquals("5101", openFigiIdentifiers.getAssetType());
	}
	
	@Test
	@InputCollectionsPath(paths = {"VfsOpenFigiChangeEventListenerTest/ivo"})
	@ModifiedCollections(collections = {"sdData","xrData","sdIvo"})
	public void testPropogateEvent_ticker_IVOLockAtSecurityLevel() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdIvoDocuments = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiChangeEventListenerTest/ivo/sdIvo.json", SdIvo.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdIvoDocuments);
		
		DataContainer existingIvoContainer=existingSdIvoDocuments.get(0);
		List<DataContainer> childDataContainers = existingIvoContainer.getChildDataContainers(DataLevel.IVO_SEC);
		DataContainer childDataContainer = childDataContainers.get(0);
		DataValue<String> tickerVal=new DataValue<>();
		tickerVal.setValue(LockLevel.RDU, "testTicker");
		childDataContainer.addAttributeValue(IvoSecurityAttrConstant.TICKER, tickerVal);
		
		service.sendRequestToVfsOpenFigi(existingIvoContainer);
		assertEquals(1, listMessageForVfsRequestQueue.size());
		Message firstMessage = listMessageForVfsRequestQueue.get(0);
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(firstMessage.getData(), VfsFigiRequestMessage.class);
		assertEquals("AU0000001869", openFigiIdentifiers.getIsin());
//		assertEquals("5101", openFigiIdentifiers.getAssetType());
	}
	
	@Test
	@InputCollectionsPath(paths = {"VfsOpenFigiChangeEventListenerTest/ivo"})
	@ModifiedCollections(collections = {"sdData","xrData","sdIvo"})
	public void testPropogateEvent_IVOLockAtTwoSecuries() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdIvoDocuments = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiChangeEventListenerTest/ivo/sdIvo.json", SdIvo.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdIvoDocuments);
		
		DataContainer existingIvoContainer=existingSdIvoDocuments.get(0);
		List<DataContainer> childDataContainers = existingIvoContainer.getChildDataContainers(DataLevel.IVO_SEC);
		DataContainer childDataContainer1 = childDataContainers.get(0);
		DataValue<String> tradingSymbolVal=new DataValue<>();
		tradingSymbolVal.setValue(LockLevel.RDU, "testTradingSymbol");
		childDataContainer1.addAttributeValue(IvoSecurityAttrConstant.TRADING_SYMBOL, tradingSymbolVal);
		
		DataContainer childContainer2 = childDataContainers.get(1);
		
		DataValue<String> exchangeTickerVal=new DataValue<>();
		exchangeTickerVal.setValue(LockLevel.RDU, "testExchangeTicker");
		childContainer2.addAttributeValue(IvoSecurityAttrConstant.EXCHANGE_TICKER, exchangeTickerVal);
		
		service.sendRequestToVfsOpenFigi(existingIvoContainer);
		assertEquals(3, listMessageForVfsRequestQueue.size());
		Message firstMessage = listMessageForVfsRequestQueue.get(0);
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(firstMessage.getData(), VfsFigiRequestMessage.class);
		assertEquals("AU0000001869", openFigiIdentifiers.getIsin());
//		assertEquals("5101", openFigiIdentifiers.getAssetType());
	}
	
	@Test
	@InputCollectionsPath(paths = {"VfsOpenFigiChangeEventListenerTest/ivo/sdIvo.json"})
	@ModifiedCollections(collections = {"sdIvo"})
	public void testPropogateEvent_noXrfDataCOntainer() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdIvoDocuments = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiChangeEventListenerTest/ivo/sdIvo.json", SdIvo.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdIvoDocuments);
		
		DataContainer existingIvoContainer=existingSdIvoDocuments.get(0);
		List<DataContainer> childDataContainers = existingIvoContainer.getChildDataContainers(DataLevel.IVO_SEC);
		DataContainer childDataContainer1 = childDataContainers.get(0);
		DataValue<String> tradingSymbolVal=new DataValue<>();
		tradingSymbolVal.setValue(LockLevel.RDU, "testTradingSymbol");
		childDataContainer1.addAttributeValue(IvoSecurityAttrConstant.TRADING_SYMBOL, tradingSymbolVal);
		
		DataContainer childContainer2 = childDataContainers.get(1);
		
		DataValue<String> exchangeTickerVal=new DataValue<>();
		exchangeTickerVal.setValue(LockLevel.RDU, "testExchangeTicker");
		childContainer2.addAttributeValue(IvoSecurityAttrConstant.EXCHANGE_TICKER, exchangeTickerVal);
		
		service.sendRequestToVfsOpenFigi(existingIvoContainer);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testPropogateEvent_IVOLockAtAusInstrumentId() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdIvoDocuments = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiChangeEventListenerTest/ivo/sdIvo.json", SdIvo.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdIvoDocuments);
		
		DataContainer existingIvoContainer=existingSdIvoDocuments.get(0);
		DataValue<String> ausInsIdVal=new DataValue<>();
		ausInsIdVal.setValue(LockLevel.RDU, "testAusId");
		existingIvoContainer.addAttributeValue(IvoInstrumentAttrConstant.AUSTRALIA_INSTRUMENT_ID, ausInsIdVal);
		service.sendRequestToVfsOpenFigi(existingIvoContainer);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@After
	public void clearSession() {
		listMessageForVfsRequestQueue.clear();
		SparkSession session = sparkUtil.getSparkContext();
		if(!session.sparkContext().isStopped()){
			session.close();
		}
	}

}
