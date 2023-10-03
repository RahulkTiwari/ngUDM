/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoMergeAndPersistServiceTest.java
 * Author : SaJadhav
 * Date : 14-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.IvoDocType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.util.MockUtil;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.IvoConstants;

/**
 * @author SaJadhav
 *
 */
@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { IvoLockServiceConfig.class })
public class IvoMergeAndPersistServiceTest  extends IvoLockTestUtil{
	private static final String CINS = "OPSCINS";
	private static final String ISIN_OPS =  "NZCFGE0001S7";
	private static final DataAttribute IVO_DOC_TYPE_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("docType", DataLevel.IVO_DOC);
	private static final DataAttribute IVO_CINS_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("cins", DataLevel.IVO_INS);;
	private static final DataAttribute IVO_INSTRUMENT_RELATIONS_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
	private static final DataAttribute IVO_REF_ID_ATTR = DataAttributeFactory.getRelationRefIdAttribute(IVO_INSTRUMENT_RELATIONS_ATTR);
	private static final DataAttribute IVO_RELATION_TYYPE_ATTR=DataAttributeFactory.getRelationTypeAttribute(IVO_INSTRUMENT_RELATIONS_ATTR);
	private static final DataAttribute DOC_TYPE_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(IvoConstants.DOC_TYPE, DataLevel.IVO_DOC);
	private static final String OPS_SEDOL = "OPSSEDOL";
	private static final DataAttribute IVO_TICKER_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("ticker", DataLevel.IVO_SEC);
	private static final String OPS_TICKER = "OPSTICKER";
	private static final DataAttribute IVO_SECURITY_RELATIONS_ATTR =  DataAttributeFactory.getAttributeByNameAndLevel(IvoConstants.SECURITY_RELATIONS, DataLevel.IVO_SEC);
	private static final DataAttribute IVO_SEC_RELATION_TYPE_ATTR=DataAttributeFactory.getRelationTypeAttribute(IVO_SECURITY_RELATIONS_ATTR);
	private static final DataAttribute IVO_SEC_REF_ID_ATTR = DataAttributeFactory.getRelationRefIdAttribute(IVO_SECURITY_RELATIONS_ATTR);
	private static final DataAttribute CINS_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("cins", DataLevel.INS);

	@Autowired
	private IvoMergeAndPersistService mergeAndPersistService;
	
	@Autowired
	private PersistenceService persistenceService;
	@Autowired
	private DataRetrievalService retrievalSerive;
	
	@Autowired
	private ListenerService listenerService;
	
	@Before
	public void init(){
		MockUtil.mock_listenerService_dataContainerUpdated(listenerService);
	}
	
	/**
	 * @return
	 */
	private DataContainer createSdIvoContainer() {
		DataContainer sdIvoContainer =new DataContainer(DataLevel.IVO_INS, DataContainerContext.builder().build());
		DataValue<String> docTypeVal=new DataValue<>();
		docTypeVal.setValue(LockLevel.RDU, IvoDocType.RDU.name());
		sdIvoContainer.addAttributeValue(IVO_DOC_TYPE_ATTR, docTypeVal);
		
		DataAttribute sdIvoInsRelAttr=DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
		
		addIvoRelations(sdIvoContainer, sdIvoInsRelAttr,RDU_INSTRUMENT_ID,XR_DOC_ID);
		
		
		DataContainer secDataContainer=DataContainerTestUtil.getDataContainer(DataLevel.IVO_SEC);;
		sdIvoContainer.addDataContainer(secDataContainer, DataLevel.IVO_SEC);
		
		
		DataValue<String> tickerVal=new DataValue<>() ;
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		secDataContainer.addAttributeValue(IVO_TICKER_ATTR, tickerVal );
		
		DataAttribute sdIvoSecRelAttr=IVO_SECURITY_RELATIONS_ATTR;
		
		addIvoRelations(secDataContainer, sdIvoSecRelAttr,RDU_SECURITY_ID,XR_DOC_ID);
		
		return sdIvoContainer;
	}

