package com.smartstreamrdu.service.lookup;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;
import com.smartstreamrdu.util.Constant.DomainStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class LookupRelationshipTest extends AbstractEmbeddedMongodbJunitParent{

	/**
	 * 
	 */
	private static final DataAttribute SECURITY_STATUS_ATTRIBUTE = SecurityAttrConstant.SECURITY_STATUS;
	
	private static final DataAttribute LOOKUP_ATTRIBUTE_VALUE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("lookupLevel", DataLevel.INS, InstrumentAttrConstant.INSTRUMENT_RELATIONS);
	
	/**
	 * 
	 */
	private static final DataAttribute SECURITY_SOURCE_UNIQUE_IDENTIFIER = DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC);

	@Autowired
	private LookupService service;

	@Autowired
	private PersistenceService persistenceService;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	private String insattVal = UUID.randomUUID().toString();
	
	private String insattVal2 = UUID.randomUUID().toString();

	private DataContainer leContainer1 = DataContainerTestUtil.getLegalEntityContainer();
	private DataContainer leContainer2 = DataContainerTestUtil.getLegalEntityContainer();
	private DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();
	private DataContainer insContainer2 = DataContainerTestUtil.getInstrumentContainer();

	@Autowired
	private DataRetrievalService retrivalService;
	
	private DataAttribute leiLookupAttribute1 = DataAttributeFactory.getAttributeByNameAndLevel("lei",DataLevel.LE);
	
	private DataAttribute leiLookupAttribute2 = DataAttributeFactory.getAttributeByNameAndLevel("legalEntitySourceUniqueId",DataLevel.LE);
	
	private DataAttribute insLookupAttribute1 = DataAttributeFactory.getAttributeByNameAndLevel("trQuoteId",DataLevel.SEC);
	
	private DomainType flagActive;
	private DomainType flagInActive;
	private DomainType flagInActiveAtRduLevel;
	private DomainType flagActiveAtRduLevel;
	private DomainType datasource;
	private DataValue<DomainType> flagActiveDV;
	private DataValue<DomainType> flagInActiveDV;
	private DataValue<DomainType> datasourceDV;
	private DataValue<DomainType> flagInActiveDVAtRduLevel;
	private DataValue<DomainType> flagActiveDVAtRduLevel;

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
		
		
		flagActiveAtRduLevel = new DomainType();
		flagActiveAtRduLevel.setNormalizedValue(DomainStatus.ACTIVE);
		flagActiveDVAtRduLevel = new DataValue<>();
		flagActiveDVAtRduLevel.setValue(LockLevel.RDU , flagActiveAtRduLevel);
		
		
		datasource = new DomainType("trdse");
		datasourceDV = new DataValue<>();
		datasourceDV.setValue(LockLevel.FEED, (DomainType) datasource);
	}

	private void saveUnderlyingInsWithTwoSecurity() {
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, insattVal);
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		//insContainer.addAttributeValue(insLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
		insContainer.addAttributeValue(instrumentStatusAttribute, flagActiveDV);
		
		
		DataContainer secContainer1 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer1.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer1.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer1.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		secContainer1.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);
		
		DataContainer secContainer2 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV2 = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer2.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV2);
		DataValue<String> objectId2 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer2.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId2);
		DataAttribute secFlagActiveAttribute2 = SECURITY_STATUS_ATTRIBUTE;
		secContainer2.addAttributeValue(secFlagActiveAttribute2, flagInActiveDV);
		DataValue<String> value2 = new DataValue<>();
		value2.setValue(LockLevel.FEED, "222222");
		secContainer2.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer2, DataLevel.SEC);
		
		
		
		persistenceService.persist(insContainer);
	}
	
	private void saveUnderlyingInsWithThreeSecurity_A_A_I() {
		
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, insattVal);
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		//insContainer.addAttributeValue(insLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
		insContainer.addAttributeValue(instrumentStatusAttribute, flagActiveDV);
		
		
		DataContainer secContainer1 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer1.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer1.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer1.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		secContainer1.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);
		
		DataContainer secContainer2 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV2 = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer2.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV2);
		DataValue<String> objectId2 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer2.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId2);
		DataAttribute secFlagActiveAttribute2 = SECURITY_STATUS_ATTRIBUTE;
		secContainer2.addAttributeValue(secFlagActiveAttribute2, flagActiveDVAtRduLevel);
		DataValue<String> value2 = new DataValue<>();
		value2.setValue(LockLevel.FEED, "222222");
		secContainer2.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer2, DataLevel.SEC);
		
		
		DataContainer secContainer3 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV3 = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer3.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV3);
		DataValue<String> objectId3 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer3.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId3);
		DataAttribute secFlagActiveAttribute3 = SECURITY_STATUS_ATTRIBUTE;
		secContainer3.addAttributeValue(secFlagActiveAttribute3, flagInActiveDV);
		value2.setValue(LockLevel.FEED, "222222");
		secContainer3.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer3, DataLevel.SEC);
		persistenceService.persist(insContainer);
	}
	
	

	private void saveUnderlyingInsWithTwoSecurity_A_A_AcrossDifferentIns() {

		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, insattVal);
		DataValue<String> value_2 = new DataValue<>();
		value_2.setValue(LockLevel.FEED, insattVal2);
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		insContainer2.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		// insContainer.addAttributeValue(insLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource",
				DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		insContainer2.addAttributeValue(attributeByNameAndLevelDataSource, dv);

		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus",
				DataLevel.INS);
		insContainer.addAttributeValue(instrumentStatusAttribute, flagActiveDV);
		insContainer2.addAttributeValue(instrumentStatusAttribute, flagActiveDV);

		DataContainer secContainer1 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer1.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer1.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer1.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		secContainer1.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);

		DataContainer secContainer2 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV2 = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer2.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV2);
		DataValue<String> objectId2 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer2.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId2);
		DataAttribute secFlagActiveAttribute2 = SECURITY_STATUS_ATTRIBUTE;
		secContainer2.addAttributeValue(secFlagActiveAttribute2, flagActiveDVAtRduLevel);
		//secContainer2.addAttributeValue(secFlagActiveAttribute2, flagInActiveDVAtRduLevel);
		DataValue<String> value2 = new DataValue<>();
		value2.setValue(LockLevel.FEED, "222222");
		secContainer2.addAttributeValue(insLookupAttribute1, value);
		insContainer2.addDataContainer(secContainer2, DataLevel.SEC);

		DataContainer secContainer3 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV3 = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer3.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV3);
		DataValue<String> objectId3 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer3.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId3);
		DataAttribute secFlagActiveAttribute3 = SECURITY_STATUS_ATTRIBUTE;
		secContainer3.addAttributeValue(secFlagActiveAttribute3, flagInActiveDV);
		value2.setValue(LockLevel.FEED, "222222");
		secContainer3.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer3, DataLevel.SEC);
		persistenceService.persist(insContainer);
		persistenceService.persist(insContainer2);
	}
	
	
	
	private void saveUnderlyingInsWithTwoSecurity_A_I_AcrossDifferentIns() {

		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, insattVal);
		DataValue<String> value_2 = new DataValue<>();
		value_2.setValue(LockLevel.FEED, insattVal2);
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		insContainer2.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		// insContainer.addAttributeValue(insLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource",
				DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		insContainer2.addAttributeValue(attributeByNameAndLevelDataSource, dv);

		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus",
				DataLevel.INS);
		insContainer.addAttributeValue(instrumentStatusAttribute, flagActiveDV);
		insContainer2.addAttributeValue(instrumentStatusAttribute, flagActiveDV);

		DataContainer secContainer1 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer1.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer1.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer1.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		secContainer1.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);

		DataContainer secContainer2 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV2 = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer2.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV2);
		DataValue<String> objectId2 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer2.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId2);
		DataAttribute secFlagActiveAttribute2 = SECURITY_STATUS_ATTRIBUTE;
		secContainer2.addAttributeValue(secFlagActiveAttribute2, flagInActiveDVAtRduLevel);
		DataValue<String> value2 = new DataValue<>();
		value2.setValue(LockLevel.FEED, "222222");
		secContainer2.addAttributeValue(insLookupAttribute1, value);
		insContainer2.addDataContainer(secContainer2, DataLevel.SEC);

		DataContainer secContainer3 = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV3 = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer3.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER, sourceUniqueDV3);
		DataValue<String> objectId3 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer3.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId3);
		DataAttribute secFlagActiveAttribute3 = SECURITY_STATUS_ATTRIBUTE;
		secContainer3.addAttributeValue(secFlagActiveAttribute3, flagInActiveDV);
		value2.setValue(LockLevel.FEED, "222222");
		secContainer3.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer3, DataLevel.SEC);
		persistenceService.persist(insContainer);
		persistenceService.persist(insContainer2);
	}
	
	
	

	@Test
	public void test_resolveLookup_Ins_To_Ins_Using_LookupAttribute_MultipleSecurity_A_A_I() throws Exception {
		saveUnderlyingInsWithThreeSecurity_A_A_I();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

		/*ComplexBuilder builder = new ComplexBuilder();
		DataValue<RelationType> value = new DataValue<>();
		value.setValue(LockLevel.FEED, RelationType.UNDERLYING);
		DataValue<String> val = new DataValue<>();*/
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insInsLink);
		DataRow ref1 = new DataRow(refData);
		
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insInsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Underlying");
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, insattVal);
	
		
		ref1.addAttribute(insLookupAttribute1, val);
		
		DataRow link1 = new DataRow(insInsLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insInsLink, valueList1);

		insContainer.addAttributeValue(insInsLink, links1);
		
		DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		insContainer.addDataContainer(secContainer, DataLevel.SEC);

		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(insLookupAttribute1.getAttributeName());
		input.setInsInsAttributes(atts);
		service.resolveLookup(insContainer, input);
		
		
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(insInsLink);
		
		Assert.assertNotNull(attributeValueAtLevel);

		
		DataRowIterator iterator = new DataRowIterator(insContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			Assert.assertNotNull(refId);
			Assert.assertNull(refId.getValue(LockLevel.FEED));
		}
		

	}
	
	
	@Test
	public void test_resolveLookup_Ins_To_Ins_Using_LookupAttribute_MultipleSecurity_AcrossMultipleInstrument_A_I_() throws Exception {
		saveUnderlyingInsWithTwoSecurity_A_I_AcrossDifferentIns();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

		/*ComplexBuilder builder = new ComplexBuilder();
		DataValue<RelationType> value = new DataValue<>();
		value.setValue(LockLevel.FEED, RelationType.UNDERLYING);
		DataValue<String> val = new DataValue<>();*/
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insInsLink);
		DataRow ref1 = new DataRow(refData);
		
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insInsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Underlying");
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, insattVal);
	
		
		ref1.addAttribute(insLookupAttribute1, val);
		
		DataRow link1 = new DataRow(insInsLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insInsLink, valueList1);

		insContainer.addAttributeValue(insInsLink, links1);
		
		DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		insContainer.addDataContainer(secContainer, DataLevel.SEC);

		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(insLookupAttribute1.getAttributeName());
		input.setInsInsAttributes(atts);
		service.resolveLookup(insContainer, input);
		
		
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(insInsLink);
		
		Assert.assertNotNull(attributeValueAtLevel);

		
		DataRowIterator iterator = new DataRowIterator(insContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			Assert.assertNotNull(refId);
			Assert.assertNotNull(refId.getValue(LockLevel.FEED));
			
			DataValue<String> lookupValue= row.getAttributeValue(LOOKUP_ATTRIBUTE_VALUE);
			Assert.assertNotNull(lookupValue);
			Assert.assertNotNull(lookupValue.getValue(LockLevel.RDU));
		}
		

	}
	
	
	
	@Test
	public void test_resolveLookup_Ins_To_Ins_Using_LookupAttribute_MultipleSecurity_AcrossMultipleInstrument_A_A_() throws Exception {
		saveUnderlyingInsWithTwoSecurity_A_A_AcrossDifferentIns();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

		/*ComplexBuilder builder = new ComplexBuilder();
		DataValue<RelationType> value = new DataValue<>();
		value.setValue(LockLevel.FEED, RelationType.UNDERLYING);
		DataValue<String> val = new DataValue<>();*/
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insInsLink);
		DataRow ref1 = new DataRow(refData);
		
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insInsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Underlying");
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, insattVal);
	
		
		ref1.addAttribute(insLookupAttribute1, val);
		
		DataRow link1 = new DataRow(insInsLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insInsLink, valueList1);

		insContainer.addAttributeValue(insInsLink, links1);
		
		DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		insContainer.addDataContainer(secContainer, DataLevel.SEC);

		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(insLookupAttribute1.getAttributeName());
		input.setInsInsAttributes(atts);
		service.resolveLookup(insContainer, input);
		
		
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(insInsLink);
		
		Assert.assertNotNull(attributeValueAtLevel);

		
		DataRowIterator iterator = new DataRowIterator(insContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			Assert.assertNotNull(refId);
			Assert.assertNull(refId.getValue(LockLevel.FEED));
		}
		

	}
	
	
	
	
	@Test
	public void test_resolveLookup_Ins_To_Ins_Using_LookupAttribute_MultipleSecurity_A_A() throws Exception {
		saveUnderlyingInsWithTwoSecurity();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

		/*ComplexBuilder builder = new ComplexBuilder();
		DataValue<RelationType> value = new DataValue<>();
		value.setValue(LockLevel.FEED, RelationType.UNDERLYING);
		DataValue<String> val = new DataValue<>();*/
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insInsLink);
		DataRow ref1 = new DataRow(refData);
		
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insInsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Underlying");
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, insattVal);
	
		
		ref1.addAttribute(insLookupAttribute1, val);
		
		DataRow link1 = new DataRow(insInsLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insInsLink, valueList1);

		insContainer.addAttributeValue(insInsLink, links1);
		
		DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		insContainer.addDataContainer(secContainer, DataLevel.SEC);

		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(insLookupAttribute1.getAttributeName());
		input.setInsInsAttributes(atts);
		service.resolveLookup(insContainer, input);
		
		
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(insInsLink);
		
		Assert.assertNotNull(attributeValueAtLevel);

		
		DataRowIterator iterator = new DataRowIterator(insContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			Assert.assertNotNull(refId);
			
			DataValue<String> valu = new DataValue<>();
			valu.setValue(LockLevel.FEED, insattVal);
			
			DataValue<String> lookupValue= row.getAttributeValue(LOOKUP_ATTRIBUTE_VALUE);
			Assert.assertNotNull(lookupValue);
			Assert.assertNotNull(lookupValue.getValue(LockLevel.RDU));

			Criteria c = Criteria.where(insLookupAttribute1).is(valu);
			List<? extends Object> result = retrivalService.retrieve(Database.Mongodb, c,false, null);

			Assert.assertEquals(((DataContainer) result.get(0)).get_id(),
					refId.getValue(LockLevel.FEED).getDocumentId().toString())
			;
		}
		

	}
	
	
	@Test
	public void test_resolveLookup_Ins_To_Ins_Using_LookupAttribute() throws Exception {
		saveUnderlyingIns();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

		/*ComplexBuilder builder = new ComplexBuilder();
		DataValue<RelationType> value = new DataValue<>();
		value.setValue(LockLevel.FEED, RelationType.UNDERLYING);
		DataValue<String> val = new DataValue<>();*/
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insInsLink);
		DataRow ref1 = new DataRow(refData);
		
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insInsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Underlying");
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, insattVal);
	
		
		ref1.addAttribute(insLookupAttribute1, val);
		
		DataRow link1 = new DataRow(insInsLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insInsLink, valueList1);

		insContainer.addAttributeValue(insInsLink, links1);
		
		DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		insContainer.addDataContainer(secContainer, DataLevel.SEC);
		


