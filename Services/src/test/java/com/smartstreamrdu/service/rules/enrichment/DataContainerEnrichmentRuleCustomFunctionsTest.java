/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerEnrichmentRuleCustomFunctionsTest.java
 * Author :SaJadhav
 * Date : 04-Apr-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.Test;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactoryFieldPojo;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.domain.autoconstants.EnDataAttrConstant;

/**
 * @author SaJadhav
 *
 */
public class DataContainerEnrichmentRuleCustomFunctionsTest {

	DataContainerEnrichmentRuleCustomFunctions customFunctions = new DataContainerEnrichmentRuleCustomFunctions();

	@Test
	public void test_getDomainValue() {
		assertEquals(new DomainType("abc"), customFunctions.getDomainValue("abc"));
	}

	@Test
	public void test_getDomainValue_nullValue() {
		assertNull(customFunctions.getDomainValue(""));
		assertNull(customFunctions.getDomainValue(null));
	}

	@Test
	public void test_getNormalizedDomainValue() {
		assertEquals(new DomainType(null, null, "A"), customFunctions.getNormalizedDomainValue("A"));
		assertNull(customFunctions.getNormalizedDomainValue(""));
		assertNull(customFunctions.getNormalizedDomainValue(null));
	}

	@Test
	public void test_formatDate() {
		LocalDate date = LocalDate.of(2022, 03, 10);
		assertEquals("20220310", customFunctions.formatDate(date, "yyyyMMdd"));
		assertEquals("2022-03-10", customFunctions.formatDate(date, "yyyy-MM-dd"));
	}

	@Test
	public void test_formatDate_invalidValues() {
		LocalDate date = LocalDate.of(2022, 03, 10);
		assertEquals("", customFunctions.formatDate(date, ""));
		assertEquals("", customFunctions.formatDate(null, "yyyyMMdd"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_enrichEventFileNames() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());

		DataValue<ArrayList<String>> eventsValue = new DataValue<>();
		ArrayList<String> list = new ArrayList<>();
		list.add("sameer.com.pdf");
		list.add("eventypes_enz.jpeg");
		eventsValue.setValue(LockLevel.RDU, list);
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENTS, eventsValue);
		DataAttribute dataAttribute = EnDataAttrConstant.EVENTS;
		ArrayList<String> enrichEventFileNames = (ArrayList<String>) customFunctions.enrichEventFileNames(dataContainer,
				dataAttribute, "ABC_", "_RDU");

		assertEquals("ABC_sameer.com_RDU.pdf", enrichEventFileNames.get(0));
		assertEquals("ABC_eventypes_enz_RDU.jpeg", enrichEventFileNames.get(1));
	}