	private void addIvoRelations(DataContainer sdIvoContainer, DataAttribute sdIvoInsRelAttr,String objectId,String documentId) {
		DataRow ivoRelLink = new DataRow(sdIvoInsRelAttr);
		DataAttribute relationTypeAttr=DataAttributeFactory.getRelationTypeAttribute(sdIvoInsRelAttr);
		
		DataValue<String> relationTypeVal=new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoRelLink.addAttribute(relationTypeAttr, relationTypeVal);
		
		DataAttribute referenceIdAttr=DataAttributeFactory.getRelationRefIdAttribute(sdIvoInsRelAttr);
		DataValue<ReferenceId> referenceIdVal=new DataValue<>();
		referenceIdVal.setValue(LockLevel.RDU, new ReferenceId(objectId, documentId));
		ivoRelLink.addAttribute(referenceIdAttr, referenceIdVal);
		
		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(ivoRelLink);
		
		DataValue<ArrayList<DataRow>> relDataValue=new DataValue<>();
		relDataValue.setValue(LockLevel.RDU, relLinkList);
		DataRow relDataRow=new DataRow(sdIvoInsRelAttr,relDataValue);
		sdIvoContainer.addAttributeValue(sdIvoInsRelAttr, relDataRow);
	}

	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void test_mergeAndPersist_merge() throws Exception{
		createPrerequisiteData();
		//first create sdIvo document
		DataContainer sdIvoContaner=createSdIvoContainer();
		persistenceService.persist(sdIvoContaner);
		
		IvoContainer ivoContainer=new IvoContainer(SD_DOC_ID,"trdse", createDataContainerContext());
		createInputSdContainer(ivoContainer);
		createInputSdIvoContainer(ivoContainer);
		mergeAndPersistService.mergeAndPersist(ivoContainer);
		checkResults();
	}
	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void test_mergeAndPersist_insert() throws Exception{
		createPrerequisiteData();
		IvoContainer ivoContainer=new IvoContainer(SD_DOC_ID,"trdse",createDataContainerContext());
		createInputSdContainer(ivoContainer);
		createInputSdIvoContainer(ivoContainer);
		mergeAndPersistService.mergeAndPersist(ivoContainer);
		
		checkResults();
	}
	

	private void checkResults() throws UdmTechnicalException {
		Criteria criteria=Criteria.where( DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.INS, false,null));
		DataValue<String> idValue=new DataValue<>();
		idValue.setValue(LockLevel.FEED, SD_DOC_ID);
		criteria=criteria.is(idValue);
		//check the persisted result in sdData
		List<DataContainer> dbContainers = retrievalSerive.retrieve(Database.Mongodb, criteria, false, null);
		
