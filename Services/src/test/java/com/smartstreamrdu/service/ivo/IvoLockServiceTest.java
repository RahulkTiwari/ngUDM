/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoLockServiceTest.java
 * Author : SaJadhav
 * Date : 26-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.IvoDocType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.EnData;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.SdIvo;
import com.smartstreamrdu.persistence.domain.XrData;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.persist.MergeAndPersistService;
import com.smartstreamrdu.service.util.MockUtil;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.IvoConstants;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { IvoLockConfig.class })
public class IvoLockServiceTest extends AbstractEmbeddedMongodbJunitParent {
	private static final String SERVICE_DESK_TICKET_ID = "UDM-9484";
	private static final String COMMENT = "first insert";
	private static final String CINS = "OPSCINS";
	private static final DataAttribute IVO_INSTRUMENT_RELATIONS_ATTR = DataAttributeFactory
			.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
	private static final DataAttribute IVO_REF_ID_ATTR = DataAttributeFactory
			.getRelationRefIdAttribute(IVO_INSTRUMENT_RELATIONS_ATTR);
	private static final DataAttribute DOC_TYPE_ATTR = DataAttributeFactory
			.getAttributeByNameAndLevel(IvoConstants.DOC_TYPE, DataLevel.IVO_DOC);

	private static final String OPS_TICKER = "BIT";
	private static final DataAttribute CINS_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("cins",
			DataLevel.INS);
	private static final String RDU_EXERCISE_STYPE_TYPE_CODE = "EUR";
	private static final String USER_NAME = "sameer.jadhav";
	private static final String XR_DOC_ID = "5c6d5419f23a5e69a44c7d5f";
	private static final String RDU_INSTRUMENT_ID = "RDUNIN000000000001";
	private static final String SD_INSTRUMENT_ID = "06066a00-3ec5-446a-a333-e222349602b6";
	private static final String LEGAL_ENTITY_ID = "4a0fbf48-0d94-4bd8-a81e-4a7a7ce4d54b";
	private static final String PRIMARY_LE_LEGAL_ENTITY_ID="549300Z4OX502YK2WA31";
	private static final DataAttribute EXERCISE_STYLE_TYPE_ATTR = InstrumentAttrConstant.EXERCISE_STYLE_CODE;
	private static final DataAttribute TICKER_ATTR = SecurityAttrConstant.TICKER;
	private static final DataAttribute SEDOL_ATTR = SecurityAttrConstant.SEDOL;
	private static final String SEDOL = "BF00P65";
	
	@Autowired
	private IvoLockService ivoLockService;
	@Autowired
	private DataRetrievalService retrievalSerive;

	@Autowired
	private MergeAndPersistService mergeAndPersistService;
	@Autowired
	private ListenerService listenerService;
	@Autowired
	private BsonConverter bsonConverter;
	@Autowired
	private IvoQueryService ivoQueryService;

