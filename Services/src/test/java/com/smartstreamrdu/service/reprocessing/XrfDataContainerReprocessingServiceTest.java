/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : XrfDataContainerReprocessingServiceTest.java
 * Author :SaJadhav
 * Date : 08-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant.CrossRefConstants;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class XrfDataContainerReprocessingServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private XrfDataContainerReprocessingService xrfDataContainerReprocessingService;
	
	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	private Message kafkaMessage;
	
	private List<Message> messages = new ArrayList<>();;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks() {
		kafkaMessage=null;
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				kafkaMessage = message;
				messages.add(message);
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}
	
	/*
	 * Here there will be 2 messages sent 1 for XrfQueue and another for FigiRequestQueue
	 * Here there will be an update in exchangeCodes domain which has eligibleForFigiRequest = true
	 * but it will be done from domainMaintanceData UUI page so it will  have columns from
	 * domainMaintanceData collection and changedDomainFields will be populated
	 */
	@Test
	public void testDomainAndColumnEligibleForFigiReqAndChangedDomainFieldsNotNull() throws UdmBaseException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath(
				"XrfDataContainerReprocessingServiceTest/input/sdData.json", SdData.class);
		List<String> changedDomainFields = Arrays.asList("mic","updDate");
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		LocalDateTime staticDataUpdateDate = LocalDateTime.of(2021, 06, 07, 12, 30);
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", changedDomainFields);
		assertEquals(2, messages.size());
		Message figiMsg = messages.get(0);
		Message xrfMsg = messages.get(1);
		assertNull(figiMsg.getKey());
		assertNotNull(figiMsg.getData());
		assertNotNull(xrfMsg.getKey());
		XrfMessage xrfMessage = JsonConverterUtil.convertFromJson(xrfMsg.getData(), XrfMessage.class);
		assertEquals(1, xrfMessage.getCrossRefChangeEntities().size());
	}
	
	/*
	 * Here there will be 2 messages sent 1 for XrfQueue and another for FigiRequestQueue
	 * Here there will be an update in exchangeCodes domain which has eligibleForFigiRequest = true
	 * but it will be done from DomainMap UUI page so it won't have any columns from
	 * domainMaintanceData collection and changedDomainFields will not be populated.
	 */
	@Test
	public void testDomainAndColumnEligibleForFigiReqAndChangedDomainFieldsAsNull() throws UdmBaseException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath(
				"XrfDataContainerReprocessingServiceTest/input/sdData.json", SdData.class);
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		LocalDateTime staticDataUpdateDate = LocalDateTime.of(2021, 06, 07, 12, 30);
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", null);
		assertEquals(2, messages.size());
		Message figiMsg = messages.get(0);
		Message xrfMsg = messages.get(1);
		assertNull(figiMsg.getKey());
		assertNotNull(figiMsg.getData());
		assertNotNull(xrfMsg.getKey());
		XrfMessage xrfMessage = JsonConverterUtil.convertFromJson(xrfMsg.getData(), XrfMessage.class);
		assertEquals(1, xrfMessage.getCrossRefChangeEntities().size());
	}
	
	/*
	 * Same as testDomainAndColumnEligibleForFigiReqAndNormalizedValueNull but in this case
	 * changedDomainFields will be empty list
	 */
	@Test
	public void testDomainAndColumnEligibleForFigiReqAndChangedDomainFieldsAsEmpty() throws UdmBaseException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath(
				"XrfDataContainerReprocessingServiceTest/input/sdData.json", SdData.class);
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		LocalDateTime staticDataUpdateDate = LocalDateTime.of(2021, 06, 07, 12, 30);
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", Collections.emptyList());
		assertEquals(2, messages.size());
		Message figiMsg = messages.get(0);
		Message xrfMsg = messages.get(1);
		assertNull(figiMsg.getKey());
		assertNotNull(figiMsg.getData());
		assertNotNull(xrfMsg.getKey());
		XrfMessage xrfMessage = JsonConverterUtil.convertFromJson(xrfMsg.getData(), XrfMessage.class);
		assertEquals(1, xrfMessage.getCrossRefChangeEntities().size());
	}
	
	/*
	 * Here there will be 1 message sent to  XrfQueue 
	 * Here there will be an update in exchangeCodes domain which has eligibleForFigiRequest = false
	 * but it will be done from DomainMap UUI page so it won't have any columns from
	 * domainMaintanceData collection.
	 */
	@Test
	public void testDomainNotElgibleForFigiReqAndChangedDomainFieldsNotNull() throws UdmBaseException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath(
				"XrfDataContainerReprocessingServiceTest/input/sdData.json", SdData.class);
		List<String> changedFields = Arrays.asList("mic","updDate");
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		LocalDateTime staticDataUpdateDate = LocalDateTime.of(2021, 06, 07, 12, 30);
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", changedFields);
		assertEquals(2, messages.size());
		Message figiMsg = messages.get(0);
		Message xrfMsg = messages.get(1);
		assertNull(figiMsg.getKey());
		assertNotNull(figiMsg.getData());
		assertNotNull(xrfMsg.getKey());
		XrfMessage xrfMessage = JsonConverterUtil.convertFromJson(xrfMsg.getData(), XrfMessage.class);
		assertEquals(1, xrfMessage.getCrossRefChangeEntities().size());
	}
	
	/*
	 * Here since the domain is not present in domainMetdata collection 
	 * hence no message would be sent
	 */
	@Test
	public void testReprocesDataContainerWhereDomainIsXrfAdditionalXrfParameterType() throws UdmBaseException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath(
				"XrfDataContainerReprocessingServiceTest/input/sdData.json", SdData.class);
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		LocalDateTime staticDataUpdateDate = LocalDateTime.of(2021, 06, 07, 12, 30);
		List<String> changedFields = Arrays.asList("name","updDate");
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, CrossRefConstants.XRF_ADD_PARAM, changedFields);
		assertEquals(0, messages.size());
		}
	
	
	/*
	 * Here there will be msg sent to only XrfQueue since reprocessing is true.
	 * Message won't be sent to FigiRequestQueue bcz eligibleForFigiRequest = false
	 */
	@Test
	public void testDataContainerWhereDomainNotEligibleButColumnEligibleForFigiReq() throws UdmBaseException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath(
				"XrfDataContainerReprocessingServiceTest/input/sdData.json", SdData.class);
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		LocalDateTime staticDataUpdateDate = LocalDateTime.of(2021, 06, 07, 12, 30);
		List<String> changedFields = Arrays.asList("name","updDate");
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "xrfAdditionalTypes", changedFields);
		assertEquals(1, messages.size());
		assertNotNull(kafkaMessage.getKey());
		XrfMessage xrfMessage = JsonConverterUtil.convertFromJson(kafkaMessage.getData(), XrfMessage.class);
		assertEquals(1, xrfMessage.getCrossRefChangeEntities().size());
	}	
	
	/*
	 * Here there will be msg sent to only XrfQueue since reprocessing is true.
	 * Message won't be sent to FigiRequestQueue bcz eligibleForFigiRequest = true
	 * but figiRequest = false
	 */
	@Test
	public void testDataContainerWhereDomainEligibleButColumnnNotEligibleForFigiReq() throws UdmBaseException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath(
				"XrfDataContainerReprocessingServiceTest/input/sdData.json", SdData.class);
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		LocalDateTime staticDataUpdateDate = LocalDateTime.of(2021, 06, 07, 12, 30);
		List<String> changedFields = Arrays.asList("name","updDate");
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", changedFields);
		assertEquals(1, messages.size());
		assertNotNull(kafkaMessage.getKey());
		XrfMessage xrfMessage = JsonConverterUtil.convertFromJson(kafkaMessage.getData(), XrfMessage.class);
		assertEquals(1, xrfMessage.getCrossRefChangeEntities().size());
	}
	
	
	@Test
	public void testReprocessDataContainer_LE() throws UdmBaseException {
		DataContainer dataContainer=new DataContainer(DataLevel.LE, DataContainerContext.builder().build());
		LocalDateTime staticDataUpdateDate=LocalDateTime.of(2021, 06, 07, 12, 30);
		List<String> changedFields = Arrays.asList("updDate");
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", changedFields);
		assertNull(kafkaMessage);
	}

	
	@Test
	public void testReprocessDataContainerLeWithReprocessingTrue() throws UdmBaseException {
		DataContainer dataContainer=new DataContainer(DataLevel.LE, DataContainerContext.builder().build());
		LocalDateTime staticDataUpdateDate=LocalDateTime.of(2021, 06, 07, 12, 30);
		List<String> changedFields = Arrays.asList("operatingExchangeCode");
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", changedFields);
		assertNull(kafkaMessage);
	}
	
	@Test
	public void testReprocessDataContainerInsWithReprocessingTrue() throws UdmBaseException {
		DataContainer dataContainer=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		LocalDateTime staticDataUpdateDate=LocalDateTime.of(2021, 06, 07, 12, 30);
		List<String> changedFields = Arrays.asList("operatingExchangeCode");
		xrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, "exchangeCodes", changedFields);
		assertNull(kafkaMessage);
	}
	
	@Test
	public void testNullXrfMsg() throws Exception {
		XrfMessage xrfMsg = null;
		DataContainer dataContainer=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		WhiteboxImpl.invokeMethod(xrfDataContainerReprocessingService,"sendXrfMessage",xrfMsg, dataContainer);
		assertNull(kafkaMessage);

	}

}
