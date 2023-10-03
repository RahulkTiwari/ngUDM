/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LookupServiceTest.java
 * Author:	Jay Sangoi
 * Date:	23-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class LookupServiceTest extends AbstractEmbeddedMongodbJunitParent{

	/**
	 * 
	 */
	private static final DataAttribute SECURITY_STATUS_ATTRIBUTE = SecurityAttrConstant.SECURITY_STATUS;

	/**
	 * 
	 */
	private static final DataAttribute SECURITY_SOURCE_UNIQUE_IDENTIFIER = DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC);

	@Autowired
	private LookupService service;

	@Autowired
	private PersistenceService persistenceService;
	
	private String leiattVal = UUID.randomUUID().toString();

	private DataContainer leContainer = DataContainerTestUtil.getLegalEntityContainer();
	private DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

	@Autowired
	private DataRetrievalService retrivalService;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	private DataAttribute leiLookupAttribute1 = DataAttributeFactory.getAttributeByNameAndLevel("lei",DataLevel.LE);
	
		private DataAttribute insSrcUniqueIdLookupAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel("instrumentSourceUniqueId", DataLevel.INS);
	private DataAttribute secSrcUniqueIdLookupAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel("securitySourceUniqueId", DataLevel.SEC);
	
	private DomainType flagActive;
	private DomainType flagInActive;
	private DomainType flagInActiveAtRduLevel;
	private DomainType datasource;
	private DataValue<DomainType> flagActiveDV;
	private DataValue<DomainType> flagInActiveDV;
	private DataValue<DomainType> datasourceDV;
	private DataValue<DomainType> flagInActiveDVAtRduLevel;

	@Before
	public void setup() {
		flagActive = new DomainType("1",null,null,"tradingStatusMap");
		flagActiveDV = new DataValue<>();
		flagActiveDV.setValue(LockLevel.FEED , flagActive);

		flagInActive = new DomainType("0",null,null,"tradingStatusMap");
		flagInActiveDV = new DataValue<>();
		flagInActiveDV.setValue(LockLevel.FEED , flagInActive);
		
		flagInActiveAtRduLevel = new DomainType();
		flagInActiveAtRduLevel.setNormalizedValue(DomainStatus.INACTIVE);
		flagInActiveDVAtRduLevel = new DataValue<>();
		flagInActiveDVAtRduLevel.setValue(LockLevel.RDU , flagInActiveAtRduLevel);
		
		datasource = new DomainType("trdse");
		datasourceDV = new DataValue<>();
		datasourceDV.setValue(LockLevel.FEED, (DomainType) datasource);
	}

	/**
	 * 
	 */
	private void saveLei() {
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, leiattVal);
		leContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE), value);
		leContainer.addAttributeValue(leiLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		leContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("legalEntityStatus", DataLevel.LE);
		leContainer.addAttributeValue(instrumentStatusAttribute, flagActiveDV);
		
		persistenceService.persist(leContainer);
	}
	
	
	
	

	
	
	

	@Test
	public void test_resolveLookup_Ins_To_Le_Using_SourceUniqueId() throws Exception {
		saveLei();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);

		DataAttribute insLeLink = DataAttributeFactory.getRelationAttributeForInsAndLe();
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insLeLink);
		DataRow ref1 = new DataRow(refData);

		/*ComplexBuilder builder = new ComplexBuilder();*/
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(DataAttributeFactory.getRelationAttributeForInsAndLe());
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Gurantor");
		
		DataAttribute sourceUniqueAttr=DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE);
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, leiattVal);
		
		ref1.addAttribute(sourceUniqueAttr, val);
		
		DataRow link1 = new DataRow(insLeLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);

		/*List<Map<DataAttribute, DataValue<? extends Serializable>>> values = builder.addAttribute(relationType, value)
				.addAttribute(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE), val).get();
		ComplexDataValue cVal = ComplexDataValueFactory.getComplexDataValue(ComplexType.RELATION, values);
*/
		DataRow links1 = new DataRow(insLeLink, valueList1);

		insContainer.addAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndLe(), links1);

		service.resolveLookup(insContainer, null);
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndLe());
		Assert.assertNotNull(attributeValueAtLevel);
		
		DataRowIterator iterator = new DataRowIterator(insContainer, insLeLink);

		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insLeLink));
			
			Assert.assertNotNull(refId);
			
			DataValue<String> valu = new DataValue<>();
			valu.setValue(LockLevel.FEED, leiattVal);

			Criteria c = Criteria.where(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE)).is(valu);
			List<? extends Object> result = retrivalService.retrieve(Database.Mongodb, c,false, null);
			
			Assert.assertEquals(((DataContainer) result.get(0)).get_id(),
					refId.getValue(LockLevel.FEED).getDocumentId().toString())
			;
			

		}
		
		
		/*RelationDataValue dv = (RelationDataValue) attributeValueAtLevel;
		List<Relation> rel = dv.getRelations();

		rel.forEach(LambdaExceptionUtil.rethrowConsumer(sc -> {
			Assert.assertNotNull(((Relation) sc).getReferenceId());

			DataValue<String> valu = new DataValue<>();
			valu.setValue(LockLevel.FEED, leiattVal);

			Criteria c = Criteria.where(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE)).is(valu);
			List<? extends Object> result = retrivalService.retrieve(Database.Mongodb, c, null);

			Assert.assertEquals(((DataContainer) result.get(0)).get_id(),
					((Relation) sc).getReferenceId().getValue(LockLevel.FEED).getDocumentId().toString());
			;

		}));*/
	}
	
	@Test
	public void test_resolveLookup_Ins_To_Le_Using_LookupAttribute() throws Exception {
		saveLei();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);

		/*ComplexBuilder builder = new ComplexBuilder();*/
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(DataAttributeFactory.getRelationAttributeForInsAndLe());

		/*List<Map<DataAttribute, DataValue<? extends Serializable>>> values = builder.addAttribute(relationType, value)
				.addAttribute(leiLookupAttribute1, val).get();
		ComplexDataValue cVal = ComplexDataValueFactory.getComplexDataValue(ComplexType.RELATION, values);*/
		DataAttribute insLeLink = DataAttributeFactory.getRelationAttributeForInsAndLe();
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insLeLink);
		DataRow ref1 = new DataRow(refData);

		/*ComplexBuilder builder = new ComplexBuilder();*/
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Gurantor");
		
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, leiattVal);
		
		ref1.addAttribute(leiLookupAttribute1, val);
		
		DataRow link1 = new DataRow(insLeLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insLeLink, valueList1);

		insContainer.addAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndLe(), links1);
		
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(leiLookupAttribute1.getAttributeName());
		input.setInsLeAttributes(atts);
		service.resolveLookup(insContainer, input);
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndLe());
		Assert.assertNotNull(attributeValueAtLevel);

		DataRowIterator iterator = new DataRowIterator(insContainer, insLeLink);

		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insLeLink));
			
			Assert.assertNotNull(refId);
			
			DataValue<String> valu = new DataValue<>();
			valu.setValue(LockLevel.FEED, leiattVal);

			Criteria c = Criteria.where(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE)).is(valu);
			List<? extends Object> result = retrivalService.retrieve(Database.Mongodb, c,false, null);
			
			Assert.assertEquals(((DataContainer) result.get(0)).get_id(),
					refId.getValue(LockLevel.FEED).getDocumentId().toString())
			;

		}
	}
	
	@Test
	public void test_resolveLookup_Ins_To_Ins_Using_Multilevel_SourceUniqueId() throws Exception {
		// Underlying 1
		String insSourceUniqueId1 = UUID.randomUUID().toString();
		String secSourceUniqueId1 = UUID.randomUUID().toString();
		DataContainer dataContainer = getInsContainer1(flagActiveDV, datasourceDV, insSourceUniqueId1, secSourceUniqueId1);
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		persistenceService.persist(dataContainer);

		// Underlying 1
		String insSourceUniqueId2 = UUID.randomUUID().toString();
		String secSourceUniqueId2 = UUID.randomUUID().toString();
		DataContainer dataContainer2 = getInsContainer1(flagActiveDV, datasourceDV, insSourceUniqueId2, secSourceUniqueId2);
		dataContainer2.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		persistenceService.persist(dataContainer2);

		// Overlying with sec and ins of same underlying container
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();
		DataAttribute insInsLink = AddUnderlyingLookupDetailsInOverlyingContainer(insSourceUniqueId1, secSourceUniqueId1, insContainer);
		
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		// Input with multiple lookup attributes in the order of SEC > INS
		LookupAttributeInput secOverInsInput = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(secSrcUniqueIdLookupAttribute.getAttributeName());
		List<String> att1 = new ArrayList<>();
		atts.add(att1);
		att1.add(insSrcUniqueIdLookupAttribute.getAttributeName());
		secOverInsInput.setInsInsAttributes(atts);

		service.resolveLookup(insContainer, secOverInsInput);
		Serializable attributeValueAtLevel = insContainer.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		assertInsInsRelations(insSourceUniqueId1, insContainer, insInsLink, attributeValueAtLevel, "SEC_1");
		
		// Input with multiple lookup attributes in the order of INS > SEC
		LookupAttributeInput insOverSec = new LookupAttributeInput();
		List<List<String>> insOverSecAtts = new ArrayList<>();
		List<String> insOverSecAtt1 = new ArrayList<>();
		insOverSecAtts.add(insOverSecAtt1);
		insOverSecAtt1.add(insSrcUniqueIdLookupAttribute.getAttributeName());
		List<String> insOverSecAtt = new ArrayList<>();
		insOverSecAtts.add(insOverSecAtt);
		insOverSecAtt.add(secSrcUniqueIdLookupAttribute.getAttributeName());
		insOverSec.setInsInsAttributes(insOverSecAtts);

		service.resolveLookup(insContainer, insOverSec);
		Serializable insOverSecDataRow = insContainer.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		assertInsInsRelations(insSourceUniqueId1, insContainer, insInsLink, insOverSecDataRow, "INS_1");

		// Input with single INS lookup attribute
		LookupAttributeInput singleInsInput = new LookupAttributeInput();
		List<List<String>> insAttrList = new ArrayList<>();
		List<String> singleInsAttr = new ArrayList<>();
		insAttrList.add(singleInsAttr);
		singleInsAttr.add(insSrcUniqueIdLookupAttribute.getAttributeName());
		singleInsInput.setInsInsAttributes(insAttrList);

		service.resolveLookup(insContainer, singleInsInput);
		Serializable insRelation = insContainer.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		assertInsInsRelations(insSourceUniqueId1, insContainer, insInsLink, insRelation, "INS_1");

		// Input with single SEC lookup attribute
		LookupAttributeInput singleSecInput = new LookupAttributeInput();
		List<List<String>> secAttrList = new ArrayList<>();
		List<String> singleSecAttr = new ArrayList<>();
		secAttrList.add(singleSecAttr);
		singleSecAttr.add(secSrcUniqueIdLookupAttribute.getAttributeName());
		singleSecInput.setInsInsAttributes(secAttrList);

		service.resolveLookup(insContainer, singleSecInput);
		Serializable insRelation2 = insContainer.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		assertInsInsRelations(insSourceUniqueId1, insContainer, insInsLink, insRelation2, "SEC_1");

		// Overlying with non-existent sec and ins underlying container
		DataContainer insContainer1 = DataContainerTestUtil.getInstrumentContainer();
		insContainer1.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		DataAttribute insInsLink1 = AddUnderlyingLookupDetailsInOverlyingContainer(insSourceUniqueId1, secSourceUniqueId1 + "_temp", insContainer1);


		// Input with multiple lookup attributes. In this case insSrcUniqueIdLookupAttribute should win
		LookupAttributeInput diffInput = new LookupAttributeInput();
		List<List<String>> atts1 = new ArrayList<>();
		List<String> diffAttrs = new ArrayList<>();
		atts1.add(diffAttrs);
		diffAttrs.add(secSrcUniqueIdLookupAttribute.getAttributeName());
		List<String> diffAttrs1 = new ArrayList<>();
		atts1.add(diffAttrs1);
		diffAttrs1.add(insSrcUniqueIdLookupAttribute.getAttributeName());
		diffInput.setInsInsAttributes(atts1);

		insContainer1.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		service.resolveLookup(insContainer1, diffInput);
		Serializable insInsRel1 = insContainer1.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		assertInsInsRelations(insSourceUniqueId1, insContainer1, insInsLink1, insInsRel1, "INS_1");

		// Overlying with sec and ins of different underlying container
		DataContainer insContainer11 = DataContainerTestUtil.getInstrumentContainer();
		insContainer11.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		DataAttribute insInsLink11 = AddUnderlyingLookupDetailsInOverlyingContainer(insSourceUniqueId1, secSourceUniqueId2, insContainer11);

		// Input with multiple lookup attributes. In this case insSrcUniqueIdLookupAttribute should win
		LookupAttributeInput diffInput1 = new LookupAttributeInput();
		List<List<String>> atts11 = new ArrayList<>();
		List<String> diffAttrs11 = new ArrayList<>();
		atts11.add(diffAttrs11);
		diffAttrs11.add(secSrcUniqueIdLookupAttribute.getAttributeName());
		List<String> diffAttrs111 = new ArrayList<>();
		atts11.add(diffAttrs111);
		diffAttrs111.add(insSrcUniqueIdLookupAttribute.getAttributeName());
		diffInput1.setInsInsAttributes(atts11);

		service.resolveLookup(insContainer11, diffInput1);
		Serializable insInsRel11 = insContainer11.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		Assert.assertNotNull(insInsRel11);

		DataRowIterator insRelationIterator = new DataRowIterator(insContainer11, insInsLink11);
		while(insRelationIterator.hasNext()){
			DataRow row = insRelationIterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink11));
			
			Assert.assertNotNull(refId);
			Assert.assertNull(refId.getValue(LockLevel.FEED));
		}
}

	private DataAttribute AddUnderlyingLookupDetailsInOverlyingContainer(String insSourceUniqueId1, String secSourceUniqueId1, DataContainer insContainer) {
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insInsLink);
		DataRow ref1 = new DataRow(refData);
		
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insInsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Underlying");
		
		DataAttribute insSourceUniqueAttr=DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS);
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, insSourceUniqueId1);
		
		DataAttribute secSourceUniqueAttr1=SECURITY_SOURCE_UNIQUE_IDENTIFIER;
		DataValue<String> val1 = new DataValue<>();
		val1.setValue(LockLevel.FEED, secSourceUniqueId1);
		
		ref1.addAttribute(secSourceUniqueAttr1, val1);
		ref1.addAttribute(insSourceUniqueAttr, val);
		
		DataRow link1 = new DataRow(insInsLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insInsLink, valueList1);

		insContainer.addAttributeValue(insInsLink, links1);
		return insInsLink;
	}

	private void assertInsInsRelations(String insSourceUniqueId1, DataContainer insContainer, DataAttribute insInsLink,
			Serializable insRelation, String objectId) throws UdmTechnicalException {
		Assert.assertNotNull(insRelation);

		DataRowIterator insRelationIterator = new DataRowIterator(insContainer, insInsLink);
		
		while(insRelationIterator.hasNext()){
			DataRow row = insRelationIterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			
			Assert.assertNotNull(refId);
			
			DataValue<String> valu = new DataValue<>();
			valu.setValue(LockLevel.FEED, insSourceUniqueId1);

			Criteria c = Criteria.where(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)).is(valu);
			List<? extends Object> result = retrivalService.retrieve(Database.Mongodb, c,false, null);
			
			Assert.assertEquals(((DataContainer) result.get(0)).get_id(), refId.getValue(LockLevel.FEED).getDocumentId().toString());
			Assert.assertEquals(objectId, refId.getValue(LockLevel.FEED).getObjectId());
		}
	}

	@Test
	public void test_getDbInstrumentContainerForSecurity() throws UdmBaseException{
		String insSourceUniqueId1 = UUID.randomUUID().toString();
		String val11 = UUID.randomUUID().toString();
		String val12 = UUID.randomUUID().toString();
		
		DataContainer insContainer1 = getInsContainer1(flagActiveDV, datasourceDV, insSourceUniqueId1, val11, val12);
		
		String insSourceUniqueId2 = UUID.randomUUID().toString();
		String val21 = UUID.randomUUID().toString();
		String val22 = UUID.randomUUID().toString();

		DataContainer insContainer2 = getInsContainer2(flagActiveDV, datasourceDV, insSourceUniqueId2, val21, val22);

		String insSourceUniqueId21 = UUID.randomUUID().toString();
		String secSourceUniqueId211 = UUID.randomUUID().toString();
		String secSourceUniqueId221 = UUID.randomUUID().toString();

		DataContainer insContainer21 = getInsContainer2(flagActiveDV, datasourceDV, insSourceUniqueId21, secSourceUniqueId211, secSourceUniqueId221);
		DataContainer dataContainer = insContainer21.getChildDataContainers(DataLevel.SEC).get(0);
		DataValue<DomainType> attributeValue1 = getNewStatusFlagLevelForFeedAndRduLock(DataLevel.SEC, flagActive, flagInActiveAtRduLevel);
		dataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.SEC), DataLevel.SEC), attributeValue1);

		String insSourceUniqueId3 = UUID.randomUUID().toString();
		String val31 = UUID.randomUUID().toString();
		String val32 = UUID.randomUUID().toString();

		DataContainer insContainer3 = getInsContainer3(flagActiveDV, datasourceDV, insSourceUniqueId3, val31, val32);

		String insSourceUniqueId4 = UUID.randomUUID().toString();
		String val41 = UUID.randomUUID().toString();
		String val44 = UUID.randomUUID().toString();
		
		DataContainer insContainer4 = getInsContainer4(flagActiveDV, flagInActiveDV, datasourceDV, val11,
				insSourceUniqueId4, val41, val44);
		
		persistenceService.persist(insContainer1);
		persistenceService.persist(insContainer2);
		persistenceService.persist(insContainer21);
		persistenceService.persist(insContainer3);
		persistenceService.persist(insContainer4);

		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;

		/**
		 * Test Case 1 - No security present in db
		 */
		List<DataContainer> searchContainer1 = new ArrayList<>(1);
		DataContainer searchsec11 = DataContainerTestUtil.getSecurityContainer();
		searchContainer1.add(searchsec11);
		DataValue<String> searchSourceUniqueValDV11 = new DataValue<>();
		String searchSourceUniqueVal11 = String.valueOf(UUID.randomUUID().toString());
		searchSourceUniqueValDV11.setValue(LockLevel.FEED, searchSourceUniqueVal11);
		
		List<DataContainer> searchOutput1 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer1);
		
		Assert.assertEquals(Collections.emptyList(), searchOutput1);
		
		/**
		 * Test Case 2 - Test for one security present in db
		 */
		List<DataContainer> searchContainer2 = new ArrayList<>(1);
		DataContainer searchsec21 = DataContainerTestUtil.getSecurityContainer();
		searchContainer2.add(searchsec21);
	
		DataValue<String> searchSourceUniqueValDV21 = new DataValue<>();
		String searchSourceUniqueVal21 = String.valueOf(val11);
		searchSourceUniqueValDV21.setValue(LockLevel.FEED, searchSourceUniqueVal21);
		searchsec21.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV21);
		
		List<DataContainer> searchOutput2 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer2);
		
		Assert.assertNotNull(searchOutput2);
		Assert.assertEquals(1, searchOutput2.size());
		Assert.assertEquals(insSourceUniqueId1, searchOutput2.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));
		
		/**
		 * Test Case 2.1 - Test for one security present in db
		 */
		List<DataContainer> searchContainer21 = new ArrayList<>(1);
		DataContainer searchsec211 = DataContainerTestUtil.getSecurityContainer();
		searchContainer21.add(searchsec211);
		
		DataValue<String> searchSourceUniqueValDV211 = new DataValue<>();
		String searchSourceUniqueVal211 = String.valueOf(secSourceUniqueId211);
		searchSourceUniqueValDV211.setValue(LockLevel.FEED, searchSourceUniqueVal211);
		searchsec211.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV211);
		searchsec211.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
