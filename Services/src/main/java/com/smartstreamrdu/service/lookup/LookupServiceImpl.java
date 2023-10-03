/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LookupServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.types.ObjectId;
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
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.persistence.service.util.AttributeToCriteriaConverterUtility;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class LookupServiceImpl implements LookupService {

	private static final long serialVersionUID = -8556065364035665542L;

	@Autowired
	private transient DataRetrievalService service;

	private static final Logger _logger = LoggerFactory.getLogger(LookupServiceImpl.class);


	@Autowired
	private transient CacheDataRetrieval cacheDataRetrieval;

	@Autowired
	private transient LookupRelationship lookupRelationshipService;
	
	@Autowired
	private transient AttributeToCriteriaConverterUtility conversionUtility;
	
	
	public void initialize() {
		if (cacheDataRetrieval == null) {
			cacheDataRetrieval = SpringUtil.getBean(CacheDataRetrieval.class);
		}
		if (service == null) {
			service = SpringUtil.getBean(DataRetrievalService.class);
		}
		if(lookupRelationshipService == null){
			lookupRelationshipService = SpringUtil.getBean(LookupRelationship.class);
		}
		if(conversionUtility == null){
			conversionUtility = SpringUtil.getBean(AttributeToCriteriaConverterUtility.class);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.lookup.LookupService#resolveLookup(com.
	 * smartstreamrdu.domain.DataContainer,
	 * com.smartstreamrdu.service.lookup.input.LookupAttributeInput)
	 */
	@Override
	public void resolveLookup(DataContainer feedContainer, LookupAttributeInput input) throws UdmBaseException {
		if (feedContainer.getLevel() == DataLevel.LE) {
			_logger.debug("Not performing lookup as data level is {}", feedContainer.getLevel());
			return;
		}
		initialize();
		
		lookupRelationshipService.resolveLookup(feedContainer, input);	
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.lookup.LookupService#getDbDataContainer(com.
	 * smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public List<DataContainer> getDbDataContainersBySourceUniqueId(DataContainer feedContainer, DomainType feedDs)
			throws UdmTechnicalException {
		if (feedContainer == null || feedDs == null) {
			return Collections.emptyList();
		}
		initialize();
		DataAttribute sourceUniqueAttr = DataAttributeFactory.getSourceUniqueIdentifierForLevel(feedContainer.getLevel());
		Serializable attributeValueAtLevel = feedContainer.getAttributeValueAtLevel(LockLevel.FEED, sourceUniqueAttr);
		Serializable[] values = new Serializable[1];
		values[0]=attributeValueAtLevel; 
		
		Criteria c = createCriteriaBasedOnSourceUniqueIdAndStatus(feedDs, values, feedContainer.getLevel());
		DataRetrivalInput input=new DataRetrivalInput();
		input.setCriteria(c);
		List<DataContainer> retrieve = service.retrieve(Database.Mongodb,input);
		if (CollectionUtils.isEmpty(retrieve)) {
			return Collections.emptyList();
		}
		DataContainerUtil.populateNewFlagAndHasChanged(retrieve);
		return retrieve;

	}


	@Override
	public List<DataContainer> getDbDataContainersByObjectId(String objectId, DataLevel dataLevel) {

		DataAttribute documentIdAttribute = DataAttributeFactory.getIdDataAttributeForDataLevel(dataLevel.getRootLevel(), false,null);
		DataValue<ObjectId> idValue = new DataValue<>();
		idValue.setValue(LockLevel.FEED, new ObjectId(objectId));
		Criteria criteria = Criteria.where(documentIdAttribute).is(idValue);
		DataRetrivalInput input = new DataRetrivalInput();
		input.setCriteria(criteria);
		List<DataContainer> retrieve = null;
		try {
			retrieve = service.retrieve(Database.Mongodb, input);
		} catch (UdmTechnicalException e) {
			_logger.error("Exception occured in retreiving datacontainer for {}",objectId,e);
		}
		if (CollectionUtils.isEmpty(retrieve)) {
			return Collections.emptyList();
		}
		DataContainerUtil.populateNewFlagAndHasChanged(retrieve);
		return retrieve;
	}
	
	@Override
	public void populateDataSource(DataContainer container, String datasource) {
		if (datasource == null) {
			_logger.warn("Not populating data source as data source in context is null. Data container is {}",
					container);
			return;
		}
		DomainType domain = new DomainType();
		domain.setVal(datasource);
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, domain);
		container.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(Constant.SdAttributeNames.DATASOURCE_ATTRIBUTE, (container.getLevel()).getRootLevel()),
				dv);

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.lookup.LookupService#
	 * getDbInstrumentContainerForSecurity(java.util.List)
	 */
	public List<DataContainer> getDbInstrumentContainerForSecurity(DomainType datasource, List<DataContainer> securityContainers)
			throws UdmBaseException {
		if(CollectionUtils.isEmpty(securityContainers) || datasource == null){
			return Collections.emptyList();
		}
		initialize();
		
		Serializable[] values = DataContainerUtil.getSourceUniqueIdColumnValue(LockLevel.FEED, securityContainers);
		if(ArrayUtils.isEmpty(values)){
			return Collections.emptyList();
		}

		Criteria c = createCriteriaBasedOnSourceUniqueIdAndStatus(datasource, values, DataLevel.SEC);

		DataRetrivalInput input=new DataRetrivalInput();
		input.setCriteria(c);
		List<DataContainer> retrieve = service.retrieve(Database.Mongodb,input);
		if (CollectionUtils.isEmpty(retrieve)) {
			return Collections.emptyList();
		}
		DataContainerUtil.populateNewFlagAndHasChanged(retrieve);
		return retrieve;
	}
	
	/**
	 * Creates criteria for fetching Instruments based on SourceUniqueIdValue & active status
	 * 
	 * @param datasource
	 * @param values
	 * @param sec 
	 * @return
	 * @throws UdmTechnicalException
	 */
	private Criteria createCriteriaBasedOnSourceUniqueIdAndStatus(DomainType datasource, Serializable[] values,
			DataLevel level) {

		List<Criteria> criteriaList = new ArrayList<>();

		// This method returns dataSource Criteria
		Criteria dataSourceCriteria = getDataSourceCriteria(datasource);
		criteriaList.add(dataSourceCriteria);

		// This method returns sourceUniqueId criteria.
		Criteria sourceUniqueIdentifier = getSourceUniqueCriteriaForLevel(values, level);
		criteriaList.add(sourceUniqueIdentifier);

		// This  method returns active status criteria.
		Criteria statusCriteria = getStatusCriteria(datasource, level);
		criteriaList.add(statusCriteria);

		return new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
	}

	/**
	 * This methods returns equals criteria where value of dataSource equals to input value.
	 * @param datasource
	 * @return
	 */
	private Criteria getDataSourceCriteria(DomainType datasource) {
		DataValue<DomainType> ds = new DataValue<>();
		ds.setValue(LockLevel.FEED,  datasource);
		DataStorageEnum dataStorageEnum = cacheDataRetrieval.getDataStorageFromDataSource(datasource.getVal());
		return Criteria.where(dataStorageEnum.getAttributeByName(Constant.SdAttributeNames.DATASOURCE_ATTRIBUTE))
				.is(ds);
	}

	/**
	 * This methods returns equals criteria where value of SourceUniqueId equals to input value.
	 * @param values
	 * @param level
	 * @return
	 */
	private Criteria getSourceUniqueCriteriaForLevel(Serializable[] values, DataLevel level) {
		List<DataValue<? extends Serializable>> uniqueIdsDV = new ArrayList<>(values.length);
		for(Serializable value : values){
			DataValue<Serializable> dv = new DataValue<>();
			uniqueIdsDV.add(dv);
			dv.setValue(LockLevel.FEED, value);
		}
		return Criteria.where(DataAttributeFactory.getSourceUniqueIdentifierForLevel(level)).in(uniqueIdsDV);
	}

	/**
	 * This methods returns active status criteria
	 * @param datasource 
	 * @param level 
	 * @return 
	 * 
	 */
	private Criteria getStatusCriteria(DomainType datasource, DataLevel level) {
		DataAttribute statusFlag = DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(level), level);
		String dataSourceDomainSourceFromCode = cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource.getVal());
		return conversionUtility.createStatusAttributeCriteria(statusFlag, DomainStatus.ACTIVE,
				dataSourceDomainSourceFromCode);
	}

	/**
	 * @param datasource
	 * @param secStatusFlag
	 * @return
	 */
	private List<DomainType> getVendorValuesForNormalizedValue(DomainType datasource, DataAttribute secStatusFlag,String normalizedValue) {
		String dsDomainSource = cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource.getVal());
		return  cacheDataRetrieval.getVendorDomainValuesFromCache(
				DataAttributeFactory.getRduDomainForDomainDataAttribute(secStatusFlag), dsDomainSource,
				normalizedValue);
	}

	@Override
	public List<DataContainer> getActiveDbDataContainersBySourceUniqueId(DataContainer feedContainer, DomainType feedDs)
			throws UdmBaseException {
		if (feedContainer == null || feedDs == null) {
			return Collections.emptyList();
		}
		
		List<DataContainer> dbDataContainers = getDbDataContainersBySourceUniqueId(feedContainer, feedDs);
		return dbDataContainers.stream().filter(isActive(feedContainer, feedDs)).collect(Collectors.toList());
	}

	/**"
	 * This method checks the activeness of container based on highest priority
	 * value, which is not possible to dynamically look via db query
	 * @param feedContainer 
	 * 
	 * @param statusFlagAttribute
	 * @param datasource
	 * @return Returns a predicate on whether the status is active on supplied
	 *         DataContainer
	 */
	private Predicate<? super DataContainer> isActive(DataContainer inputContainer,DomainType datasource) {
		return dc -> {	
			if(inputContainer.getLevel().getParentLevel() == null) {
				DataAttribute identifierForLevel = DataAttributeFactory.getSourceUniqueIdentifierForLevel(inputContainer.getLevel());
				String sourceUniqueId = inputContainer.getHighestPriorityValue(identifierForLevel);
				return isActiveSourceUniqueIdPresentInDB(Arrays.asList(dc),sourceUniqueId,datasource);
			}		
			else {
				List<DataContainer> containerList = new ArrayList<>();
				containerList.add(inputContainer);
				return getMatchingChildContainer(dc.getAllChildDataContainers(), containerList,datasource) != null;
			}
		};
	}

	
	@Override
	public List<DataContainer> getActiveDbInstrumentContainerForSecurity(DomainType datasource,
			List<DataContainer> securityContainers) throws UdmBaseException {
		List<DataContainer> dbDataContainers = getDbInstrumentContainerForSecurity(datasource, securityContainers);
		return dbDataContainers.stream().filter(isSecurityActive(securityContainers,datasource)).collect(Collectors.toList());
	}

	/**
	 * This method checks, whether the list of securityContainers are "actively"
	 * present in the fetched containers. i.e. check if there are no highest
	 * priority status which is inactive.
	 * 
	 * @param securityContainers
	 * @param datasource 
	 * @return
	 */
	private Predicate<? super DataContainer> isSecurityActive(List<DataContainer> securityContainers, DomainType datasource) {
		return dc -> {
			List<DataContainer> dbSecDataContainers= dc.getChildDataContainers(DataLevel.SEC);
			DataContainer matchingSec = getMatchingChildContainer(dbSecDataContainers, securityContainers,datasource);
			return matchingSec != null;
		};
	}


	@SuppressWarnings("unchecked")
	private DataContainer getMatchingChildContainer(List<DataContainer> dbSecDataContainers, List<DataContainer> securityContainers, DomainType datasource) {
		for (DataContainer sec : securityContainers) {
			DataAttribute identifierForLevel = DataAttributeFactory.getSourceUniqueIdentifierForLevel(sec.getLevel());
			DataValue<String> sourceUniqueId = (DataValue<String>) sec.getAttributeValue(identifierForLevel);
			if (isActiveSourceUniqueIdPresentInDB(dbSecDataContainers, sourceUniqueId.getValue(),datasource)) {
				return sec;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private boolean isActiveSourceUniqueIdPresentInDB(List<DataContainer> dbSecDataContainers, String sourceUniqueId, DomainType datasource) {
		for (DataContainer dbSec : dbSecDataContainers) {
			DataAttribute identifierForLevel = DataAttributeFactory.getSourceUniqueIdentifierForLevel(dbSec.getLevel());
			DataValue<String> dbSourceUniqueId = (DataValue<String>) dbSec.getAttributeValue(identifierForLevel);

			DataStorageEnum dataStorage = cacheDataRetrieval.getDataStorageFromDataSource(datasource.getVal());
			DataAttribute statusFlagForLevel = dataStorage.getAttributeByName(DataAttributeFactory.getStatusFlagForLevel(dbSec.getLevel()));

			DataValue<DomainType> attributeValue = (DataValue<DomainType>) dbSec.getAttributeValue(statusFlagForLevel);
			//dbSourceUniqId will be null in case of Technical Securities
			if (dbSourceUniqueId!=null && sourceUniqueId.equals(dbSourceUniqueId.getValue())) {
				DomainType value = attributeValue.getValue();
				DataAttribute statusFlagAttribute = DataAttributeFactory.getAttributeByNameAndLevel(
						DataAttributeFactory.getStatusFlagForLevel(dbSec.getLevel()), dbSec.getLevel());
				List<DomainType> listVendorValues = getVendorValuesForNormalizedValue(datasource, statusFlagAttribute,DomainStatus.ACTIVE);

				// If normalized is null, it means, no RDU override. The FEED level check would have already been done before it reached here.
				if ( listVendorValues.contains(value) || (value.getNormalizedValue() != null
						&& DomainStatus.ACTIVE.equals(value.getNormalizedValue()))) {
					return true;
				}
			}
		}
		return false;
	}

}
