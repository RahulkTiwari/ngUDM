package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.IvoDocType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.EnData;
import com.smartstreamrdu.persistence.domain.autoconstants.EnDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.EnUnderlyingsAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.util.MockUtil;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.IvoConstants;

@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { IvoLockServiceConfig.class })
public class IvoLockRemovalServiceImplTest extends IvoLockTestUtil{

	private static final DataAttribute EnUnderlyingSedolAttribute = DataAttributeFactory.
			getAttributeByNameAndLevelAndParent(EnUnderlyingsAttrConstant.COL_SEDOL, DataLevel.EN, EnDataAttrConstant.UNDERLYINGS);

	private static final DataAttribute EnUnderlyingIsinAttribute = DataAttributeFactory.
			getAttributeByNameAndLevelAndParent(EnUnderlyingsAttrConstant.COL_ISIN, DataLevel.EN, EnDataAttrConstant.UNDERLYINGS);

	@Autowired
	PersistenceService persistenceService;
	
	@Autowired
	DataRetrievalService retrivalService;
	
	@Autowired
	IvoLockRemovalService lockRemovalService;
	
	@Autowired
	private ListenerService listenerService;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	protected static final String FEED_TICKER = "GENWB";
	protected static final String RDU_SECURITY_ID = "RDUNSE000000000001";
	protected static final String FEED_AUS_INS_ID = "FEED_AUS_INS_ID";
	protected static final DataAttribute EXERCISE_STYLE_TYPE_ATTR = InstrumentAttrConstant.EXERCISE_STYLE_CODE;
	protected static final String FEED_EXERCISE_STYLE_TYPE = "E";	
	private static final String OPS_GICS_IND_CODE = "402040";
	private static final String OPS_LE_NAME = "Kingfish Limited";
	private static final String CINS = "OPSCINS";
	private static final String ISIN_OPS = "GB0000767003";
	private static final DataAttribute IVO_DOC_TYPE_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("docType",
			DataLevel.IVO_DOC);
	private static final String OPS_SEDOL = "6077439";
	private static final DataAttribute IVO_TICKER_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("ticker",
			DataLevel.IVO_SEC);

	private static final String OPS_TICKER = "BIT";
	private static final DataAttribute IVO_SECURITY_RELATIONS_ATTR = DataAttributeFactory
			.getAttributeByNameAndLevel(IvoConstants.SECURITY_RELATIONS, DataLevel.IVO_SEC);
	private static final String OPS_AUS_INS_ID = "OPS_AUS_INS_ID";
	private static final DataAttribute CINS_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("cins",
			DataLevel.INS);
	private static final String OPS_RIC = "BIT.NZ";
	private static final DataAttribute RIC_ATTR = SecurityAttrConstant.RIC;

	protected static final String XR_DOC_ID = "5c6d5419f23a5e69a44c7d5f";
	protected static final String RDU_INSTRUMENT_ID = "RDUNIN000000000001";

	private static final String DATASOURCE_TRDSE = "trdse";
	
