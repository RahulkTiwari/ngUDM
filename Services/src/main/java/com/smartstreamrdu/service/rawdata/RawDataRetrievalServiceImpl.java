/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataRetrievalServiceImpl.java
 * Author:  Padgaonkar
 * Date:    Jul 19, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.domain.autoconstants.EnRawDataAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.DomainStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Padgaonkar
 *
 */
@Slf4j
@Component
public class RawDataRetrievalServiceImpl implements RawDataRetrievalService {

	@Autowired
	private DataRetrievalService retrieve;

	@Autowired
	private CacheDataRetrieval cacheDataRetrieve;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject retrieveRawDataJSON(String rawDataId, String dataSource) throws UdmTechnicalException {

		DataStorageEnum dataStorageFromDataSource = cacheDataRetrieve.getDataStorageFromDataSource(dataSource);
		DataLevel rawDataLevel = DataStorageEnum.valueOf(dataStorageFromDataSource.name()).getRawDataLevel();

		Criteria criteria = getCriteria(rawDataId, rawDataLevel);

		DataRetrivalInput input = new DataRetrivalInput();
		input.setCriteria(criteria);
		DataContainer rawDataContainer = retrieveRawDataContainer(input);
		
		if(null == rawDataContainer) {
			return null;
		}

		DataAttribute recordAttribute = DataAttributeFactory.getAttributeByNameAndLevel(EnRawDataAttrConstant.COL_RAW_DATA_RECORD, rawDataLevel);
		String highestPriorityValue = rawDataContainer.getHighestPriorityValue(recordAttribute);
		
		JSONObject json = null;
		
		
		try {
			// Moved parser initialization to method level from class level due to suspected concurrency issues.
			JSONParser parser = new JSONParser();
			json= (JSONObject) parser.parse(highestPriorityValue);
		} catch (ParseException e) {
			throw new UdmTechnicalException("Error while parsing the record [" + highestPriorityValue + "]  : ", e);
		}	
		return json ;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataContainer retrieveRawDataContainer(DataRetrivalInput input) throws UdmTechnicalException {
		List<DataContainer> rawDataContainers = retrieve.retrieve(Database.Mongodb, input);

		if (rawDataContainers.isEmpty()) {
			log.error("No rawDataContainers found for input rawDataId", input);
			return null;
		}

		if (rawDataContainers.size() > 1) {
			log.error("Multiple DataContainers are retrieved for given rawDataId", input);
			throw new UdmTechnicalException("Multiple DataContainers are retrieved for given rawDataId", null);

		}
		return rawDataContainers.get(0);

	}

	/**
	 * This method generated criteria for retrieving rawData.
	 */
	private Criteria getCriteria(String rawDataId, DataLevel rawDataLevel) {
		Criteria statusCriteria = getStatusCriteria(rawDataLevel);
		Criteria idCriteria = getIdCriteria(rawDataLevel, rawDataId);
		return idCriteria.andOperator(statusCriteria);
	}

	/**
	 * @param rawDataLevel
	 * @param rawDataId
	 * @return
	 * 
	 */
	private Criteria getIdCriteria(DataLevel rawDataLevel, String rawDataId) {
		DataValue<String> idValue = new DataValue<>();
		idValue.setValue(LockLevel.NONE, rawDataId);
		DataAttribute idDataAttributeForDataLevel = DataAttributeFactory.getIdDataAttributeForDataLevel(rawDataLevel,
				false, LockLevel.NONE);
		return Criteria.where(idDataAttributeForDataLevel).is(idValue);
	}

	/**
	 * This method returns status criteria.
	 * @return
	 * 
	 */
	private Criteria getStatusCriteria(DataLevel rawDataLevel) {
		DataValue<DomainType> statusValue = new DataValue<>();
		statusValue.setValue(LockLevel.FEED, new DomainType(DomainStatus.ACTIVE));
		DataAttribute statusAttr = DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_STATUS_ATTRIBUTE, rawDataLevel);
		return Criteria.where(statusAttr).is(statusValue);
	}

}
