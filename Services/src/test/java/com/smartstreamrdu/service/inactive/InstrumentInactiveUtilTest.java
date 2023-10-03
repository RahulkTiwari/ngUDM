/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InstrumentInactiveUtilTest.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

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
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.exception.UdmBusinessException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.LegalEntityAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.PersistenceEntityRepository;
import com.smartstreamrdu.persistence.service.PersistenceServiceImpl;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.DataSourceConstants;

/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class InstrumentInactiveUtilTest extends AbstractEmbeddedMongodbJunitParent{
	
	private static final DataAttribute INSTRUMENT_STATUS_ATTR = InstrumentAttrConstant.INSTRUMENT_STATUS;
	private static final DataAttribute SECURITY_STATUS_ATTR = SecurityAttrConstant.SECURITY_STATUS;
  
	public final static DataAttribute INS_Vendor_mapping = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
	public final static DataAttribute DOMAIN_VALUE=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainValue", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);
	public final static DataAttribute DOMAIN_NAME=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainName", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);
	public final static DataAttribute DOMAIN_SOURCE=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainSource", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);
	private static final DataAttribute DOMAIN_STATUS = DataAttributeFactory.getAttributeByNameAndLevelAndParent("status", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);;

	
	@Autowired
	private PersistenceServiceImpl persist;
	
	@Autowired
	private ReloadIgniteCache cacheRelod;
	
	/**
	 * @return
	 */
	private DataContainer createDomainMapContainer(String vendorValue, String normalizedValueString,String rdudomainName,String  domainDataSource) {
		DataContainer container1 = new DataContainer(DataLevel.DV_DOMAIN_MAP, new DataContainerContext(null, null));

		DataRow dataRow = new DataRow(INS_Vendor_mapping);

		DomainType domainType = new DomainType();
		domainType.setVal(vendorValue);
		DataValue<DomainType> domainTypeValue = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		domainTypeValue.setValue(LockLevel.RDU, domainType, rduLockLevelInfo);
		dataRow.addAttribute(DOMAIN_VALUE, domainTypeValue);

		DataValue<String> domainNameValue = new DataValue<>();
		domainNameValue.setValue(LockLevel.RDU, rdudomainName, rduLockLevelInfo);
		dataRow.addAttribute(DOMAIN_NAME, domainNameValue);

		DataValue<String> domainSourceValue = new DataValue<>();
		domainSourceValue.setValue(LockLevel.RDU, domainDataSource, rduLockLevelInfo);
		dataRow.addAttribute(DOMAIN_SOURCE, domainSourceValue);
		
		DataValue<String> statusActive = new DataValue<>();
		statusActive.setValue(LockLevel.RDU, "A");
		dataRow.addAttribute(DOMAIN_STATUS, statusActive);
		
				ArrayList<DataRow> dataRowList = new ArrayList<>();
		dataRowList.add(dataRow);

		DataValue<ArrayList<DataRow>> dataVal = new DataValue<>();
		dataVal.setValue(LockLevel.RDU, dataRowList, rduLockLevelInfo);

		DataRow dataRowOuter = new DataRow(INS_Vendor_mapping, dataVal);
		
		DataValue<String> rduDomainValue = new DataValue<>();
		rduDomainValue.setValue(LockLevel.RDU, "statuses", rduLockLevelInfo);
		
		DataValue<String> normalizedValue = new DataValue<>();
		normalizedValue.setValue(LockLevel.RDU, normalizedValueString, rduLockLevelInfo);
		
		container1.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("normalizedValue", DataLevel.DV_DOMAIN_MAP), normalizedValue);
		container1.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("rduDomain", DataLevel.DV_DOMAIN_MAP), rduDomainValue);
		container1.addAttributeValue(INS_Vendor_mapping, dataRowOuter);
		
		return container1;
	}
	
	@Before
	public void insertDomains() {
		persist.persist(createDomainMapContainer("0","I","instrumentStatusMap","esma"));
		persist.persist(createDomainMapContainer("0","I","tradingStatusMap","trds"));
		persist.persist(createDomainMapContainer("0","I","tradingStatusMap","figi"));
	}
	
	@Test
	@ModifiedCollections(collections = { "dvDomainMap" })
	public void test_inactiveInstrumentIfAllSecurituesInactive_trdse() throws UdmBusinessException, UdmTechnicalException{
		cacheRelod.reloadCache(DataLevel.DV_DOMAIN_MAP.getCollectionName());
		String TRDSE = "trdse";
		String inactiveStr = "0";
		String activeStr = "1";
		DataContainer leContainer = new DataContainer(DataLevel.LE, DataContainerContext.builder().build()) ;
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, leContainer);
		
		DataAttribute leStatusAtt = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.LE),DataLevel.LE);
	
		assertNull(leContainer.getAttributeValue(leStatusAtt));
		
		
		DataContainer inscontainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
	
		DataValue<DomainType> inactiveVal = new DataValue<>();
		inactiveVal.setValue(LockLevel.FEED, new DomainType(inactiveStr));
		
		//Already inactive instrument
		inscontainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, inactiveVal);
		
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, inscontainer);
		
		assertEquals(inactiveVal, inscontainer.getAttributeValue(INSTRUMENT_STATUS_ATTR));
		
		// W/o any securities
		DataValue<DomainType> activeVal = new DataValue<>();
		activeVal.setValue(LockLevel.FEED, new DomainType(activeStr));
		inscontainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeVal);
		
		assertEquals(activeVal.getValue(LockLevel.FEED), 
				inscontainer.getAttributeValueAtLevel(LockLevel.FEED, INSTRUMENT_STATUS_ATTR));

		//All inactive securities
		
		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;
		inscontainer.addDataContainer(secContainer1, DataLevel.SEC);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveVal);
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, inscontainer);
		DomainType insStatusFeedVal = (DomainType) inscontainer.getAttributeValueAtLevel(LockLevel.FEED, INSTRUMENT_STATUS_ATTR);
		assertEquals(inactiveStr, insStatusFeedVal.getVal());
		assertEquals("tradingStatusMap", insStatusFeedVal.getDomain());
		
		//Some active and some inactive
		inscontainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeVal);
		DataContainer secContainer2 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;
		inscontainer.addDataContainer(secContainer2, DataLevel.SEC);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, activeVal);
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, inscontainer);
		assertEquals(activeStr, ((DomainType)inscontainer.getAttributeValueAtLevel(LockLevel.FEED, INSTRUMENT_STATUS_ATTR)).getVal());

	}
	
	@Test
	@ModifiedCollections(collections = { "dvDomainMap" })
	public void test_inactiveInstrumentIfAllSecurituesInactive_esmaFirds() throws UdmBusinessException, UdmTechnicalException{
		cacheRelod.reloadCache(DataLevel.DV_DOMAIN_MAP.getCollectionName());

		 String esma = "esmaFirds";
		String inactiveStr = "0";
		String activeStr = "1";
		DataContainer leContainer = new DataContainer(DataLevel.LE, DataContainerContext.builder().build()) ;
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(esma, leContainer);
		
		DataAttribute leStatusAtt = LegalEntityAttrConstant.LEGAL_ENTITY_STATUS;
	
		assertNull(leContainer.getAttributeValue(leStatusAtt));
		
		
		DataContainer inscontainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
	
		DataValue<DomainType> inactiveVal = new DataValue<>();
		inactiveVal.setValue(LockLevel.FEED, new DomainType(inactiveStr));
		
		//Already inactive instrument
		inscontainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, inactiveVal);
		
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(esma, inscontainer);
		
		assertEquals(inactiveVal, inscontainer.getAttributeValue(INSTRUMENT_STATUS_ATTR));
		
		// W/o any securities
		DataValue<DomainType> activeVal = new DataValue<>();
		activeVal.setValue(LockLevel.FEED, new DomainType(activeStr));
		inscontainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeVal);
		
		assertEquals(activeVal.getValue(LockLevel.FEED), 
				inscontainer.getAttributeValueAtLevel(LockLevel.FEED, INSTRUMENT_STATUS_ATTR));

		//All inactive securities
		
		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;
		inscontainer.addDataContainer(secContainer1, DataLevel.SEC);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveVal);
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(esma, inscontainer);
		DomainType insStatusFeedVal = (DomainType) inscontainer.getAttributeValueAtLevel(LockLevel.FEED, INSTRUMENT_STATUS_ATTR);
		assertEquals(inactiveStr, insStatusFeedVal.getVal());
		assertEquals("instrumentStatusMap", insStatusFeedVal.getDomain());
		
		//Some active and some inactive
		inscontainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeVal);
		DataContainer secContainer2 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build()) ;
		inscontainer.addDataContainer(secContainer2, DataLevel.SEC);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, activeVal);
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(esma, inscontainer);
		assertEquals(activeStr, ((DomainType)inscontainer.getAttributeValueAtLevel(LockLevel.FEED, INSTRUMENT_STATUS_ATTR)).getVal());

	}
	@Test
	@ModifiedCollections(collections = { "dvDomainMap" })
	public void testRduInactiveInstrumentSecurities_trdse() throws UdmBusinessException, UdmTechnicalException {
		cacheRelod.reloadCache(DataLevel.DV_DOMAIN_MAP.getCollectionName());
		String TRDSE = "trdse";
		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DomainType activeDomainType = new DomainType(null, null, Constant.DomainStatus.ACTIVE);
		DomainType inactiveDomainType = new DomainType(null, null, Constant.DomainStatus.INACTIVE);
		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.ACTIVE));

		DataValue<DomainType> inactiveDataValue = new DataValue<>();
		inactiveDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.INACTIVE));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, insContainer);

		assertEquals(activeDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, inactiveDataValue);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));

		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);

		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, activeDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, insContainer);

		assertEquals(inactiveDomainType, insContainer.getChildDataContainers(DataLevel.SEC).get(0)
				.getAttributeValueAtLevel(LockLevel.RDU, SECURITY_STATUS_ATTR));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		DataContainer secContainer2 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		insContainer.addDataContainer(secContainer2, DataLevel.SEC);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, insContainer);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));

		activeDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.ACTIVE));
		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, activeDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, insContainer);

		assertEquals(activeDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));
		
		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(TRDSE, insContainer);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));
	}
	
	@Test
	@ModifiedCollections(collections = { "dvDomainMap" })
	public void testRduInactiveInstrumentSecurities_esma() throws UdmBusinessException, UdmTechnicalException {
		cacheRelod.reloadCache(DataLevel.DV_DOMAIN_MAP.getCollectionName());
		String ESMA = "esmaFirds";
		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DomainType activeDomainType = new DomainType(null, null, Constant.DomainStatus.ACTIVE);
		DomainType inactiveDomainType = new DomainType(null, null, Constant.DomainStatus.INACTIVE);
		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.ACTIVE));

		DataValue<DomainType> inactiveDataValue = new DataValue<>();
		inactiveDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.INACTIVE));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);

		EsmaFirdsInstrumentInactivation esmaInActivationBean = SpringUtil.getBean(EsmaFirdsInstrumentInactivation.class);
		InactiveBean bean = new InactiveBean();
		bean.setDbContainer(insContainer);
		bean.setDatasource("esmaFirds");
		esmaInActivationBean.inactivateIfRequired(bean);

		assertEquals(activeDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, inactiveDataValue);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));

		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);

		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, activeDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(ESMA, insContainer);

		assertEquals(inactiveDomainType, insContainer.getChildDataContainers(DataLevel.SEC).get(0)
				.getAttributeValueAtLevel(LockLevel.RDU, SECURITY_STATUS_ATTR));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		DataContainer secContainer2 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		insContainer.addDataContainer(secContainer2, DataLevel.SEC);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(ESMA, insContainer);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));

		activeDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.ACTIVE));
		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, activeDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(ESMA, insContainer);

		assertEquals(activeDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));
		
		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(ESMA, insContainer);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));
	}
	
	@Test
	@ModifiedCollections(collections = { "dvDomainMap" })
	public void testRduInactiveInstrumentSecurities_figi() throws UdmBusinessException, UdmTechnicalException {
		cacheRelod.reloadCache(DataLevel.DV_DOMAIN_MAP.getCollectionName());
		String figiDS = DataSourceConstants.FIGI_DS;
		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DomainType activeDomainType = new DomainType(null, null, Constant.DomainStatus.ACTIVE);
		DomainType inactiveDomainType = new DomainType(null, null, Constant.DomainStatus.INACTIVE);
		DataValue<DomainType> activeDataValue = new DataValue<>();
		activeDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.ACTIVE));

		DataValue<DomainType> inactiveDataValue = new DataValue<>();
		inactiveDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.INACTIVE));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);

		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);

		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, activeDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(figiDS, insContainer);

		assertEquals(activeDomainType, insContainer.getChildDataContainers(DataLevel.SEC).get(0)
				.getAttributeValueAtLevel(LockLevel.RDU, SECURITY_STATUS_ATTR));

		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		DataContainer secContainer2 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		insContainer.addDataContainer(secContainer2, DataLevel.SEC);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(figiDS, insContainer);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));

		activeDataValue.setValue(LockLevel.RDU, new DomainType(null, null, Constant.DomainStatus.ACTIVE));
		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, activeDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(figiDS, insContainer);

		assertEquals(activeDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));
		
		insContainer.addAttributeValue(INSTRUMENT_STATUS_ATTR, activeDataValue);
		secContainer1.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);
		secContainer2.addAttributeValue(SECURITY_STATUS_ATTR, inactiveDataValue);

		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(figiDS, insContainer);

		assertEquals(inactiveDomainType, insContainer.getAttributeValueAtLevel(LockLevel.RDU, INSTRUMENT_STATUS_ATTR));
	}
	
}
