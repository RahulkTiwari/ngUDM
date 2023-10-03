/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataGenerationServiceImpl.java
 * Author:	Divya Bharadwaj
 * Date:	07-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RawDataConstants;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.util.Constant;

/**
 * @author Bharadwaj
 *
 */
@Component
public class RawDataGenerationServiceImpl implements RawDataGenerationService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7180296260772149233L;
	
	@Autowired
	private CacheDataRetrieval cacheDataRetrieve;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.rawdata.RawDataGenerationService#generateRawData(com.smartstreamrdu.domain.Record, java.lang.String, java.lang.String)
	 */
	@Override
	public DataContainer generateRawData(RawDataInputPojo obj,String dataSource) {
		DataStorageEnum dataStorageFromDataSource = cacheDataRetrieve.getDataStorageFromDataSource(dataSource);
		DataLevel rawDataLevel = dataStorageFromDataSource.getRawDataLevel();
		DataContainer container = new DataContainer(rawDataLevel , DataContainerContext.builder().build());
		
		prepareData(obj,container);
		
		return container;
	}

	private void prepareData(RawDataInputPojo obj, DataContainer container) {
		DataLevel attributeLevel = container.getLevel();
		DataAttribute rawData = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_RECORD_ATTRIBUTE, attributeLevel);
		DataAttribute rawDataUniqueCode = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_SOURCE_UNIQUE_CODE_ATTRIBUTE, attributeLevel);
		DataAttribute rawDataCodeHash = DataAttributeFactory
				.getAttributeByNameAndLevel(RawDataConstants.CODE_HASH_ATTRIBUTE, attributeLevel);
		DataAttribute rawDataLevel = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_LEVEL_ATTRIBUTE, attributeLevel);
		DataAttribute rawDataStatus = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_STATUS_ATTRIBUTE, attributeLevel);
		DataAttribute dataSource = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.DATASOURCE_ATTRIBUTE, attributeLevel);
		DataAttribute feedExecutionDetail = DataAttributeFactory.getAttributeByNameAndLevel(
				Constant.SdAttributeNames.FEED_EXECUTION_DETAIL_ID_ATTRIBUTE, attributeLevel);
		DataAttribute fileType = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.FILE_TYPE_ATTRIBUTE, attributeLevel);
		DataValue<String> rawDataValue=new DataValue<>();
		rawDataValue.setValue(LockLevel.FEED, JsonConverterUtil.convertToJson(obj.getRawData()));
		DataValue<String> uniqueCode= new DataValue<>();
		uniqueCode.setValue(LockLevel.FEED,obj.getUniqueColumnValue());
		
		DataValue<String> codeHash =  new DataValue<>();
		codeHash.setValue(LockLevel.FEED,obj.getCodeHash());
		
		DataValue<DomainType> rawDataDataSource = new DataValue<>();
		DomainType val=new DomainType();
		val.setVal(obj.getDataSourceValue());
		rawDataDataSource.setValue(LockLevel.FEED, val);
		
		DataValue<String> rawDataLevelValue=new DataValue<>();
		rawDataLevelValue.setValue(LockLevel.FEED, obj.getRawDataLevelValue());
		
		DataValue<DomainType> rawDataDataStatusVal = new DataValue<>();
		DomainType val1=new DomainType();
		val1.setVal(Constant.DomainStatus.ACTIVE);
		rawDataDataStatusVal.setValue(LockLevel.FEED, val1);
		
		DataValue<String> feedExecutionDetailId=new DataValue<>();
		feedExecutionDetailId.setValue(LockLevel.FEED, obj.getFeedExecutionDetailId());
		
		DataValue<String> fileTypeVal=new DataValue<>();
		fileTypeVal.setValue(LockLevel.FEED, obj.getFileType());
		
		container.addAttributeValue(rawData, rawDataValue);
		container.addAttributeValue(rawDataUniqueCode, uniqueCode);
		container.addAttributeValue(rawDataCodeHash, codeHash);
		container.addAttributeValue(rawDataLevel, rawDataLevelValue);
		container.addAttributeValue(rawDataStatus, rawDataDataStatusVal);
		container.addAttributeValue(dataSource, rawDataDataSource);
		container.addAttributeValue(feedExecutionDetail, feedExecutionDetailId);
		container.addAttributeValue(fileType, fileTypeVal);

	}
}
