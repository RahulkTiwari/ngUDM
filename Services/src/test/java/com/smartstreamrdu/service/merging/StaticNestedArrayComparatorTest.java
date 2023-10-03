/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: StaticNestedArrayComparatorTest.java
 * Author : SaJadhav
 * Date : 21-Aug-2019
 * 
 */
package com.smartstreamrdu.service.merging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

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
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class StaticNestedArrayComparatorTest {
	
	private static final DataAttribute VENDOR_MAPPINGS=DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
	protected final static DataAttribute DOMAIN_VALUE=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainValue", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS);
	
	protected final static DataAttribute DOMAIN_NAME=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainName", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS);
	protected final static DataAttribute DOMAIN_SOURCE=DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainSource", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS);
	protected final static DataAttribute DOMAIN_STATUS=DataAttributeFactory.getAttributeByNameAndLevelAndParent("status", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS);
	
	@Autowired
	private StaticNestedArrayComparator comparator;
	
	@Test
	public void testCompare(){
		DataContainer domainMapContainer = createDvDomainMapContainer("currencyCodes","AED");
		addVendorMapping(domainMapContainer, "trds", "currencyCodesMap", "usd", null, "A");
		addVendorMapping(domainMapContainer, "figi", "currencyMap", "USDollar", null, "A");
		
		
		DataRow feedRow=createDataRow("figi", "currencyMap", "USDollar", null, "A");
		
		NestedArrayComparatorInputPojo inputPojo = NestedArrayComparatorInputPojo.builder().feed(feedRow).dbIterator(new DataRowIterator(domainMapContainer, VENDOR_MAPPINGS))
				.arrayAttribute(VENDOR_MAPPINGS).build();
		
		DataRow matchingRow = comparator.compare(inputPojo);
		
		assertEquals(feedRow, matchingRow);
		
		 feedRow=createDataRow("trds", "currencyMap", "USDollar", null, "A");
		
		 matchingRow = comparator.compare(inputPojo);
		 
		 assertNull(matchingRow);
	}
	
	private DataContainer createDvDomainMapContainer(String rduDomain, String normalizedVal) {
		DataContainer container = new DataContainer(DataLevel.DV_DOMAIN_MAP, DataContainerContext.builder().build());
		DataValue<String> normalizedValue=new DataValue<>();
		normalizedValue.setValue(LockLevel.RDU, normalizedVal);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("normalizedValue", DataLevel.DV_DOMAIN_MAP), normalizedValue);
		DataValue<String> rduDomainValue=new DataValue<>();
		rduDomainValue.setValue(LockLevel.RDU, rduDomain);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("rduDomain", DataLevel.DV_DOMAIN_MAP), rduDomainValue);
		return container;
	}
	
	/**
	 * @param dvDomainMapCOntainer
	 * @param domainSource
	 * @param domainName
	 * @param domainValue
	 * @param string
	 */
	private void addVendorMapping(DataContainer dvDomainMapContainer, String domainSource, String domainName,
			String domainValue,String val2, String status) {
		List<DataRow> dataRowList=getVendorMappingDataRowList(dvDomainMapContainer);
		
		DataRow dataRow = createDataRow(domainSource, domainName, domainValue, val2, status);
		dataRowList.add(dataRow);
	}

	private DataRow createDataRow(String domainSource, String domainName, String domainValue, String val2,
			String status) {
		DataRow dataRow = new DataRow(VENDOR_MAPPINGS);

		DomainType domainType1 = new DomainType();
		domainType1.setVal(domainValue);
		domainType1.setVal2(val2);
		DataValue<DomainType> domainTypeValue1 = new DataValue<>();
		domainTypeValue1.setValue(LockLevel.RDU, domainType1);
		dataRow.addAttribute(DOMAIN_VALUE, domainTypeValue1);

		DataValue<String> domainNameValue1 = new DataValue<>();
		domainNameValue1.setValue(LockLevel.RDU, domainName);
		dataRow.addAttribute(DOMAIN_NAME, domainNameValue1);

		DataValue<String> domainSourceValue1 = new DataValue<>();
		domainSourceValue1.setValue(LockLevel.RDU, domainSource);
		dataRow.addAttribute(DOMAIN_SOURCE, domainSourceValue1);
		
		DataValue<String> statusVal = new DataValue<>();
		statusVal.setValue(LockLevel.RDU, status);
		dataRow.addAttribute(DOMAIN_STATUS, statusVal);
		return dataRow;
	}
	
	/**
	 * @param dvDomainMapContainer
	 * @return
	 */
	private List<DataRow> getVendorMappingDataRowList(DataContainer dvDomainMapContainer) {
		DataRow dataRow=(DataRow) dvDomainMapContainer.getAttributeValue(VENDOR_MAPPINGS);
		ArrayList<DataRow> dataRowList=null;
		if(dataRow==null){
			DataValue<ArrayList<DataRow>> dataVal = new DataValue<>();
			dataRowList = new ArrayList<>();
			dataVal.setValue(LockLevel.RDU, dataRowList);

			DataRow dataRowOuter = new DataRow(VENDOR_MAPPINGS, dataVal);
			dvDomainMapContainer.addAttributeValue(VENDOR_MAPPINGS, dataRowOuter);
		}else{
			DataValue<ArrayList<DataRow>> dataValue = dataRow.getValue();
			dataRowList=dataValue.getValue();
		}
		return dataRowList;
	}
}
