/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataFilterTest.java
 * Author:  Padgaonkar
 * Date:    May 28, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.domain.autoconstants.SdRawDataAttrConstant;
import com.smartstreamrdu.util.Constant;

/**
 * @author Padgaonkar
 *
 */
public class RawDataFilterTest {

	public static final DataAttribute RAW_DATA_INS_DATE = DataAttributeFactory.getAttributeByNameAndLevel("insDate",DataLevel.SD_RAW_DATA);
	public static final DataAttribute RAW_DATA_UPD_DATE = DataAttributeFactory.getAttributeByNameAndLevel("updDate",DataLevel.SD_RAW_DATA);
		
	@Test
	public void testRawDataFullLoadFilter() {
		DataContainer feedDataContainer;
		DataContainer dbDataContainer;
		feedDataContainer = new DataContainer(DataLevel.SD_RAW_DATA,null);
		dbDataContainer = new DataContainer(DataLevel.SD_RAW_DATA,null);	
		boolean filter = new RawDataFullLoadFilter().filter(feedDataContainer, dbDataContainer, null);	
		Assert.assertEquals(false, filter);
	}
	
	
	@Test
	public void testRawDataDeltaProcessingFilter() {
		DataContainer feedDataContainer;
		DataContainer dbDataContainer;
		feedDataContainer = new DataContainer(DataLevel.SD_RAW_DATA, null);
		dbDataContainer = new DataContainer(DataLevel.SD_RAW_DATA, null);

		DataValue<Serializable> codeHash1 = new DataValue<>();
		codeHash1.setValue(LockLevel.RDU, "12345");
		DataAttribute codeHash = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.CODE_HASH_ATTRIBUTE, DataLevel.SD_RAW_DATA);
		feedDataContainer.addAttributeValue(codeHash, codeHash1);
		
		boolean filter = new RawDataDeltaProcessingFilter().filter(feedDataContainer, null, null);
		Assert.assertEquals(false, filter);
		
		dbDataContainer.addAttributeValue(codeHash, codeHash1);
		
		boolean filter1 = new RawDataDeltaProcessingFilter().filter(feedDataContainer, dbDataContainer, null);
		Assert.assertEquals(true, filter1);
		
		DataValue<Serializable> codeHash12 = new DataValue<>();
		codeHash12.setValue(LockLevel.RDU, "123452");
		feedDataContainer.addAttributeValue(codeHash, codeHash12);
		
		boolean filter2 = new RawDataDeltaProcessingFilter().filter(feedDataContainer, dbDataContainer, null);
		Assert.assertEquals(false, filter2);
	}
	
	
	@Test
	public void testRawDataReprocessingFilter() {
		DataContainer feedDataContainer;
		DataContainer dbDataContainer;
		feedDataContainer = new DataContainer(DataLevel.SD_RAW_DATA, null);
		dbDataContainer = new DataContainer(DataLevel.SD_RAW_DATA, null);

		DataValue<Serializable> codeHash1 = new DataValue<>();
		codeHash1.setValue(LockLevel.RDU, "12345");
		DataAttribute codeHash = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.CODE_HASH_ATTRIBUTE, DataLevel.SD_RAW_DATA);
		feedDataContainer.addAttributeValue(codeHash, codeHash1);
		dbDataContainer.addAttributeValue(codeHash, codeHash1);
		
		DataAttribute rawDataInsDate = SdRawDataAttrConstant.INS_DATE;
		
		DataValue<LocalDateTime> insDate = new DataValue<>();
		LocalDateTime insDateVal = LocalDateTime.of(1992, 12, 11, 03, 05);
		insDate.setValue(LockLevel.RDU, insDateVal);
		dbDataContainer.addAttributeValue(rawDataInsDate, insDate);

		RawDataFilterContext context = RawDataFilterContext.builder().pendingFileProcessingStartDate(LocalDateTime.of(1992, 12, 11, 03, 06)).build();
		boolean filter1 = new RawDataReprocessingFilter().filter(feedDataContainer, dbDataContainer, context);
		Assert.assertEquals(true, filter1);
	}
	
	@Test
	public void testRawDataReprocessingFilter_updDate() {
		DataContainer feedDataContainer;
		DataContainer dbDataContainer;
		feedDataContainer = new DataContainer(DataLevel.SD_RAW_DATA, null);
		dbDataContainer = new DataContainer(DataLevel.SD_RAW_DATA, null);

		DataValue<Serializable> codeHash1 = new DataValue<>();
		codeHash1.setValue(LockLevel.RDU, "12345");
		DataAttribute codeHash = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.CODE_HASH_ATTRIBUTE, DataLevel.SD_RAW_DATA);
		feedDataContainer.addAttributeValue(codeHash, codeHash1);
		dbDataContainer.addAttributeValue(codeHash, codeHash1);
		
	   DataAttribute rawDataUpdateDate = SdRawDataAttrConstant.UPD_DATE;
		
		DataValue<LocalDateTime> insDate = new DataValue<>();
		LocalDateTime insDateVal = LocalDateTime.of(1992, 12, 11, 03, 10);
		insDate.setValue(LockLevel.RDU, insDateVal);
		dbDataContainer.addAttributeValue(rawDataUpdateDate, insDate);

		RawDataFilterContext context = RawDataFilterContext.builder().pendingFileProcessingStartDate(LocalDateTime.of(1992, 12, 11, 03, 06)).build();
		boolean filter1 = new RawDataReprocessingFilter().filter(feedDataContainer, dbDataContainer, context);
		Assert.assertEquals(false, filter1);
	}
	
}
