/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergeAndPersistServiceTest.java
 * Author:	Jay Sangoi
 * Date:	16-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.lookup.SecurityFetchService;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * @author Jay Sangoi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class SecurityFetchServiceTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private SecurityFetchService service;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Test
	public void test_fetchExistingSecToBeInactivated() throws UdmBaseException {

		/**
		 * Create Input in db One instrument with One security One instrument
		 * with one security One instrument with multi listed security One
		 * instrument with multi listed security
		 */

		DataAttribute flagActiveAttribute = SecurityAttrConstant.SECURITY_STATUS;

		DomainType flagActive = new DomainType("1",null,null,"tradingStatusMap");
		DataValue<DomainType> flagActiveDV = new DataValue<>();
		flagActiveDV.setValue(LockLevel.FEED, flagActive);

		DomainType flagInActive = new DomainType("0",null,null,"tradingStatusMap");
		DataValue<DomainType> flagInActiveDV = new DataValue<>();
		flagInActiveDV.setValue(LockLevel.FEED, flagInActive);

		DomainType datasource = new DomainType("trdse");
		DataValue<DomainType> datasourceDV = new DataValue<>();
		datasourceDV.setValue(LockLevel.FEED, (DomainType) datasource);

		DataContainer insContainer1 = DataContainerTestUtil.getInstrumentContainer();
		insContainer1.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), datasourceDV);
		DataValue<String> sourceUniqueDV1 = new DataValue<>();
		String val1 = UUID.randomUUID().toString();
		sourceUniqueDV1.setValue(LockLevel.FEED, val1);
		insContainer1.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV1);

		DataContainer secContainer11 = DataContainerTestUtil.getSecurityContainer();
		insContainer1.addDataContainer(secContainer11, DataLevel.SEC);
		DataValue<String> sourceUniqueDV11 = new DataValue<>();
		String val11 = UUID.randomUUID().toString();
		sourceUniqueDV11.setValue(LockLevel.FEED, val11);
		secContainer11.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV11);
		secContainer11.addAttributeValue(flagActiveAttribute, flagActiveDV);

		DataContainer insContainer2 = DataContainerTestUtil.getInstrumentContainer();
		insContainer2.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV2 = new DataValue<>();
		String val2 = UUID.randomUUID().toString();
		sourceUniqueDV2.setValue(LockLevel.FEED, val2);
		insContainer2.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV2);

		DataContainer secContainer21 = DataContainerTestUtil.getSecurityContainer();
		insContainer2.addDataContainer(secContainer21, DataLevel.SEC);
		DataValue<String> sourceUniqueDV21 = new DataValue<>();
		String val21 = UUID.randomUUID().toString();
		sourceUniqueDV21.setValue(LockLevel.FEED, val21);
		secContainer21.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV21);
		secContainer21.addAttributeValue(flagActiveAttribute, flagActiveDV);

		DataContainer insContainer3 = DataContainerTestUtil.getInstrumentContainer();
		insContainer3.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV3 = new DataValue<>();
		String val3 = UUID.randomUUID().toString();
		sourceUniqueDV3.setValue(LockLevel.FEED, val3);
		insContainer3.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV3);

		DataContainer secContainer31 = DataContainerTestUtil.getSecurityContainer();
		insContainer3.addDataContainer(secContainer31, DataLevel.SEC);
		DataValue<String> sourceUniqueDV31 = new DataValue<>();
		String val31 = UUID.randomUUID().toString();
		sourceUniqueDV31.setValue(LockLevel.FEED, val31);
		secContainer31.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV31);
		secContainer31.addAttributeValue(flagActiveAttribute, flagActiveDV);

		DataContainer secContainer32 = DataContainerTestUtil.getSecurityContainer();
		insContainer3.addDataContainer(secContainer32, DataLevel.SEC);
		DataValue<String> sourceUniqueDV32 = new DataValue<>();
		String val32 = UUID.randomUUID().toString();
		sourceUniqueDV32.setValue(LockLevel.FEED, val32);
		secContainer32.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV32);
		secContainer32.addAttributeValue(flagActiveAttribute, flagActiveDV);

		DataContainer insContainer4 = DataContainerTestUtil.getInstrumentContainer();
		insContainer4.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV4 = new DataValue<>();
		String val4 = UUID.randomUUID().toString();
		sourceUniqueDV4.setValue(LockLevel.FEED, val4);
		insContainer4.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV4);

		persistenceService.persist(insContainer1);
		persistenceService.persist(insContainer2);
		persistenceService.persist(insContainer3);
		persistenceService.persist(insContainer4);

		/**
		 * Case 1 - Single security is inserted
		 */
		DataContainer insContainer111 = DataContainerTestUtil.getInstrumentContainer();
		insContainer111.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV111 = new DataValue<>();
		String val111 = UUID.randomUUID().toString();
		sourceUniqueDV111.setValue(LockLevel.FEED, val111);
		insContainer111.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV111);
		insContainer111.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataContainer secContainer111111 = DataContainerTestUtil.getSecurityContainer();
		insContainer111.addDataContainer(secContainer111111, DataLevel.SEC);
		DataValue<String> sourceUniqueDV111111 = new DataValue<>();
		String val111111 = UUID.randomUUID().toString();
		sourceUniqueDV111111.setValue(LockLevel.FEED, val111111);
		secContainer111111.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV111111);
		secContainer111111.addAttributeValue(flagActiveAttribute, flagActiveDV);

		Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated1 = service
				.fetchExistingSecToBeInactivated(insContainer111, null);

		Assert.assertNotNull(fetchExistingSecToBeInactivated1);

		Assert.assertEquals(0, fetchExistingSecToBeInactivated1.size());

		/**
		 * Case 2 - Multi listed security and all inserted
		 */

		DataContainer insContainer222 = DataContainerTestUtil.getInstrumentContainer();
		insContainer222.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV222 = new DataValue<>();
		String val222 = UUID.randomUUID().toString();
		sourceUniqueDV222.setValue(LockLevel.FEED, val222);
		insContainer222.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV222);
		insContainer222.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataContainer secContainer2221 = DataContainerTestUtil.getSecurityContainer();
		insContainer222.addDataContainer(secContainer2221, DataLevel.SEC);
		DataValue<String> sourceUniqueDV2221 = new DataValue<>();
		String val2221 = UUID.randomUUID().toString();
		sourceUniqueDV2221.setValue(LockLevel.FEED, val2221);
		secContainer2221.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV2221);
		secContainer2221.addAttributeValue(flagActiveAttribute, flagActiveDV);
		secContainer2221.addAttributeValue(flagActiveAttribute, flagActiveDV);
		
		DataContainer secContainer222222 = DataContainerTestUtil.getSecurityContainer();

		insContainer222.addDataContainer(secContainer222222, DataLevel.SEC);
		DataValue<String> sourceUniqueDV222222 = new DataValue<>();
		String val222222 = UUID.randomUUID().toString();
		sourceUniqueDV222222.setValue(LockLevel.FEED, val222222);
		secContainer222222.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV222222);
		secContainer222222.addAttributeValue(flagActiveAttribute, flagActiveDV);
		secContainer222222.addAttributeValue(flagActiveAttribute, flagActiveDV);
		
		Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated2 = service
				.fetchExistingSecToBeInactivated(insContainer222, null);

		Assert.assertNotNull(fetchExistingSecToBeInactivated2);

		Assert.assertEquals(0, fetchExistingSecToBeInactivated2.size());

		/**
		 * Case 3 - Single Input security with change in instrument
		 */
		DataContainer insContainer311 = DataContainerTestUtil.getInstrumentContainer();
		insContainer311.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV311 = new DataValue<>();
		String val311 = UUID.randomUUID().toString();
		sourceUniqueDV311.setValue(LockLevel.FEED, val311);
		insContainer311.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV311);

		insContainer311.addDataContainer(secContainer11, DataLevel.SEC);
		insContainer311.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated3 = service
				.fetchExistingSecToBeInactivated(insContainer311, null);

		Assert.assertNotNull(fetchExistingSecToBeInactivated3);
		Assert.assertEquals(1, fetchExistingSecToBeInactivated3.size());
		DataContainer outputCOntainer3 = fetchExistingSecToBeInactivated3.keySet().iterator().next();
		Assert.assertEquals(val1, outputCOntainer3.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));

		/**
		 * Case 4 - Multilisted security, with one input security belonging to
		 * different instrument. The db instrument had only one input
		 * security, hence needs to be inactivated
		 */

		DataContainer insContainer411 = DataContainerTestUtil.getInstrumentContainer();
		insContainer411.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV411 = new DataValue<>();
		String val411 = UUID.randomUUID().toString();
		sourceUniqueDV411.setValue(LockLevel.FEED, val411);
		insContainer411.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV411);

		insContainer411.addDataContainer(secContainer11, DataLevel.SEC);
		insContainer411.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);

		DataContainer secContainer4441 = DataContainerTestUtil.getSecurityContainer();
		insContainer411.addDataContainer(secContainer4441, DataLevel.SEC);
		DataValue<String> sourceUniqueDV4441 = new DataValue<>();
		String val4441 = UUID.randomUUID().toString();
		sourceUniqueDV4441.setValue(LockLevel.FEED, val4441);
		secContainer4441.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV4441);
		secContainer4441.addAttributeValue(flagActiveAttribute, flagActiveDV);
		secContainer4441.addAttributeValue(flagActiveAttribute, flagActiveDV);
	
		Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated4 = service
				.fetchExistingSecToBeInactivated(insContainer411, null);

		Assert.assertNotNull(fetchExistingSecToBeInactivated4);
		Assert.assertEquals(1, fetchExistingSecToBeInactivated4.size());
		DataContainer outputCOntainer4 = fetchExistingSecToBeInactivated4.keySet().iterator().next();
		Assert.assertEquals(val1, outputCOntainer4.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));

		DomainType status = (DomainType) outputCOntainer4.getAttributeValueAtLevel(LockLevel.ENRICHED, DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS), DataLevel.INS));
		Assert.assertEquals(DomainStatus.INACTIVE, status.getNormalizedValue());
		
		/**
		 * Case 5 - Multilisted security, with one input security belonging to
		 * different instrument. The db instrument had other securities apart
		 * from the input security, hence only security needs to be inactivated
		 */

		DataContainer insContainer511 = DataContainerTestUtil.getInstrumentContainer();
		insContainer511.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV511 = new DataValue<>();
		String val511 = UUID.randomUUID().toString();
		sourceUniqueDV511.setValue(LockLevel.FEED, val511);
		insContainer511.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV511);

		insContainer511.addDataContainer(secContainer31, DataLevel.SEC);
		insContainer511.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);

		DataContainer secContainer5551 = DataContainerTestUtil.getSecurityContainer();
		insContainer511.addDataContainer(secContainer5551, DataLevel.SEC);
		DataValue<String> sourceUniqueDV5551 = new DataValue<>();
		String val5551 = UUID.randomUUID().toString();
		sourceUniqueDV5551.setValue(LockLevel.FEED, val5551);
		secContainer5551.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV5551);
		secContainer5551.addAttributeValue(flagActiveAttribute, flagActiveDV);
		secContainer5551.addAttributeValue(flagActiveAttribute, flagActiveDV);
		
		Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated5 = service
				.fetchExistingSecToBeInactivated(insContainer511, null);

		Assert.assertNotNull(fetchExistingSecToBeInactivated5);
		Assert.assertEquals(1, fetchExistingSecToBeInactivated5.size());
		DataContainer outputCOntainer5 = fetchExistingSecToBeInactivated5.keySet().iterator().next();
		Assert.assertEquals(val3, outputCOntainer5.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));

		status = (DomainType) outputCOntainer5.getAttributeValueAtLevel(LockLevel.ENRICHED, DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS), DataLevel.INS));
		Assert.assertNull(status);

		

		/**
		 * Case 6 - Multilisted security, with multiple input security belonging
		 * to different instruments.
		 */
		
		DataContainer insContainer611 = DataContainerTestUtil.getInstrumentContainer();
		insContainer611.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV611 = new DataValue<>();
		String val611 = UUID.randomUUID().toString();
		sourceUniqueDV611.setValue(LockLevel.FEED, val611);
		insContainer611.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV611);

		insContainer611.addDataContainer(secContainer11, DataLevel.SEC);
		insContainer611.addDataContainer(secContainer31, DataLevel.SEC);

		insContainer611.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);

		DataContainer secContainer5561 = DataContainerTestUtil.getSecurityContainer();
		insContainer611.addDataContainer(secContainer5561, DataLevel.SEC);
		DataValue<String> sourceUniqueDV5561 = new DataValue<>();
		String val5561 = UUID.randomUUID().toString();
		sourceUniqueDV5561.setValue(LockLevel.FEED, val5561);
		secContainer5561.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueDV5561);
		secContainer5561.addAttributeValue(flagActiveAttribute, flagActiveDV);
		secContainer5561.addAttributeValue(flagActiveAttribute, flagActiveDV);
		
		Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated6 = service
				.fetchExistingSecToBeInactivated(insContainer611, null);

		Assert.assertNotNull(fetchExistingSecToBeInactivated6);
		Assert.assertEquals(2, fetchExistingSecToBeInactivated6.size());

		TreeMap<Serializable, Serializable> expectedMap = new TreeMap<>(); 
		expectedMap.put(val1, DomainStatus.INACTIVE);

		
		TreeMap<Serializable, Serializable> actualMap = new TreeMap<>(); 
		
		for(DataContainer c : fetchExistingSecToBeInactivated6.keySet()){
			DomainType value  = (DomainType)c.getAttributeValueAtLevel(LockLevel.ENRICHED, DataAttributeFactory
					.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS), DataLevel.INS));
			if(value!=null) {
			actualMap.put(c.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)),value.getNormalizedValue());
			}
		}
		
		Assert.assertEquals(expectedMap, actualMap);
		
		/**
		 * Case 7 - Test for Null feed container
		 */
		Assert.assertTrue(service
				.fetchExistingSecToBeInactivated(null, null).isEmpty());
		
		
		/**
		 * Case 8 - Multilisted, with one security present in db and another in different instrument
		 */
		
		DataContainer insContainer811 = DataContainerTestUtil.getInstrumentContainer();
		insContainer811.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(), datasourceDV);
		DataValue<String> sourceUniqueDV811 = new DataValue<>();
		
		sourceUniqueDV811.setValue(LockLevel.FEED, val1);
		insContainer811.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV811);

		insContainer811.addDataContainer(secContainer11, DataLevel.SEC);
		insContainer811.addDataContainer(secContainer31, DataLevel.SEC);
		
		List<DataContainer> dbList8 = new ArrayList<>();
		dbList8.add(insContainer1);
		
		Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated8 = service
				.fetchExistingSecToBeInactivated(insContainer811, dbList8);

		Assert.assertNotNull(fetchExistingSecToBeInactivated8);
		Assert.assertEquals(1, fetchExistingSecToBeInactivated8.size());
		DataContainer outputCOntainer8 = fetchExistingSecToBeInactivated8.keySet().iterator().next();
		Assert.assertEquals(val3, outputCOntainer8.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));

		status = (DomainType) outputCOntainer8.getAttributeValueAtLevel(LockLevel.ENRICHED, DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS), DataLevel.INS));
		Assert.assertNull(status);

	}
	
	@Test
	@InputCollectionsPath(paths = "SecurityFetchServiceTest/test_InsLevelFeed/database/sdData.json")
	@ModifiedCollections(collections = "sdData")
	public void test_ActiveInsLevelFeed_idcApex() throws UdmBaseException, IOException {

		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"SecurityFetchServiceTest/test_InsLevelFeed/input/sdData.json", SdData.class);

		DataContainer dataContainer = inputContainers.get(0);

		Map<DataContainer, DataContainer> mapOfSecTobeInactivated = service
				.fetchExistingSecToBeInactivated(dataContainer, null);

		
		Collection<DataContainer> dataContainers = mapOfSecTobeInactivated.values();
		List<DataContainer> containerList = new ArrayList<DataContainer>(dataContainers);
		DataContainer container = containerList.get(0);

		DomainType status = container.getHighestPriorityValue(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS));

		Assert.assertEquals("1", status.getVal());
	}	
	
	
	@Test
	@InputCollectionsPath(paths = "SecurityFetchServiceTest/test_SecLevelFeed/database/sdData.json")
	@ModifiedCollections(collections = "sdData")
	public void test_InActiveSecLevelFeed_trdse() throws UdmBaseException, IOException {

		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"SecurityFetchServiceTest/test_SecLevelFeed/input/sdData.json", SdData.class);

		DataContainer dataContainer = inputContainers.get(0);

		Map<DataContainer, DataContainer> mapOfSecTobeInactivated = service
				.fetchExistingSecToBeInactivated(dataContainer, null);

		Collection<DataContainer> dataContainers = mapOfSecTobeInactivated.keySet();
		List<DataContainer> containerList = new ArrayList<DataContainer>(dataContainers);
		DataContainer container = containerList.get(0);

		DomainType status = container.getHighestPriorityValue(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS));

		Assert.assertEquals(DomainStatus.INACTIVE, status.getNormalizedValue());
	}	
	
	
}
