/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : SdInsReprocessingDataRetrievalService.java
 * Author :SaJadhav
 * Date : 07-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;

/**
 * Service to retrieve the affected SD instruments/securities documents for the change in domain mappings.
 * 
 * @author SaJadhav
 *
 */
@Component("SDReprocessingDataRetrievalService")
public class SdInsReprocessingDataRetrievalService extends AbstractReprocessingDataRetrievalService {

	@Override
	protected DataStorageEnum getDataStorageLevel() {
		return DataStorageEnum.SD;
	}

	@Override
	protected List<DataLevel> getApplicableDataLevels() {
		return Arrays.asList(DataLevel.INS,DataLevel.SEC);
	}

}