		Assert.assertNotNull(dbContainers);
		Assert.assertEquals(1, dbContainers.size());
		DataValue<String> isinVal = (DataValue<String>) dbContainers.get(0).
				getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS));
		Assert.assertEquals(SD_ISIN,isinVal.getValue(LockLevel.FEED));
		Assert.assertEquals(ISIN_OPS,isinVal.getValue(LockLevel.RDU));
		
		//check results in sdIvo
		
			DataValue<ReferenceId> referenceIdVal=new DataValue<>();
			ReferenceId referenceId=new ReferenceId();
			referenceId.setDocumentId(XR_DOC_ID);
			referenceId.setObjectId(RDU_INSTRUMENT_ID);
			referenceIdVal.setValue(LockLevel.RDU, referenceId);
			Criteria sdIvoCriteria=Criteria.where(IVO_REF_ID_ATTR).is(referenceIdVal);
			DataValue<String> docTypeVal=new DataValue<>();
			docTypeVal.setValue(LockLevel.RDU, IvoDocType.RDU.name());
			criteria=criteria.andOperator(Criteria.where(DOC_TYPE_ATTR).is(docTypeVal));
			List<DataContainer> sdIvoContainers = retrievalSerive.retrieve(Database.Mongodb, sdIvoCriteria, false, null);
			Assert.assertNotNull(sdIvoContainers);
			Assert.assertEquals(1, sdIvoContainers.size());
			DataContainer sdIvoContainer=sdIvoContainers.get(0);
			String docTypeval=(String) sdIvoContainer.getAttributeValueAtLevel(LockLevel.RDU, DOC_TYPE_ATTR);
			String cinsVal=(String) sdIvoContainer.getAttributeValueAtLevel(LockLevel.RDU, IVO_CINS_ATTR);
			Assert.assertEquals(CINS,cinsVal);
			Assert.assertEquals(IvoDocType.RDU.name(),docTypeval);
			DataRowIterator iteraor=new DataRowIterator(sdIvoContainer, IVO_INSTRUMENT_RELATIONS_ATTR);
			DataRow relationsRow = iteraor.next();
			String ivoRelationType = (String) relationsRow.getAttributeValueAtLevel(LockLevel.RDU, IVO_RELATION_TYYPE_ATTR);
			Assert.assertEquals(RelationType.IVO.name(),ivoRelationType);
			ReferenceId refdVal = (ReferenceId) relationsRow.getAttributeValueAtLevel(LockLevel.RDU, IVO_REF_ID_ATTR);
			Assert.assertEquals(XR_DOC_ID,refdVal.getDocumentId());
			Assert.assertEquals(RDU_INSTRUMENT_ID,refdVal.getObjectId());
			
			//check child containers
			Assert.assertNotNull(sdIvoContainer.getAllChildDataContainers());
			Assert.assertEquals(1, sdIvoContainer.getAllChildDataContainers().size());
			DataContainer sdIvoChildContainer = sdIvoContainer.getAllChildDataContainers().get(0);
			
			String tickerVal = (String) sdIvoChildContainer.getAttributeValueAtLevel(LockLevel.RDU, IVO_TICKER_ATTR);
			Assert.assertEquals(OPS_TICKER, tickerVal);
			
			DataRowIterator secIterator=new DataRowIterator(sdIvoChildContainer, IVO_SECURITY_RELATIONS_ATTR);
			DataRow secRelationsRow = secIterator.next();
			String ivoSecRelationType = (String) secRelationsRow.getAttributeValueAtLevel(LockLevel.RDU, IVO_SEC_RELATION_TYPE_ATTR);
			Assert.assertEquals(RelationType.IVO.name(),ivoSecRelationType);
			ReferenceId secRefdVal = (ReferenceId) secRelationsRow.getAttributeValueAtLevel(LockLevel.RDU, IVO_SEC_REF_ID_ATTR);
			Assert.assertEquals(XR_DOC_ID,secRefdVal.getDocumentId());
			Assert.assertEquals(RDU_SECURITY_ID,secRefdVal.getObjectId());
			
	}

	private void createInputSdIvoContainer(IvoContainer ivoContainer) throws UdmBaseException {
		
		final Map<DataLevel,String> dataLevelVsObjectIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsObjectIdMap.put(DataLevel.INS, SD_INSTRUMENT_ID);
		dataLevelVsObjectIdMap.put(DataLevel.SEC, SECURITY_ID);
		
		final Map<DataLevel,String> dataLevelVsSourceUniqueIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.INS, INS_SRCUNQID);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.SEC, SEC_SRCUNQID);
		
		DataValue<String> cinsVal=new DataValue<>();
		cinsVal.setValue(LockLevel.RDU, CINS);
		ivoContainer.addToDataContainer(CINS_ATTR, cinsVal, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap,DataLevel.IVO_INS);
		
		
		DataValue<String> tickerVal=new DataValue<>() ;
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		ivoContainer.addToDataContainer(TICKER_ATTR, tickerVal, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap,DataLevel.IVO_INS);
	}

	/**
	 * @param ivoContainer 
	 * @return
	 * @throws UdmTechnicalException 
	 */
	private void createInputSdContainer(IvoContainer ivoContainer) throws UdmTechnicalException {
		
		final Map<DataLevel,String> dataLevelVsObjectIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsObjectIdMap.put(DataLevel.INS, SD_INSTRUMENT_ID);
		dataLevelVsObjectIdMap.put(DataLevel.SEC, SECURITY_ID);
		
		final Map<DataLevel,String> dataLevelVsSourceUniqueIdMap=new EnumMap<>(DataLevel.class);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.INS, INS_SRCUNQID);
		dataLevelVsSourceUniqueIdMap.put(DataLevel.SEC, SEC_SRCUNQID);
		
		DataValue<String> isinValue=new DataValue<>();
		isinValue.setValue(LockLevel.RDU, ISIN_OPS);
		ivoContainer.addToDataContainer(ISIN_ATTR, isinValue, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap,DataLevel.INS);
		
		DataValue<String> sedolVal=new DataValue<>() ;
		sedolVal.setValue(LockLevel.RDU, OPS_SEDOL);
		
		DataValue<String> tickerVal=new DataValue<>() ;
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		
		ivoContainer.addToDataContainer(TICKER_ATTR, tickerVal, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap,DataLevel.INS);
		ivoContainer.addToDataContainer(SEDOL_ATTR, sedolVal, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap,DataLevel.INS);
	}

	/**
	 * @return
	 */
	private DataContainerContext createDataContainerContext() {
		return DataContainerContext.builder().withUpdateBy("sajadhav").withComment("edit")
				.withServiceDeskTicketId("UDM-6363").build();
	}

}