//		searchsec211.addAttributeValue(secFlagActiveAttribute, flagInActiveDVAtRduLevel); // Deactivated by UI from Ops
		
		List<DataContainer> searchOutput21 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer21);
		
		Assert.assertNotNull(searchOutput21);
		Assert.assertEquals(0, searchOutput21.size()); // Because Ops has deactivated the record, it is not returned by the API.

		/**
		 * Test case 3 - Test for multiple security in same instrument
		 */
		List<DataContainer> searchContainer3 = new ArrayList<>(1);
		DataContainer searchsec31 = DataContainerTestUtil.getSecurityContainer();
		searchContainer3.add(searchsec31);
		DataValue<String> searchSourceUniqueValDV31 = new DataValue<>();
		String searchSourceUniqueVal31 = String.valueOf(val11);
		searchSourceUniqueValDV31.setValue(LockLevel.FEED, searchSourceUniqueVal31);
		searchsec31.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV31);

		DataContainer searchsec32 = DataContainerTestUtil.getSecurityContainer();
		searchContainer3.add(searchsec32);
		DataValue<String> searchSourceUniqueValDV32 = new DataValue<>();
		String searchSourceUniqueVal32 = String.valueOf(val12);
		searchSourceUniqueValDV32.setValue(LockLevel.FEED, searchSourceUniqueVal32);
		searchsec32.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV32);
		
		List<DataContainer> searchOutput3 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer3);
		Assert.assertNotNull(searchOutput3);
		Assert.assertEquals(1, searchOutput3.size());
		Assert.assertEquals(insSourceUniqueId1, searchOutput3.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));
		
		/**
		 * Test Case 4 - Test for multiple security in different document
		 */

		List<DataContainer> searchContainer4 = new ArrayList<>(1);
		DataContainer searchsec41 = DataContainerTestUtil.getSecurityContainer();
		searchContainer4.add(searchsec41);
		DataValue<String> searchSourceUniqueValDV41 = new DataValue<>();
		String searchSourceUniqueVal41 = String.valueOf(val11);
		searchSourceUniqueValDV41.setValue(LockLevel.FEED, searchSourceUniqueVal41);
		searchsec41.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV41);

		DataContainer searchsec42 = DataContainerTestUtil.getSecurityContainer();
		searchContainer4.add(searchsec42);
		DataValue<String> searchSourceUniqueValDV42 = new DataValue<>();
		String searchSourceUniqueVal42 = String.valueOf(val22);
		searchSourceUniqueValDV42.setValue(LockLevel.FEED, searchSourceUniqueVal42);
		searchsec42.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV42);
		 	
		List<DataContainer> searchOutput4 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer4);
		Assert.assertNotNull(searchOutput4);
		Assert.assertEquals(2, searchOutput4.size());
		Assert.assertFalse(searchOutput4.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)).equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput4.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId2.equals(searchOutput4.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId2.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));

		/**
		 * Test Case 5 - Test for multiple security in same and different document
		 */

		List<DataContainer> searchContainer5 = new ArrayList<>(1);
		DataContainer searchsec51 = DataContainerTestUtil.getSecurityContainer();
		searchContainer5.add(searchsec51);
		DataValue<String> searchSourceUniqueValDV51 = new DataValue<>();
		String searchSourceUniqueVal51 = String.valueOf(val11);
		searchSourceUniqueValDV51.setValue(LockLevel.FEED, searchSourceUniqueVal51);
		searchsec51.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV51);

		DataContainer searchsec52 = DataContainerTestUtil.getSecurityContainer();
		searchContainer5.add(searchsec52);
		DataValue<String> searchSourceUniqueValDV52 = new DataValue<>();
		String searchSourceUniqueVal52 = String.valueOf(val22);
		searchSourceUniqueValDV52.setValue(LockLevel.FEED, searchSourceUniqueVal52);
		searchsec52.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV52);

		DataContainer searchsec53 = DataContainerTestUtil.getSecurityContainer();
		searchContainer5.add(searchsec53);
		DataValue<String> searchSourceUniqueValDV53 = new DataValue<>();
		String searchSourceUniqueVal53 = String.valueOf(val12);
		searchSourceUniqueValDV53.setValue(LockLevel.FEED, searchSourceUniqueVal53);
		searchsec53.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV53);

		
		List<DataContainer> searchOutput5 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer5);
		Assert.assertNotNull(searchOutput5);
		Assert.assertEquals(2, searchOutput5.size());
		Assert.assertFalse(searchOutput5.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)).equals(searchOutput5.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput5.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId2.equals(searchOutput5.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId2.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		
		
		/**
		 * Test Case 6 - Search for Inactive security
		 */
		
		List<DataContainer> searchContainer6 = new ArrayList<>(6);
		DataContainer searchsec66 = DataContainerTestUtil.getSecurityContainer();
		searchContainer6.add(searchsec66);
		DataValue<String> searchSourceUniqueValDV66 = new DataValue<>();
		searchSourceUniqueValDV66.setValue(LockLevel.FEED, val44);
		searchsec66.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV66);

		List<DataContainer> searchOutput6 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer6);
		
		Assert.assertEquals(Collections.emptyList(), searchOutput6);
		
		/**
		 * Test Case 7 - Search for security which is active andd inactive in same instrument
		 */
		List<DataContainer> searchContainer7 = new ArrayList<>(7);
		DataContainer searchsec77 = DataContainerTestUtil.getSecurityContainer();
		searchContainer7.add(searchsec77);
		DataValue<String> searchSourceUniqueValDV77 = new DataValue<>();
		searchSourceUniqueValDV77.setValue(LockLevel.FEED, val41);
		searchsec77.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV77);
		
		List<DataContainer> searchOutput7 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer7);

		Assert.assertNotNull(searchOutput7);
		Assert.assertEquals(1, searchOutput7.size());
		Assert.assertEquals(insSourceUniqueId4, searchOutput7.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));

		/**
		 * Test Case 8 - Null data source
		 */
		List<DataContainer> searchOutput8 = service.getActiveDbInstrumentContainerForSecurity(null, null);
		
		Assert.assertEquals(0, searchOutput8.size());
		
		/**
		 * Test case 9 - Data container w/o source unique id
		 */
		List<DataContainer> searchContainer9 = new ArrayList<>(6);
		DataContainer searchsec9 = DataContainerTestUtil.getSecurityContainer();
		searchContainer9.add(searchsec9);
		List<DataContainer> searchOutput9 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer9);
		Assert.assertEquals(0, searchOutput9.size());

	}

	private DataValue<DomainType> getNewStatusFlagLevelForFeedAndRduLock(DataLevel sec, DomainType feedValue,
			DomainType rduValue) {
		DataValue<DomainType> flag = new DataValue<>();
		if (feedValue != null)
			flag.setValue(LockLevel.FEED, feedValue);
		if (rduValue != null)
			flag.setValue(LockLevel.RDU, rduValue);
		return flag;
	}

	/**
	 * 1 instrument, 4 securities - 1 Active, 3 inactive (1 is present as both active and inactive)
	 * @param secFlagActiveAttribute
	 * @param flagActiveDV
	 * @param flagInActiveDV
	 * @param datasourceDV
	 * @param secSourceUniqueId2
	 * @param insSourceUniqueId
	 * @param secSourceUniqueId1
	 * @param secSourceUniqueId3
	 * @return
	 */
