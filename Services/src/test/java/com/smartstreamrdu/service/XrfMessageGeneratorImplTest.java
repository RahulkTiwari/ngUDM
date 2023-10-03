/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfMessageGeneratorImplTest.java
 * Author: Rushikesh Dedhia
 * Date: May 15, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.xrf.CrossRefBaseDocument;
import com.smartstreamrdu.commons.xrf.CrossRefChangeEntity;
import com.smartstreamrdu.commons.xrf.XrfAttributeValue;
import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.commons.xrf.XrfProcessMode;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.message.UdmMessageKey;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.service.domain.ServiceTestConfiguration;
import com.smartstreamrdu.service.message.XrfUdmMessageKeyGenerationService;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.service.util.MockUtil;
import com.smartstreamrdu.service.xrf.messaging.XrfMessageGenerator;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.EventListenerConstants;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.Setter;

/**
 * @author Dedhia
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfiguration.class })
public class XrfMessageGeneratorImplTest {
	
	ChangeEventInputPojo inputPojo = null;
	
	private static final DataAttribute sedol = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEDOL, DataLevel.SEC);
	private static final DataAttribute cusip = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.CUSIP, DataLevel.INS);
	private static final DataAttribute instrumentId = DataAttributeFactory.getAttributeByNameAndLevel("_instrumentId", DataLevel.INS);
	private static final DataAttribute securityId = DataAttributeFactory.getAttributeByNameAndLevel("_securityId", DataLevel.SEC);
	private static final DataAttribute assetType = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_TYPE_CODE, DataLevel.INS);
	private static final DataAttribute dataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
	private static final DataAttribute tradeCurrencyCode = DataAttributeFactory.getAttributeByNameAndLevel("tradeCurrencyCode", DataLevel.SEC);
	private static final DataAttribute securityStatus = DataAttributeFactory.getAttributeByNameAndLevel("securityStatus", DataLevel.SEC);
	private static final DataAttribute instrumentStatus = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
	private static final DataAttribute isin = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.ISIN, DataLevel.INS);
	private static final DataAttribute additionalAttribute = DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC);
	private DataAttribute updDate =DataAttributeFactory.getAttributeByNameAndLevel("updDate", DataLevel.Document);
	private DataAttribute insDate =DataAttributeFactory.getAttributeByNameAndLevel("insDate", DataLevel.Document);
	private LocalDateTime insDateVal=LocalDateTime.of(2020, 01, 18, 12, 30, 30);
	private LocalDateTime updDateVal=LocalDateTime.of(2020, 01, 20, 12, 30, 30);
	
	private DataContainer secDataContainer ;
	private DataContainer insDataContainer;
	
	@Autowired
	XrfMessageGenerator messageGenerator;
	
	@Autowired
	private NormalizedValueService normalizedValueService;

	@Setter
	@Autowired
	private XrfUdmMessageKeyGenerationService xrfUdmMessageKeyGenerationService;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		MockUtil.createNormalizedService(normalizedValueService);
		
		secDataContainer= DataContainerTestUtil.getSecurityContainer();
		insDataContainer = DataContainerTestUtil.getInstrumentContainer();
		
		inputPojo = new ChangeEventInputPojo();
		
		DataValue<String> sedolDataValue = new DataValue<String>();
		sedolDataValue.setValue(LockLevel.FEED, "ABC");
		secDataContainer.addAttributeValue(sedol, sedolDataValue);
		insDataContainer.addDataContainer(secDataContainer, DataLevel.SEC);
		DataValue<DomainType> secStatusValue = new DataValue<>();
		DomainType secStatusDomainValue = new DomainType();
		secStatusDomainValue.setVal("1");
		secStatusValue.setValue(LockLevel.FEED, secStatusDomainValue);
		secDataContainer.addAttributeValue(securityStatus, secStatusValue);
		
		DataValue<String> securityIdValue = new DataValue<String>();
		securityIdValue.setValue(LockLevel.FEED, "a1b2c3");
		secDataContainer.addAttributeValue(securityId, securityIdValue);
		
		DataValue<DomainType> tradeCurrencyValue = new DataValue<>();
		DomainType tradeCurrencyDomainValue = new DomainType();
		tradeCurrencyDomainValue.setVal("USd");
		tradeCurrencyValue.setValue(LockLevel.FEED, tradeCurrencyDomainValue);
		secDataContainer.addAttributeValue(tradeCurrencyCode, tradeCurrencyValue);
		
		DataValue<String> cusipDataValue = new DataValue<String>();
		cusipDataValue.setValue(LockLevel.FEED, "XYZ");
		insDataContainer.addAttributeValue(cusip, cusipDataValue);
		
		DataValue<String> instrumentIdValue = new DataValue<String>();
		instrumentIdValue.setValue(LockLevel.FEED, "1a2b3c");
		insDataContainer.addAttributeValue(instrumentId, instrumentIdValue);
		
		insDataContainer.addAttributeValue(instrumentStatus, secStatusValue);
		
		DataValue<String> isinValue=new DataValue<>();
		isinValue.setValue(LockLevel.FEED, "US0126531013");
		insDataContainer.addAttributeValue(isin, isinValue);
		
		DataValue<LocalDateTime> updDateValue=new DataValue<>();
		updDateValue.setValue(LockLevel.RDU, updDateVal);
		insDataContainer.addAttributeValue(updDate, updDateValue);
		
		DataValue<LocalDateTime> insDateValue=new DataValue<>();
		insDateValue.setValue(LockLevel.RDU, insDateVal);
		insDataContainer.addAttributeValue(insDate, insDateValue);
		
		DataValue<DomainType> assetTypeValue = new DataValue<DomainType>();
		DomainType domainType = new DomainType();
		domainType.setVal("CME");
		domainType.setVal2("EL");
		assetTypeValue.setValue(LockLevel.FEED, domainType);
		
		DataValue<DomainType> dataSourceValue = new DataValue<DomainType>();
		DomainType domainType1 = new DomainType();
		domainType1.setVal("figi");
		dataSourceValue.setValue(LockLevel.FEED, domainType1);
		
		insDataContainer.addAttributeValue(assetType, assetTypeValue);
		insDataContainer.addAttributeValue(dataSource, dataSourceValue);
		
		inputPojo.setPostChangeContainer(insDataContainer);
		
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.xrf.messaging.XrfMessageGeneratorImpl#generateMessage(com.smartstreamrdu.service.events.ChangeEventInputPojo)}.
	 */
	@Test
	public void testGenerateMessage_1() {
		XrfMessage message = messageGenerator.generateMessage(inputPojo,null);
		Assert.assertNotNull(message);
		assertEquals(1, message.getCrossRefChangeEntities().size());
		CrossRefChangeEntity crossRefChangeEntity = message.getCrossRefChangeEntities().get(0);
		assertEquals(Timestamp.valueOf(updDateVal), crossRefChangeEntity.getPostChangeDocument().getLastModifiedDate());
	}
	
	@Test
	public void test_generateMessage_withIvoLocks(){
		DataValue<DomainType> insStatusVal=new DataValue<>();
		insStatusVal.setValue(LockLevel.FEED, new DomainType("1"));
		insStatusVal.setValue(LockLevel.RDU, new DomainType(null,null,"A"));
		insDataContainer.addAttributeValue(instrumentStatus, insStatusVal);
		
		DataValue<String> isinValue=new DataValue<>();
		isinValue.setValue(LockLevel.FEED, "US0126531013");
		isinValue.setValue(LockLevel.RDU, "US0126531015");
		insDataContainer.addAttributeValue(isin, isinValue);
		
		DataValue<DomainType> secStatusValue=new DataValue<>();
		secStatusValue.setValue(LockLevel.FEED, new DomainType("1"));
		secStatusValue.setValue(LockLevel.RDU, new DomainType(null,null,"A"));
		secDataContainer.addAttributeValue(securityStatus, secStatusValue);
		
		DataValue<DomainType> additional=new DataValue<>();
		
		DomainType domainType = new DomainType();
		domainType.setVal("Default");
		
		DomainType domainType1 = new DomainType();
		domainType1.setVal("Default");
		
		additional.setValue(LockLevel.FEED, domainType);
		additional.setValue(LockLevel.RDU, domainType1);
		secDataContainer.addAttributeValue(additionalAttribute, additional);
		
		
		XrfMessage message = messageGenerator.generateMessage(inputPojo,null);
		Assert.assertNotNull(message);
		List<CrossRefChangeEntity> xrChangedEntities = message.getCrossRefChangeEntities();
		assertEquals(1, xrChangedEntities.size());
		CrossRefChangeEntity xrChangeEntity = xrChangedEntities.get(0);
		assertEquals("A", xrChangeEntity.getPostChangeDocument().getInstrumentStatus());
		assertEquals("A", xrChangeEntity.getPostChangeDocument().getSecurityStatus());
		assertEquals("US0126531015", xrChangeEntity.getPostChangeDocument().getAttributeValueByName("isin"));
	}
	
	@Test
	public void test_generateMessage_withOtherLevels(){
		DataContainer ivoContainer=new DataContainer(DataLevel.IVO_INS,
				DataContainerContext.builder().withComment("ABC").withUpdateBy("sameer").build());
		inputPojo.setPostChangeContainer(ivoContainer);
		XrfMessage message = messageGenerator.generateMessage(inputPojo,null);
		assertNull(message);
		
		DataContainer leContainer=new DataContainer(DataLevel.LE,
				DataContainerContext.builder().withComment("ABC").withUpdateBy("sameer").build());
		inputPojo.setPostChangeContainer(leContainer);
		message = messageGenerator.generateMessage(inputPojo,null);
		assertNull(message);
		
	}
	
	@Test
	public void test_generateMessage_withUIAndValidateApproved(){
		DataContainer datacontainer = getUIDataContainer();
		
		inputPojo.setPostChangeContainer(datacontainer);
		
		XrfMessage message = messageGenerator.generateMessage(inputPojo,null);
		
		assertEquals("userName", message.getCrossRefChangeEntities().get(0).getPostChangeDocument().getDataApprovedBy());
		assertEquals(XrfProcessMode.RDUEDIT, message.getCrossRefChangeEntities().get(0).getPostChangeDocument().getXrfProcessMode());
	}
	
	@Test
	public void testGenerateMessageWithUIAndValidateApprovedWithReEvaluate() {
		DataContainer dataContainer = getUIDataContainer();
		
		inputPojo.setPostChangeContainer(dataContainer);
		inputPojo.addToMessageContext(EventListenerConstants.XRF_PROCESS_MODE, XrfProcessMode.REEVALUATE);
		XrfMessage message = messageGenerator.generateMessage(inputPojo, null);
		assertEquals("userName", message.getCrossRefChangeEntities().get(0).getPostChangeDocument().getDataApprovedBy());
		assertEquals(XrfProcessMode.REEVALUATE, message.getCrossRefChangeEntities().get(0).getPostChangeDocument().getXrfProcessMode());
	}

	private DataContainer getUIDataContainer() {
		DataContainer datacontainer=new DataContainer(DataLevel.INS,
				DataContainerContext.builder().withComment("ABC").withUpdateBy("userName").withProgram("NG-EquityWeb").build());
		
		datacontainer.addDataContainer(secDataContainer, DataLevel.SEC);
		
		DataValue<DomainType> insStatusVal=new DataValue<>();
		insStatusVal.setValue(LockLevel.FEED, new DomainType("1"));
		insStatusVal.setValue(LockLevel.RDU, new DomainType(null,null,"A"));
		datacontainer.addAttributeValue(instrumentStatus, insStatusVal);
		
		DataValue<String> isinValue=new DataValue<>();
		isinValue.setValue(LockLevel.FEED, "US0126531013");
		isinValue.setValue(LockLevel.RDU, "US0126531015");
		datacontainer.addAttributeValue(isin, isinValue);
		
		DataValue<DomainType> secStatusValue=new DataValue<>();
		secStatusValue.setValue(LockLevel.FEED, new DomainType("1"));
		secStatusValue.setValue(LockLevel.RDU, new DomainType(null,null,"A"));
		secDataContainer.addAttributeValue(securityStatus, secStatusValue);
		
		DataValue<DomainType> additional=new DataValue<>();
		
		DomainType domainType = new DomainType();
		domainType.setVal("Default");
		
		DomainType domainType1 = new DomainType();
		domainType1.setVal("Default");
		
		additional.setValue(LockLevel.FEED, domainType);
		additional.setValue(LockLevel.RDU, domainType1);
		secDataContainer.addAttributeValue(additionalAttribute, additional);
		
		DataValue<DomainType> secType=new DataValue<>();
		secType.setValue(LockLevel.ENRICHED, new DomainType(null,null,"Regular"));
		secDataContainer.addAttributeValue(SdDataAttributeConstant.RDU_SEC_TYPE, secType);
		
		
		DataValue<DomainType> dataSourceValue = new DataValue<DomainType>();
		DomainType domainType2 = new DomainType();
		domainType2.setVal("figi");
		dataSourceValue.setValue(LockLevel.FEED, domainType2);
		
		DataValue<DomainType> assetTypeValue = new DataValue<DomainType>();
		DomainType domainType3 = new DomainType();
		domainType3.setVal("CME");
		domainType3.setVal2("EL");
		assetTypeValue.setValue(LockLevel.FEED, domainType3);
		
		
		datacontainer.addAttributeValue(assetType, assetTypeValue);
		datacontainer.addAttributeValue(dataSource, dataSourceValue);
		return datacontainer;
	}
	
	@Test
	public void test_generateMessageWithStaticDataUpdateDate(){
		LocalDateTime staticDataUpdateDate=LocalDateTime.of(2020, 01, 22, 12, 30, 30);
		XrfMessage message = messageGenerator.generateMessage(inputPojo,staticDataUpdateDate);
		Assert.assertNotNull(message);
		assertEquals(1, message.getCrossRefChangeEntities().size());
		CrossRefChangeEntity crossRefChangeEntity = message.getCrossRefChangeEntities().get(0);
		assertEquals(Timestamp.valueOf(staticDataUpdateDate), crossRefChangeEntity.getPostChangeDocument().getLastModifiedDate());
	}
	
	@Test
	public void test_generateXrfMessageKeyNullPointerException() {
		//new UdmMessageKey.Builder().message(null).build();
		UdmMessageKey udmMessageKey = UdmMessageKey.builder().action(null).variableAttributeValue(null).build();
		assertNotNull(udmMessageKey);
	}
	
	@Test
	public void test_generateXrfMessageKeyXrfProcessModeEdit() {
		DataContainer datacontainer = getUIDataContainer();

		inputPojo.setPostChangeContainer(datacontainer);

		XrfMessage message = messageGenerator.generateMessage(inputPojo, null);
		String documentId = "5e1443d9b8fbfd3a1195ef1d";
		message.getCrossRefChangeEntities().get(0).getPostChangeDocument().setDocumentId(documentId);
		UdmMessageKey.builder().action(null).variableAttributeValue(null).build();
		UdmMessageKey generateXrfMessageKey = xrfUdmMessageKeyGenerationService.generateUdmMessageKey(message);
		assertNotNull(generateXrfMessageKey);
		assertEquals("rduEdit",generateXrfMessageKey.getAction());
		assertEquals(documentId, generateXrfMessageKey.getVariableAttributeValue());
	}
	
	@Test
	public void test_generateXrfMessageKeyXrfProcessModeNull() {
		DataContainer datacontainer = getUIDataContainer();

		inputPojo.setPostChangeContainer(datacontainer);

		XrfMessage message = messageGenerator.generateMessage(inputPojo, null);
		String documentId = "5e1443d9b8fbfd3a1195ef2w";
		message.getCrossRefChangeEntities().get(0).getPostChangeDocument().setDocumentId(documentId);
		message.getCrossRefChangeEntities().get(0).getPostChangeDocument().setXrfProcessMode(null);
		
		UdmMessageKey generateXrfMessageKey = xrfUdmMessageKeyGenerationService.generateUdmMessageKey(message);
		assertNotNull(generateXrfMessageKey);
		assertEquals("xrfDefault",generateXrfMessageKey.getAction());
		assertEquals(documentId, generateXrfMessageKey.getVariableAttributeValue());
	}
	
	@Test
	public void test_generateXrfMessageKeyXrfProcessModeFeed() {
		XrfMessage message = messageGenerator.generateMessage(inputPojo, null);
		String documentId = "5e1443d9b8fbfd3a1197ef2x";
		message.getCrossRefChangeEntities().get(0).getPostChangeDocument().setDocumentId(documentId);
		UdmMessageKey generateXrfMessageKey = xrfUdmMessageKeyGenerationService.generateUdmMessageKey(message);
		assertNotNull(generateXrfMessageKey);
		assertEquals("xrfDefault",generateXrfMessageKey.getAction());
		assertEquals(documentId, generateXrfMessageKey.getVariableAttributeValue());
	}
	
	@Test(expected = NullPointerException.class)
	public void test_generateXrfMessageKeyCrossRefChangeEntitiesNull() {
	xrfUdmMessageKeyGenerationService.generateUdmMessageKey(new XrfMessage());
	}
	
	@Test
	public void test_defaultSecurityType() {
		DataContainer datacontainer = getUIDataContainer();
		inputPojo.setPostChangeContainer(datacontainer);
		XrfMessage message = messageGenerator.generateMessage(inputPojo, null);
		assertEquals("Regular", message.getCrossRefChangeEntities().get(0).getPostChangeDocument().getRduSecurityType());
	}
	
	@Test
	public void test_ReprocessingMessage() {
		CrossRefBaseDocument doc = new CrossRefBaseDocument();
		doc.setSecurityRduId("rdu_Sec_Id_1");
		doc.setInstrumentRduId("rdu_Ins_Id_1");

		XrfMessage xrfMessage = messageGenerator.generateReprocessingXrfMessage(doc);
		CrossRefBaseDocument postChangeDocument = xrfMessage.getCrossRefChangeEntities().get(0).getPostChangeDocument();
	
		Assert.assertEquals(XrfProcessMode.XRFREPROCESSING, postChangeDocument.getXrfProcessMode());
		Assert.assertEquals("rdu_Sec_Id_1",postChangeDocument.getSecurityRduId());

	}
	
	@Test
	public void testGenerateMessageForCusip() {
		XrfMessage message = messageGenerator.generateMessage(inputPojo,null);
		Assert.assertNotNull(message);
		assertEquals(1, message.getCrossRefChangeEntities().size());
		CrossRefChangeEntity crossRefChangeEntity = message.getCrossRefChangeEntities().get(0);
		assertNotNull(crossRefChangeEntity.getPostChangeDocument().getCrossRefAttributes().get("cusip"));
		XrfAttributeValue cusipValue=crossRefChangeEntity.getPostChangeDocument().getCrossRefAttributes().get("cusip");
		Assert.assertEquals("XYZ",cusipValue.getValue());
	}
	
	@Test
	public void testGenerateMesssageNull() {
		ChangeEventInputPojo inputPojo = new ChangeEventInputPojo();
		assertEquals(null,messageGenerator.generateMessage(inputPojo,null));
	}
}