	@Test
	public void test_enrichEventFileNames_EmptyList() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());

		DataValue<ArrayList<String>> eventsValue = new DataValue<>();
		ArrayList<String> list = new ArrayList<>();
		eventsValue.setValue(LockLevel.RDU, list);
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENTS, eventsValue);
		DataAttribute dataAttribute = EnDataAttrConstant.EVENTS;

		assertNull(customFunctions.enrichEventFileNames(dataContainer, dataAttribute, "ABC_", "_RDU"));
	}

	@Test
	public void test_test_enrichEventFileNames_invalidData() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		assertNull(customFunctions.enrichEventFileNames(null, EnDataAttrConstant.EVENTS, "ABC_", "_RDU"));
		assertNull(customFunctions.enrichEventFileNames(dataContainer, null, "ABC_", "_RDU"));
		assertNull(customFunctions.enrichEventFileNames(dataContainer, EnDataAttrConstant.EVENTS, "", null));
		assertNull(customFunctions.enrichEventFileNames(dataContainer, EnDataAttrConstant.EVENTS, "ABC_", ""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_test_enrichEventFileNames_invalidDataAttribute() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		customFunctions.enrichEventFileNames(dataContainer, EnDataAttrConstant.EVENT_INITIAL_URL, "ABC_", "_RDU");
	}

	@Test
	public void test_getAttributeValueFromDataContainer() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.RDU, "TestSubject");
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_SUBJECT, value);
		Serializable eventSubject = customFunctions.getAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_EVENT_SUBJECT);
		assertEquals("TestSubject", eventSubject);
	}

	@Test
	public void test_getAttributeValueFromDataContainer_date() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataValue<LocalDate> value = new DataValue<>();
		value.setValue(LockLevel.RDU, LocalDate.of(2021, 04, 05));
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_INSERT_DATE, value);
		Serializable eventInsertDate = customFunctions.getAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_EVENT_INSERT_DATE);
		assertEquals(LocalDate.of(2021, 04, 05), eventInsertDate);
	}

	@Test
	public void test_getAttributeValueFromDataContainer_noValue() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		Serializable eventInsertDate = customFunctions.getAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_EVENT_INSERT_DATE);
		assertNull(eventInsertDate);
	}

	@Test
	public void test_getAttributeValueFromDataContainer_AttributeNotPopulated() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		Serializable eventInsertDate = customFunctions.getAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_EVENT_INSERT_DATE);
		assertNull(eventInsertDate);
	}

	@Test
	public void test_getAttributeValueFromDataContainer_NullDataContainer() {
		Serializable eventInsertDate = customFunctions.getAttributeValueFromDataContainer(null,
				EnDataAttrConstant.COL_EVENT_INSERT_DATE);
		assertNull(eventInsertDate);
	}

	@Test
	public void test_getNormalizedAttributeValueFromDataContainer() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataValue<DomainType> eventStatusVal = new DataValue<>();
		eventStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, "A"));
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_STATUS, eventStatusVal);
		String eventStatus = customFunctions.getNormalizedAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_EVENT_STATUS);
		assertEquals("A", eventStatus);
	}

	@Test
	public void test_getNormalizedAttributeValueFromDataContainer_val1Populated() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataValue<DomainType> eventStatusVal = new DataValue<>();
		eventStatusVal.setValue(LockLevel.RDU, new DomainType("A"));
		dataContainer.addAttributeValue(EnDataAttrConstant.EVENT_STATUS, eventStatusVal);
		String eventStatus = customFunctions.getNormalizedAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_EVENT_STATUS);
		assertNull(eventStatus);
	}

	@Test
	public void test_getNormalizedAttributeValueFromDataContainer_noData() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		String eventStatus = customFunctions.getNormalizedAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_EVENT_STATUS);
		assertNull(eventStatus);
	}

	@Test
	public void test_getNormalizedAttributeValueFromDataContainer_nullDataContainer() {
		String eventStatus = customFunctions.getNormalizedAttributeValueFromDataContainer(null,
				EnDataAttrConstant.COL_EVENT_STATUS);
		assertNull(eventStatus);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_getNormalizedAttributeValueFromDataContainer_invalidAttribute() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		customFunctions.getNormalizedAttributeValueFromDataContainer(dataContainer,
				EnDataAttrConstant.COL_ADJUSTMENT_FACTOR);
	}

	@Test
	public void test_getNestedArrayCounter() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataAttribute counterAttribute = DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter",
				"underlyings");

		DataValue<ArrayList<DataRow>> dataValue = new DataValue<>();
		DataRow dataRow = new DataRow(EnDataAttrConstant.UNDERLYINGS, dataValue);

		ArrayList<DataRow> listDataRow = new ArrayList<>();
		DataRow row1 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "TestIsin1");
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue);
		listDataRow.add(row1);

		DataRow row2 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue2 = new DataValue<>();
		isinValue2.setValue(LockLevel.RDU, "TestIsin2");
		row2.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue2);
		listDataRow.add(row2);
		dataValue.setValue(LockLevel.RDU, listDataRow);
		dataContainer.addAttributeValue(EnDataAttrConstant.UNDERLYINGS, dataRow);

		assertEquals(Long.valueOf(1l), (Long) customFunctions.getNestedArrayCounter(dataContainer, counterAttribute));
	}

	@Test
	public void test_getNestedArrayCounter_2() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataAttribute counterAttribute = DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter",
				"underlyings");

		DataValue<ArrayList<DataRow>> dataValue = new DataValue<>();
		DataRow dataRow = new DataRow(EnDataAttrConstant.UNDERLYINGS, dataValue);

		ArrayList<DataRow> listDataRow = new ArrayList<>();
		DataRow row1 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "TestIsin1");
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue);
		DataValue<Long> counterValue = new DataValue<>();
		counterValue.setValue(LockLevel.RDU, 1l);
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings"),
				counterValue);
		listDataRow.add(row1);

		DataRow row2 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue2 = new DataValue<>();
		isinValue2.setValue(LockLevel.RDU, "TestIsin2");
		row2.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue2);
		listDataRow.add(row2);
		dataValue.setValue(LockLevel.RDU, listDataRow);
		dataContainer.addAttributeValue(EnDataAttrConstant.UNDERLYINGS, dataRow);

		assertEquals(Long.valueOf(2l), (Long) customFunctions.getNestedArrayCounter(dataContainer, counterAttribute));
	}

	@Test
	public void test_getNestedArrayCounter_Row1AndRow2CounterPopulated() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataAttribute counterAttribute = DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter",
				"underlyings");

		DataValue<ArrayList<DataRow>> dataValue = new DataValue<>();
		DataRow dataRow = new DataRow(EnDataAttrConstant.UNDERLYINGS, dataValue);

		ArrayList<DataRow> listDataRow = new ArrayList<>();
		DataRow row1 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "TestIsin1");
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue);
		DataValue<Long> counterValue = new DataValue<>();
		counterValue.setValue(LockLevel.RDU, 1l);
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings"),
				counterValue);
		listDataRow.add(row1);

		DataRow row2 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue2 = new DataValue<>();
		isinValue2.setValue(LockLevel.RDU, "TestIsin2");
		row2.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue2);
		DataValue<Long> row2CounterValue = new DataValue<>();
		row2CounterValue.setValue(LockLevel.RDU, 2l);
		row2.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings"),
				row2CounterValue);
		listDataRow.add(row2);
		dataValue.setValue(LockLevel.RDU, listDataRow);
		dataContainer.addAttributeValue(EnDataAttrConstant.UNDERLYINGS, dataRow);

		assertEquals(Long.valueOf(3l), (Long) customFunctions.getNestedArrayCounter(dataContainer, counterAttribute));
	}

	@Test
	public void test_getNestedArrayCounter_3() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataAttribute counterAttribute = DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter",
				"underlyings");

		DataValue<ArrayList<DataRow>> dataValue = new DataValue<>();
		DataRow dataRow = new DataRow(EnDataAttrConstant.UNDERLYINGS, dataValue);

		ArrayList<DataRow> listDataRow = new ArrayList<>();
		DataRow row1 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "TestIsin1");
		row1.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue);
		listDataRow.add(row1);

		DataRow row2 = new DataRow(EnDataAttrConstant.UNDERLYINGS);
		DataValue<String> isinValue2 = new DataValue<>();
		isinValue2.setValue(LockLevel.RDU, "TestIsin2");
		row2.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("isin", "underlyings"), isinValue2);
		DataValue<Long> row2CounterValue = new DataValue<>();
		row2CounterValue.setValue(LockLevel.RDU, 2l);
		row2.addAttribute(DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings"),
				row2CounterValue);
		listDataRow.add(row2);
		dataValue.setValue(LockLevel.RDU, listDataRow);
		dataContainer.addAttributeValue(EnDataAttrConstant.UNDERLYINGS, dataRow);

		assertEquals(Long.valueOf(3l), (Long) customFunctions.getNestedArrayCounter(dataContainer, counterAttribute));
	}
	
	@Test
	public void test_getNestedArrayCounter_AttributeValueNotPresent() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataAttribute counterAttribute = DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter",
				"underlyings");

		DataValue<ArrayList<DataRow>> dataValue = new DataValue<>();
		DataRow dataRow = new DataRow(EnDataAttrConstant.UNDERLYINGS, dataValue);
		dataContainer.addAttributeValue(EnDataAttrConstant.UNDERLYINGS, dataRow);
		assertNull(customFunctions.getNestedArrayCounter(dataContainer, counterAttribute));
	}

	@Test
	public void test_getNestedArrayCounter_NullDataValue() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataAttribute counterAttribute = DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter",
				"underlyings");
		DataRow dataRow = new DataRow(EnDataAttrConstant.UNDERLYINGS, null);
		dataContainer.addAttributeValue(EnDataAttrConstant.UNDERLYINGS, dataRow);
		assertNull(customFunctions.getNestedArrayCounter(dataContainer, counterAttribute));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_getNestedArrayCounter_ParentAttributeOfInvalidType() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		DataAttribute parentAttr = new DataAttribute(DataAttributeFactoryFieldPojo.builder().attributeName("parentAttr")
				.dataType(DataType.DATETIME).lockCategory(LockLevel.FEED).build());
		DataAttribute testAttr = new DataAttribute(DataAttributeFactoryFieldPojo.builder().attributeName("childAttr")
				.dataType(DataType.DATETIME).level(DataLevel.EN).atLockLevel(true).lockCategory(LockLevel.FEED)
				.isKeyAttribute(false).indexType("").parentAttribute(parentAttr).build());

		customFunctions.getNestedArrayCounter(dataContainer, testAttr);
	}

	@Test
	public void test_getNestedArrayCounter_invalidAttribute() {
		DataContainer dataContainer = new DataContainer(DataLevel.EN, DataContainerContext.builder().build());
		assertNull(customFunctions.getNestedArrayCounter(dataContainer, EnDataAttrConstant.EVENT_INITIAL_URL));
		assertNull(customFunctions.getNestedArrayCounter(null,
				DataStorageEnum.EN.getAttributeByNameAndParent("instrumentCounter", "underlyings")));
		assertNull(customFunctions.getNestedArrayCounter(dataContainer, null));
	}

}
