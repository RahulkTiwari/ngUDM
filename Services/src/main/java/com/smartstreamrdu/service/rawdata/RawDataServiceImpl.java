/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataHandler.java
 * Author:	Divya Bharadwaj
 * Date:	07-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.event.process.listener.EventListenerFactory;
import com.smartstreamrdu.service.exception.LoaderExceptionLoggingHandler;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.merging.DataContainerMergingService;
import com.smartstreamrdu.service.rawdata.listeners.RawDataEvent;
import com.smartstreamrdu.service.rawdata.listeners.RawDataListenerInput;
import com.smartstreamrdu.service.rawdata.listeners.RawDataProcessListener;
import com.smartstreamrdu.service.telemetry.RecordTelemetry;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.SdRawDataConstant;

import lombok.Setter;

/**
 * @author Bharadwaj
 *
 */
@Component
public class RawDataServiceImpl implements RawDataService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3977109081226929269L;
	
	@Autowired
	private PersistenceService persistDao;
	@Autowired
	private RawDataGenerationService rawDataGenerator;
	@Autowired
	private DataContainerMergingService mergingService;
	@Autowired
	private DataRetrievalService service;
	@Autowired
	private LoaderExceptionLoggingHandler exceptionLoggingHandler;
	@Autowired
	private transient FeedVsDbRawdataMergeFactory feedVsDbRawdataMergeFactory;
	
	@Autowired
	private CacheDataRetrieval cacheDataRetrieve;
	
	@Setter
	@Autowired
	private EventListenerFactory eventListenerFactory;
	
	@Setter
	@Autowired
	private transient RawdataFeedContainerUpdateStrategyFactory rawdataFeedContainerUpdateStrategyFactory;
	
	private static final Logger _logger = LoggerFactory.getLogger(RawDataServiceImpl.class);

	
	/**
	 * @param dataSource
	 * @param codeHash
	 * @param uniqueColumnValue
	 * @return
	 * @throws Exception 
	 */
	private List<DataContainer> getRawDataFromDb(String dataSource,Collection<String> uniqueColumnValue) {
		Criteria criteria = getCriteria(dataSource,uniqueColumnValue);

		if (criteria != null) {
			List<DataContainer> retrive;
			try {
				DataRetrivalInput input=new DataRetrivalInput();
				input.setCriteria(criteria);
				retrive = service.retrieve(Database.Mongodb,input);

				if (retrive != null) {
					return retrive;
				}
			} catch (Exception e) {
				_logger.error(
						"RawData container retrival based on datasource:{},uniqueCode:{} failed due to {}",
						dataSource, uniqueColumnValue, e.getMessage());
			}
		}
		return Arrays.asList();
	}

	/**
	 * @param dataSource
	 * @param fileType
	 * @param uniqueColumnValue
	 * @return
	 */
	private Criteria getCriteria(String dataSource, Collection<String> uniqueColumnValues) {
		
		DataStorageEnum dataStorageFromDataSource = cacheDataRetrieve.getDataStorageFromDataSource(dataSource);
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		DomainType val=new DomainType();
		val.setVal(dataSource);
		dataSourceValue.setValue(LockLevel.FEED, val);
		
		Collection<DataValue<? extends Serializable>> uniqueCodeValCollection = new ArrayList<>();
		
		for (String uniqueColumnValue : uniqueColumnValues) {
			DataValue<String> uniqueCodeVal=new DataValue<>();
			uniqueCodeVal.setValue(LockLevel.FEED, uniqueColumnValue);
			uniqueCodeValCollection.add(uniqueCodeVal);
		}
		
		DataValue<DomainType> rawDataDataStatusVal = new DataValue<>();
		DomainType val1=new DomainType();
		val1.setVal(Constant.DomainStatus.ACTIVE);
		rawDataDataStatusVal.setValue(LockLevel.FEED, val1);
		
		DataLevel rawDataLevel = DataStorageEnum.valueOf(dataStorageFromDataSource.name()).getRawDataLevel();
		DataAttribute statusAttr=DataAttributeFactory.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_STATUS_ATTRIBUTE, rawDataLevel);
		DataAttribute rawDataUniqueCode=DataAttributeFactory.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_SOURCE_UNIQUE_CODE_ATTRIBUTE, rawDataLevel);
		DataAttribute dataSourceAttr=DataAttributeFactory.getAttributeByNameAndLevel(Constant.SdAttributeNames.DATASOURCE_ATTRIBUTE, rawDataLevel);
		
		if(dataSource!=null && !CollectionUtils.isEmpty(uniqueCodeValCollection)){			
			Criteria criteria = Criteria
					.where(dataSourceAttr)
					.is(dataSourceValue);
			List<Criteria> criterias = new ArrayList<>();
			criterias.add(Criteria.where(rawDataUniqueCode).in(uniqueCodeValCollection));
			criterias.add(Criteria.where(statusAttr).is(rawDataDataStatusVal));
			criteria=criteria.andOperator(criterias.toArray(new Criteria[criterias.size()]));
			return criteria;
		}
		return null;
	}

	public RawDataInputPojo createRawDataInput(Record record, String dataSource, List<String> deltaFields, String sourceUniqueIdValue, String feedExecutionDetailId,String rawLevel, String fileType){
		String codeHash=createCodeHash(record,deltaFields);
		RawDataInputPojo pojo = new RawDataInputPojo();
		pojo.setCodeHash(codeHash);
		pojo.setDataSourceValue(dataSource);
		pojo.setFeedExecutionDetailId(feedExecutionDetailId);
		pojo.setRawData(record.getRecordRawData().getRawData());
		pojo.setRawDataLevelValue(rawLevel);
		pojo.setUniqueColumnValue(sourceUniqueIdValue);
		pojo.setFileType(fileType);
		return pojo;
		
	}

	
	/**
	 * Take codeHash from record Object or calculate it from rawdata fields.
	 * @param record
	 * @param deltaFields
	 * @return
	 */
	private String createCodeHash(Record record, List<String> deltaFields) {
		if(record.getHashValue()!=null) {
			//in case of esma/anna we store codeHash here so take it from record object.
			return record.getHashValue();
		}		
		return RawDataUtil.createCodeHash(record, deltaFields);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.rawdata.RawDataService#handleRawData(java.util.List, java.lang.String, java.util.List, java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.smartstreamrdu.service.rawdata.RawDataInactiveFilterService)
	 */
	@Override
	public List<Record> handleRawData(List<RawRecordContextDetailsPojo> batchOfRecords, RawDataInactiveFilterService inactiveFilterService,RawDataFilter rawDataFilter,
			RawDataFilterContext rawDataFilterContext, String feedVsDbRawDataMergeStrategy, String filePath) {
		
		if (CollectionUtils.isEmpty(batchOfRecords)) {
			return Collections.emptyList();
		}
		
		Map<String, RawDataInputPojo> inputMap = new HashMap<>();
		Map<String, DataContainer> feedDataContainerMap = new HashMap<>();
		
		populateUniqueIdVsDataMaps(batchOfRecords, inputMap, feedDataContainerMap);
		
		List<DataContainer> dbRawDataContainers=getRawDataFromDb(batchOfRecords.get(0).getDataSource(), inputMap.keySet());
		
		Map<String, DataContainer> rawSourceUniqueIdVsDataContainer = populateRawSourceUniqueIdVsDataContainerMap(inputMap.keySet(), dbRawDataContainers);
		
		List<Record> processedRecords = new ArrayList<>();
		
		for (RawRecordContextDetailsPojo pojo : batchOfRecords) {
			
			Record v1 = pojo.getRecord();
			String rawSourceUniqueValue = pojo.getSourceUniqueIdAttributeValue();
			
			DataContainer feedRawdataContainer = feedDataContainerMap.get(rawSourceUniqueValue);
			
			RawDataInputPojo feedInput = inputMap.get(rawSourceUniqueValue);
			
			DataContainer dbDataContainer = rawSourceUniqueIdVsDataContainer.get(rawSourceUniqueValue);
			
			boolean shouldPersist = true;
			if(inactiveFilterService != null && rawSourceUniqueIdVsDataContainer != null) {
				shouldPersist = inactiveFilterService.shouldPersistRawData(v1, dbDataContainer == null ? null : Arrays.asList(dbDataContainer));
			}
			if(!shouldPersist){
				v1.setToBeProcessed(false);
				_logger.debug("Not persisting the record :{} as inactiveFilterService has returned false",v1);
				continue;
			}

			/**
			 * Get an instance of FeedVsDbMergeService by calling the FeedVsDbRawdataMergeFactory and 
			 * passing the parameter 'feedVsDbRawDataMergeStrategy'(i.e. populated in feedConfiguration collection).
			 */
			FeedVsDbRawdataMergeService feedVsDbMergeStrategy = feedVsDbRawdataMergeFactory.getFeedVsDbMergeStrategy(feedVsDbRawDataMergeStrategy);
			v1 = mergeFeedVsDbRawdata(v1, dbDataContainer, feedVsDbMergeStrategy);
			
			RawdataFeedContainerUpdateStrategy rawdataFeedContainerUpdateStrategy = rawdataFeedContainerUpdateStrategyFactory.getStrategy(pojo.getDataSource());
			
			if (rawdataFeedContainerUpdateStrategy != null) {
				feedRawdataContainer = rawdataFeedContainerUpdateStrategy.updatedFeedDataContainer(v1, pojo, feedRawdataContainer);
			}
			
			if (rawDataFilter.filter(feedRawdataContainer, dbDataContainer, rawDataFilterContext)) {
				_logger.debug("code hash matched, not persisting this record: {}", v1);
				RecordTelemetry.addDeltaRejectEvent(v1);
				//Invoking onRawRecordFiltered Listeners
				invokeApplicableListeners(filePath, feedRawdataContainer, dbDataContainer);
				continue;
			}
			
			handlePersistence(pojo, v1, feedRawdataContainer, feedInput, dbDataContainer);
			
			v1.setFileType(pojo.getFileType());
			v1.getRecordRawData().setRawDataLevel(pojo.getRawDataLevelForParser());
			v1.setRawSourceUniqueCode(rawSourceUniqueValue);
			if (StringUtils.isEmpty(v1.getRecordRawData().getRawDataId())) {
				_logger.error("RawdataId not available for record {}, thus not sending the record for further processing.", v1);
			} 
			
			processedRecords.add(v1);
		}
		
		return processedRecords;
	}

	/**
	 * Invoking applicable listeners based on dataSource.
	 * @param filePath
	 * @param feedRawdataContainer
	 * @param dbDataContainer
	 */
	private void invokeApplicableListeners(String filePath, DataContainer feedRawdataContainer,
			DataContainer dbDataContainer) {
		DomainType dataSourceVal = feedRawdataContainer.getHighestPriorityValue(DataAttributeFactory
				.getAttributeByNameAndLevel(SdDataAttrConstant.COL_DATA_SOURCE, feedRawdataContainer.getLevel()));
		List<RawDataProcessListener> rawListeners = eventListenerFactory.getApplicableListeners(dataSourceVal.getVal(), RawDataEvent.ON_RAW_RECORD_FILTERD.name());
		RawDataListenerInput input = RawDataListenerInput.builder().filePath(filePath).dbDataContainer(dbDataContainer).build();
		rawListeners.forEach(listn -> listn.onRawRecordFiltered(input));
	}


	/**
	 *  Handles persistence of raw data object based on what type of
	 *  workflow it is.
	 * 
	 * @param pojo
	 * @param v1
	 * @param feedRawdataContainer
	 * @param feedInput
	 * @param dbDataContainer
	 */
	private void handlePersistence(RawRecordContextDetailsPojo pojo, Record v1, DataContainer feedRawdataContainer,
			RawDataInputPojo feedInput, DataContainer dbDataContainer) {
		 
		// This check is to prevent the persistence of rawData into sdRawData again in
		// case of re-processing sdRawData.
		if (!pojo.getFileType().contains(SdRawDataConstant.RAW_REPROCESSING_FILETYPE)) {
			mergeAndPersist(v1, feedRawdataContainer, feedInput, dbDataContainer);
		} else {
			// For re-processing workflow, the raw data id is made available in the raw data of the record object.
			// Here we set the same in the Record object and remove it from the raw data.
			// This is done to ensure no fields that are not part of the feed are allowed in the raw data
			// object.
			v1.getRecordRawData().setRawDataId(String.valueOf(v1.getRecordRawData().getRawData().get(SdRawDataConstant.RAW_DATA_ID)));
			v1.getRecordRawData().getRawData().remove(SdRawDataConstant.RAW_DATA_ID);
		}
	}

	/**
	 * This method will eventually call FeedVsDbRawdataMergeService.merge() method to merge feed and DB rawdata.
	 * Before calling merge method, this will first extract JSONObject from input DB DataContainer.
	 * 
	 * @param v1
	 * @param dbDataContainer
	 * @param feedVsDbMergeStrategy
	 * @return
	 */
	private Record mergeFeedVsDbRawdata(Record v1, DataContainer dbDataContainer,
			FeedVsDbRawdataMergeService feedVsDbMergeStrategy) {
		if(null!=feedVsDbMergeStrategy && null!=dbDataContainer) {
			DataValue<?> rawDataRecord = (DataValue<?>) dbDataContainer.getAttributeValue(DataAttributeFactory
					.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_RECORD_ATTRIBUTE, dbDataContainer.getLevel()));
			JSONObject dbRawDataJson = JsonConverterUtil.convertToSimpleJson(rawDataRecord.getValue().toString());
			v1 = feedVsDbMergeStrategy.merge(v1, dbRawDataJson);
		}
		return v1;
	}

	/**
	 * @param dbRawDataContainers
	 * @param v1
	 * @param feedRawdataContainer
	 * @param feedInput
	 * @param mergedContainer
	 * @param dbDataContainer
	 */
	private void mergeAndPersist(Record v1, DataContainer feedRawdataContainer, RawDataInputPojo feedInput,
			DataContainer dbDataContainer) {

		DataContainer mergedContainer = null;
		DataAttribute dateAttribute = null;
			if (dbDataContainer != null) {
				try {
					DataLevel attributeLevel=dbDataContainer.getLevel();
					List<DataContainer> merge = mergingService.merge(feedRawdataContainer,
							Arrays.asList(dbDataContainer));
					mergedContainer = merge.get(0);
					dateAttribute = DataAttributeFactory
							.getAttributeByNameAndLevel(Constant.ListenerConstants.updateDate, attributeLevel);

				} catch (DataContainerMergeException e) {
					_logger.error("RawData container merging failed due to {}", e.getMessage());
				}
			} else if (feedRawdataContainer != null) {
				mergedContainer = feedRawdataContainer;
				dateAttribute = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.insertDate,
						mergedContainer.getLevel());
			}
			if (mergedContainer != null) {
				try {
					DataValue<LocalDateTime> dateTime = new DataValue<>();
					dateTime.setValue(LockLevel.FEED, LocalDateTime.now());
					mergedContainer.addAttributeValue(dateAttribute, dateTime);
					persistDao.persist(mergedContainer);
					v1.getRecordRawData().setRawDataId(mergedContainer.get_id());
				} catch (Exception e) {
					_logger.error("Failed saving raw data container with unique code:{} due to error:{}",
							feedInput.getUniqueColumnValue(), e.getMessage());
				}
			}
	}

	/**
	 * @param batchOfRecords
	 * @param dataSource
	 * @param deltaFieldsByParser
	 * @param sourceUniqueIdAttributeName
	 * @param feedExecutionDetailId
	 * @param rawLevel
	 * @param fileType
	 * @param inputMap
	 * @param feedDataContainerMap
	 */
	private void populateUniqueIdVsDataMaps(List<RawRecordContextDetailsPojo> batchOfRecords, Map<String, RawDataInputPojo> inputMap, Map<String, DataContainer> feedDataContainerMap) {
		
		for (RawRecordContextDetailsPojo recordPojo : batchOfRecords) {
			
			if (recordPojo.getFileType().contains(SdRawDataConstant.RAW_REPROCESSING_FILETYPE)) {
				// this is to avoid querying the database for raw data as during re-processing, the incoming 
				// data is actually the exact raw data from the sdRawData collection.
				continue;
			}
			
			try {
				RawDataInputPojo input = createRawDataInput(recordPojo.getRecord(), recordPojo.getDataSource(),recordPojo.getDeltaFieldsByParser(),recordPojo.getSourceUniqueIdAttributeValue(), recordPojo.getFeedExecutionDetailId(), recordPojo.getRawDataLevelForParser(), recordPojo.getFileType());
				
				if(!StringUtils.isEmpty(input.getUniqueColumnValue())){
					inputMap.put(input.getUniqueColumnValue(), input);
					feedDataContainerMap.put(input.getUniqueColumnValue(), rawDataGenerator.generateRawData(input,recordPojo.getDataSource()));
				}
			}catch(Exception e) {
				exceptionLoggingHandler.logException(e, this.getClass().getName(), recordPojo.getRecord());
			}
		}
	}

	/**
	 * @param keySet
	 * @param dbRawDataContainers
	 * @return
	 */
	private Map<String, DataContainer> populateRawSourceUniqueIdVsDataContainerMap(Set<String> keySet, List<DataContainer> dbRawDataContainers) {
		
		if (CollectionUtils.isEmpty(keySet)) {
			return new HashMap<>();
		}
		
		Map<String, DataContainer> rawSourceUniqueIdVsDataContainerMap = new HashMap<>();
		
		for (String rawSourceUniqueId : keySet) {
			dbRawDataContainers.forEach(dataContainer -> {
				String rawSourceUniqueIdFromDbContainer = (String) dataContainer
						.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getAttributeByNameAndLevel(
								Constant.SdAttributeNames.RAW_SOURCE_UNIQUE_CODE_ATTRIBUTE, dataContainer.getLevel()));

				if (rawSourceUniqueId.equals(rawSourceUniqueIdFromDbContainer)) {
					rawSourceUniqueIdVsDataContainerMap.put(rawSourceUniqueId, dataContainer);
				}
			});

			if (!rawSourceUniqueIdVsDataContainerMap.containsKey(rawSourceUniqueId)) {
				rawSourceUniqueIdVsDataContainerMap.put(rawSourceUniqueId, null);
			}
		}
		
		return rawSourceUniqueIdVsDataContainerMap;
	}

}
