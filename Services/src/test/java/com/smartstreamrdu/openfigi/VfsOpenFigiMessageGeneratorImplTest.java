/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: VfsOpenFigiMessageGeneratorImplTest.java
 * Author: Rushikesh Dedhia
 * Date: Jul 10, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.openfigi;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestOrderTypeEnum;
import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.OpenFigiRequestMetrics;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.SpringMongoConversionService;
import com.smartstreamrdu.service.openfigi.VfsOpenFigiMessageGenerator;

/**
 * @author Dedhia
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class VfsOpenFigiMessageGeneratorImplTest extends AbstractEmbeddedMongodbJunitParent {
	
	private DataContainer secDataContainer;
	private DataContainer insDataContainer;
	
	@Autowired
	private VfsOpenFigiMessageGenerator vfsOpenFigiMessaageGenerator;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Autowired
	private SpringMongoConversionService conversionService;
	
	
	/**
	 * Test method for {@link com.smartstreamrdu.service.openfigi.VfsOpenFigiMessageGeneratorImpl#generateVfsOpenFigiMessaage(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)}.
	 * @throws IOException 
	 * @throws UdmTechnicalException 
	 */
	@Test
	public void testGenerateVfsOpenFigiMessage_secStatusActive() throws UdmTechnicalException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiMessageGeneratorImplTest/input/sdData_idcApex_active.json", SdData.class);
		insDataContainer=listOfDataContainersFromFilePath.get(0);
		secDataContainer = insDataContainer.getAllChildDataContainers().get(0);
		VfsFigiRequestMessage vfsRequestMessage = vfsOpenFigiMessaageGenerator.generateVfsOpenFigiMessaage(secDataContainer, insDataContainer, VfsFigiRequestTypeEnum.NEW_OR_UPDATE);
		assertEquals("idcApex", vfsRequestMessage.getDataSource());
		assertEquals("6409c8537a05c55f08668912", vfsRequestMessage.getPrimarySourceDocId());
		assertEquals("1", vfsRequestMessage.getPrimarySourceSecurityId());
		assertEquals("A", vfsRequestMessage.getSecurityStatus());
		assertEquals("DN0455281065", vfsRequestMessage.getIsin());
		assertEquals("BK1WN11",vfsRequestMessage.getSedol());
		assertEquals(VfsFigiRequestTypeEnum.NEW_OR_UPDATE,vfsRequestMessage.getRequestType());
	}
	
	@Test
	public void testGenerateVfsOpenFigiMessage_secStatusInactive() throws UdmTechnicalException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiMessageGeneratorImplTest/input/sdData_idcApex_inactive.json", SdData.class);
		insDataContainer=listOfDataContainersFromFilePath.get(0);
		secDataContainer = insDataContainer.getAllChildDataContainers().get(0);
		VfsFigiRequestMessage vfsRequestMessage = vfsOpenFigiMessaageGenerator.generateVfsOpenFigiMessaage(secDataContainer, insDataContainer, VfsFigiRequestTypeEnum.NEW_OR_UPDATE);
		assertEquals("idcApex", vfsRequestMessage.getDataSource());
		assertEquals("6409c8537a05c55f08668912", vfsRequestMessage.getPrimarySourceDocId());
		assertEquals("1", vfsRequestMessage.getPrimarySourceSecurityId());
		assertEquals("I", vfsRequestMessage.getSecurityStatus());
		assertEquals("DN0455281065", vfsRequestMessage.getIsin());
		assertEquals("BK1WN11",vfsRequestMessage.getSedol());
		assertEquals(VfsFigiRequestTypeEnum.NEW_OR_UPDATE,vfsRequestMessage.getRequestType());
	}
	
	@Test
	public void testGenerateVfsOpenFigiMessage_withLocks() throws UdmTechnicalException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiMessageGeneratorImplTest/input/sdData_idcApex_activeWithLocks.json", SdData.class);
		insDataContainer=listOfDataContainersFromFilePath.get(0);
		secDataContainer = insDataContainer.getAllChildDataContainers().get(0);
		VfsFigiRequestMessage vfsRequestMessage = vfsOpenFigiMessaageGenerator.generateVfsOpenFigiMessaage(secDataContainer, insDataContainer, VfsFigiRequestTypeEnum.NEW_OR_UPDATE);
		assertEquals("idcApex", vfsRequestMessage.getDataSource());
		assertEquals("6409c8537a05c55f08668912", vfsRequestMessage.getPrimarySourceDocId());
		assertEquals("1", vfsRequestMessage.getPrimarySourceSecurityId());
		assertEquals("A", vfsRequestMessage.getSecurityStatus());
		assertEquals("DN0455281065", vfsRequestMessage.getIsin());
		assertEquals("ABC.XYZ", vfsRequestMessage.getSedol());
		assertEquals(VfsFigiRequestTypeEnum.NEW_OR_UPDATE,vfsRequestMessage.getRequestType());
	}
	
	@Test
	public void testGenerateVfsOpenFigiMessage_statusOutsideDomain() throws UdmTechnicalException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter.getListOfDataContainersFromFilePath("VfsOpenFigiMessageGeneratorImplTest/input/sdData_idcApex_active.json", SdData.class);
		insDataContainer=listOfDataContainersFromFilePath.get(0);
		secDataContainer = insDataContainer.getAllChildDataContainers().get(0);
		DataValue<DomainType> secDataValue=new DataValue<>();
		secDataValue.setValue(LockLevel.FEED, new DomainType("randomValue", null, null, "randomDomain"));
		secDataContainer.addAttributeValue(SecurityAttrConstant.SECURITY_STATUS, secDataValue);
		
		VfsFigiRequestMessage vfsRequestMessage = vfsOpenFigiMessaageGenerator.generateVfsOpenFigiMessaage(secDataContainer, insDataContainer, VfsFigiRequestTypeEnum.NEW_OR_UPDATE);
		
		assertEquals("idcApex", vfsRequestMessage.getDataSource());
		assertEquals("6409c8537a05c55f08668912", vfsRequestMessage.getPrimarySourceDocId());
		assertEquals("1", vfsRequestMessage.getPrimarySourceSecurityId());
		assertEquals("A", vfsRequestMessage.getSecurityStatus());
		assertEquals("DN0455281065", vfsRequestMessage.getIsin());
		assertEquals("BK1WN11",vfsRequestMessage.getSedol());
		assertEquals(VfsFigiRequestTypeEnum.NEW_OR_UPDATE,vfsRequestMessage.getRequestType());
	}
	
	@Test
	public void testGenerateVfsOpenFigiMessageFromRequestMetrics() throws IOException {
		List<Document> listOfDocumentsFromFilePath = bsonConverter.getListOfDocumentsFromFilePath("VfsOpenFigiMessageGeneratorImplTest/input/vfsProcessedRequest.json");
		OpenFigiRequestMetrics metrics = conversionService.convertToSerializable(listOfDocumentsFromFilePath.get(0), OpenFigiRequestMetrics.class);
		VfsFigiRequestMessage message = vfsOpenFigiMessaageGenerator.generateVfsOpenFigiMessageFromRequestMetrics(metrics, VfsFigiRequestTypeEnum.PRIMARY_SOURCE_INACTIVATION, 1);
		
		assertEquals("idcApex", message.getDataSource());
		assertEquals("6409c8537a05c55f08668912", message.getPrimarySourceDocId());
		assertEquals("1", message.getPrimarySourceSecurityId());
		assertEquals(OpenFigiRequestOrderTypeEnum.DEFAULT_ISIN_BASED, message.getOpenFigiRequestOrderType());
		assertEquals(OpenFigiRequestRuleEnum.ISIN_CURR_MIC, message.getOpenFigiRequestRuleEnum());
		assertEquals(VfsFigiRequestTypeEnum.PRIMARY_SOURCE_INACTIVATION, message.getRequestType());
		assertEquals(1, message.getRetryCount());
		assertEquals("DN0455281065",message.getIsin());
		assertEquals(null,message.getCusip());
		assertEquals("BK1WN11",message.getSedol());
		assertEquals("XNYS",message.getExchangeCode());
		assertEquals("USD", message.getTradeCurrencyCode());
		
	}
	
	@Test
	public void testGenerateVfsOpenFigiMessageFromRequestMetrics_onlyISINBASED() throws IOException {
		List<Document> listOfDocumentsFromFilePath = bsonConverter.getListOfDocumentsFromFilePath("VfsOpenFigiMessageGeneratorImplTest/input/vfsProcessedRequest.json");
		OpenFigiRequestMetrics metrics = conversionService.convertToSerializable(listOfDocumentsFromFilePath.get(0), OpenFigiRequestMetrics.class);
		
		metrics.setSuccessfulRequest("{\"idType\":\"ID_ISIN\",\"idValue\":\"DN0455281065\"}");
		VfsFigiRequestMessage message = vfsOpenFigiMessaageGenerator.generateVfsOpenFigiMessageFromRequestMetrics(metrics, VfsFigiRequestTypeEnum.PRIMARY_SOURCE_INACTIVATION, 1);
		
		assertEquals("idcApex", message.getDataSource());
		assertEquals("6409c8537a05c55f08668912", message.getPrimarySourceDocId());
		assertEquals("1", message.getPrimarySourceSecurityId());
		assertEquals(OpenFigiRequestOrderTypeEnum.DEFAULT_ISIN_BASED, message.getOpenFigiRequestOrderType());
		assertEquals(OpenFigiRequestRuleEnum.ISIN_ONLY, message.getOpenFigiRequestRuleEnum());
		assertEquals(VfsFigiRequestTypeEnum.PRIMARY_SOURCE_INACTIVATION, message.getRequestType());
		assertEquals(1, message.getRetryCount());
		assertEquals("DN0455281065",message.getIsin());
		assertEquals(null,message.getCusip());
		assertEquals("BK1WN11",message.getSedol());
		assertEquals("XNYS",message.getExchangeCode());
		assertEquals("USD", message.getTradeCurrencyCode());
	}
	
	@Test
    public void testGenerateVfsOpenFigiMessageFromRequestMetrics_SedolMatchingMicBased() throws IOException {
        List<Document> listOfDocumentsFromFilePath = bsonConverter.getListOfDocumentsFromFilePath("VfsOpenFigiMessageGeneratorImplTest/TestSedolMatchingMicBasedRule/vfsProcessedRequest.json");
        OpenFigiRequestMetrics metrics = conversionService.convertToSerializable(listOfDocumentsFromFilePath.get(0), OpenFigiRequestMetrics.class);
        
        VfsFigiRequestMessage message = vfsOpenFigiMessaageGenerator.generateVfsOpenFigiMessageFromRequestMetrics(metrics, VfsFigiRequestTypeEnum.SUCCESS_RETRY, 0);
        List<String> list = Arrays.asList("XTKS");
        assertEquals("trdse", message.getDataSource());
        assertEquals("64355bce5d24161a19d88e4a", message.getPrimarySourceDocId());
        assertEquals("1", message.getPrimarySourceSecurityId());
        assertEquals(OpenFigiRequestOrderTypeEnum.DEFAULT_ISIN_BASED, message.getOpenFigiRequestOrderType());
        assertEquals(OpenFigiRequestRuleEnum.SEDOL_MATCHINGMIC, message.getOpenFigiRequestRuleEnum());
        assertEquals(VfsFigiRequestTypeEnum.SUCCESS_RETRY, message.getRequestType());
        assertEquals(0, message.getRetryCount());
        assertEquals(0, message.getMicIndex());
        assertEquals(list, message.getDependentMics());
        assertEquals("6804541",message.getSedol());
        assertEquals("XJPX",message.getExchangeCode());
        assertEquals("JPY", message.getTradeCurrencyCode());
    }
}