	private DataContainer createSdIvoContainer() {
		DataContainer sdIvoContainer = new DataContainer(DataLevel.IVO_INS, DataContainerContext.builder().build());
		sdIvoContainer.set_id(SD_DOC_ID);
		DataValue<String> docTypeVal = new DataValue<>();
		docTypeVal.setValue(LockLevel.RDU, IvoDocType.RDU.name());
		sdIvoContainer.addAttributeValue(IVO_DOC_TYPE_ATTR, docTypeVal);

		DataAttribute sdIvoInsRelAttr = DataAttributeFactory
				.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
		DataRow ivoRelLink = new DataRow(sdIvoInsRelAttr);
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(sdIvoInsRelAttr);

		DataValue<String> relationTypeVal = new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoRelLink.addAttribute(relationTypeAttr, relationTypeVal);

		DataAttribute referenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(sdIvoInsRelAttr);
		DataValue<ReferenceId> referenceIdVal = new DataValue<>();
		referenceIdVal.setValue(LockLevel.RDU, new ReferenceId(RDU_INSTRUMENT_ID, XR_DOC_ID));
		ivoRelLink.addAttribute(referenceIdAttr, referenceIdVal);

		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(ivoRelLink);

		DataValue<ArrayList<DataRow>> relDataValue = new DataValue<>();
		relDataValue.setValue(LockLevel.RDU, relLinkList);
		DataRow relDataRow = new DataRow(sdIvoInsRelAttr, relDataValue);
		sdIvoContainer.addAttributeValue(sdIvoInsRelAttr, relDataRow);

		DataContainer secDataContainer = DataContainerTestUtil.getDataContainer(DataLevel.IVO_SEC);
		sdIvoContainer.addDataContainer(secDataContainer, DataLevel.IVO_SEC);

		DataValue<String> tickerVal = new DataValue<>();
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		secDataContainer.addAttributeValue(IVO_TICKER_ATTR, tickerVal);
		
		DataAttribute sdIvoSecRelAttr = IVO_SECURITY_RELATIONS_ATTR;
		DataRow ivoSecRelLink = new DataRow(sdIvoSecRelAttr);
		DataAttribute secRelationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(sdIvoSecRelAttr);

		DataValue<String> secRelationTypeVal = new DataValue<>();
		secRelationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoSecRelLink.addAttribute(secRelationTypeAttr, secRelationTypeVal);

		DataAttribute secReferenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(sdIvoSecRelAttr);
		DataValue<ReferenceId> secReferenceIdVal = new DataValue<>();
		secReferenceIdVal.setValue(LockLevel.RDU, new ReferenceId(RDU_SECURITY_ID, XR_DOC_ID));
		ivoSecRelLink.addAttribute(secReferenceIdAttr, secReferenceIdVal);

		ArrayList<DataRow> secRelLinkList = new ArrayList<>();
		secRelLinkList.add(ivoSecRelLink);

		DataValue<ArrayList<DataRow>> secRelDataValue = new DataValue<>();
		secRelDataValue.setValue(LockLevel.RDU, secRelLinkList);
		DataRow secRelDataRow = new DataRow(sdIvoSecRelAttr, secRelDataValue);
		secDataContainer.addAttributeValue(sdIvoSecRelAttr, secRelDataRow);
		

		return sdIvoContainer;
	}

	private DataContainer createInputSdIvoContainer() {
		DataContainer sdIvoContainer = new DataContainer(DataLevel.IVO_INS, DataContainerContext.builder().build());
		sdIvoContainer.set_id(SD_DOC_ID);
		DataValue<String> docTypeVal = new DataValue<>();
		docTypeVal.setValue(LockLevel.RDU, IvoDocType.RDU.name());
		sdIvoContainer.addAttributeValue(IVO_DOC_TYPE_ATTR, docTypeVal);

		DataAttribute sdIvoInsRelAttr = DataAttributeFactory
				.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
		DataRow ivoRelLink = new DataRow(sdIvoInsRelAttr);
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(sdIvoInsRelAttr);

		DataValue<String> relationTypeVal = new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoRelLink.addAttribute(relationTypeAttr, relationTypeVal);

		DataAttribute referenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(sdIvoInsRelAttr);
		DataValue<ReferenceId> referenceIdVal = new DataValue<>();
		referenceIdVal.setValue(LockLevel.RDU, new ReferenceId(RDU_INSTRUMENT_ID, XR_DOC_ID));
		ivoRelLink.addAttribute(referenceIdAttr, referenceIdVal);

		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(ivoRelLink);

		DataValue<ArrayList<DataRow>> relDataValue = new DataValue<>();
		relDataValue.setValue(LockLevel.RDU, relLinkList);
		DataRow relDataRow = new DataRow(sdIvoInsRelAttr, relDataValue);
		sdIvoContainer.addAttributeValue(sdIvoInsRelAttr, relDataRow);

		DataContainer secDataContainer = DataContainerTestUtil.getDataContainer(DataLevel.IVO_SEC);
		sdIvoContainer.addDataContainer(secDataContainer, DataLevel.IVO_SEC);

		DataAttribute sdIvoSecRelAttr = IVO_SECURITY_RELATIONS_ATTR;
		DataRow ivoSecRelLink = new DataRow(sdIvoSecRelAttr);
		DataAttribute secRelationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(sdIvoSecRelAttr);

		DataValue<String> secRelationTypeVal = new DataValue<>();
		secRelationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoSecRelLink.addAttribute(secRelationTypeAttr, secRelationTypeVal);

		DataAttribute secReferenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(sdIvoSecRelAttr);
		DataValue<ReferenceId> secReferenceIdVal = new DataValue<>();
		secReferenceIdVal.setValue(LockLevel.RDU, new ReferenceId(RDU_SECURITY_ID, XR_DOC_ID));
		ivoSecRelLink.addAttribute(secReferenceIdAttr, secReferenceIdVal);

		ArrayList<DataRow> secRelLinkList = new ArrayList<>();
		secRelLinkList.add(ivoSecRelLink);

		DataValue<ArrayList<DataRow>> secRelDataValue = new DataValue<>();
		secRelDataValue.setValue(LockLevel.RDU, secRelLinkList);
		DataRow secRelDataRow = new DataRow(sdIvoSecRelAttr, secRelDataValue);
		secDataContainer.addAttributeValue(sdIvoSecRelAttr, secRelDataRow);
		
		DataValue<String> tickerVal = new DataValue<>();
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		secDataContainer.addAttributeValue(IVO_TICKER_ATTR, tickerVal);
		

		
		return sdIvoContainer;
	}
	@Before
	public void init() {
		MockUtil.mock_listenerService_dataContainerUpdated(listenerService);
	}
	
	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void TestLockRemovalOnSdDataContainer() throws UdmTechnicalException {
		
		lockRemovalService.removeLockFromDataContainer(createContainerToBeDelete(), createDbSdDataContainer(),DATASOURCE_TRDSE);

		Criteria criteria=Criteria.where( DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.INS,false,null));
		DataValue<String> idValue=new DataValue<>();
		idValue.setValue(LockLevel.FEED,SD_DOC_ID);
		criteria=criteria.is(idValue);
		List<DataContainer> retrieve = retrivalService.retrieve(Database.Mongodb, criteria, false, null);
		DataContainer dataContainer = retrieve.get(0);
		DataRow dataRow = dataContainer.getDataRow();
		Assert.assertEquals(5, dataRow.getRowData().keySet().size());
		
