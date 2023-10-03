/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DefaultRawdataFeedContainerUpdateStrategy.java
 * Author:	Dedhia
 * Date:	Mar 17, 2023
 *
 *******************************************************************/
package com.smartstreamrdu.service.rawdata;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.Record;

/**
 *  DSOS specific implementation class for RawdataFeedContainerUpdateStrategy
 * 
 * @author Dedhia
 *
 */
@Component("rdsoRawdataFeedContainerUpdateStrategy")
public class DsosRawdataFeedContainerUpdateStrategy implements RawdataFeedContainerUpdateStrategy {

	@Autowired
	private RawDataGenerationService rawDataGenerator;

	/**
	 *
	 * Creates a new feed raw data container from the supplied Record and
	 * RawRecordContextDetailsPojo objects and returns the same.
	 *
	 */
	@Override
	public DataContainer updatedFeedDataContainer(Record rawdataRecord, RawRecordContextDetailsPojo recordPojo,
			DataContainer feedContainer) {
		RawDataInputPojo input = createRawDataInput(rawdataRecord, recordPojo.getDataSource(),recordPojo.getDeltaFieldsByParser(),recordPojo.getSourceUniqueIdAttributeValue(), recordPojo.getFeedExecutionDetailId(), recordPojo.getRawDataLevelForParser(), recordPojo.getFileType());
		return rawDataGenerator.generateRawData(input,recordPojo.getDataSource());
	}

	/**
	 * 
	 *  Create the object of RawDataInputPojo from the 
	 *  given data.
	 * 
	 * @param rawdataRecord
	 * @param dataSource
	 * @param deltaFields
	 * @param sourceUniqueIdValue
	 * @param feedExecutionDetailId
	 * @param rawLevel
	 * @param fileType
	 * @return
	 */
	private RawDataInputPojo createRawDataInput(Record rawdataRecord, String dataSource, List<String> deltaFields, String sourceUniqueIdValue, String feedExecutionDetailId,String rawLevel, String fileType){
		String codeHash=createCodeHash(rawdataRecord,deltaFields);
		RawDataInputPojo pojo = new RawDataInputPojo();
		pojo.setCodeHash(codeHash);
		pojo.setDataSourceValue(dataSource);
		pojo.setFeedExecutionDetailId(feedExecutionDetailId);
		pojo.setRawData(rawdataRecord.getRecordRawData().getRawData());
		pojo.setRawDataLevelValue(rawLevel);
		pojo.setUniqueColumnValue(sourceUniqueIdValue);
		pojo.setFileType(fileType);
		return pojo;
	}

	/**
	 *  Generates the code hash for the provided raw data record object based
	 *  on the delta fields provided.
	 * 
	 * @param rawdataRecord
	 * @param deltaFields
	 * @return
	 */
	private String createCodeHash(Record rawdataRecord, List<String> deltaFields) {
		return RawDataUtil.createCodeHash(rawdataRecord, deltaFields);
	}
	
}