private DataContainer getInsContainer4(DataValue<DomainType> flagActiveDV,
		DataValue<DomainType> flagInActiveDV, DataValue<DomainType> datasourceDV, String secSourceUniqueId2, String insSourceUniqueId,
		String secSourceUniqueId1, String secSourceUniqueId3) {
	DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
	DataContainer insContainer4 = DataContainerTestUtil.getInstrumentContainer();
	insContainer4.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), datasourceDV);
	DataValue<String> sourceUniqueDV4 = new DataValue<>();
	sourceUniqueDV4.setValue(LockLevel.FEED, insSourceUniqueId);
	insContainer4.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), sourceUniqueDV4);

	DataContainer secContainer41 = DataContainerTestUtil.getSecurityContainer();
	insContainer4.addDataContainer(secContainer41, DataLevel.SEC);
	DataValue<String> sourceUniqueDV41 = new DataValue<>();
	sourceUniqueDV41.setValue(LockLevel.FEED, secSourceUniqueId1);
	secContainer41.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV41);
	secContainer41.addAttributeValue(secFlagActiveAttribute, flagActiveDV);

	DataContainer secContainer42 = DataContainerTestUtil.getSecurityContainer();
	insContainer4.addDataContainer(secContainer42, DataLevel.SEC);
	DataValue<String> sourceUniqueDV42 = new DataValue<>();
	sourceUniqueDV42.setValue(LockLevel.FEED, secSourceUniqueId1);
	secContainer42.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV42);
	secContainer42.addAttributeValue(secFlagActiveAttribute, flagInActiveDV);

	DataContainer secContainer43 = DataContainerTestUtil.getSecurityContainer();
	insContainer4.addDataContainer(secContainer43, DataLevel.SEC);
	DataValue<String> sourceUniqueDV43 = new DataValue<>();
	sourceUniqueDV43.setValue(LockLevel.FEED, secSourceUniqueId2);
	secContainer43.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV43);
	secContainer43.addAttributeValue(secFlagActiveAttribute, flagInActiveDV);

	DataContainer secContainer44 = DataContainerTestUtil.getSecurityContainer();
	insContainer4.addDataContainer(secContainer44, DataLevel.SEC);
	DataValue<String> sourceUniqueDV44 = new DataValue<>();
	sourceUniqueDV44.setValue(LockLevel.FEED, secSourceUniqueId3);
	secContainer44.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV44);
	secContainer44.addAttributeValue(secFlagActiveAttribute, flagInActiveDV);
	return insContainer4;
}