		List<DataContainer> allChildDataContainers = dataContainer.getAllChildDataContainers();
		Assert.assertEquals(1, allChildDataContainers.size());
		
		DataContainer childContainer = allChildDataContainers.get(0);
		
		Assert.assertEquals(4, childContainer.getDataRow().getRowData().keySet().size());
		
	}
	
	
	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void TestLockRemovalOnSdIvoDataContainer() throws UdmTechnicalException {
		
		DataContainer createSdIvoContainer = createSdIvoContainer();
		DataContainer createInputSdIvoContainer = createInputSdIvoContainer();
		lockRemovalService.removeLockFromDataContainer(createInputSdIvoContainer, createSdIvoContainer,DATASOURCE_TRDSE);

		Criteria criteria=Criteria.where( DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.IVO_INS,false,null));
		DataValue<String> idValue=new DataValue<>();
		idValue.setValue(LockLevel.FEED,SD_DOC_ID);
		criteria=criteria.is(idValue);
		List<DataContainer> retrieve = retrivalService.retrieve(Database.Mongodb, criteria, false, null);
		Assert.assertNotNull(retrieve);
		
		DataContainer dataContainer = retrieve.get(0);
		DataRow dataRow = dataContainer.getDataRow();
		Assert.assertNull(dataContainer.getAttributeValue(IVO_TICKER_ATTR));
		
		
	}
	
	
	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void TestLockRemovalOnLeContainer() throws UdmTechnicalException {
		
		DataContainer inputLeContainer = createInputLeContainer("5c6d540975ecec105e4243aa");
		
		DataContainer dbLeContainer = createDbLeContainer("5c6d540975ecec105e4243aa");
				
		lockRemovalService.removeLockFromDataContainer(inputLeContainer, dbLeContainer,DATASOURCE_TRDSE);
		
		Criteria criteria=Criteria.where( DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.LE,false,null));
		DataValue<String> idValue=new DataValue<>();
		idValue.setValue(LockLevel.FEED,SD_DOC_ID);
		criteria=criteria.is(idValue);
		List<DataContainer> retrieve = retrivalService.retrieve(Database.Mongodb, criteria, false, null);
		Assert.assertNotNull(retrieve);
		
		DataContainer dataContainer = retrieve.get(0);
		DataRow dataRow = dataContainer.getDataRow();
		Assert.assertEquals(4, dataRow.getRowData().keySet().size());
	
	}
	
	@Test
	@InputCollectionsPath(paths = "IvoLockRemovalServiceImplTest/testRemoveLock_nestedArray/enData.json")
	@ModifiedCollections(collections = {"enData"})
	public void testRemoveLock_nestedArray() throws UdmTechnicalException, IOException {
		DataContainer dbContainer=bsonConverter.getListOfDataContainersFromFilePath("IvoLockRemovalServiceImplTest/testRemoveLock_nestedArray/enData.json", EnData.class).get(0);
		DataContainer deletedContainer= bsonConverter.getListOfDataContainersFromFilePath("IvoLockRemovalServiceImplTest/testRemoveLock_nestedArray/deletedEnData.json", EnData.class).get(0) ;
		lockRemovalService.removeLockFromDataContainer(deletedContainer, dbContainer, "rduEns");
		
		
		Criteria criteria=Criteria.where(DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.EN,false,null));
		DataValue<String> idValue=new DataValue<>();
		idValue.setValue(LockLevel.FEED,"605cc352b88f2b5f3fed07d2");
		criteria=criteria.is(idValue);
		List<DataContainer> editedDataContainers = retrivalService.retrieve(Database.Mongodb, criteria, false, null);
		Assert.assertNotNull(editedDataContainers);
		DataContainer editedDataContainer = editedDataContainers.get(0);
		DataRow underlyingsValue = (DataRow) editedDataContainer.getAttributeValue(EnDataAttrConstant.UNDERLYINGS);
		ArrayList<DataRow> listDataRow = underlyingsValue.getValue().getValue();
		DataRow undelryingDataRow = listDataRow.get(0);
		Serializable underlyingIsinFeedValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.FEED, EnUnderlyingIsinAttribute);
		assertEquals("HK1111111111", underlyingIsinFeedValue);
		Serializable underlyingIsinRduValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.RDU, EnUnderlyingIsinAttribute);
		assertNull(underlyingIsinRduValue);
		
		Serializable underlyingSedolFeedValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.FEED, EnUnderlyingSedolAttribute);
		assertEquals("1111111", underlyingSedolFeedValue);
		Serializable underlyingSedolRduValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.RDU, EnUnderlyingSedolAttribute);
		assertNull(underlyingSedolRduValue);
	}
	
	@Test
	@InputCollectionsPath(paths = "IvoLockRemovalServiceImplTest/testRemoveLock_nestedArray/enData.json")
	@ModifiedCollections(collections = {"enData"})
	public void testRemoveLock_nestedArray_noMatchingUnderying() throws UdmTechnicalException, IOException {
		DataContainer dbContainer=bsonConverter.getListOfDataContainersFromFilePath("IvoLockRemovalServiceImplTest/testRemoveLock_nestedArray/enData.json", EnData.class).get(0);
		DataContainer deletedContainer= bsonConverter.getListOfDataContainersFromFilePath("IvoLockRemovalServiceImplTest/testRemoveLock_nestedArray/deletedEnData1.json", EnData.class).get(0) ;
		lockRemovalService.removeLockFromDataContainer(deletedContainer, dbContainer, "rduEns");
		
		
		Criteria criteria=Criteria.where(DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.EN,false,null));
		DataValue<String> idValue=new DataValue<>();
		idValue.setValue(LockLevel.FEED,"605cc352b88f2b5f3fed07d2");
		criteria=criteria.is(idValue);
		List<DataContainer> editedDataContainers = retrivalService.retrieve(Database.Mongodb, criteria, false, null);
		Assert.assertNotNull(editedDataContainers);
		DataContainer editedDataContainer = editedDataContainers.get(0);
		DataRow underlyingsValue = (DataRow) editedDataContainer.getAttributeValue(EnDataAttrConstant.UNDERLYINGS);
		ArrayList<DataRow> listDataRow = underlyingsValue.getValue().getValue();
		DataRow undelryingDataRow = listDataRow.get(0);
		Serializable underlyingIsinFeedValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.FEED, EnUnderlyingIsinAttribute);
		assertEquals("HK1111111111", underlyingIsinFeedValue);
		Serializable underlyingIsinRduValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.RDU, EnUnderlyingIsinAttribute);
		assertEquals("HK1111111111_OPS", underlyingIsinRduValue);
		
		Serializable underlyingSedolFeedValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.FEED, EnUnderlyingSedolAttribute);
		assertEquals("1111111", underlyingSedolFeedValue);
		Serializable underlyingSedolRduValue = undelryingDataRow.getAttributeValueAtLevel(LockLevel.RDU, EnUnderlyingSedolAttribute);
		assertEquals("1111111_OPS", underlyingSedolRduValue);
	}
	
	private DataContainer createContainerToBeDelete() {

		DataContainer sdContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);
		sdContainer.set_id(SD_DOC_ID);
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, ISIN_OPS);
		sdContainer.addAttributeValue(ISIN_ATTR, isinValue);
		DataValue<String> instrumentIdVal = new DataValue<>();
		instrumentIdVal.setValue(LockLevel.FEED, SD_INSTRUMENT_ID);
		sdContainer.addAttributeValue(INSTRUMENT_ID_ATTR, instrumentIdVal);
		
		DataValue<DomainType> datasourceVal = new DataValue<>();
		datasourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		sdContainer.addAttributeValue(DATASOURCE_ATTR, datasourceVal);

		DataValue<String> instrumentSourceUniqueIdVal = new DataValue<>();
		instrumentSourceUniqueIdVal.setValue(LockLevel.FEED, INS_SRCUNQID);
		sdContainer.addAttributeValue(INSTRUMENT_SRCUNQID_ATTR, instrumentSourceUniqueIdVal);

		DataContainer secDataContainer = DataContainerTestUtil.getDataContainer(DataLevel.SEC);
		sdContainer.addDataContainer(secDataContainer, DataLevel.SEC);

		DataValue<String> securitySourceUniqueIdVal = new DataValue<>();
		securitySourceUniqueIdVal.setValue(LockLevel.FEED, SEC_SRCUNQID);
		secDataContainer.addAttributeValue(SECURITY_SRCUNQID_ATTR, securitySourceUniqueIdVal);

		DataValue<String> tickerVal = new DataValue<>();
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		secDataContainer.addAttributeValue(TICKER_ATTR, tickerVal);
		
		DataValue<String> secIdVal = new DataValue<>();
		secIdVal.setValue(LockLevel.FEED, SECURITY_ID);
		secDataContainer.addAttributeValue(SECURITY_ID_ATTR, secIdVal);
		return sdContainer;
	}


	private DataContainer createDbSdDataContainer() {

		DataContainer sdContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);
		sdContainer.set_id(SD_DOC_ID);
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, ISIN_OPS);
		sdContainer.addAttributeValue(ISIN_ATTR, isinValue);
		DataValue<String> instrumentIdVal = new DataValue<>();
		instrumentIdVal.setValue(LockLevel.FEED, SD_INSTRUMENT_ID);
		sdContainer.addAttributeValue(INSTRUMENT_ID_ATTR, instrumentIdVal);

		DataValue<DomainType> datasourceVal = new DataValue<>();
		datasourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		sdContainer.addAttributeValue(DATASOURCE_ATTR, datasourceVal);

		DataValue<String> instrumentSourceUniqueIdVal = new DataValue<>();
		instrumentSourceUniqueIdVal.setValue(LockLevel.FEED, INS_SRCUNQID);
		sdContainer.addAttributeValue(INSTRUMENT_SRCUNQID_ATTR, instrumentSourceUniqueIdVal);

		DataValue<String> ausInsIdVal = new DataValue<>();
		ausInsIdVal.setValue(LockLevel.RDU, OPS_AUS_INS_ID);
		sdContainer.addAttributeValue(AUS_INS_ID_ATTR, ausInsIdVal);

		DataValue<String> cinsVal = new DataValue<>();
		cinsVal.setValue(LockLevel.RDU, CINS);
		sdContainer.addAttributeValue(CINS_ATTR, cinsVal);

		DataContainer secDataContainer = DataContainerTestUtil.getDataContainer(DataLevel.SEC);
		sdContainer.addDataContainer(secDataContainer, DataLevel.SEC);

		DataValue<String> securitySourceUniqueIdVal = new DataValue<>();
		securitySourceUniqueIdVal.setValue(LockLevel.FEED, SEC_SRCUNQID);
		secDataContainer.addAttributeValue(SECURITY_SRCUNQID_ATTR, securitySourceUniqueIdVal);

		DataValue<String> sedolVal = new DataValue<>();
		sedolVal.setValue(LockLevel.RDU, OPS_SEDOL);
		secDataContainer.addAttributeValue(SEDOL_ATTR, sedolVal);

		DataValue<String> tickerVal = new DataValue<>();
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		secDataContainer.addAttributeValue(TICKER_ATTR, tickerVal);
		
		DataValue<String> ricVal = new DataValue<>();
		ricVal.setValue(LockLevel.RDU, OPS_RIC);
		secDataContainer.addAttributeValue(RIC_ATTR, ricVal);

		DataValue<String> secIdVal = new DataValue<>();
		secIdVal.setValue(LockLevel.FEED, SECURITY_ID);
		secDataContainer.addAttributeValue(SECURITY_ID_ATTR, secIdVal);
		return sdContainer;
	}
	
	private DataContainer createInputLeContainer(String objectId) {
		DataContainer sdLeContainer = new DataContainer(DataLevel.LE, DataContainerContext.builder().build());
		sdLeContainer.set_id(SD_DOC_ID);
		DataValue<DomainType> datasourceVal = new DataValue<>();
		datasourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		sdLeContainer.addAttributeValue(DATASOURCE_ATTR, datasourceVal);
		if (objectId != null) {
			DataValue<String> legalEntityIdVal = new DataValue<>();
			legalEntityIdVal.setValue(LockLevel.FEED, objectId);
			sdLeContainer.addAttributeValue(LEGAL_ENTITY_ID_ATTR, legalEntityIdVal);
		}

		DataValue<String> legalEntitySrcIdVal = new DataValue<>();
		legalEntitySrcIdVal.setValue(LockLevel.FEED, LEGAL_ENTITY_SOURCE_UNQ_ID);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_SRCUNQID_ATTR, legalEntitySrcIdVal);

		DataValue<String> legalEntityNameVal = new DataValue<>();
		legalEntityNameVal.setValue(LockLevel.RDU, OPS_LE_NAME);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_NAME_ATTR, legalEntityNameVal);

		
		DataValue<DomainType> status = new DataValue<>();
		DomainType s = new DomainType();
		s.setVal("0");
		s.setNormalizedValue("A");
		status.setValue(LockLevel.FEED, s);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_STATUS_ATTR, status);

		return sdLeContainer;
	}
	
	private DataContainer createDbLeContainer(String objectId) {
		DataContainer sdLeContainer = new DataContainer(DataLevel.LE, DataContainerContext.builder().build());
		sdLeContainer.set_id(SD_DOC_ID);
		DataValue<DomainType> datasourceVal = new DataValue<>();
		datasourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		sdLeContainer.addAttributeValue(DATASOURCE_ATTR, datasourceVal);
		if (objectId != null) {
			DataValue<String> legalEntityIdVal = new DataValue<>();
			legalEntityIdVal.setValue(LockLevel.FEED, objectId);
			sdLeContainer.addAttributeValue(LEGAL_ENTITY_ID_ATTR, legalEntityIdVal);
		}

		DataValue<String> legalEntitySrcIdVal = new DataValue<>();
		legalEntitySrcIdVal.setValue(LockLevel.FEED, LEGAL_ENTITY_SOURCE_UNQ_ID);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_SRCUNQID_ATTR, legalEntitySrcIdVal);

		DataValue<String> legalEntityNameVal = new DataValue<>();
		legalEntityNameVal.setValue(LockLevel.RDU, OPS_LE_NAME);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_NAME_ATTR, legalEntityNameVal);

		DataValue<String> gicsIndustryVal = new DataValue<>();
		gicsIndustryVal.setValue(LockLevel.RDU, OPS_GICS_IND_CODE);
		sdLeContainer.addAttributeValue(GICS_INDUSTRY_CODE_ATTR, gicsIndustryVal);
		
		DataValue<DomainType> status = new DataValue<>();
		DomainType s = new DomainType();
		s.setVal("0");
		s.setNormalizedValue("A");
		status.setValue(LockLevel.FEED, s);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_STATUS_ATTR, status);

		return sdLeContainer;
	}
	
}