	@Before
	public void init() {
		MockUtil.mock_listenerService_dataContainerUpdated(listenerService);
	}

	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/test_persist_merge/input"})
	@ModifiedCollections(collections={"sdData","xrData","sdIvo"})
	public void test_persist_merge() throws Exception {
		List<DataContainer> sdContainers = bsonConverter
				.getListOfDataContainersFromFilePath("IvoLockServiceTest/test_persist_merge/sdData.json", SdData.class);
		sdContainers.get(0)
				.updateDataContainerContext(createDataContainerContext(USER_NAME, COMMENT, SERVICE_DESK_TICKET_ID,"trdse"));
		ivoLockService.persist(sdContainers);

		// compare sdIvo container
		Criteria sdIvoCriteria = createIvoCriteria(XR_DOC_ID, RDU_INSTRUMENT_ID);
		List<DataContainer> actualOutputContainers = retrievalSerive.retrieve(Database.Mongodb, sdIvoCriteria, false,
				null);
		compareExpectedOutput("IvoLockServiceTest/test_persist_merge/output/sdIvo.json", actualOutputContainers);

		// compare sdData container
		Criteria criteria = createObjectIdCriteria(SD_INSTRUMENT_ID,DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.INS));
		// check the persisted result in sdData
		List<DataContainer> dbContainers = retrievalSerive.retrieve(Database.Mongodb, criteria, false, null);
		compareExpectedOutput("IvoLockServiceTest/test_persist_merge/output/sdData.json", dbContainers);
	}
	

	/**
	 * @param objectId
	 * @param objectIdAttribute 
	 * @return
	 */
	private Criteria createObjectIdCriteria(String objectId, DataAttribute objectIdAttribute) {
		DataValue<String> objectIdVal=new DataValue<>();
		objectIdVal.setValue(LockLevel.FEED, objectId);
		Criteria criteria=Criteria.where(objectIdAttribute).is(objectIdVal);
		return criteria;
	}

	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/test_persist_Separate_LE/input"})
	@ModifiedCollections(collections={"sdData","xrData"})
	public void test_persist_Separate_LE() throws ClassNotFoundException, IOException, UdmBaseException {
		List<DataContainer> sdLecontainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/test_persist_Separate_LE/sdData.json", SdData.class);
		ivoLockService.persist(sdLecontainers);

		Criteria criteria = createObjectIdCriteria(LEGAL_ENTITY_ID, DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.LE));
		// check the persisted result in sdData
		List<DataContainer> dbContainers = retrievalSerive.retrieve(Database.Mongodb, criteria, false, null);

		compareExpectedOutput("IvoLockServiceTest/test_persist_Separate_LE/output/sdData.json", dbContainers);
	}

	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/test_persist_Embedded_LE/input"})
	@ModifiedCollections(collections={"sdData","sdIvo","xrData"})
	public void test_persist_Embedded_LE() throws ClassNotFoundException, IOException, UdmBaseException {
		List<DataContainer> sdLecontainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/test_persist_Embedded_LE/sdData.json", SdData.class);
		ivoLockService.persist(sdLecontainers);
		Criteria sdIvoCriteria = createIvoCriteria(XR_DOC_ID,RDU_INSTRUMENT_ID);
		List<DataContainer> sdIvoContainers = retrievalSerive.retrieve(Database.Mongodb, sdIvoCriteria, false, null);
		compareExpectedOutput("IvoLockServiceTest/test_persist_Embedded_LE/output/sdIvo.json", sdIvoContainers);
	}
	
	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/test_persist_Embedded_Underlying/input"})
	@ModifiedCollections(collections={"sdData","xrData","sdIvo"})
	public void test_persist_Embedded_Underlying() throws ClassNotFoundException, IOException, UdmBaseException {
		List<DataContainer> sdLecontainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/test_persist_Embedded_Underlying/sdData.json", SdData.class);
		ivoLockService.persist(sdLecontainers);
		Criteria sdIvoCriteria = createIvoCriteria(XR_DOC_ID,RDU_INSTRUMENT_ID);
		List<DataContainer> sdIvoContainers = retrievalSerive.retrieve(Database.Mongodb, sdIvoCriteria, false, null);
		compareExpectedOutput("IvoLockServiceTest/test_persist_Embedded_Underlying/output/sdIvo.json", sdIvoContainers);
	}
	
	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/test_only_securityLevelAttribute/input"})
	@ModifiedCollections(collections={"sdData","xrData","sdIvo"})
	public void test_only_securityLevelAttribute() throws ClassNotFoundException, IOException, UdmBaseException {
		List<DataContainer> sdLecontainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/test_only_securityLevelAttribute/sdData.json", SdData.class);
		ivoLockService.persist(sdLecontainers);

		Criteria sdIvoCriteria = createIvoCriteria(XR_DOC_ID, RDU_INSTRUMENT_ID);
		List<DataContainer> sdIvoContainers = retrievalSerive.retrieve(Database.Mongodb, sdIvoCriteria, false, null);
		compareExpectedOutput("IvoLockServiceTest/test_only_securityLevelAttribute/output/sdIvo.json", sdIvoContainers);
	}

	/**
	 * First apply OPS lock on isin and sedol
	 * Send FEED update for isin and sedol with same value as OPS lock value
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws UdmBaseException 
	 */
	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/testLockRemoval/input"})
	@ModifiedCollections(collections={"sdData","xrData","sdIvo"})
	public void testLockRemoval() throws ClassNotFoundException, IOException, UdmBaseException {
		List<DataContainer> sdContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/testLockRemoval/addLock.json", SdData.class);
		sdContainers.get(0).updateDataContainerContext(createDataContainerContext(USER_NAME,COMMENT,SERVICE_DESK_TICKET_ID,"trdse"));
		ivoLockService.persist(sdContainers);

		List<DataContainer> feedContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/testLockRemoval/removeLock.json", SdData.class);
		// check the persisted result in sdData
		List<DataContainer> dbContainers = retrievalSerive.retrieve(Database.Mongodb, createObjectIdCriteria(SD_INSTRUMENT_ID,DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.INS)), false, null);

		mergeAndPersistService.mergeAndPersistSd("trdse", feedContainers.get(0), dbContainers, null);
		//assert results
		List<DataContainer> expectedCriteria = retrievalSerive.retrieve(Database.Mongodb, createObjectIdCriteria(SD_INSTRUMENT_ID,DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.INS)), false, null);
		compareExpectedOutput("IvoLockServiceTest/testLockRemoval/output/sdData.json", expectedCriteria);
	}

	/**
	 * Apply OPS Lock on isin, sedol, australianInstrumentId, ticker
	 * @throws Exception
	 */
	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/test_persist_insert/input"})
	@ModifiedCollections(collections={"sdData","xrData","sdIvo"})
	public void test_persist_insert() throws Exception {
		List<DataContainer> sdContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/test_persist_insert/sdData.json", SdData.class);
		sdContainers.get(0)
				.updateDataContainerContext(createDataContainerContext(USER_NAME, COMMENT, SERVICE_DESK_TICKET_ID,"trdse"));
		// sdContainerList.add(sdContainer);
		ivoLockService.persist(sdContainers);

		// compare sdIvo container
		Criteria sdIvoCriteria = createIvoCriteria(XR_DOC_ID, RDU_INSTRUMENT_ID);
		List<DataContainer> actualOutputContainers = retrievalSerive.retrieve(Database.Mongodb, sdIvoCriteria, false,
				null);
		compareExpectedOutput("IvoLockServiceTest/test_persist_insert/output/sdIvo.json", actualOutputContainers);

		// compare sdData container
		Criteria criteria = createObjectIdCriteria(SD_INSTRUMENT_ID,DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.INS));
		// check the persisted result in sdData
		List<DataContainer> dbContainers = retrievalSerive.retrieve(Database.Mongodb, criteria, false, null);
		compareExpectedOutput("IvoLockServiceTest/test_persist_insert/output/sdData.json", dbContainers);
	}
	
	@Test
	@InputCollectionsPath(paths={"IvoLockServiceTest/test_persist_primaryLE_withEmbedded_LE/input"})
	@ModifiedCollections(collections={"sdData"})
	public void test_persist_primaryLE_withEmbedded_LE() throws IOException, ClassNotFoundException, UdmBaseException{

		List<DataContainer> sdContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/test_persist_primaryLE_withEmbedded_LE/legalEntity.json", SdData.class);
		sdContainers.get(0)
				.updateDataContainerContext(createDataContainerContext(USER_NAME, COMMENT, SERVICE_DESK_TICKET_ID,"trdse"));
		// sdContainerList.add(sdContainer);
		ivoLockService.persist(sdContainers);

		// compare sdIvo container
		Criteria criteria = createObjectIdCriteria(PRIMARY_LE_LEGAL_ENTITY_ID, DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.LE));
		List<DataContainer> actualOutputContainers = retrievalSerive.retrieve(Database.Mongodb, criteria, false,
				null);
		compareExpectedOutput("IvoLockServiceTest/test_persist_primaryLE_withEmbedded_LE/output/sdData.json", actualOutputContainers);
	}
	
	@Test
	@InputCollectionsPath(paths = {"IvoLockServiceTest/testPersistAnnaDataContainer/input"})
	@ModifiedCollections(collections = {"sdData"})
	public void testPersistAnnaDataContainer() throws IOException, UdmBaseException, ClassNotFoundException {
		List<DataContainer> sdContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"IvoLockServiceTest/testPersistAnnaDataContainer/editedData.json", SdData.class);
		DataContainerContext context = createDataContainerContext(USER_NAME, COMMENT, SERVICE_DESK_TICKET_ID,"asbIsin");
		sdContainers.get(0)
				.updateDataContainerContext(context);
		// sdContainerList.add(sdContainer);
		ivoLockService.persist(sdContainers);

		List<DataContainer> actualDataContainers = ivoQueryService.getMatchingDataContainerByDocumentId(sdContainers.get(0));
		
		assertEquals(1, actualDataContainers.size());
		DataContainer dataContainer = actualDataContainers.get(0);
		assertEquals(new DomainType(null, null, "AnnaRDU"), dataContainer.getHighestPriorityValue(InstrumentAttrConstant.INSTRUMENT_TYPE_CODE));
		assertEquals("RDU_ESVUFR", dataContainer.getHighestPriorityValue(InstrumentAttrConstant.CFI_CODE2015));
	
	}
	
	@Test
	@InputCollectionsPath(paths = {"IvoLockServiceTest/testPersistEnDataContainer/input"})
	@ModifiedCollections(collections = {"enData"})
	public void testPersistEnDataContainer() throws ClassNotFoundException, IOException, UdmBaseException {
		List<DataContainer> listOfDataContainers = bsonConverter.getListOfDataContainersFromFilePath("IvoLockServiceTest/testPersistEnDataContainer/editedEnData.json",EnData.class);
		listOfDataContainers.get(0)
		.updateDataContainerContext(createDataContainerContext(USER_NAME, "OPS lock on enData", SERVICE_DESK_TICKET_ID,"rduEns"));
		ivoLockService.persist(listOfDataContainers);
		List<DataContainer> actualDataContainers = ivoQueryService.getMatchingDataContainerByDocumentId(listOfDataContainers.get(0));
		assertEquals(3, actualDataContainers.get(0).getAudit().size());
		compareExpectedOutput("IvoLockServiceTest/testPersistEnDataContainer/output/enData.json", actualDataContainers);
	}
	
	@Test
	@InputCollectionsPath(paths = {"IvoLockServiceTest/testRemoveIvoLocks_enData/input"})
	@ModifiedCollections(collections = {"enData"})
	public void testRemoveIvoLocks_enData() throws UdmBaseException, IOException, ClassNotFoundException {
		List<DataContainer> deletedDataContainerList=bsonConverter.getListOfDataContainersFromFilePath("IvoLockServiceTest/testRemoveIvoLocks_enData/editedEnData.json",EnData.class);
		deletedDataContainerList.get(0).updateDataContainerContext(createDataContainerContext(USER_NAME, "OPS lock on enData", SERVICE_DESK_TICKET_ID,"rduEns"));
		ivoLockService.removeIvoLocks(deletedDataContainerList);
		List<DataContainer> actualDataContainers = ivoQueryService.getMatchingDataContainerByDocumentId(deletedDataContainerList.get(0));
		
		assertEquals(4, actualDataContainers.get(0).getAudit().size());
		
		compareExpectedOutput("IvoLockServiceTest/testRemoveIvoLocks_enData/output/enData.json", actualDataContainers);
	}
	
	@Test
	@InputCollectionsPath(paths = {"IvoLockServiceTest/testRemoveIvoLocks_LE/input"})
	@ModifiedCollections(collections = {"sdData"})
	public void testRemoveIvoLocks_LE() throws UdmBaseException, IOException, ClassNotFoundException {
		List<DataContainer> deletedDataContainerList=bsonConverter.getListOfDataContainersFromFilePath("IvoLockServiceTest/testRemoveIvoLocks_LE/editedLE.json",SdData.class);
		deletedDataContainerList.get(0).updateDataContainerContext(createDataContainerContext(USER_NAME, "remove OPS lock on LE", SERVICE_DESK_TICKET_ID,"trdse"));
		ivoLockService.removeIvoLocks(deletedDataContainerList);
		
		List<DataContainer> actualDataContainers = ivoQueryService.getMatchingDataContainerByDocumentId(deletedDataContainerList.get(0));
		
		assertEquals(2, actualDataContainers.get(0).getAudit().size());
		
		compareExpectedOutput("IvoLockServiceTest/testRemoveIvoLocks_LE/output/sdData.json", actualDataContainers);
	}

	private Criteria createIvoCriteria(String documentId,String objectId){
		DataValue<ReferenceId> referenceIdVal = new DataValue<>();
		ReferenceId referenceId = new ReferenceId();
		referenceId.setDocumentId(XR_DOC_ID);
		referenceId.setObjectId(RDU_INSTRUMENT_ID);
		referenceIdVal.setValue(LockLevel.RDU, referenceId);
		Criteria sdIvoCriteria = Criteria.where(IVO_REF_ID_ATTR).is(referenceIdVal);
		DataValue<String> docTypeVal = new DataValue<>();
		docTypeVal.setValue(LockLevel.RDU, IvoDocType.RDU.name());
		return  sdIvoCriteria.andOperator(Criteria.where(DOC_TYPE_ATTR).is(docTypeVal));
	}

	/**
	 * @param string
	 * @param string2
	 * @param string3
	 * @return
	 */
	private DataContainerContext createDataContainerContext(String userName, String comment,
			String serviceDeskTicketId,String dataSource) {
		return DataContainerContext.builder().withUpdateBy(userName).withComment(comment)
				.withServiceDeskTicketId(serviceDeskTicketId).withDataSource(dataSource).build();
	}

	@Test
	public void test_mergeLocks() throws UdmTechnicalException, IOException{
		List<DataContainer> sdIvoContainers = bsonConverter.getListOfDataContainersFromFilePath("IvoLockServiceTest/test_mergeLocks/sdIvo.json", SdIvo.class);
		DataContainer sdIvoContainer = sdIvoContainers.get(0);
		List<DataContainer> sdDataContainers = bsonConverter.getListOfDataContainersFromFilePath("IvoLockServiceTest/test_mergeLocks/sdData.json", SdData.class);
		DataContainer sdContainer = sdDataContainers.get(0);
		List<DataContainer> xrContainers = bsonConverter.getListOfDataContainersFromFilePath("IvoLockServiceTest/test_mergeLocks/xrData.json", XrData.class);
		DataContainer xrContainer = xrContainers.get(0);
		DataContainer mergedContainer = ivoLockService.mergeContainers(sdContainer, sdIvoContainer, xrContainer);
		
		String highestPriorityValue = mergedContainer.getHighestPriorityValue(CINS_ATTR);
		Assert.assertEquals(CINS, highestPriorityValue);
		
		Assert.assertEquals(new DomainType(null, null, RDU_EXERCISE_STYPE_TYPE_CODE), mergedContainer.getHighestPriorityValue(EXERCISE_STYLE_TYPE_ATTR));
		
		List<DataContainer> childCotainers = mergedContainer.getChildDataContainers(DataLevel.SEC);
		Assert.assertEquals(1, childCotainers.size());
		DataContainer secContainer = childCotainers.get(0);
		Assert.assertEquals(OPS_TICKER,secContainer.getHighestPriorityValue(TICKER_ATTR));
		
		Assert.assertEquals(SEDOL,secContainer.getHighestPriorityValue(SEDOL_ATTR));
	}
}