/**
 * 1 instrument, 2 securities - All active
 * @param secFlagActiveAttribute
 * @param flagActiveDV
 * @param datasourceDV
 * @param val3
 * @param val31
 * @param val32
 * @return
 */
private DataContainer getInsContainer3(DataValue<DomainType> flagActiveDV,
		DataValue<DomainType> datasourceDV, String val3, String val31, String val32) {
	DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
	DataContainer insContainer3 = DataContainerTestUtil.getInstrumentContainer();
	insContainer3.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), datasourceDV);
	DataValue<String> sourceUniqueDV3 = new DataValue<>();
	sourceUniqueDV3.setValue(LockLevel.FEED, val3);
	insContainer3.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), sourceUniqueDV3);

	
	DataContainer secContainer31 = DataContainerTestUtil.getSecurityContainer();
	insContainer3.addDataContainer(secContainer31, DataLevel.SEC);
	DataValue<String> sourceUniqueDV31 = new DataValue<>();
	sourceUniqueDV31.setValue(LockLevel.FEED, val31);
	secContainer31.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV31);
	secContainer31.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
	
	
	
	DataContainer secContainer32 = DataContainerTestUtil.getSecurityContainer();
	insContainer3.addDataContainer(secContainer32, DataLevel.SEC);
	DataValue<String> sourceUniqueDV32 = new DataValue<>();
	sourceUniqueDV32.setValue(LockLevel.FEED, val32);
	secContainer32.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV32);
	secContainer32.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
	return insContainer3;
}