/*		List<Map<DataAttribute, DataValue<? extends Serializable>>> values = builder.addAttribute(relationType, value)
				.addAttribute(insLookupAttribute1, val).get();
		ComplexDataValue cVal = ComplexDataValueFactory.getComplexDataValue(ComplexType.RELATION, values);

		insContainer.addAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns(), cVal);*/
		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(insLookupAttribute1.getAttributeName());
		input.setInsInsAttributes(atts);
		service.resolveLookup(insContainer, input);
		
		
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(insInsLink);
		
		Assert.assertNotNull(attributeValueAtLevel);

		
		DataRowIterator iterator = new DataRowIterator(insContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			Assert.assertNotNull(refId);
			
			DataValue<String> valu = new DataValue<>();
			valu.setValue(LockLevel.FEED, insattVal);
			
			DataValue<String> lookupValue= row.getAttributeValue(LOOKUP_ATTRIBUTE_VALUE);
			Assert.assertNotNull(lookupValue);
			Assert.assertNotNull(lookupValue.getValue(LockLevel.RDU));

			Criteria c = Criteria.where(insLookupAttribute1).is(valu);
			List<? extends Object> result = retrivalService.retrieve(Database.Mongodb, c,false, null);

			Assert.assertEquals(((DataContainer) result.get(0)).get_id(),
					refId.getValue(LockLevel.FEED).getDocumentId().toString())
			;
		}
		

	}
	
	private void saveUnderlyingIns() {
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, insattVal);
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), value);
		//insContainer.addAttributeValue(insLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
		insContainer.addAttributeValue(instrumentStatusAttribute, flagActiveDV);
		
		
		DataContainer secContainer = DataContainerTestUtil.getSecurityContainer();
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, "1234");
		secContainer.addAttributeValue(SECURITY_SOURCE_UNIQUE_IDENTIFIER,	sourceUniqueDV);
		DataValue<String> objectId1 = new DataValue<>();
		objectId1.setValue(LockLevel.FEED, DataLevel.SEC + "_1");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), objectId1);
		DataAttribute secFlagActiveAttribute = SECURITY_STATUS_ATTRIBUTE;
		secContainer.addAttributeValue(secFlagActiveAttribute, flagActiveDV);
		secContainer.addAttributeValue(insLookupAttribute1, value);
		insContainer.addDataContainer(secContainer, DataLevel.SEC);
		
		persistenceService.persist(insContainer);
	}
	
	
	@Test
	public void test_resolveLookup_Ins_To_Ins_Using_SourceUniqueId() throws Exception {
		saveUnderlyingIns();
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		
		/*ComplexBuilder builder = new ComplexBuilder();
		DataValue<RelationType> value = new DataValue<>();
		value.setValue(LockLevel.FEED, RelationType.UNDERLYING);
		*/
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		DataAttribute refData=DataAttributeFactory.getRelationRefDataAttribute(insInsLink);
		DataRow ref1 = new DataRow(refData);
		
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insInsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Underlying");
		/*List<Map<DataAttribute, DataValue<? extends Serializable>>> values = builder.addAttribute(relationType, value)
				.addAttribute(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), val).get();
		ComplexDataValue cVal = ComplexDataValueFactory.getComplexDataValue(ComplexType.RELATION, values);
*/		
		
		DataAttribute sourceUniqueAttr=DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS);
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, insattVal);
	
		
		ref1.addAttribute(sourceUniqueAttr, val);
		
		DataRow link1 = new DataRow(insInsLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);
		
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		DataRow links1 = new DataRow(insInsLink, valueList1);

		insContainer.addAttributeValue(insInsLink, links1);
		

		service.resolveLookup(insContainer, null);
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		Assert.assertNotNull(attributeValueAtLevel);
		DataRowIterator iterator = new DataRowIterator(insContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			
			Assert.assertNotNull(refId);
			
			DataValue<String> lookupValue= row.getAttributeValue(LOOKUP_ATTRIBUTE_VALUE);
			Assert.assertNotNull(lookupValue);
			Assert.assertNotNull(lookupValue.getValue(LockLevel.RDU));
			
			DataValue<String> valu = new DataValue<>();
			valu.setValue(LockLevel.FEED, insattVal);

			Criteria c = Criteria.where(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS)).is(valu);
			List<? extends Object> result = retrivalService.retrieve(Database.Mongodb, c,false, null);
			
			Assert.assertEquals(((DataContainer) result.get(0)).get_id(),
					refId.getValue(LockLevel.FEED).getDocumentId().toString())
			;
			

		}
		
	}
	
	@Test
	public void test_resolveLookup_Relationship_having_SameSourceUniqueId_AcrossDifferentDataSource() throws Exception {
		saveLeiOfTrdse(); //legalEntitySourceUniqueId is 112958609 for trdse
		saveLeiOfApex(); //legalEntitySourceUniqueId is 112958609 for idcApex
		
		DataContainer insContainer = DataContainerTestUtil.getInstrumentContainer();

		DomainType domain = new DomainType();
		domain.setVal("idcApex");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource",
				DataLevel.Document);
		insContainer.addAttributeValue(attributeByNameAndLevelDataSource, dv);

		DataAttribute insLeLink = DataAttributeFactory.getRelationAttributeForInsAndLe();
		DataAttribute refData = DataAttributeFactory.getRelationRefDataAttribute(insLeLink);
		DataRow ref1 = new DataRow(refData);

		/* ComplexBuilder builder = new ComplexBuilder(); */
		DataAttribute relationType = DataAttributeFactory
				.getRelationTypeAttribute(DataAttributeFactory.getRelationAttributeForInsAndLe());
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "Gurantor");

		DataAttribute sourceUniqueAttr = DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE);
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, "112958609");

		ref1.addAttribute(sourceUniqueAttr, val);

		DataRow link1 = new DataRow(insLeLink);
		link1.addAttribute(relationType, value);
		link1.addAttribute(refData, ref1);

		ArrayList<DataRow> linkList1 = new ArrayList<>();
		linkList1.add(link1);

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);

		DataRow links1 = new DataRow(insLeLink, valueList1);

		insContainer.addAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndLe(), links1);
		
		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(leiLookupAttribute2.getAttributeName());
		input.setInsLeAttributes(atts);
		

		service.resolveLookup(insContainer, input);
		Serializable attributeValueAtLevel = insContainer
				.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndLe());
		Assert.assertNotNull(attributeValueAtLevel);
	}
	
	@Test
	@InputCollectionsPath(paths = "LookupRelationshipTest/input/sdData.json")
	@ModifiedCollections(collections = {"sdData"})
	public void test_resolveLookup_Ins_To_Ins_Using_LookupAttribute_withInavalidUnderlyingStatus()
			throws UdmBaseException, IOException {
		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("LookupRelationshipTest/input/overlying.json", SdData.class)
				.get(0);
		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(insLookupAttribute1.getAttributeName());
		input.setInsInsAttributes(atts);
		service.resolveLookup(instrumentContainer, input);
		
		Serializable attributeValueAtLevel = instrumentContainer
				.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		Assert.assertNotNull(attributeValueAtLevel);
		
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		DataRowIterator iterator = new DataRowIterator(instrumentContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			Assert.assertNotNull(refId);
			assertNull(refId.getValue());
		}
		
	}
	
	@Test
	@InputCollectionsPath(paths = "LookupRelationshipTest/InactiveUnderlyingSecurityStatus/sdData.json")
	@ModifiedCollections(collections = {"sdData"})
	public void test_resolveLookup_Ins_To_Ins_Using_LookupAttribute_withInactiveUnderlyingSecurityStatus()
			throws UdmBaseException, IOException {
		DataContainer instrumentContainer = bsonConverter
				.getListOfDataContainersFromFilePath("LookupRelationshipTest/InactiveUnderlyingSecurityStatus/overlying.json", SdData.class)
				.get(0);
		LookupAttributeInput input = new LookupAttributeInput();
		List<List<String>> atts = new ArrayList<>();
		List<String> att = new ArrayList<>();
		atts.add(att);
		att.add(insLookupAttribute1.getAttributeName());
		input.setInsInsAttributes(atts);
		service.resolveLookup(instrumentContainer, input);
		
		Serializable attributeValueAtLevel = instrumentContainer
				.getAttributeValue(DataAttributeFactory.getRelationAttributeForInsAndIns());
		Assert.assertNotNull(attributeValueAtLevel);
		
		DataAttribute insInsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		DataRowIterator iterator = new DataRowIterator(instrumentContainer, insInsLink);
		
		while(iterator.hasNext()){
			DataRow row = iterator.next();
			DataValue<ReferenceId> refId = row.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsLink));
			Assert.assertNotNull(refId);
			assertNull(refId.getValue());
			
			DataValue<String> lookupValue= row.getAttributeValue(LOOKUP_ATTRIBUTE_VALUE);
			Assert.assertNotNull(lookupValue);
			assertNull(lookupValue.getValue(LockLevel.RDU));
		}
		
	}

	private void saveLeiOfTrdse() {
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "112958609");
		leContainer1.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE), value);
		leContainer1.addAttributeValue(leiLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		leContainer1.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		persistenceService.persist(leContainer1);
	}
	
	private void saveLeiOfApex() {
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, "112958609");
		leContainer2.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE), value);
		leContainer2.addAttributeValue(leiLookupAttribute1, value);
		DomainType domain = new DomainType();
		domain.setVal("idcApex");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		DataAttribute attributeByNameAndLevelDataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
		leContainer2.addAttributeValue(attributeByNameAndLevelDataSource, dv);
		persistenceService.persist(leContainer2);
	}
	
}
