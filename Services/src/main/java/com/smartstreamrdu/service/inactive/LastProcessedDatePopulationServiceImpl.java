/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LastProcessedDatePopulationServiceImpl.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SdRawDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.persistence.service.util.AttributeToCriteriaConverterUtility;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.FullLoadBasedInactivationConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LastProcessedDatePopulationServiceImpl implements LastProcessedDatePopulationService {


	@Autowired
	@Setter
	private DataRetrievalService retrieve;

	@Autowired
	@Setter
	private PersistenceService persistence;

	@Autowired
	@Setter
	private AttributeToCriteriaConverterUtility conversionUtility;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateLastProcessedDateBasedOnRawContainer(String fileName, DataContainer rawDbDataContainer,
			EventConfiguration config) {

		//retrieving dataSource from Container
		DomainType dataSourceVal = rawDbDataContainer.getHighestPriorityValue(DataAttributeFactory
				.getAttributeByNameAndLevel(SdDataAttrConstant.COL_DATA_SOURCE, rawDbDataContainer.getLevel()));

		//Retrieving rawDataLevel for Container
		String rawDataLevel = rawDbDataContainer.getHighestPriorityValue(DataAttributeFactory
				.getAttributeByNameAndLevel(SdRawDataAttrConstant.COL_RAW_DATA_LEVEL, rawDbDataContainer.getLevel()));

		String dataSource = dataSourceVal.getVal();

		//checking whether input rawDataContainer is applicable or not based on supplied config
		if (!isRawContainerApplicable(config, fileName, rawDataLevel)) {
			return;
		}

		//Retrieving sdDataContainer based on input rawDataContainer
		DataContainer dc = retrieveSdDataContainerBasedOnRawUniqueId(config, rawDbDataContainer, dataSource);

		if (dc != null) {
			String inactivationLevel = config.getConditions().get(FullLoadBasedInactivationConstant.INACTIVATION_LEVEL);
			populateLastProcessedDate(dc, DataLevel.valueOf(inactivationLevel));
			persistence.persist(dc);
		}
	}

	/**
	 * This method checks whether container is applicable or not based on supplied config.
	 * @param config
	 * @param dataSource
	 * @param fileName
	 * @param rawDataLevel
	 * @return
	 */
	private boolean isRawContainerApplicable(EventConfiguration config, String fileName,
			String rawDataLevel) {

		Map<String, String> conditions = config.getConditions();
		String regex = conditions.get(FullLoadBasedInactivationConstant.REGEX_FOR_FULL_LOAD);
		String configRawDataLevel = conditions.get(FullLoadBasedInactivationConstant.RAW_DATA_LEVEL);

		//If following conditions matches then we execute event on container
		// if file is full file(based on regex)
		//If rawConatinerLevel == config Level(INS,LE)
		return fileName!= null && fileName.contains(regex)
				&& configRawDataLevel.equals(rawDataLevel);
	}

	/**
	 * Retrieving sdDataContainer based on rawDataContainer
	 * @param config
	 * @param rawDbDataContainer
	 * @param dataSource
	 * @return
	 */
	private DataContainer retrieveSdDataContainerBasedOnRawUniqueId(EventConfiguration config,
			DataContainer rawDbDataContainer, String dataSource) {

		String level = config.getConditions().get(FullLoadBasedInactivationConstant.RAW_DATA_LEVEL);

		if (DataLevel.INS.equals(DataLevel.valueOf(level))) {

			String rawObjectId = rawDbDataContainer.get_id();

			DataAttribute insRawDataId = InstrumentAttrConstant.INS_RAW_DATA_ID;

			DataValue<String> idValue = new DataValue<>();
			idValue.setValue(LockLevel.FEED, rawObjectId);

			Criteria cri = Criteria.where(insRawDataId).is(idValue);

			Criteria finalCriteria = cri.andOperator(getInsStatusCri(dataSource));
			DataRetrivalInput input = new DataRetrivalInput();
			input.setCriteria(finalCriteria);

			return getDataContainer(input, rawObjectId);
		}
		return null;

	}

	private Criteria getInsStatusCri(String dataSource) {
		return conversionUtility.createStatusAttributeCriteria(InstrumentAttrConstant.INSTRUMENT_STATUS, DomainStatus.ACTIVE,
				dataSource);
	}

	/**
	 * This method return sdDataContainer
	 * @param input
	 * @param insRawUniqueInd
	 * @return
	 */
	private DataContainer getDataContainer(DataRetrivalInput input, String insRawUniqueInd) {

		List<? extends Object> dataContainers = retriveContainer(input);

		if (dataContainers == null || dataContainers.isEmpty()) {
			log.error("No Active dataContainer found for requested rawSourceUniqueId :{}", insRawUniqueInd);
			return null;
		}

		if (dataContainers.size() > 1) {
			log.error("Multiple dataContainers found for requested rawSourceUniqueId :{}", insRawUniqueInd);
			return null;
		} else {
			return (DataContainer) dataContainers.get(0);
		}

	}

	private List<? extends Object> retriveContainer(DataRetrivalInput input) {
		List<? extends Object> dataContainers = new ArrayList<>();
		try {
			dataContainers = retrieve.retrieve(Database.Mongodb, input);
		} catch (UdmTechnicalException e) {
			log.error("Following error occured while retrieving dataContainers :{} from database", dataContainers);
		}
		return dataContainers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateLastProcessedDateInFeedContainer(DataContainer feedDataContainer, EventConfiguration config) {

		//checking whether feedContainer supplied is applicable or not based on config.
		//Currently this method is getting called postRuleExecution to add lastProcessedDate in container
		if (isFeedContainerApplicable(feedDataContainer, config)) {
			
			String level = config.getConditions().get(FullLoadBasedInactivationConstant.INACTIVATION_LEVEL);
			
			//populating last Processed date in container
			populateLastProcessedDate(feedDataContainer, DataLevel.valueOf(level));
		}
	}

	/**
	 * This method checks whether feedContainer is applicable or not based on supplied config.
	 * @param dc
	 * @param config
	 * @return
	 */
	private boolean isFeedContainerApplicable(DataContainer dc, EventConfiguration config) {

		Map<String, String> conditions = config.getConditions();
		String level = conditions.get(FullLoadBasedInactivationConstant.CONTAINER_LEVEL);

		//checking container level
		return DataLevel.valueOf(level).equals(dc.getLevel());
	}

	@Override
	public void populateLastProcessedDate(DataContainer dc, DataLevel level) {

		// Currently feature is implemented for seclastProcessedDate only.
		if (DataLevel.SEC == level) {
			List<DataContainer> allChildDataContainers = dc.getAllChildDataContainers();
			for (DataContainer con : allChildDataContainers) {
				DataAttribute lastOccuranceDate = SecurityAttrConstant.SEC_LAST_PROCESSED_DATE;
				DataValue<LocalDateTime> localDate = new DataValue<>();
				localDate.setValue(LockLevel.ENRICHED, java.time.LocalDateTime.now());
				con.addAttributeValue(lastOccuranceDate, localDate);
			}
		}else {
			log.error("Request level:{} is not configured for populating lastProcessedDate",level);
		}
	}
}