/**
 * 1 instrument, 2 securities - All active
 * @param secFlagActiveAttribute
 * @param flagActiveDV
 * @param datasourceDV
 * @param insSourceUniqueId
 * @param secSourceUniqueIds
 * @param val22
 * @return
 */
private DataContainer getInsContainer2(DataValue<DomainType> flagActiveDV,
		DataValue<DomainType> datasourceDV, String insSourceUniqueId, String... secSourceUniqueIds) {
	DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
	DataContainer insContainer2 = DataContainerTestUtil.getInstrumentContainer();
	insContainer2.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), datasourceDV);
	DataValue<String> sourceUniqueDV2 = new DataValue<>();
	sourceUniqueDV2.setValue(LockLevel.FEED, insSourceUniqueId);
	insContainer2.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), sourceUniqueDV2);

	for (String secSourceUniqueId : secSourceUniqueIds) {
		DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, secSourceUniqueId);
		secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV);
		secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		insContainer2.addDataContainer(secContainer, DataLevel.SEC);
	}
	
	
	return insContainer2;
}

	/**
	 * 1 instrument, 2 securities - All active
	 * 
	 * @param secFlagActiveAttribute
	 * @param flagActiveDV
	 * @param datasourceDV
	 * @param insSourceUniqueId
	 * @param secSourceUniqueId1
	 * @param secSourceUniqueId2
	 * @return
	 */
	private DataContainer getInsContainer1(DataValue<DomainType> flagActiveDV, DataValue<DomainType> datasourceDV,
			String insSourceUniqueId, String... secSourceUniqueIds) {

		DataAttribute insFlagActiveAttribute = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS),DataLevel.INS);
		DataContainer insContainer1 = DataContainerTestUtil.getInstrumentContainer();
		insContainer1.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), datasourceDV);
		DataValue<String> sourceUniqueDV1 = new DataValue<>();
		sourceUniqueDV1.setValue(LockLevel.FEED, insSourceUniqueId);
		insContainer1.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), sourceUniqueDV1);
		insContainer1.addAttributeValue(insFlagActiveAttribute, flagActiveDV);
		Integer id = 1;
		DataValue<String> objectId = new DataValue<>();
		objectId.setValue(LockLevel.FEED, DataLevel.INS + "_" + String.valueOf(id));
		insContainer1.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.INS), objectId);

		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		for (String secSourceUniqueId : secSourceUniqueIds) {
			DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
			DataValue<String> sourceUniqueDV = new DataValue<>();
			sourceUniqueDV.setValue(LockLevel.FEED, secSourceUniqueId);
			secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
			DataValue<String> objectId1 = new DataValue<>();
			objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_" + String.valueOf(id++));
			secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
			secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
			insContainer1.addDataContainer(secContainer, DataLevel.SEC);
		}
		
		return insContainer1;
	}

	@Test
	public void test_getDbDataContainersBySourceUniqueIdNullNull() throws UdmBaseException{
		List<DataContainer> dbDataContainersBySourceUniqueId = service.getDbDataContainersBySourceUniqueId(null, null);
		Assert.assertEquals(0, dbDataContainersBySourceUniqueId.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getDbDataContainersBySourceUniqueId() throws UdmBaseException{

		String insSourceUniqueId = UUID.randomUUID().toString();
		String secSourceUniqueId1 = UUID.randomUUID().toString();
		String secSourceUniqueId2 = UUID.randomUUID().toString();
		String secSourceUniqueId3 = UUID.randomUUID().toString();
		
		DataContainer insContainer1 = getInsContainer1(flagActiveDV, datasourceDV, insSourceUniqueId, secSourceUniqueId1, secSourceUniqueId2, secSourceUniqueId3);
		
		DataContainer secDc = insContainer1.getChildDataContainers(DataLevel.SEC).get(2);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secDc.addAttributeValue(secFlagActiveAttribute, flagInActiveDV);
		
		persistenceService.persist(insContainer1);

		List<DataContainer> dbDataContainersBySourceUniqueId1 = service.getDbDataContainersBySourceUniqueId(insContainer1, null);
		Assert.assertEquals(0, dbDataContainersBySourceUniqueId1.size());

		List<DataContainer> dbDataContainersBySourceUniqueId2 = service.getDbDataContainersBySourceUniqueId(insContainer1, datasource);
		Assert.assertEquals(1, dbDataContainersBySourceUniqueId2.size());
		DataContainer dataContainer = dbDataContainersBySourceUniqueId2.get(0);
		Assert.assertNotNull(dataContainer);
		DataValue<String> attributeValue = (DataValue<String>) dataContainer.getAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS));
		Assert.assertEquals(insSourceUniqueId, attributeValue.getValue());
		Assert.assertEquals(3, dbDataContainersBySourceUniqueId2.get(0).getChildDataContainers(DataLevel.SEC).size());
		
	}
	
	@Test
	public void test_getActiveDbDataContainersBySourceUniqueIdNullNull() throws UdmBaseException{
		List<DataContainer> activeDbDataContainersBySourceUniqueId = service.getActiveDbDataContainersBySourceUniqueId(null, null);
		Assert.assertEquals(0, activeDbDataContainersBySourceUniqueId.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getActiveDbDataContainersBySourceUniqueId() throws UdmBaseException{
		String insSourceUniqueId = UUID.randomUUID().toString();
		String secSourceUniqueId1 = UUID.randomUUID().toString();
		String secSourceUniqueId2 = UUID.randomUUID().toString();
		String secSourceUniqueId3 = UUID.randomUUID().toString();
		
		DataContainer insContainer1 = getInsContainer1(flagActiveDV, datasourceDV, insSourceUniqueId, secSourceUniqueId1, secSourceUniqueId2, secSourceUniqueId3);
		
		DataContainer secDc = insContainer1.getChildDataContainers(DataLevel.SEC).get(2);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secDc.addAttributeValue(secFlagActiveAttribute, flagInActiveDV);
		
		persistenceService.persist(insContainer1);

		List<DataContainer> dbDataContainersBySourceUniqueId1 = service.getActiveDbDataContainersBySourceUniqueId(insContainer1, null);
		Assert.assertEquals(0, dbDataContainersBySourceUniqueId1.size());

		List<DataContainer> dbDataContainersBySourceUniqueId2 = service.getActiveDbDataContainersBySourceUniqueId(insContainer1, datasource);
		Assert.assertEquals(1, dbDataContainersBySourceUniqueId2.size());
		DataContainer dataContainer = dbDataContainersBySourceUniqueId2.get(0);
		Assert.assertNotNull(dataContainer);
		DataValue<String> attributeValue = (DataValue<String>) dataContainer.getAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS));
		Assert.assertEquals(insSourceUniqueId, attributeValue.getValue());
		Assert.assertEquals(3, dbDataContainersBySourceUniqueId2.get(0).getChildDataContainers(DataLevel.SEC).size());
	}
	
	@Test
	public void test_getActiveDbInstrumentContainerForSecurity() throws UdmBaseException{
		String insSourceUniqueId1 = UUID.randomUUID().toString();
		String secSourceUniqueId11 = UUID.randomUUID().toString();
		String secSourceUniqueId12 = UUID.randomUUID().toString();
		
		DataContainer insContainer1 = getInsContainer1(flagActiveDV, datasourceDV, insSourceUniqueId1, secSourceUniqueId11, secSourceUniqueId12);
		
		String insSourceUniqueId2 = UUID.randomUUID().toString();
		String secSourceUniqueId21 = UUID.randomUUID().toString();
		String secSourceUniqueId22 = UUID.randomUUID().toString();

		DataContainer insContainer2 = getInsContainer2(flagActiveDV, datasourceDV, insSourceUniqueId2, secSourceUniqueId21, secSourceUniqueId22);

		String insSourceUniqueId21 = UUID.randomUUID().toString();
		String secSourceUniqueId211 = UUID.randomUUID().toString();
		String secSourceUniqueId221 = UUID.randomUUID().toString();
		
		DataContainer insContainer21 = getInsContainer2(flagActiveDV, datasourceDV, insSourceUniqueId21, secSourceUniqueId211, secSourceUniqueId221);
		DataContainer dataContainer = insContainer21.getChildDataContainers(DataLevel.SEC).get(0);
		DataValue<DomainType> attributeValue1 = getNewStatusFlagLevelForFeedAndRduLock(DataLevel.SEC, flagActive, flagInActiveAtRduLevel);
//		DataValue<DomainType> attributeValue = (DataValue<DomainType>) dataContainer.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.SEC), DataLevel.SEC));
//		attributeValue.setValue(LockLevel.FEED, flagActive);
//		attributeValue.setValue(LockLevel.RDU, flagInActiveAtRduLevel);
		dataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.SEC), DataLevel.SEC), attributeValue1);
		
		String insSourceUniqueId3 = UUID.randomUUID().toString();
		String secSourceUniqueId31 = UUID.randomUUID().toString();
		String secSourceUniqueId32 = UUID.randomUUID().toString();

		DataContainer insContainer3 = getInsContainer3(flagActiveDV, datasourceDV, insSourceUniqueId3, secSourceUniqueId31, secSourceUniqueId32);

		String insSourceUniqueId4 = UUID.randomUUID().toString();
		String secSourceUniqueId41 = UUID.randomUUID().toString();
		String secSourceUniqueId44 = UUID.randomUUID().toString();
		
		DataContainer insContainer4 = getInsContainer4(flagActiveDV, flagInActiveDV, datasourceDV, secSourceUniqueId11,
				insSourceUniqueId4, secSourceUniqueId41, secSourceUniqueId44);

		
		persistenceService.persist(insContainer1);
		persistenceService.persist(insContainer2);
		persistenceService.persist(insContainer21);
		persistenceService.persist(insContainer3);
		persistenceService.persist(insContainer4);

		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;

		/**
		 * Test Case 1 - No security present in db
		 */
		List<DataContainer> searchContainer1 = new ArrayList<>(1);
		DataContainer searchsec11 = DataContainerTestUtil.getSecurityContainer();
		searchContainer1.add(searchsec11);
		DataValue<String> searchSourceUniqueValDV11 = new DataValue<>();
		String searchSourceUniqueVal11 = String.valueOf(UUID.randomUUID().toString());
		searchSourceUniqueValDV11.setValue(LockLevel.FEED, searchSourceUniqueVal11);
		
		List<DataContainer> searchOutput1 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer1);
		
		Assert.assertEquals(Collections.emptyList(), searchOutput1);
		
		/**
		 * Test Case 2 - Test for one security present in db
		 */
		List<DataContainer> searchContainer2 = new ArrayList<>(1);
		DataContainer searchsec21 = DataContainerTestUtil.getSecurityContainer();
		searchContainer2.add(searchsec21);
	
		DataValue<String> searchSourceUniqueValDV21 = new DataValue<>();
		String searchSourceUniqueVal21 = String.valueOf(secSourceUniqueId21);
		searchSourceUniqueValDV21.setValue(LockLevel.FEED, searchSourceUniqueVal21);
		searchsec21.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV21);
		searchsec21.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		
		List<DataContainer> searchOutput2 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer2);
		
		Assert.assertNotNull(searchOutput2);
		Assert.assertEquals(1, searchOutput2.size());
		Assert.assertEquals(insSourceUniqueId2, searchOutput2.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));
		
		/**
		 * Test Case 2.1 - Test for one security present in db
		 */
		List<DataContainer> searchContainer21 = new ArrayList<>(1);
		DataContainer searchsec211 = DataContainerTestUtil.getSecurityContainer();
		searchContainer21.add(searchsec211);
		
		DataValue<String> searchSourceUniqueValDV211 = new DataValue<>();
		String searchSourceUniqueVal211 = String.valueOf(secSourceUniqueId211);
		searchSourceUniqueValDV211.setValue(LockLevel.FEED, searchSourceUniqueVal211);
		searchsec211.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV211);
		searchsec211.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		searchsec211.addAttributeValue(secFlagActiveAttribute, flagInActiveDVAtRduLevel); // Deactivated by UI from Ops
		
		List<DataContainer> searchOutput21 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer21);
		
		Assert.assertNotNull(searchOutput21);
		Assert.assertEquals(0, searchOutput21.size()); // Because Ops has deactivated the record, it is not returned by the API.
		
		/**
		 * Test case 3 - Test for multiple security in same instrument
		 */
		List<DataContainer> searchContainer3 = new ArrayList<>(1);
		DataContainer searchsec31 = DataContainerTestUtil.getSecurityContainer();
		searchContainer3.add(searchsec31);
		DataValue<String> searchSourceUniqueValDV31 = new DataValue<>();
		String searchSourceUniqueVal31 = String.valueOf(secSourceUniqueId11);
		searchSourceUniqueValDV31.setValue(LockLevel.FEED, searchSourceUniqueVal31);
		searchsec31.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV31);
		searchsec31.addAttributeValue(secFlagActiveAttribute, flagActiveDV);

		DataContainer searchsec32 = DataContainerTestUtil.getSecurityContainer();
		searchContainer3.add(searchsec32);
		DataValue<String> searchSourceUniqueValDV32 = new DataValue<>();
		String searchSourceUniqueVal32 = String.valueOf(secSourceUniqueId12);
		searchSourceUniqueValDV32.setValue(LockLevel.FEED, searchSourceUniqueVal32);
		searchsec32.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV32);
		searchsec32.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		
		List<DataContainer> searchOutput3 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer3);
		Assert.assertNotNull(searchOutput3);
		Assert.assertEquals(1, searchOutput3.size());
		Assert.assertEquals(insSourceUniqueId1, searchOutput3.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));
		
		/**
		 * Test Case 4 - Test for multiple security in different document
		 */

		List<DataContainer> searchContainer4 = new ArrayList<>(1);
		DataContainer searchsec41 = DataContainerTestUtil.getSecurityContainer();
		searchContainer4.add(searchsec41);
		DataValue<String> searchSourceUniqueValDV41 = new DataValue<>();
		String searchSourceUniqueVal41 = String.valueOf(secSourceUniqueId11);
		searchSourceUniqueValDV41.setValue(LockLevel.FEED, searchSourceUniqueVal41);
		searchsec41.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV41);
		searchsec41.addAttributeValue(secFlagActiveAttribute, flagActiveDV);

		DataContainer searchsec42 = DataContainerTestUtil.getSecurityContainer();
		searchContainer4.add(searchsec42);
		DataValue<String> searchSourceUniqueValDV42 = new DataValue<>();
		String searchSourceUniqueVal42 = String.valueOf(secSourceUniqueId221);
		searchSourceUniqueValDV42.setValue(LockLevel.FEED, searchSourceUniqueVal42);
		searchsec42.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV42);
		searchsec42.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		 	
		List<DataContainer> searchOutput4 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer4);
		Assert.assertNotNull(searchOutput4);
		Assert.assertEquals(2, searchOutput4.size());
		Assert.assertFalse(searchOutput4.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)).equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput4.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId21.equals(searchOutput4.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId21.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));

		/**
		 * Test Case 5 - Test for multiple security in same and different document
		 */

		List<DataContainer> searchContainer5 = new ArrayList<>(1);
		DataContainer searchsec51 = DataContainerTestUtil.getSecurityContainer();
		searchContainer5.add(searchsec51);
		DataValue<String> searchSourceUniqueValDV51 = new DataValue<>();
		String searchSourceUniqueVal51 = String.valueOf(secSourceUniqueId11);
		searchSourceUniqueValDV51.setValue(LockLevel.FEED, searchSourceUniqueVal51);
		searchsec51.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV51);
		searchsec51.addAttributeValue(secFlagActiveAttribute, flagActiveDV);

		DataContainer searchsec52 = DataContainerTestUtil.getSecurityContainer();
		searchContainer5.add(searchsec52);
		DataValue<String> searchSourceUniqueValDV52 = new DataValue<>();
		String searchSourceUniqueVal52 = String.valueOf(secSourceUniqueId221);
		searchSourceUniqueValDV52.setValue(LockLevel.FEED, searchSourceUniqueVal52);
		searchsec52.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV52);
		searchsec52.addAttributeValue(secFlagActiveAttribute, flagActiveDV);

		DataContainer searchsec53 = DataContainerTestUtil.getSecurityContainer();
		searchContainer5.add(searchsec53);
		DataValue<String> searchSourceUniqueValDV53 = new DataValue<>();
		String searchSourceUniqueVal53 = String.valueOf(secSourceUniqueId12);
		searchSourceUniqueValDV53.setValue(LockLevel.FEED, searchSourceUniqueVal53);
		searchsec53.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV53);
		searchsec53.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		
		List<DataContainer> searchOutput5 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer5);
		Assert.assertNotNull(searchOutput5);
		Assert.assertEquals(2, searchOutput5.size());
		Assert.assertFalse(searchOutput5.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)).equals(searchOutput5.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput5.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId21.equals(searchOutput5.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		Assert.assertTrue(insSourceUniqueId1.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))) || insSourceUniqueId21.equals(searchOutput4.get(1).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS))));
		
		
		/**
		 * Test Case 6 - Search for Inactive security
		 */
		
		List<DataContainer> searchContainer6 = new ArrayList<>(6);
		DataContainer searchsec66 = DataContainerTestUtil.getSecurityContainer();
		searchContainer6.add(searchsec66);
		DataValue<String> searchSourceUniqueValDV66 = new DataValue<>();
		searchSourceUniqueValDV66.setValue(LockLevel.FEED, secSourceUniqueId44);
		searchsec66.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV66);
		searchsec66.addAttributeValue(secFlagActiveAttribute, flagActiveDV);

		List<DataContainer> searchOutput6 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer6);
		
		Assert.assertEquals(Collections.emptyList(), searchOutput6);
		
		/**
		 * Test Case 7 - Search for security which is active andd inactive in same instrument
		 */
		List<DataContainer> searchContainer7 = new ArrayList<>(7);
		DataContainer searchsec77 = DataContainerTestUtil.getSecurityContainer();
		searchContainer7.add(searchsec77);
		DataValue<String> searchSourceUniqueValDV77 = new DataValue<>();
		searchSourceUniqueValDV77.setValue(LockLevel.FEED, secSourceUniqueId41);
		searchsec77.addAttributeValue( SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV77);
		searchsec77.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		
		List<DataContainer> searchOutput7 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer7);

		Assert.assertNotNull(searchOutput7);
		Assert.assertEquals(1, searchOutput7.size());
		Assert.assertEquals(insSourceUniqueId4, searchOutput7.get(0).getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)));
		

		/**
		 * Test Case 8 - Null data source
		 */
		List<DataContainer> searchOutput8 = service.getActiveDbInstrumentContainerForSecurity(null, null);
		
		Assert.assertEquals(0, searchOutput8.size());
		
		/**
		 * Test case 9 - Data container w/o source unique id
		 */
		List<DataContainer> searchContainer9 = new ArrayList<>(6);
		DataContainer searchsec9 = DataContainerTestUtil.getSecurityContainer();
		searchContainer9.add(searchsec9);
		List<DataContainer> searchOutput9 = service.getActiveDbInstrumentContainerForSecurity(datasource, searchContainer9);
		Assert.assertEquals(0, searchOutput9.size());

	}
	
	
	
	@Test
	@InputCollectionsPath(paths = "LookupServiceTest/input/sdData.json")
	@ModifiedCollections(collections = "sdData")
	public void test_getDbInstrumentContainerForSecurity_inactiveWithOpsLock() throws UdmBaseException {
		
		List<DataContainer> searchContainer1 = new ArrayList<>(1);
		DataContainer searchsec11 = DataContainerTestUtil.getSecurityContainer();
		searchContainer1.add(searchsec11);
		DataValue<String> searchSourceUniqueValDV11 = new DataValue<>();
		searchSourceUniqueValDV11.setValue(LockLevel.FEED, "NO0010785967.MUND");
		searchsec11.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, searchSourceUniqueValDV11);
		List<DataContainer> activeDbInstrumentContainerForSecurity = service.getActiveDbInstrumentContainerForSecurity(new DomainType("esmaFirds"), searchContainer1);
		assertEquals(0, activeDbInstrumentContainerForSecurity.size());
	}
	
	@Test
	@InputCollectionsPath(paths = "LookupServiceTest/input/UDM-54724/sdData.json")
	@ModifiedCollections(collections = "sdData")
	public void test_UDM_54724() throws UdmBaseException, IOException {
		
		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"LookupServiceTest/input/UDM-54724/feedInput.json", SdData.class);
		
		List<DataContainer> childDataContainers = inputContainers.get(0).getAllChildDataContainers();			
		List<DataContainer> activeDbInstrumentContainerForSecurity = service.getActiveDbInstrumentContainerForSecurity(new DomainType("idcApex"), childDataContainers);
		assertEquals(1, activeDbInstrumentContainerForSecurity.size());
	}
	
	
	@Test
	@InputCollectionsPath(paths = "LookupServiceTest/input/UDM-54311/sdData.json")
	@ModifiedCollections(collections = "sdData")
	public void test_UDM_54311() throws UdmBaseException, IOException {
		
		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"LookupServiceTest/input/UDM-54311/feedInput.json", SdData.class);
		
		List<DataContainer> childDataContainers = inputContainers.get(0).getAllChildDataContainers();
		List<DataContainer> activeContainers = service.getActiveDbDataContainersBySourceUniqueId(childDataContainers.get(0), new DomainType("trdse"));

		assertEquals(1, activeContainers.size());
	
	}
	
	@Test
	@InputCollectionsPath(paths = "LookupServiceTest/input/UDM-54311/sdData.json")
	@ModifiedCollections(collections = "sdData")
	public void test_DataContainers() throws UdmBaseException, IOException {
		
		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"LookupServiceTest/input/UDM-54311/sdData.json", SdData.class);
		
		DataContainer container = inputContainers.get(0);
		List<DataContainer> activeContainers = service.getActiveDbDataContainersBySourceUniqueId(container, new DomainType("trdse"));

		assertEquals(1, activeContainers.size());
	
	}
	
	@Test
	public void test_DataContainers_NoContainer() throws UdmBaseException, IOException {
		
		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"LookupServiceTest/input/UDM-54311/sdData.json", SdData.class);
		List<DataContainer> activeContainers = service.getActiveDbDataContainersBySourceUniqueId(inputContainers.get(0), new DomainType("trdse"));

		Assert.assertTrue(activeContainers.isEmpty());
	
	}
	
	@Test
	public void test_populateDataSource() throws UdmBaseException, IOException {		
		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"LookupServiceTest/input/testDataSource/sdData.json", SdData.class);		
		DataContainer dataContainer = inputContainers.get(0);
		service.populateDataSource(dataContainer, "trdse");
		Serializable highestPriorityValue = dataContainer.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute());
		Assert.assertNotNull(highestPriorityValue);
	}
	
	@Test
	public void test_populateDataSource_Empty() throws UdmBaseException, IOException {		
		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"LookupServiceTest/input/testDataSource/sdData.json", SdData.class);		
		DataContainer dataContainer = inputContainers.get(0);
		service.populateDataSource(dataContainer, null);
		Serializable highestPriorityValue = dataContainer.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute());
		Assert.assertNull(highestPriorityValue);
	}
	
	@Test
	public void test_DataContainers_Invalid_Status() throws UdmBaseException, IOException {
		
		List<DataContainer> inputContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"LookupServiceTest/input/inValidStatus/sdData.json", SdData.class);
		
		DataContainer container = inputContainers.get(0);
		List<DataContainer> activeContainers = service.getActiveDbDataContainersBySourceUniqueId(container, new DomainType("trdse"));

		assertEquals(0, activeContainers.size());	
	}

}
