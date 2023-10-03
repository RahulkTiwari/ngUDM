/*******************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    MergingStrategyFactoryTest.java
 * Author:    Padgaonkar
 * Date:    09-Mar-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.PersistenceServiceImpl;



/**
 * @author Padgaonkar
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class MergingStrategyFactoryTest extends AbstractEmbeddedMongodbJunitParent{

	public final static DataAttribute INS_Vendor_mapping = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
	public final static DataAttribute DOMAIN_VALUE=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainValue", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);
	public final static DataAttribute DOMAIN_NAME=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainName", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);
	public final static DataAttribute DOMAIN_SOURCE=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainSource", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);
	private static final DataAttribute DOMAIN_STATUS = DataAttributeFactory.getAttributeByNameAndLevelAndParent("status", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);;
	
	@Autowired
	private PersistenceServiceImpl persist;
	
	@Autowired
	private MergingStrategyFactory mergingStrategy;
	
	@Autowired
	private ReloadIgniteCache cacheRelod;
	
	@Test
	public void testEsmaContainerMerging() {
		DataContainerMergingService mergingService = mergingStrategy.getMergingStrategy(getEsmaFeedContainer(), getEsmaDbContainer());
		
		Assert.assertNotNull(mergingService);
		Assert.assertEquals(DataContainerMergingServiceImpl.class, mergingService.getClass());
	}

	@Test
	public void testDefaultMergingStrategy() {
		DataContainer container = getFeedContainer();

		DataContainer dbContainer = getDbContainer();

		List<DataContainer> containers = new ArrayList<DataContainer>();
		containers.add(dbContainer);
		DataContainerMergingService mergingService = mergingStrategy.getMergingStrategy(container, containers);
		
		Assert.assertNotNull(mergingService);
		Assert.assertEquals(DataContainerMergingServiceImpl.class, mergingService.getClass());
	}
	
	@Test
	public void testDefaultMergingStrategy_1() {
		DataContainer container = getFeedContainer_1();

		DataContainer dbContainer = getDbContainer();

		List<DataContainer> containers = new ArrayList<DataContainer>();
		containers.add(dbContainer);
		DataContainerMergingService mergingService = mergingStrategy.getMergingStrategy(container, containers);
		
		Assert.assertNotNull(mergingService);
		Assert.assertEquals(DataContainerMergingServiceImpl.class, mergingService.getClass());
	}

	private DataContainer getFeedContainer_1() {
		DataContainer container = DataContainerTestUtil.getInstrumentContainer();
		DataValue<String> dataValue = new DataValue<String>();
		dataValue.setValue(LockLevel.FEED, "XS0129477047");
		container.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);

		DomainType status1 = new DomainType();
		status1.setNormalizedValue("A");
		DataValue<DomainType> sourceValue1 = new DataValue<DomainType>();
		sourceValue1.setValue(LockLevel.FEED, status1);
		container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_STATUS, sourceValue1);
		
		return container;
	}

	private DataContainer getDbContainer() {
		DataContainer dbContainer = DataContainerTestUtil.getInstrumentContainer();
		DataValue<String> dataValue = new DataValue<String>();
		dataValue.setValue(LockLevel.FEED, "XS0129477047");
		dbContainer.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);

		return dbContainer;
	}

	private DataContainer getFeedContainer() {
		DataContainer container = DataContainerTestUtil.getInstrumentContainer();
		DataValue<String> dataValue = new DataValue<String>();
		dataValue.setValue(LockLevel.FEED, "XS0129477047");
		container.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);

		return container;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@ModifiedCollections(collections = { "dvDomainMap" })
	public void testSnpXfrContainerMerging() throws DataContainerMergeException {
		persist.persist(createDomainMapContainer("D","I"));
		cacheRelod.reloadCache(DataLevel.DV_DOMAIN_MAP.getCollectionName());

		DataContainerMergingService mergingService = mergingStrategy.getMergingStrategy(getSnpFeedContainer(), getSnpDbContainers());
		
		Assert.assertNotNull(mergingService);
		Assert.assertEquals(SnpXfrInActiveContainerMergingServiceImpl.class, mergingService.getClass());
		
		List<DataContainer> mergeContainers = mergingService.merge(getSnpFeedContainer(), getSnpDbContainers());
		
		DataContainer mergeContainer = mergeContainers.get(0);
		
		DataValue<DomainType> statusValue = (DataValue<DomainType>) mergeContainer.getAttributeValue(InstrumentAttrConstant.INSTRUMENT_STATUS);
		
		Assert.assertEquals("D",statusValue.getValue().getVal());
		
		DataValue<String> isinValue = (DataValue<String>) mergeContainer.getAttributeValue(InstrumentAttrConstant.ISIN);
		
		Assert.assertEquals("7047",isinValue.getValue());
	}

	/**
	 * @return
	 */
	private DataContainer getSnpFeedContainer() {
		DataContainer container = getFeedContainer();

		DataValue<String> dataValue = new DataValue<String>();
		dataValue.setValue(LockLevel.FEED, "XS0129477047");
		container.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);

		DomainType status1 = new DomainType();
		status1.setVal("D");
		status1.setDomain("instrumentStatusMap");
		DataValue<DomainType> sourceValue1 = new DataValue<DomainType>();
		sourceValue1.setValue(LockLevel.FEED, status1);
		container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_STATUS, sourceValue1);
		
		DomainType domainType1 = new DomainType();
		domainType1.setVal("snpXfr");
		DataValue<DomainType> sourceValue = new DataValue<DomainType>();
		sourceValue.setValue(LockLevel.FEED, domainType1);
		container.addAttributeValue(SdDataAttrConstant.DATA_SOURCE, sourceValue);

        return container;
	}
	
	

	/**
	 * @return
	 */
	private List<DataContainer> getSnpDbContainers() {
		DataContainer container = getFeedContainer();

		DataValue<String> dataValue = new DataValue<String>();
		dataValue.setValue(LockLevel.FEED, "7047");
		container.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);

		
		DomainType status1 = new DomainType();
		status1.setVal("A");
		status1.setDomain("instrumentStatusMap");
		DataValue<DomainType> sourceValue1 = new DataValue<DomainType>();
		sourceValue1.setValue(LockLevel.FEED, status1);
		container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_STATUS, sourceValue1);
		
		DomainType domainType1 = new DomainType();
		domainType1.setVal("snpXfr");
		DataValue<DomainType> sourceValue = new DataValue<DomainType>();
		sourceValue.setValue(LockLevel.FEED, domainType1);
		container.addAttributeValue(SdDataAttrConstant.DATA_SOURCE, sourceValue);

		List<DataContainer> containerList = new ArrayList<>();
		containerList.add(container);
        return containerList;
	}


	/**
	 * @return 
	 * 
	 */
	private DataContainer getEsmaFeedContainer() {
		DataContainer container = getFeedContainer();

		DataValue<String> dataValue = new DataValue<String>();
		dataValue.setValue(LockLevel.FEED, "XS0129477047");
		container.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);


		DataValue<String> dv1 = new DataValue<String>();
		dv1.setValue(LockLevel.FEED, "DBFCAB");
		container.addAttributeValue(InstrumentAttrConstant.CFI_CODE2015, dv1);

		DomainType domainType = new DomainType();
		domainType.setVal("DBFCAB");
		DataValue<DomainType> domainDataType = new DataValue<DomainType>();
		domainDataType.setValue(LockLevel.FEED, domainType);
		container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_TYPE_CODE,
				domainDataType);
		
		DomainType status1 = new DomainType();
		status1.setVal("1");
		DataValue<DomainType> sourceValue1 = new DataValue<DomainType>();
		sourceValue1.setValue(LockLevel.FEED, status1);
		container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_STATUS, sourceValue1);
		
		DomainType domainType1 = new DomainType();
		domainType1.setVal("esmaFirds");
		DataValue<DomainType> sourceValue = new DataValue<DomainType>();
		sourceValue.setValue(LockLevel.FEED, domainType1);
		container.addAttributeValue(SdDataAttrConstant.DATA_SOURCE, sourceValue);

        return container;
	}
	
	
	
	/**
	 * @return 
	 * 
	 */
	private List<DataContainer> getEsmaDbContainer() {
		List<DataContainer> dbContainers = new ArrayList<>();
		DataContainer container = getDbContainer();

		DataValue<String> dataValue = new DataValue<String>();
		dataValue.setValue(LockLevel.FEED, "XS0129477047");
		container.addAttributeValue(InstrumentAttrConstant.ISIN, dataValue);


		DataValue<String> dv1 = new DataValue<String>();
		dv1.setValue(LockLevel.FEED, "DBFCAB");
		container.addAttributeValue(InstrumentAttrConstant.CFI_CODE2015, dv1);

		DomainType domainType = new DomainType();
		domainType.setVal("DBFCAB");
		DataValue<DomainType> domainDataType = new DataValue<DomainType>();
		domainDataType.setValue(LockLevel.FEED, domainType);
		container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_TYPE_CODE,
				domainDataType);

		DomainType domainType1 = new DomainType();
		domainType1.setVal("esmaFirds");
		DataValue<DomainType> sourceValue = new DataValue<DomainType>();
		sourceValue.setValue(LockLevel.FEED, domainType1);
		container.addAttributeValue(SdDataAttrConstant.DATA_SOURCE, sourceValue);

		dbContainers.add(container);
        return dbContainers;
	}
	
	/**
	 * @return
	 */
	private DataContainer createDomainMapContainer(String vendorValue, String normalizedValueString) {
		DataContainer container1 = new DataContainer(DataLevel.DV_DOMAIN_MAP, new DataContainerContext(null, null));

		DataRow dataRow = new DataRow(INS_Vendor_mapping);

		DomainType domainType = new DomainType();
		domainType.setVal(vendorValue);
		DataValue<DomainType> domainTypeValue = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		domainTypeValue.setValue(LockLevel.RDU, domainType, rduLockLevelInfo);
		dataRow.addAttribute(DOMAIN_VALUE, domainTypeValue);

		DataValue<String> domainNameValue = new DataValue<>();
		domainNameValue.setValue(LockLevel.RDU, "instrumentStatusMap", rduLockLevelInfo);
		dataRow.addAttribute(DOMAIN_NAME, domainNameValue);

		DataValue<String> domainSourceValue = new DataValue<>();
		domainSourceValue.setValue(LockLevel.RDU, "snpXf", rduLockLevelInfo);
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

}
