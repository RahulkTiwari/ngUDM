/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoLockTestUtil.java
 * Author : SaJadhav
 * Date : 05-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

/**
 * creates prerequisite data for IvoLock tests
 * @author SaJadhav
 *
 */
public abstract class IvoLockTestUtil extends AbstractEmbeddedMongodbJunitParent{
	
	protected static final String LE_DOC_ID = "5c6d540975ecec105e4243aa";
	protected static final String FEED_GICS_INDUSTRYCODE = "402030";
	protected static final String FEED_LENAME = "Kingfish Ltd";
	protected static final String XR_DOC_ID = "5c6d5419f23a5e69a44c7d5f";
	protected static final String RDU_INSTRUMENT_ID = "RDUNIN000000000001";
	protected static final String SECURITY_ID = "1";
	protected static final String SEDOL = "BF00P65";
	protected static final String SEC_SRCUNQID = "0x00100b001434b85a";
	protected static final String INS_SRCUNQID = "0x00100b000923dda7";
	protected static final String SD_INSTRUMENT_ID = "06066a00-3ec5-446a-a333-e222349602b6";
	protected static final String SD_ISIN = "NZNTLE0003S1";
	protected static final String SD_DOC_ID = "5c6d541775ecec105e4243fe";
	protected static final String SD_DOC_ID_2 = "5c6d541775ecec105e4243ff";
	protected static final String LEGAL_ENTITY_ID = "4a0fbf48-0d94-4bd8-a81e-4a7a7ce4d54b";
	protected static final String LEGAL_ENTITY_SOURCE_UNQ_ID = "100313105";
	protected static final DataAttribute ISIN_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
	protected static final DataAttribute INSTRUMENT_ID_ATTR = DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.INS);
	protected static final DataAttribute INSTRUMENT_SRCUNQID_ATTR =  DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS);
	protected static final DataAttribute SEDOL_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.SEC);
	protected static final DataAttribute SECURITY_SRCUNQID_ATTR = DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC);
	protected static final DataAttribute SECURITY_ID_ATTR = DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC);
	protected static final DataAttribute LEGAL_ENTITY_ID_ATTR = DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.LE);
	protected static final DataAttribute LEGAL_ENTITY_SRCUNQID_ATTR =  DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE);
	protected static final DataAttribute DATASOURCE_ATTR = DataAttributeFactory.getDatasourceAttribute(DataLevel.INS);
	protected static final DataAttribute TICKER_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("ticker", DataLevel.SEC);
	protected static final DataAttribute AUS_INS_ID_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("australiaInstrumentId", DataLevel.INS);
	protected static final DataAttribute GICS_INDUSTRY_CODE_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("gicsIndustryCode", DataLevel.LE);
	protected static final DataAttribute LEGAL_ENTITY_NAME_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("legalEntityName", DataLevel.LE);
	protected static final DataAttribute LEGAL_ENTITY_STATUS_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(Constant.SdAttributeNames.LEGAL_ENTITY_STATUS, DataLevel.LE);
	protected final static DataAttribute INS_Vendor_mapping_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
	
	
	
	protected static final String FEED_TICKER = "GENWB";
	protected static final String RDU_SECURITY_ID = "RDUNSE000000000001";
	protected static final String FEED_AUS_INS_ID = "FEED_AUS_INS_ID";
	protected static final DataAttribute EXERCISE_STYLE_TYPE_ATTR = InstrumentAttrConstant.EXERCISE_STYLE_CODE;
	protected static final String FEED_EXERCISE_STYLE_TYPE = "E";
	private static final String BBG_INSTRUMENT_ID = "06066a00-3ec5-446a-a333-e222349602m4";
	
	@Autowired
	private PersistenceService persistenceService;
	
	protected void createPrerequisiteData() {
		//createStaticData();
		//persist Sd DataContainer
		createSdContainers();
		DataContainer xrDataContainer=createXrDataContainer();
		persistenceService.persist(xrDataContainer);
	}

	private void createSdContainers() {
		DataContainer sdDataContainer=createSdDataContainer();
		persistenceService.persist(sdDataContainer);
		DataContainer leContainer=createLegalEntityContainer();
		persistenceService.persist(leContainer);
	}
	
	protected void createDelPrequisitedata() {
		createSdContainers();
		DataContainer inactiveXrContainer = createInactiveXrDataContainer();
		persistenceService.persist(inactiveXrContainer);
	}
	
	protected DataContainer createSdDataContainer() {
		
		DataContainer insContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);
		insContainer.set_id(SD_DOC_ID);
		
		DataValue<String> isinVal=new DataValue<>() ;
		isinVal.setValue(LockLevel.FEED, SD_ISIN);
		insContainer.addAttributeValue(ISIN_ATTR, isinVal );
		
		DataValue<LocalDateTime> insDateVal=new DataValue<>();
		insDateVal.setValue(LockLevel.RDU, LocalDateTime.now());
		insContainer.addAttributeValue(SdDataAttrConstant.INS_DATE, insDateVal);
		
		DataValue<String> instrumentIdVal=new DataValue<>() ;
		instrumentIdVal.setValue(LockLevel.FEED, SD_INSTRUMENT_ID);
		insContainer.addAttributeValue(INSTRUMENT_ID_ATTR, instrumentIdVal );
		
		DataValue<String> instrumentSourceUniqueIdVal=new DataValue<>() ;
		instrumentSourceUniqueIdVal.setValue(LockLevel.FEED, INS_SRCUNQID);
		insContainer.addAttributeValue(INSTRUMENT_SRCUNQID_ATTR, instrumentSourceUniqueIdVal );
		
		DataValue<DomainType> datasourceVal=new DataValue<>() ;
		datasourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		insContainer.addAttributeValue(DATASOURCE_ATTR, datasourceVal );
		
		DataValue<String> ausInsIdVal=new DataValue<>() ;
		ausInsIdVal.setValue(LockLevel.FEED, FEED_AUS_INS_ID);
		insContainer.addAttributeValue(AUS_INS_ID_ATTR, ausInsIdVal );
		
		DataValue<DomainType> exerciseStyleTypeVal=new DataValue<>();
		exerciseStyleTypeVal.setValue(LockLevel.FEED, new DomainType(FEED_EXERCISE_STYLE_TYPE));
		insContainer.addAttributeValue(EXERCISE_STYLE_TYPE_ATTR, exerciseStyleTypeVal);
		
		DomainType insStatusDomainType = new DomainType("1");
		DataValue<DomainType> insStatusDataValue = new DataValue<>();
		insStatusDataValue.setValue(LockLevel.FEED, insStatusDomainType);

		DataAttribute insStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_STATUS, DataLevel.INS);
		insContainer.addAttributeValue(insStatusAttribute, insStatusDataValue);
		
		
		
		DataContainer secDataContainer=DataContainerTestUtil.getDataContainer(DataLevel.SEC);;
		insContainer.addDataContainer(secDataContainer, DataLevel.SEC);
		
		DomainType secStatusDomainType = new DomainType("1");
		DataValue<DomainType> secStatusDataValue = new DataValue<>();
		secStatusDataValue.setValue(LockLevel.FEED, secStatusDomainType);

		DataAttribute secStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.SECURITY_STATUS, DataLevel.SEC);
		secDataContainer.addAttributeValue(secStatusAttribute, secStatusDataValue);
		
		
		DataValue<String> securitySourceUniqueIdVal=new DataValue<>() ;
		securitySourceUniqueIdVal.setValue(LockLevel.FEED, SEC_SRCUNQID);
		secDataContainer.addAttributeValue(SECURITY_SRCUNQID_ATTR, securitySourceUniqueIdVal );
		
		DataValue<String> sedolVal=new DataValue<>() ;
		sedolVal.setValue(LockLevel.FEED, SEDOL);
		secDataContainer.addAttributeValue(SEDOL_ATTR, sedolVal );
		
		DataValue<String> tickerVal=new DataValue<>() ;
		tickerVal.setValue(LockLevel.FEED, FEED_TICKER);
		secDataContainer.addAttributeValue(TICKER_ATTR, tickerVal );
		
		
		
		DataValue<String> secIdVal=new DataValue<>() ;
		secIdVal.setValue(LockLevel.FEED, SECURITY_ID);
		secDataContainer.addAttributeValue(SECURITY_ID_ATTR, secIdVal );
		
		return insContainer;
	}
	
	/**
	 * @return
	 */
	private DataContainer createLegalEntityContainer() {
		DataContainer sdLeContainer =new DataContainer(DataLevel.LE, DataContainerContext.builder().build());
		sdLeContainer.set_id(LE_DOC_ID);
		DataValue<DomainType> datasourceVal=new DataValue<>() ;
		datasourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		sdLeContainer.addAttributeValue(DATASOURCE_ATTR, datasourceVal );
		
		DataValue<String> legalEntityIdVal=new DataValue<>();
		legalEntityIdVal.setValue(LockLevel.FEED, LEGAL_ENTITY_ID);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_ID_ATTR, legalEntityIdVal);
		
		DataValue<String> legalEntitySrcIdVal=new DataValue<>();
		legalEntitySrcIdVal.setValue(LockLevel.FEED, LEGAL_ENTITY_SOURCE_UNQ_ID);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_SRCUNQID_ATTR, legalEntitySrcIdVal);
		
		DataValue<String> legalEntityNameVal=new DataValue<>();
		legalEntityNameVal.setValue(LockLevel.FEED, FEED_LENAME);
		sdLeContainer.addAttributeValue(LEGAL_ENTITY_NAME_ATTR, legalEntityNameVal);
		
		DataValue<String> gicsIndustryVal=new DataValue<>();
		gicsIndustryVal.setValue(LockLevel.FEED, FEED_GICS_INDUSTRYCODE);
		sdLeContainer.addAttributeValue(GICS_INDUSTRY_CODE_ATTR, gicsIndustryVal);
		
		DataValue<LocalDateTime> insDateVal=new DataValue<>();
		insDateVal.setValue(LockLevel.RDU, LocalDateTime.now());
		sdLeContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("insDate", DataLevel.Document), insDateVal);
		return sdLeContainer;
	}
	
	protected DataContainer createXrDataContainer() {
		DataContainer xrContainer =new DataContainer(DataLevel.XRF_INS, DataContainerContext.builder().build());
		xrContainer.set_id(XR_DOC_ID);
		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, SD_INSTRUMENT_ID, rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		assetType1.setValue(LockLevel.RDU, new DomainType("Equity"), rduLockLevelInfo);
		
		DataValue<DomainType> assetType2 = new DataValue<>();
		assetType2.setValue(LockLevel.RDU, new DomainType("Equity"), rduLockLevelInfo);

		
		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		instrumentXrfdataSource1.setValue(LockLevel.RDU, new DomainType(null,null,"trdse"), rduLockLevelInfo);
		
		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		instrumentXrfdataSource2.setValue(LockLevel.RDU, new DomainType(null,null,"bbgf"), rduLockLevelInfo);
		
		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType(null, null,  DomainStatus.ACTIVE), rduLockLevelInfo);
		
		DataValue<DomainType> instrumentXrfLinkStatus2 = new DataValue<>();
		instrumentXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType(null, null,  DomainStatus.ACTIVE), rduLockLevelInfo);
		
		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);
		
		
		DataValue<LocalDateTime> instrumentLinkStartDate2 = new DataValue<>();
		instrumentLinkStartDate2.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		
		DataValue<LocalDateTime> instrumentLinkEndDate1 = new DataValue<>();
		instrumentLinkEndDate1.setValue(LockLevel.RDU, LocalDateTime.now().plusDays(100), rduLockLevelInfo);
		
		
		DataValue<LocalDateTime> instrumentLinkEndDate2 = new DataValue<>();
		instrumentLinkEndDate2.setValue(LockLevel.RDU, LocalDateTime.now().plusDays(40), rduLockLevelInfo);

		

		DataValue<DomainType> securityXrfdataSource1 = new DataValue<>();
		securityXrfdataSource1.setValue(LockLevel.RDU, new DomainType(null,null,"bbgf"), rduLockLevelInfo);
		
		DataValue<DomainType> securityXrfdataSource2 = new DataValue<>();
		securityXrfdataSource2.setValue(LockLevel.RDU, new DomainType(null,null,"trdse"), rduLockLevelInfo);
		
		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.ACTIVE), rduLockLevelInfo);
		
		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType(null, null,  DomainStatus.ACTIVE), rduLockLevelInfo);

		
		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId(SD_INSTRUMENT_ID, SD_DOC_ID), rduLockLevelInfo);
		
		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId(BBG_INSTRUMENT_ID, "5c6d541775ecec105e4243fe"), rduLockLevelInfo);

		
		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		tradeCurrency1.setValue(LockLevel.RDU, new DomainType("TC1"), rduLockLevelInfo);
		
		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		tradeCurrency2.setValue(LockLevel.RDU, new DomainType("TC2"), rduLockLevelInfo);
		
		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		exchangeCode1.setValue(LockLevel.RDU, new DomainType("EC1"), rduLockLevelInfo);
		
		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		exchangeCode2.setValue(LockLevel.RDU, new DomainType("EC2"), rduLockLevelInfo);
		
		DataValue<DomainType> additionalXrfParameter1 = new DataValue<>();
		additionalXrfParameter1.setValue(LockLevel.RDU, new DomainType("AA1"), rduLockLevelInfo);

		DataValue<DomainType> additionalXrfParameter2 = new DataValue<>();
		additionalXrfParameter2.setValue(LockLevel.RDU, new DomainType("AA2"), rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, RDU_SECURITY_ID, rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC2", rduLockLevelInfo);

		
		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU,SECURITY_ID, rduLockLevelInfo);


		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId(SECURITY_ID,SD_DOC_ID), rduLockLevelInfo);
		
		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU,  new ReferenceId("1","5c6d541775ecec105e4243fe"), rduLockLevelInfo);

		
		DataAttribute xrfInsLink = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);
		DataRow link2 = new DataRow(xrfInsLink);
		DataValue<String> isinVal2 = new DataValue<>();
		
		isinVal2.setValue(LockLevel.RDU, "isin2", rduLockLevelInfo);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_DATASOURCE, DataLevel.XRF_INS), instrumentXrfdataSource2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS), instrumentXrfLinkStatus2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS), instrumentRef2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_START_DATE, DataLevel.XRF_INS), instrumentLinkStartDate2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_END_DATE, DataLevel.XRF_INS), instrumentLinkEndDate2);
		
		
		DataRow link1 = new DataRow(xrfInsLink);
		DataValue<String> isinVal1 = new DataValue<>();
		isinVal1.setValue(LockLevel.RDU, "isin1", rduLockLevelInfo);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_DATASOURCE, DataLevel.XRF_INS), instrumentXrfdataSource1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS), instrumentXrfLinkStatus1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS), instrumentRef1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_START_DATE, DataLevel.XRF_INS), instrumentLinkStartDate1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_END_DATE, DataLevel.XRF_INS), instrumentLinkEndDate1);
		
		DataAttribute xrfInsLinks = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);

		ArrayList<DataRow> linkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
		valueList.setValue(LockLevel.RDU, linkList, rduLockLevelInfo);

		linkList.add(link1);
		linkList.add(link2);

		DataRow links = new DataRow(xrfInsLinks, valueList);
		
		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, SD_ISIN, rduLockLevelInfo);
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal);
		DataValue<String> rduInstrumentIdVal=new DataValue<>();
		rduInstrumentIdVal.setValue(LockLevel.RDU, RDU_INSTRUMENT_ID);
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("rduInstrumentId", DataLevel.XRF_INS), rduInstrumentIdVal);
		
		DataValue<DomainType> xrInsStatusVal=new DataValue<>();
		xrInsStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.ACTIVE));
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_INS_STATUS, DataLevel.XRF_INS), xrInsStatusVal);
		
		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "Cusip1", rduLockLevelInfo);
		
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS), cusip1);
		
		xrContainer.addAttributeValue(xrfInsLinks, links);

		DataContainer secContainer = new DataContainer(DataLevel.XRF_SEC, DataContainerContext.builder().build());
		xrContainer.addDataContainer(secContainer, DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		sedol1.setValue(LockLevel.RDU, SEDOL, rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU,"sedol2", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_DATASOURCE, DataLevel.XRF_SEC), securityXrfdataSource2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC), securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC), exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC), additionalXrfParameter2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC), _securityXrfRefId2);
		
		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC), securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_DATASOURCE, DataLevel.XRF_SEC), securityXrfdataSource1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC), additionalXrfParameter1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC), _securityXrfRefId1);
		
		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XRF_TRADE_CURR, DataLevel.XRF_SEC),
				tradeCurrency1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfAdditionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
		
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		
		
		DataValue<DomainType> xrSecStatusVal=new DataValue<>();
		xrSecStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.ACTIVE));
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_SEC_STATUS, DataLevel.XRF_SEC), xrSecStatusVal);

		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return xrContainer;
	}
	
	protected DataContainer createInactiveXrDataContainer() {
		DataContainer xrContainer =new DataContainer(DataLevel.XRF_INS, DataContainerContext.builder().build());
		xrContainer.set_id(XR_DOC_ID);
		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, SD_INSTRUMENT_ID, rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		assetType1.setValue(LockLevel.RDU, new DomainType("Equity"), rduLockLevelInfo);
		
		DataValue<DomainType> assetType2 = new DataValue<>();
		assetType2.setValue(LockLevel.RDU, new DomainType("Equity"), rduLockLevelInfo);

		
		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		instrumentXrfdataSource1.setValue(LockLevel.RDU, new DomainType(null,null,"trdse"), rduLockLevelInfo);
		
		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		instrumentXrfdataSource2.setValue(LockLevel.RDU, new DomainType(null,null,"bbgf"), rduLockLevelInfo);
		
		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType(null, null,  DomainStatus.ACTIVE), rduLockLevelInfo);
		
		DataValue<DomainType> instrumentXrfLinkStatus2 = new DataValue<>();
		instrumentXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType(null, null,  DomainStatus.ACTIVE), rduLockLevelInfo);
		
		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);
		
		
		DataValue<LocalDateTime> instrumentLinkStartDate2 = new DataValue<>();
		instrumentLinkStartDate2.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		
		DataValue<LocalDateTime> instrumentLinkEndDate1 = new DataValue<>();
		instrumentLinkEndDate1.setValue(LockLevel.RDU, LocalDateTime.now().plusDays(100), rduLockLevelInfo);
		
		
		DataValue<LocalDateTime> instrumentLinkEndDate2 = new DataValue<>();
		instrumentLinkEndDate2.setValue(LockLevel.RDU, LocalDateTime.now().plusDays(40), rduLockLevelInfo);

		

		DataValue<DomainType> securityXrfdataSource1 = new DataValue<>();
		securityXrfdataSource1.setValue(LockLevel.RDU, new DomainType(null,null,"bbgf"), rduLockLevelInfo);
		
		DataValue<DomainType> securityXrfdataSource2 = new DataValue<>();
		securityXrfdataSource2.setValue(LockLevel.RDU, new DomainType(null,null,"trdse"), rduLockLevelInfo);
		
		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.ACTIVE), rduLockLevelInfo);
		
		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType(null, null,  DomainStatus.DELETE), rduLockLevelInfo);

		
		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId(SD_INSTRUMENT_ID, SD_DOC_ID), rduLockLevelInfo);
		
		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId(BBG_INSTRUMENT_ID, "5c6d541775ecec105e4243fe"), rduLockLevelInfo);

		
		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		tradeCurrency1.setValue(LockLevel.RDU, new DomainType("TC1"), rduLockLevelInfo);
		
		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		tradeCurrency2.setValue(LockLevel.RDU, new DomainType("TC2"), rduLockLevelInfo);
		
		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		exchangeCode1.setValue(LockLevel.RDU, new DomainType("EC1"), rduLockLevelInfo);
		
		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		exchangeCode2.setValue(LockLevel.RDU, new DomainType("EC2"), rduLockLevelInfo);
		
		DataValue<DomainType> additionalXrfParameter1 = new DataValue<>();
		additionalXrfParameter1.setValue(LockLevel.RDU, new DomainType("AA1"), rduLockLevelInfo);

		DataValue<DomainType> additionalXrfParameter2 = new DataValue<>();
		additionalXrfParameter2.setValue(LockLevel.RDU, new DomainType("AA2"), rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, RDU_SECURITY_ID, rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC2", rduLockLevelInfo);

		
		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU,SECURITY_ID, rduLockLevelInfo);


		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId(SECURITY_ID,SD_DOC_ID_2), rduLockLevelInfo);
		
		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU,  new ReferenceId("1","5c6d541775ecec105e4243fe"), rduLockLevelInfo);

		
		DataAttribute xrfInsLink = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);
		DataRow link2 = new DataRow(xrfInsLink);
		DataValue<String> isinVal2 = new DataValue<>();
		
		isinVal2.setValue(LockLevel.RDU, "isin2", rduLockLevelInfo);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_DATASOURCE, DataLevel.XRF_INS), instrumentXrfdataSource2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS), instrumentXrfLinkStatus2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS), instrumentRef2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_START_DATE, DataLevel.XRF_INS), instrumentLinkStartDate2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_END_DATE, DataLevel.XRF_INS), instrumentLinkEndDate2);
		
		
		DataRow link1 = new DataRow(xrfInsLink);
		DataValue<String> isinVal1 = new DataValue<>();
		isinVal1.setValue(LockLevel.RDU, "isin1", rduLockLevelInfo);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_DATASOURCE, DataLevel.XRF_INS), instrumentXrfdataSource1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS), instrumentXrfLinkStatus1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS), instrumentRef1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_START_DATE, DataLevel.XRF_INS), instrumentLinkStartDate1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_END_DATE, DataLevel.XRF_INS), instrumentLinkEndDate1);
		
		DataAttribute xrfInsLinks = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);

		ArrayList<DataRow> linkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
		valueList.setValue(LockLevel.RDU, linkList, rduLockLevelInfo);

		linkList.add(link1);
		linkList.add(link2);

		DataRow links = new DataRow(xrfInsLinks, valueList);
		
		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, SD_ISIN, rduLockLevelInfo);
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal);
		DataValue<String> rduInstrumentIdVal=new DataValue<>();
		rduInstrumentIdVal.setValue(LockLevel.RDU, RDU_INSTRUMENT_ID);
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("rduInstrumentId", DataLevel.XRF_INS), rduInstrumentIdVal);
		
		DataValue<DomainType> xrInsStatusVal=new DataValue<>();
		xrInsStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.ACTIVE));
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_INS_STATUS, DataLevel.XRF_INS), xrInsStatusVal);
		
		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "Cusip1", rduLockLevelInfo);
		
		xrContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS), cusip1);
		
		xrContainer.addAttributeValue(xrfInsLinks, links);

		DataContainer secContainer = new DataContainer(DataLevel.XRF_SEC, DataContainerContext.builder().build());
		xrContainer.addDataContainer(secContainer, DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		sedol1.setValue(LockLevel.RDU, SEDOL, rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU,"sedol2", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_DATASOURCE, DataLevel.XRF_SEC), securityXrfdataSource2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC), securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC), exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC), additionalXrfParameter2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC), _securityXrfRefId2);
		
		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC), securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_DATASOURCE, DataLevel.XRF_SEC), securityXrfdataSource1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC), additionalXrfParameter1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC), _securityXrfRefId1);
		
		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

//		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XRF_TRADE_CURR, DataLevel.XRF_SEC),
				tradeCurrency1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfAdditionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
	
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		
		
		DataValue<DomainType> xrSecStatusVal=new DataValue<>();
		xrSecStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.DELETE));
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_SEC_STATUS, DataLevel.XRF_SEC), xrSecStatusVal);

		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return xrContainer;
	}
}
