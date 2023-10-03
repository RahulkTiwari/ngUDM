/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoContainerTest.java
* Author : VRamani
* Date : Feb 12, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
* @author VRamani
*
*/
@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class IvoContainerTest extends IvoLockTestUtil{
	
	private final DataAttribute RIC = DataAttributeFactory.getAttributeByNameAndLevel("ric", DataLevel.SEC);
	private final DataAttribute INSTRUMENT_NAME = DataAttributeFactory.getAttributeByNameAndLevel("nameLong", DataLevel.INS);
	private final DataAttribute IVO_INSTRUMENT_NAME=DataAttributeFactory.getAttributeByNameAndLevel("nameLong", DataLevel.IVO_INS);;
	private final DataAttribute IVO_RIC=DataAttributeFactory.getAttributeByNameAndLevel("ric", DataLevel.IVO_SEC);

	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void testAddToIvoContainer() throws UdmBaseException {
		createPrerequisiteData();
		IvoContainer ivoContainer = new IvoContainer(SD_DOC_ID,"trdse", createDataContainerContext());
		final Map<DataLevel,String> dataLevelVsObjectIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsObjectIdMap.put(DataLevel.INS, SD_INSTRUMENT_ID);
		dataLevelVsObjectIdMap.put(DataLevel.SEC, SECURITY_ID);
		
		final Map<DataLevel,String> dataLevelVsSourceUniqueIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.INS, INS_SRCUNQID);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.SEC, SEC_SRCUNQID);
		
		
		DataValue<String> instrumentNameValue = new DataValue<>();
		instrumentNameValue.setValue(LockLevel.RDU, "InstrumentNameTest");
		ivoContainer.addToDataContainer(INSTRUMENT_NAME, instrumentNameValue,dataLevelVsObjectIdMap,dataLevelVsSourceUniqueIdMap,DataLevel.IVO_INS);
		
		DataValue<String> ricValue = new DataValue<>();
		ricValue.setValue(LockLevel.RDU, "RicTest");
		ivoContainer.addToDataContainer(RIC, ricValue,dataLevelVsObjectIdMap,dataLevelVsSourceUniqueIdMap,DataLevel.IVO_INS);
		
		assertNotNull(ivoContainer.getSdIvoContainer());
		assertTrue(ivoContainer.getSdIvoContainer().isPresent());
		DataContainer sdIvoContainer = ivoContainer.getSdIvoContainer().get();
		
		assertEquals("InstrumentNameTest", sdIvoContainer.getAttributeValueAtLevel(LockLevel.RDU, IVO_INSTRUMENT_NAME));
		assertEquals("RicTest", sdIvoContainer.getAllChildDataContainers().get(0).getAttributeValueAtLevel(LockLevel.RDU, IVO_RIC));
	}
	
	/**
	 * @return
	 */
	private DataContainerContext createDataContainerContext() {
		return DataContainerContext.builder().withUpdateBy("sajadhav").withComment("edit")
				.withServiceDeskTicketId("UDM-6363").build();
	}


	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void testAddToSdContainer() throws UdmTechnicalException {
		IvoContainer ivoContainer = new IvoContainer(SD_DOC_ID,"trdse", createDataContainerContext());
		final Map<DataLevel,String> dataLevelVsObjectIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsObjectIdMap.put(DataLevel.INS, SD_INSTRUMENT_ID);
		dataLevelVsObjectIdMap.put(DataLevel.SEC, SECURITY_ID);
		
		final Map<DataLevel,String> dataLevelVsSourceUniqueIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.INS, INS_SRCUNQID);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.SEC, SEC_SRCUNQID);
		
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "isin123456");
		DataAttribute isinAttribute = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
		ivoContainer.addToDataContainer(isinAttribute, isinValue,dataLevelVsObjectIdMap,dataLevelVsSourceUniqueIdMap,DataLevel.INS);
		
		DataValue<String> sedolValue = new DataValue<>();
		sedolValue.setValue(LockLevel.RDU, "sedol123456");
		DataAttribute sedolAttribute = DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.SEC);
		ivoContainer.addToDataContainer(sedolAttribute, sedolValue,dataLevelVsObjectIdMap,dataLevelVsSourceUniqueIdMap,DataLevel.INS);
		
		assertNotNull(ivoContainer.getSdContainer());
		assertTrue(ivoContainer.getSdContainer().isPresent());
		DataContainer sdContainer = ivoContainer.getSdContainer().get();
		
		assertEquals("isin123456", sdContainer.getAttributeValueAtLevel(LockLevel.RDU, isinAttribute));
		assertEquals("sedol123456", sdContainer.getAllChildDataContainers().get(0).getAttributeValueAtLevel(LockLevel.RDU, sedolAttribute));
	}
	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void testAddToLeContainer() throws UdmTechnicalException {
		IvoContainer ivoContainer = new IvoContainer(SD_DOC_ID,"trdse", createDataContainerContext());
		DataValue<String> leiValue = new DataValue<>();
		leiValue.setValue(LockLevel.RDU, "5493003MFZSDE3P66969");
		
		final Map<DataLevel,String> dataLevelVsObjectIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsObjectIdMap.put(DataLevel.LE, LEGAL_ENTITY_ID);
		dataLevelVsObjectIdMap.put(DataLevel.SEC, SECURITY_ID);
		
		final Map<DataLevel,String> dataLevelVsSourceUniqueIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.LE, LEGAL_ENTITY_SOURCE_UNQ_ID);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.SEC, SEC_SRCUNQID);
		
		DataAttribute leiAttribute = DataAttributeFactory.getAttributeByNameAndLevel("lei", DataLevel.LE);
		ivoContainer.addToDataContainer(leiAttribute, leiValue,dataLevelVsObjectIdMap,dataLevelVsSourceUniqueIdMap,DataLevel.LE);
		
		assertNotNull(ivoContainer.getSdLeContainer());
		assertTrue(ivoContainer.getSdLeContainer().isPresent());
		DataContainer sdLeContainer = ivoContainer.getSdLeContainer().get();
		assertEquals("5493003MFZSDE3P66969", sdLeContainer.getAttributeValueAtLevel(LockLevel.RDU, leiAttribute));
	}
	
	@Test
	public void testSetDataContainer_EN() {
		IvoContainer ivoContainer = new IvoContainer("ENDOCID","rduEns", createDataContainerContext());
		DataContainer dataContainer=new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		ivoContainer.setDataContainer(dataContainer);
		assertNotNull(ivoContainer.getEnDataContainer().get());
		assertTrue(ivoContainer.getSdContainer().isEmpty());
	}
	
	@Test
	public void testSetDataContainer_SD() {
		IvoContainer ivoContainer = new IvoContainer("8835320997hhk","trdse", createDataContainerContext());
		DataContainer dataContainer=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		ivoContainer.setDataContainer(dataContainer);
		assertNotNull(ivoContainer.getSdContainer().get());
		assertTrue(ivoContainer.getEnDataContainer().isEmpty());
	}
	
	@Test
	public void testSetDataContainer_LE() {
		IvoContainer ivoContainer = new IvoContainer("8835320997hhk","trdse", createDataContainerContext());
		DataContainer dataContainer=new DataContainer(DataLevel.LE, DataContainerContext.builder().build());
		ivoContainer.setDataContainer(dataContainer);
		assertTrue(ivoContainer.getSdContainer().isEmpty());
		assertTrue(ivoContainer.getEnDataContainer().isEmpty());
		assertNotNull(ivoContainer.getSdLeContainer().get());
	}

}
