/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ProformaMessageGeneratorUtil.java
 * Author: Rushikesh Dedhia
 * Date: October 04, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.domain.autoconstants.EnDataAttrConstant;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

public class ProformaMessageGeneratorUtil {

	private static final DataAttribute ISIN_ATTRIBUTE = DataAttributeFactory
			.getAttributeByNameAndLevel(SdAttributeNames.ISIN, DataLevel.INS);
	private static final DataAttribute LEI_ATTRIBUTE = DataAttributeFactory
			.getAttributeByNameAndLevel(SdAttributeNames.LEI, DataLevel.LE);
	
	private static CacheDataRetrieval cacheDataRetrieval = SpringUtil.getBean(CacheDataRetrieval.class);

	// Private default constructor
	private ProformaMessageGeneratorUtil() {}
	
	public static ProformaMessage createProformaMessage(DataContainer postChangeDataContainer,
			DataLevel dataLevel) {
		
		ProformaMessage proformaMessage = new ProformaMessage();

		// Populate the data source in the Proforma message.
		populateDataSource(proformaMessage, postChangeDataContainer);

		// Populate the document id in the Proforma message.
		populateDocumentId(proformaMessage, postChangeDataContainer);

		// Populate the last modified date in the Proforma message.
		populateLastModifiedDate(proformaMessage, postChangeDataContainer);

		// Populate the unique lookup attributes in the Proforma message.
		populateUniqueAttribtutes(proformaMessage, postChangeDataContainer, dataLevel);

		return proformaMessage;
	}

	
	/**
	 * Populates the data source in the Proforma Message from the given data
	 * container.
	 * 
	 * @param proformaMessage
	 * @param postChangeDataContainer
	 */
	private static void populateDataSource(ProformaMessage proformaMessage, DataContainer postChangeDataContainer) {
		DomainType  dataSource = postChangeDataContainer.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute(postChangeDataContainer.getLevel()));
		Objects.requireNonNull(dataSource, "Data source cannot be null.");
		proformaMessage.setDataSource(dataSource.getVal());
	}
	
	/**
	 * This method will derive the last modified date of a data container based on
	 * the values of the latest INS_DATE and UPD_DATE.
	 * 
	 * @param proformaMessage
	 * @param postChangeDataContainer
	 */
	private static void populateLastModifiedDate(ProformaMessage proformaMessage, DataContainer postChangeDataContainer) {
		DomainType  dataSource = postChangeDataContainer.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute(postChangeDataContainer.getLevel()));
	    
		LocalDateTime insDate = (LocalDateTime) getValueForDataAttribute(getDataAttributeByNameAndDataSource(Constant.ListenerConstants.insertDate, dataSource), postChangeDataContainer);
		LocalDateTime updDate = (LocalDateTime) getValueForDataAttribute(getDataAttributeByNameAndDataSource(Constant.ListenerConstants.updateDate, dataSource), postChangeDataContainer);
		LocalDateTime dateTobeUsed = updDate != null ? updDate : insDate;
		Objects.requireNonNull(dateTobeUsed, "Last modified date is not available.");

		proformaMessage.setLastModifiedDate(Timestamp.valueOf(dateTobeUsed));
	}
	
	/**
	 * get data attribute by data source
	 */
	private static DataAttribute getDataAttributeByNameAndDataSource(String attributeName, DomainType dataSource) {
		DataStorageEnum dataStorage = cacheDataRetrieval.getDataStorageFromDataSource(dataSource.getVal());
	    return dataStorage.getAttributeByName(attributeName);
	}

	/**
	 * Populates the document id in the Proforma Message from the given data
	 * container.
	 * 
	 * @param proformaMessage
	 * @param postChangeDataContainer
	 */
	private static void populateDocumentId(ProformaMessage proformaMessage, DataContainer postChangeDataContainer) {
		String documentId = postChangeDataContainer.get_id();
		Objects.requireNonNull(documentId, "Document ID cannot be null.");
		proformaMessage.setDocumentId(documentId);
	}
	
	
	/**
	 * This method populates the unique attributes as per the data level of the data
	 * container in the uniqueAttributeVsValueMap of the ProformaMessage.
	 * 
	 * @param proformaMessage
	 * @param postChangeDataContainer
	 * @param proformaMessages 
	 * @param dataLevel 
	 */
	private static void populateUniqueAttribtutes(ProformaMessage proformaMessage, DataContainer postChangeDataContainer, DataLevel dataLevel) {

		if (dataLevel == DataLevel.INS) {
			// If the data container is of level INS, then ISIN is the lookup attribute for
			// proforma view
			proformaMessage.addUniqueAttributeValue(ISIN_ATTRIBUTE.getAttributeName(),
					getValueForDataAttribute(ISIN_ATTRIBUTE, postChangeDataContainer));
		} else if (dataLevel == DataLevel.LE) {
			// If the data container is of level LE, then LEI is the lookup attribute for
			// proforma view
			proformaMessage.addUniqueAttributeValue(LEI_ATTRIBUTE.getAttributeName(),
					getValueForDataAttribute(LEI_ATTRIBUTE, postChangeDataContainer));
		}else if (dataLevel == DataLevel.EN) {
			proformaMessage.addUniqueAttributeValue(EnDataAttrConstant.COL_EVENT_SOURCE_UNIQUE_ID,
                    getValueForDataAttribute(EnDataAttrConstant.EVENT_SOURCE_UNIQUE_ID, postChangeDataContainer));
		}
	}
	
	/**
	 * Returns the highest level value for the given data attribute from the given
	 * data container.
	 * 
	 * @param attribute
	 * @param postChangeDataContainer
	 * @return
	 */
	public static Serializable getValueForDataAttribute(DataAttribute attribute, DataContainer postChangeDataContainer) {
		return postChangeDataContainer.getHighestPriorityValue(attribute);
	}
}
