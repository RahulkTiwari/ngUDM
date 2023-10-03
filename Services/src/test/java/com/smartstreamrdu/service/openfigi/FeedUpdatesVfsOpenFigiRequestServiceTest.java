/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: FeedUpdatesVfsOpenFigiRequestServiceTest.java
 * Author: AThanage
 * Date: Dec 01, 2021
 *
 *******************************************************************/
package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.OpenFigiContants;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author AThanage
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class FeedUpdatesVfsOpenFigiRequestServiceTest extends AbstractEmbeddedMongodbJunitParent{

	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private BsonConverter bsonConverter;

	@Autowired
	private FeedUpdatesVfsOpenFigiRequestService feedUpdatesVfsOpenFigiRequestService;

	private List<Message> listMessageForVfsRequestQueue = new ArrayList<>();

	private static final DataAttribute sedol = DataAttributeFactory.getAttributeByNameAndLevel(OpenFigiContants.SEDOL,
			DataLevel.SEC);
	private static final DataAttribute cusip = DataAttributeFactory.getAttributeByNameAndLevel(OpenFigiContants.CUSIP,
			DataLevel.INS);
	private static final DataAttribute instrumentId = DataAttributeFactory.getAttributeByNameAndLevel("_instrumentId",
			DataLevel.INS);
	private static final DataAttribute instrumentStatus = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus",
			DataLevel.INS);

	private static final DataAttribute securityId = DataAttributeFactory.getAttributeByNameAndLevel("_securityId",
			DataLevel.SEC);
	private static final DataAttribute dataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource",
			DataLevel.Document);
	private static final DataAttribute exchangeCode = DataAttributeFactory
			.getAttributeByNameAndLevel(OpenFigiContants.EX_CODE, DataLevel.SEC);

	private static final DataAttribute securityStatus = DataAttributeFactory
			.getAttributeByNameAndLevel(OpenFigiContants.SECURITY_STATUS, DataLevel.SEC);

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

	/**
	 * @throws java.lang.Exception
	 */
	public DataContainer setUp(String dataSourceVal) throws Exception {
		
		DataContainer secDataContainer = DataContainerTestUtil.getSecurityContainer();
		DataContainer insDataContainer = DataContainerTestUtil.getInstrumentContainer();
		populateSecurityContainer(secDataContainer, "ABC", "NYS", "a1b2c3");

		DataValue<String> cusipDataValue = new DataValue<String>();
		cusipDataValue.setValue(LockLevel.FEED, "XYZ");
		insDataContainer.addAttributeValue(cusip, cusipDataValue);

		DataValue<String> instrumentIdValue = new DataValue<String>();
		instrumentIdValue.setValue(LockLevel.FEED, "1a2b3c");
		insDataContainer.addAttributeValue(instrumentId, instrumentIdValue);
		
		DataValue<DomainType> insStatusValue = new DataValue<DomainType>();
		DomainType insStatusDomainType = new DomainType();
		insStatusDomainType.setVal("A");
		insStatusValue.setValue(LockLevel.FEED, insStatusDomainType);
		insDataContainer.addAttributeValue(instrumentStatus, insStatusValue);


		DataValue<DomainType> dataSourceValue = new DataValue<DomainType>();
		DomainType domainType1 = new DomainType();
		domainType1.setVal(dataSourceVal);
		dataSourceValue.setValue(LockLevel.FEED, domainType1);
		
		DataValue<DomainType> secStatusValue = new DataValue<DomainType>();
		DomainType secStatusDomainType = new DomainType();
		secStatusDomainType.setVal("A");
		secStatusValue.setValue(LockLevel.FEED, secStatusDomainType);
		secDataContainer.addAttributeValue(securityStatus, secStatusValue);

		insDataContainer.addAttributeValue(dataSource, dataSourceValue);
		insDataContainer.addDataContainer(secDataContainer, DataLevel.SEC);
		return insDataContainer;
	}

	private void populateSecurityContainer(DataContainer securityContainer, String sedolValue, String exchangeCodeValue,
			String securityIdValue) {
		DataValue<String> sedolDataValue = new DataValue<String>();
		sedolDataValue.setValue(LockLevel.FEED, sedolValue);
		securityContainer.addAttributeValue(sedol, sedolDataValue);

		DataValue<String> securityIdVal = new DataValue<String>();
		securityIdVal.setValue(LockLevel.FEED, securityIdValue);
		securityContainer.addAttributeValue(securityId, securityIdVal);

		DataValue<DomainType> exchangeCodeVal = new DataValue<DomainType>();
		DomainType exchangeCodeDomainType = new DomainType();
		exchangeCodeDomainType.setVal(exchangeCodeValue);
		exchangeCodeVal.setValue(LockLevel.FEED, exchangeCodeDomainType);
		securityContainer.addAttributeValue(exchangeCode, exchangeCodeVal);
		
		DataValue<DomainType> secStatusValue = new DataValue<DomainType>();
		DomainType secStatusDomainType = new DomainType();
		secStatusDomainType.setVal("A");
		secStatusValue.setValue(LockLevel.FEED, secStatusDomainType);
		securityContainer.addAttributeValue(securityStatus, secStatusValue);
	}

	/**
	 * Test method for
	 * {@link com.smartstreamrdu.service.openfigi.FeedUpdatesVfsOpenFigiRequestService#sendRequestToVfsOpenFigi(com.smartstreamrdu.domain.DataContainer)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testSendRequestToVfsOpenFigi() throws Exception {
		DataContainer insDataContainer = setUp("trdse");
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(insDataContainer);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(1, listMessageForVfsRequestQueue.size());
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(listMessageForVfsRequestQueue.get(0).getData(),
				VfsFigiRequestMessage.class);
		assertEquals("ABC", openFigiIdentifiers.getSedol());
		assertEquals(OpenFigiRequestRuleEnum.DEFAULT, openFigiIdentifiers.getOpenFigiRequestRuleEnum());
		assertEquals(-1, openFigiIdentifiers.getMicIndex());
		assertEquals("XYZ", openFigiIdentifiers.getCusip());
	}

	/**
	 * Test method for
	 * {@link com.smartstreamrdu.service.openfigi.FeedUpdatesVfsOpenFigiRequestService#sendRequestToVfsOpenFigi(com.smartstreamrdu.domain.DataContainer)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testSendRequestToVfsOpenFigi_OpenFigiDataSourceWithNullSecurityData() throws Exception {
		DataContainer insDataContainer = setUp("trdse");
		// Null child data container.
		insDataContainer.replaceChildDataContainersForLevel(null, DataLevel.SEC);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(insDataContainer);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public final void testSendRequestToVfsOpenFigi_OpenFigiDataSourceWithEmptySecurityData() throws Exception {
		DataContainer insDataContainer = setUp("trdse");
		// Null child data container.
		insDataContainer.replaceChildDataContainersForLevel(Collections.emptyList(), DataLevel.SEC);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(insDataContainer);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testSendRequestToVfsOpenFigi_asbIsinDataSource() throws Exception {
		DataContainer insDataContainer = setUp("asbIsin");
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(insDataContainer);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testSendRequestToVfsOpenFigi_nonXrfValueChange() throws Exception {
		DataContainer insDataContainer = setUp("trdse");
		DataContainerUtil.populateNewFlagAndHasChanged(List.of(insDataContainer));
		DataValue<String> nameLongValue=new DataValue<>();
		nameLongValue.setValue(LockLevel.FEED, "testNameLong");
		insDataContainer.addAttributeValue(InstrumentAttrConstant.NAME_LONG, nameLongValue);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(insDataContainer);
		assertEquals(1, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testSendRequestToVfsOpenFigi_inactiveSecurity() throws Exception {
		DataContainer insDataContainer = setUp("trdse");
		DataContainerUtil.populateNewFlagAndHasChanged(List.of(insDataContainer));
		
		DataContainer secDataContainer = insDataContainer.getAllChildDataContainers().get(0);
		DataValue<DomainType> secStatusValue=new DataValue<>();
		secStatusValue.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.INACTIVE));
		secDataContainer.addAttributeValue(SecurityAttrConstant.SECURITY_STATUS, secStatusValue);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(insDataContainer);
		assertEquals(1, listMessageForVfsRequestQueue.size());
	}
	
	/**
	 * Test method for
	 * {@link com.smartstreamrdu.service.openfigi.FeedUpdatesVfsOpenFigiRequestService#sendRequestToVfsOpenFigi(com.smartstreamrdu.domain.DataContainer)}.
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public final void testSendRequestToVfsOpenFigi_NullDataContainer() throws Exception {
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(null);
	}

	/**
	 * Test method for
	 * {@link com.smartstreamrdu.service.openfigi.FeedUpdatesVfsOpenFigiRequestService#sendRequestToVfsOpenFigi(com.smartstreamrdu.domain.DataContainer)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testSendRequestToVfsOpenFigi_noAttributeChange() throws Exception {
		DataContainer insDataContainer = setUp("trdse");
		DataContainerUtil.populateNewFlagAndHasChanged(List.of(insDataContainer));
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(insDataContainer);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testPropogateEvent_multiListedInstrument() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiChangeEventListenerTest/input/sdData.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		DataContainer existingSdContainer=existingSdDocuments.get(0);
		
		DataContainer newSecContainer = DataContainerTestUtil.getSecurityContainer();
		existingSdContainer.addDataContainer(newSecContainer, DataLevel.SEC);
		populateSecurityContainer(newSecContainer, "ABC.XYZ", "NYQ", "2");
		
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertNotNull(listMessageForVfsRequestQueue);
		assertEquals(1, listMessageForVfsRequestQueue.size());
		VfsFigiRequestMessage openFigiIdentifiers=JsonConverterUtil.convertFromJson(listMessageForVfsRequestQueue.get(0).getData(), VfsFigiRequestMessage.class);
		assertEquals("ABC.XYZ", openFigiIdentifiers.getSedol());
		assertEquals(OpenFigiRequestRuleEnum.DEFAULT, openFigiIdentifiers.getOpenFigiRequestRuleEnum());
		assertEquals(-1, openFigiIdentifiers.getMicIndex());
	}
	
	@Test
	public void testPropogateEventUpdateIsin() throws UdmTechnicalException, IOException {
		String newIsin = "IN1234567891";
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		DataContainer existingSdContainer = existingSdDocuments.get(0);

		DataValue<String> updateIsin = new DataValue<String>();
		updateIsin.setValue(LockLevel.FEED, newIsin);
		existingSdContainer.addAttributeValue(InstrumentAttrConstant.ISIN, updateIsin);
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(existingSdContainer);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);;
		assertEquals(1, listMessageForVfsRequestQueue.size());
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(listMessageForVfsRequestQueue.get(0).getData(),
				VfsFigiRequestMessage.class);
		assertEquals(newIsin, openFigiIdentifiers.getIsin());
		assertEquals(OpenFigiRequestRuleEnum.DEFAULT, openFigiIdentifiers.getOpenFigiRequestRuleEnum());
		assertEquals(-1, openFigiIdentifiers.getMicIndex());
	}

	@Test
	public void testPropogateEventUpdateSedol() throws UdmTechnicalException, IOException {
		String newSedol = "BDC82D1";
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		DataContainer existingSdContainer = existingSdDocuments.get(0);

		DataValue<String> sedolDataValue = new DataValue<String>();
		sedolDataValue.setValue(LockLevel.RDU, newSedol);
		DataContainer dataContainerCild = existingSdContainer.getAllChildDataContainers().get(0);
		dataContainerCild.addAttributeValue(sedol, sedolDataValue);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertEquals(1, listMessageForVfsRequestQueue.size());
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(listMessageForVfsRequestQueue.get(0).getData(),
				VfsFigiRequestMessage.class);
		assertEquals(newSedol, openFigiIdentifiers.getSedol());
		assertEquals(OpenFigiRequestRuleEnum.DEFAULT, openFigiIdentifiers.getOpenFigiRequestRuleEnum());
		assertEquals(-1, openFigiIdentifiers.getMicIndex());
	}

	@Test
	public void testPropogateEventUpdateSedolMultiListed() throws UdmTechnicalException, IOException {
		String newSedol = "CED92C2";
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData.json", SdData.class);
		DataContainer existingSdContainer = existingSdDocuments.get(0);

		DataContainer newSecContainer = DataContainerTestUtil.getSecurityContainer();
		existingSdContainer.addDataContainer(newSecContainer, DataLevel.SEC);
		populateSecurityContainer(newSecContainer, "ABC.XYZ", "NYQ", "2");
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);

		DataValue<String> sedolDataValue = new DataValue<String>();
		sedolDataValue.setValue(LockLevel.FEED, newSedol);
		DataContainer dataContainerCild = existingSdContainer.getAllChildDataContainers().get(1);
		dataContainerCild.addAttributeValue(sedol, sedolDataValue);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);;
		assertEquals(1, listMessageForVfsRequestQueue.size());
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(listMessageForVfsRequestQueue.get(0).getData(),
				VfsFigiRequestMessage.class);
		assertEquals(newSedol, openFigiIdentifiers.getSedol());
		assertEquals(OpenFigiRequestRuleEnum.DEFAULT, openFigiIdentifiers.getOpenFigiRequestRuleEnum());
		assertEquals(-1, openFigiIdentifiers.getMicIndex());
	}

	@Test
	public void testPropogateEventUpdateExchangeCode() throws UdmTechnicalException, IOException {
		String newExCode = "XNYA";
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		DataContainer existingSdContainer = existingSdDocuments.get(0);

		DataValue<DomainType> exchangeCodeVal = new DataValue<DomainType>();
		DomainType exchangeCodeDomainType = new DomainType();
		exchangeCodeDomainType.setVal(newExCode);
		exchangeCodeVal.setValue(LockLevel.FEED, exchangeCodeDomainType);
		DataContainer dataContainerCild = existingSdContainer.getAllChildDataContainers().get(0);
		dataContainerCild.addAttributeValue(exchangeCode, exchangeCodeVal);
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(existingSdContainer);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertEquals(1, listMessageForVfsRequestQueue.size());
		VfsFigiRequestMessage openFigiIdentifiers = JsonConverterUtil.convertFromJson(listMessageForVfsRequestQueue.get(0).getData(),
				VfsFigiRequestMessage.class);
		assertEquals(OpenFigiRequestRuleEnum.DEFAULT, openFigiIdentifiers.getOpenFigiRequestRuleEnum());
		assertEquals(-1, openFigiIdentifiers.getMicIndex());
	}
	
	@Test
	public void testPropogateEventNoUpdate() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		DataContainer existingSdContainer = existingSdDocuments.get(0);
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(existingSdContainer);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertEquals(0, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testPropogateEvent_UiReprocessing() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		
		DataContainer existingSdContainer = existingSdDocuments.get(0);
		existingSdContainer.updateDataContainerContext(DataContainerContext.builder().reprocessingFromUI(true).build());
		
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(existingSdContainer);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertEquals(1, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testPropogateEvent_update_ric_roundLotSize_multiListed() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData_multilisted.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		
		DataContainer existingSdContainer = existingSdDocuments.get(0);
		
		DataContainer childCOntainer1 = existingSdContainer.getAllChildDataContainers().get(0);
		
		DataValue<String> ricVal=new DataValue<>();
		ricVal.setValue(LockLevel.FEED, "testRic");
		childCOntainer1.addAttributeValue(SecurityAttrConstant.RIC, ricVal);
		
		DataValue<Long> roundLotSizeVal=new DataValue<>();
		roundLotSizeVal.setValue(LockLevel.FEED, 10l);
		childCOntainer1.addAttributeValue(SecurityAttrConstant.ROUND_LOT_SIZE, roundLotSizeVal);
		
		DataContainer childContainer2 = existingSdContainer.getAllChildDataContainers().get(1);
		DataValue<String> trConsolidatedRicVal=new DataValue<>();
		trConsolidatedRicVal.setValue(LockLevel.FEED, "testRic");
		childContainer2.addAttributeValue(SecurityAttrConstant.TR_CONSOLIDATED_RIC, trConsolidatedRicVal);
		
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(existingSdContainer);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertEquals(1, listMessageForVfsRequestQueue.size());
	}
	
	@Test
	public void testPropogateEvent_update_ticker_exchangeTicker_tradingSymbol() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData_multilisted.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		
		DataContainer existingSdContainer = existingSdDocuments.get(0);
		
		DataContainer childCOntainer = existingSdContainer.getAllChildDataContainers().get(0);
		
		DataValue<String> tickerVal=new DataValue<>();
		tickerVal.setValue(LockLevel.FEED, "testTicker");
		childCOntainer.addAttributeValue(SecurityAttrConstant.TICKER, tickerVal);
		
		DataValue<String> exchangeTickerVal=new DataValue<>();
		exchangeTickerVal.setValue(LockLevel.FEED, "tesrExTicker");
		childCOntainer.addAttributeValue(SecurityAttrConstant.EXCHANGE_TICKER, exchangeTickerVal);
		
		DataValue<String> tradingSymbolVal=new DataValue<>();
		tradingSymbolVal.setValue(LockLevel.FEED, "testTradingSymbol");
		childCOntainer.addAttributeValue(SecurityAttrConstant.TRADING_SYMBOL, tradingSymbolVal);
		
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(existingSdContainer);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertNotNull(listMessageForVfsRequestQueue);
	}
	
	@Test
	public void testPropogateEvent_update_nameLong() throws UdmTechnicalException, IOException {
		List<DataContainer> existingSdDocuments = bsonConverter.getListOfDataContainersFromFilePath(
				"VfsOpenFigiChangeEventListenerTest/input/sdData_multilisted.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(existingSdDocuments);
		
		DataContainer existingSdContainer = existingSdDocuments.get(0);
		
		DataValue<String> nameLongVal=new DataValue<>();
		nameLongVal.setValue(LockLevel.FEED, "testNameLong");
		existingSdContainer.addAttributeValue(InstrumentAttrConstant.NAME_LONG, nameLongVal);
		
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(existingSdContainer);
		feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(existingSdContainer);
		assertEquals(2, listMessageForVfsRequestQueue.size());
	}
}
