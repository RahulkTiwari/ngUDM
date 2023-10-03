/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : EnReprocessingDataRetrievalService.java
 * Author :SaJadhav
 * Date : 04-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;

/**
 * Service to retrieve the affected EN documents for the change in domain mappings
 * 
 * @author SaJadhav
 *
 */
@Component("ENReprocessingDataRetrievalService")
public class EnReprocessingDataRetrievalService extends AbstractReprocessingDataRetrievalService {

	@Override
	protected DataStorageEnum getDataStorageLevel() {
		return DataStorageEnum.EN;
	}

	@Override
	protected List<DataLevel> getApplicableDataLevels() {
		return Arrays.asList(DataLevel.EN);
	}

	
}
