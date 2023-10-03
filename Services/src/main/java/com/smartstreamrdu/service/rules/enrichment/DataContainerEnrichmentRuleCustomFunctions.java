/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerEnrichmentRuleCustomFunctions.java
 * Author :SaJadhav
 * Date : 31-Mar-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DomainType;

/**
 * Custom java function which are invoked by the javascript rules
 * 
 * @author SaJadhav
 *
 */
public class DataContainerEnrichmentRuleCustomFunctions {
	
	/**
	 * Returns DomainType object with val attribute populated
	 * 
	 * @param val
	 * @return
	 */
	public DomainType getDomainValue(String val) {
		if(StringUtils.isNotBlank(val)) {
			return new DomainType(val);
		}
		return null;
	}
	
	/**
	 * Returns DomainType object with normalizedValue attribute populated
	 * 
	 * @param normalizedValue
	 * @return
	 */
	public DomainType getNormalizedDomainValue(String normalizedValue) {
		if(StringUtils.isNotBlank(normalizedValue)) {
			return new DomainType(null,null,normalizedValue);
		}
		return null;
	}
	
	/**
	 * Returns highest priority value for attribute from the dataContainer
	 * 
	 * @param dataContainer
	 * @param attribute
	 * @return
	 */
	public Serializable getAttributeValueFromDataContainer(DataContainer dataContainer, String attribute) {
		if (dataContainer == null) {
			return null;
		}
		DataAttribute attributeByName = DataStorageEnum.getStorageByLevel(dataContainer.getLevel())
				.getAttributeByName(attribute);
		if (attributeByName != null) {
			return dataContainer.getHighestPriorityValue(attributeByName);
		}
		return null;
	}
	
	/**
	 * Returns the normalized value for domain type data attribute added in dataContainer.
	 * Throws IllegalArgumentException if attribute is not domain type attribute
	 * 
	 * @param dataContainer
	 * @param attributeName
	 * @return
	 */
	public String getNormalizedAttributeValueFromDataContainer(DataContainer dataContainer, String attributeName) {
		if (dataContainer == null) {
			return null;
		}
		DataAttribute attributeByName = DataStorageEnum.getStorageByLevel(dataContainer.getLevel())
				.getAttributeByName(attributeName);
		
		if(attributeByName!=null && !DataType.DOMAIN.equals(attributeByName.getDataType())) {
			throw new IllegalArgumentException("Attribute should be of Domain type:: "+attributeName);
		}
		
		if (attributeByName != null) {
			 DomainType domainValue = dataContainer.getHighestPriorityValue(attributeByName);
			 return domainValue!=null?domainValue.getNormalizedValue():null;
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 * Returns formatted date as per the input dateFormat
	 * 
	 * @param date
	 * @param dateFormat
	 * @return
	 */
	public String formatDate(LocalDate date, String dateFormat) {
		if(date !=null && StringUtils.isNotBlank(dateFormat)) {
			DateTimeFormatter formatter=DateTimeFormatter.ofPattern(dateFormat);
			return date.format(formatter);
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 * Returns current date in UTC
	 * @return
	 */
	public LocalDate getCurrentDateInUtc() {
		return LocalDate.now(ZoneId.of("Z"));
	}
	
	/**
	 * Enriches the files names with {@code prepender} and {@code appender} for LIST_STRING type field
	 * {@code dataAttribute} stored in {@code dataContainer}.
	 * <p>
	 * If {@code dataAttribute} is not List_String type then it throws IllegalArgumentException
	 * 
	 * @param prepender
	 * @param appender
	 * @return
	 */
	public Serializable enrichEventFileNames(DataContainer dataContainer, DataAttribute dataAttribute, String prepender,
			String appender) {
		if (dataContainer != null && dataAttribute != null && StringUtils.isNotBlank(prepender)
				&& StringUtils.isNotBlank(appender)) {

			if (!DataType.LIST_STRING.equals(dataAttribute.getDataType())) {
				throw new IllegalArgumentException("wrong input dataAttribute " + dataAttribute);
			}
			
			ArrayList<String> listStrings = dataContainer.getHighestPriorityValue(dataAttribute);
			if (!CollectionUtils.isEmpty(listStrings)) {
				return (Serializable) listStrings.stream()
						.map(fileName -> enrichFileName(fileName, prepender, appender)).collect(Collectors.toList());
			}
		}
		return null;
	}
	
	/**
	 * Returns the next incremented value for {@code counterAttribute} in the NESTED_ARRAY.
	 * 
	 * @param dataContainer
	 * @param counterAttribute
	 * @return
	 */
	public Long getNestedArrayCounter(DataContainer dataContainer, DataAttribute counterAttribute) {
		if(dataContainer!=null && counterAttribute!=null && counterAttribute.getParent()!=null)  {
			DataAttribute parent = counterAttribute.getParent();
			if(!DataType.NESTED_ARRAY.equals(parent.getDataType())) {
				throw new IllegalArgumentException("Parent of the dataAttribute should be NESTED_ATTRIBUTE type "+counterAttribute);
			}
			
			DataRow dataRow = (DataRow) dataContainer.getAttributeValue(parent);
			if(dataRow.getValue()!=null && !CollectionUtils.isEmpty(dataRow.getValue().getValue())) {
				return getNextCounter(dataRow.getValue().getValue(),counterAttribute);
			}
		}
		return null;
		
	}

	/**
	 * @param value
	 * @param counterAttribute
	 * @return
	 */
	private Long getNextCounter(ArrayList<DataRow> listDataRow, DataAttribute counterAttribute) {
		Optional<DataRow> max = listDataRow.stream().max((row1,row2)->compareCounterValues(counterAttribute, row1, row2));
		if(max.isPresent()) {
			Long counterValue = max.get().getHighestPriorityAttributeValue(counterAttribute);
			if(counterValue!=null) {
				return counterValue + 1l ;
			}
		}
		return Long.valueOf(1l);
	}

	/**
	 * @param counterAttribute
	 * @param row1
	 * @param row2
	 * @return
	 */
	private int compareCounterValues(DataAttribute counterAttribute, DataRow row1, DataRow row2) {
		int compare=0;
		Long row1Couner=row1.getHighestPriorityAttributeValue(counterAttribute);
		Long row2Counter= row2.getHighestPriorityAttributeValue(counterAttribute);
		if(row1Couner==null && row2Counter == null) {
			compare= 0;
		}else if(row1Couner!=null && row2Counter==null) {
			compare= 1;
		}else if(row1Couner==null) {
			compare= -1;
		}else {
			compare = row1Couner.compareTo(row2Counter);
		}
		return compare;
	}

	/**
	 * @param fileName
	 * @param prepender
	 * @param appender
	 * @return
	 */
	private Object enrichFileName(String filename, String prepender, String appender) {
		String baseName = FilenameUtils.getBaseName(filename);
		String extension = FilenameUtils.getExtension(filename);
		StringBuilder builder = new StringBuilder();
		builder.append(prepender).append(baseName).append(appender).append(".").append(extension);
		return builder.toString();
	}
	
	

}
