/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FullFileInactivationService.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.enrichment.service;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaRDD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.persistence.domain.UdlMetrics;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.repository.service.UdlMetricsRepositoryService;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.persistence.service.util.AttributeToCriteriaConverterUtility;
import com.smartstreamrdu.service.retrieval.MongoSparkDataRetrieval;
import com.smartstreamrdu.service.spark.SparkUtil;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.FullLoadBasedInactivationConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This service  is responsible for inactivating records which are not part of
 * the full file load.This is based on databaseConfigurtion mentioned in {@link EventConfiguration}
 * collection.
 * 
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class FullFileInactivationService {

	@Autowired
	@Setter
	private MongoSparkDataRetrieval mongoSparkDataRetrieval;

	@Autowired
	@Setter
	private SparkUtil sparkUtil;

	@Autowired
	@Setter
	private AttributeToCriteriaConverterUtility conversionUtility;

	@Autowired
	@Setter
	private UdlMetricsRepositoryService repository;
	
	@Autowired
	@Setter
	private DataRetrievalService retrieveService;

	/**
	 * Based on provided EventConfiguration & EventMessage this method checks whether bean is applicable or not
	 * If service is applicable,then its inactivate securities/instrument which are not part of the full file load.
	 * 
	 * @param config
	 * @param message
	 * @throws UdmTechnicalException 
	 */
	public void inactivationBasedOnFullLoad(EventConfiguration config, EventMessage message) throws UdmTechnicalException {

		if (!isApplicable(message)) {
			return;
		}

		//calculating when first full file get for mentioned timeframe into system
		Date fullFileLoadStartDate = getFullFileLoadStartDate(config.getConditions(),config.getDataSource());
		
		
		if(fullFileLoadStartDate == null) {
			log.error("Failed to retrieve full load startDate from udlMetrics");
			return;
		}
		LocalDateTime fullLoadStartDateTime = Instant.ofEpochMilli(fullFileLoadStartDate.getTime())
				.atZone(ZoneId.systemDefault()).toLocalDateTime();

		//Retrieving inactivationLevel from config
		Map<String, Serializable> eventAttributes = message.getEventAttributes();
		String level = config.getConditions().get(FullLoadBasedInactivationConstant.INACTIVATION_LEVEL);

		// Calculating threshold for inactivation
		if (!isCountForInactivationIsValid(level, config, eventAttributes, fullLoadStartDateTime)) {
			log.error("As calculated threshold is greater than expected threshold,Not processing inactivation");
			return;
		}
	
		//Currently inactivation based on fullLoad is supported for sec level only.Means we are populating 
		//secLastProcessedDate attribute during loader execution & based on this & fullFileLoadStartDate 
		//we in-activate security which are not part of the full file load.
		if (DataLevel.SEC.equals(DataLevel.valueOf(level))) {

			//Generating input criteria to retrieve securities from database
			DataRetrivalInput dataRetrievalInput = getDataRetrievalInput(fullLoadStartDateTime, eventAttributes);

			JavaRDD<DataContainer> rddOfApplicableInsForInactivation = getDataContainerRdd(dataRetrievalInput);

			if (rddOfApplicableInsForInactivation == null) {	
				return;
			}

			rddOfApplicableInsForInactivation.foreachPartition(dataContainerIterator -> {
				FullLoadBasedDataContainerInactivationService inactivation = SpringUtil
						.getBean(FullLoadBasedDataContainerInactivationService.class);
				
				//Inactivating applicable securities
				inactivation.inactivateApplicableSecurity(dataContainerIterator, fullLoadStartDateTime);
			});
		}
       
		log.info("Inactivation based on full file load completed");
	}

	/**
	 * This method calculates threshold for inactivation.
	 * 
	 * @param level
	 * @param config
	 * @param eventAttributes
	 * @param fullLoadStartDateTime
	 * @return
	 * @throws UdmTechnicalException
	 */
	private boolean isCountForInactivationIsValid(String level, EventConfiguration config,
			Map<String, Serializable> eventAttributes, LocalDateTime fullLoadStartDateTime)
			throws UdmTechnicalException {
		String dataSource = (String) eventAttributes.get(SdDataAttrConstant.COL_DATA_SOURCE);
		String threshold = config.getConditions().get(FullLoadBasedInactivationConstant.SEC_INACTIVATION_THRESHOLD);

		float expThreshld = Float.parseFloat(threshold);
		float calThrshld = 0.0f;

		if (DataLevel.SEC.equals(DataLevel.valueOf(level))) {
			int noOfSecuritiesTobeInactivated = getNoOfSecuritiesTobeInactivated(dataSource, fullLoadStartDateTime);
			int activeSecurityCount = getActiveSecurityCount(dataSource);

			float calThresholdRatio = (float) noOfSecuritiesTobeInactivated / (float) activeSecurityCount;
			log.info("Calculated threshold value is :{}", calThresholdRatio * 100);
			log.info("Expected threshold value is :{}", expThreshld);

			calThrshld = calThresholdRatio * 100;

			if (expThreshld > calThrshld) {
				log.info(
						"Calculated threshold count :{} is less than expected threshold count:{} hence processing inactivation",
						calThrshld, expThreshld);
				return true;
			}
		}
		log.error(
				"Calculated threshold:{} count is greater than expected threshold:{} count hence not processing inactivation",
				calThrshld, expThreshld);
		return false;
	}

	/**
	 * This method returns active security count.
	 */
	private int getActiveSecurityCount(String dataSource) throws UdmTechnicalException {
		//get active instrument status criteria
		DataRetrivalInput input = getCountQueryRetrivalInput(dataSource,null);
		
		log.info("Retrieving no of  active Securities from system");
		int containerCount = retrieveService.getContainerCount(Database.Mongodb, input);
		log.info("No of active securities in system are : {}", containerCount);

		return containerCount;	
	}

	/**
	 * Retrieval Criteria for calculating threshold
	 * 
	 * @param secLastProcessedDateCri
	 */
	private DataRetrivalInput getCountQueryRetrivalInput(String dataSource, Criteria secLastProcessedDateCri) {
		Criteria insStatusCri = getActiveInsStatusCri(dataSource);

		// adding criteria for dataSource.
		Criteria dataSourceCri = getDataSourceCriteria(dataSource);

		Criteria secStatusCri = getActiveSecStatusCri(dataSource);

		DataRetrivalInput input = new DataRetrivalInput();

		if (null == secLastProcessedDateCri) {
			input.setCriteria(insStatusCri.andOperator(secStatusCri, dataSourceCri));
		} else {
			input.setCriteria(insStatusCri.andOperator(secStatusCri, dataSourceCri, secLastProcessedDateCri));
		}
		input.setLevel(DataLevel.INS);

		input.setOutputUnwindPath(FullLoadBasedInactivationConstant.SEC_UNWIND_PATH);

		return input;

	}

	/**
	 * This method returns no of securities to be inactivated
	 * @param dataSource
	 * @param fullLoadStartDateTime
	 * @return
	 * @throws UdmTechnicalException
	 */
	private int getNoOfSecuritiesTobeInactivated(String dataSource, LocalDateTime fullLoadStartDateTime)
			throws UdmTechnicalException {

		Criteria secLastProcessedDateCri = getSecLastProcessedDateCriteria(fullLoadStartDateTime);
		DataRetrivalInput countQueryRetrivalInput = getCountQueryRetrivalInput(dataSource,secLastProcessedDateCri);
		
		// get getSecLastProcessedDate criteria

		countQueryRetrivalInput.getCriteria().andOperator(secLastProcessedDateCri);

		log.info("Retrieving no of Securities are about to inactive");
		int containerCount = retrieveService.getContainerCount(Database.Mongodb, countQueryRetrivalInput);
		log.info("Expected no of :{}securities about to inactive are :", containerCount);
		
		return containerCount;

	}

	private JavaRDD<DataContainer> getDataContainerRdd(DataRetrivalInput dataRetrievalInput) {
		JavaRDD<DataContainer> rddOfApplicableInsForInactivation = null;
		try {
			rddOfApplicableInsForInactivation = mongoSparkDataRetrieval.retrieveSparkRDD(dataRetrievalInput,
					sparkUtil.getSparkContext());
		} catch (UdmTechnicalException e) {
			log.error("Following error:{} occured while retrieving dataContainer", e);
		}
		return rddOfApplicableInsForInactivation;
	}

	/**
	 * Generating input criteria to determine security which needs to be inactivated.
	 * @param fullLoadStartDateTime
	 * @param eventAttributes
	 * @return
	 */
	private DataRetrivalInput getDataRetrievalInput(LocalDateTime fullLoadStartDateTime,Map<String, Serializable> eventAttributes) {

		String dataSource = (String) eventAttributes.get(SdDataAttrConstant.COL_DATA_SOURCE);
		
		//get getSecLastProcessedDate criteria
		Criteria cri = getSecLastProcessedDateCriteria(fullLoadStartDateTime);
		
		//get active instrument status criteria
		Criteria insStatusCri = getActiveInsStatusCri(dataSource);

		//adding criteria for dataSource.
		Criteria dataSourceCri = getDataSourceCriteria(dataSource);

		DataRetrivalInput input1 = new DataRetrivalInput();
		input1.setCriteria(cri.andOperator(insStatusCri, dataSourceCri));
		input1.setLevel(DataLevel.INS);

		return input1;
	}

	/**
	 * DataSource Criteria
	 */
	private Criteria getDataSourceCriteria(String dataSource) {
		DataValue<DomainType> dataSourceVal = new DataValue<>();
		dataSourceVal.setValue(LockLevel.FEED, new DomainType(dataSource));
		return Criteria.where(SdDataAttrConstant.DATA_SOURCE).is(dataSourceVal);
	}

	/**
	 * Security Last Processed Date Criteria
	 */
	private Criteria getSecLastProcessedDateCriteria(LocalDateTime fullLoadStartDateTime) {
		DataAttribute secLastProcessedDate = SecurityAttrConstant.SEC_LAST_PROCESSED_DATE;

		DataValue<LocalDateTime> dateValue = new DataValue<>();
		dateValue.setValue(LockLevel.ENRICHED, fullLoadStartDateTime);

		//security lastProcessedDate is needs to be  less than fullFileLoadStartDate
		return  Criteria.where(secLastProcessedDate).lte(dateValue);
	}

	/**
	 * Instrument status criteria
	 */
	private Criteria getActiveInsStatusCri(String dataSource) {
		return conversionUtility.createStatusAttributeCriteria(InstrumentAttrConstant.INSTRUMENT_STATUS, DomainStatus.ACTIVE,
				dataSource);

	}
	
	/**
	 * Security status criteria
	 */
	private Criteria getActiveSecStatusCri(String dataSource) {
		return conversionUtility.createStatusAttributeCriteria(SecurityAttrConstant.SECURITY_STATUS, DomainStatus.ACTIVE,
				dataSource);

	}

	/**
	 * This method returns full file load startDate for mentioned timeframe.
	 * @param attributeToValueMap
	 * @param dataSource 
	 * @return
	 */
	private Date getFullFileLoadStartDate(Map<String, String> attributeToValueMap, String dataSource) {
		String regex = attributeToValueMap.get(FullLoadBasedInactivationConstant.REGEX_FOR_FULL_LOAD);

		//Retrieve UdlMetrics entries based on dataSource & fullFileName regex
		List<UdlMetrics> udlMetricsEntriesBasedOnFileNameRegex = repository
				.getUdlMetricsEntriesBasedOnFileNameRegex(dataSource, regex);

		//Based on mentioned timeFrame determine how many days worth of file load need to consider.
		//say if timeFrame = 7days, the we need to consider all full files loaded in last 7 days.
		LocalDateTime startDateToConsider = LocalDateTime.now()
				.minusDays(Long.valueOf(attributeToValueMap.get(FullLoadBasedInactivationConstant.FULL_LOAD_TIME_FRAME)));
		
		Date lastWeekStartDate = Date.from(startDateToConsider.atZone(ZoneId.systemDefault()).toInstant());
		//List of all full files which has got loaded within mentioned timeframe
		List<UdlMetrics> filteredList = udlMetricsEntriesBasedOnFileNameRegex.stream()
				.filter(entry -> entry.getStartDate().after(lastWeekStartDate)).collect(Collectors.toList());

		
		// Sorting all entries based on startDate
		Collections.sort(filteredList, new UdlMetricsDateComparator());

		//Considering firstFile startDate -- full file load startDate for mentioned timeframe
		if (!filteredList.isEmpty()) {
			log.info("Full load start udlMetrics entry is :{}", filteredList.get(0));
			log.info("Full load start date retrieved is : {}", filteredList.get(0).getStartDate());
			return filteredList.get(0).getStartDate();
		}
		return null;
	}

	/**
	 * This method validates whether bean is applicable or not based on eventMessage.
	 * @param config
	 * @param message
	 * @return
	 */
	private boolean isApplicable(EventMessage message) {
		String eventName = message.getEventName();
		
		//If eventName is inactivationBasedOnFullLoad then bean is applicable
		return eventName != null && eventName.equals(FullLoadBasedInactivationConstant.INACTIVATION_BASED_ON_FULL_LOAD);
		
	}
}
