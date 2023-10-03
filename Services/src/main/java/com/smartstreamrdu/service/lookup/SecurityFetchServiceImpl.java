/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	SecurityFetchServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	17-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.domain.DataContainerCloneService;
import com.smartstreamrdu.service.merging.DataContainerMergingService;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jay Sangoi
 *
 */
@Component
@Slf4j
public class SecurityFetchServiceImpl implements SecurityFetchService {
	

	@Autowired
	@Setter
	private LookupService lookupService;

	@Autowired
	@Setter
	private DataContainerMergingService mergingService;
	
	@Autowired
	@Setter
	private DataContainerCloneService cloneService;
	
	@Autowired
	@Setter
	private CacheDataRetrieval dataRetrieval;
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.persist.MergeAndPersistService#
	 * fetchExistingSecToBeInactivated()
	 */
	@Override
	public Map<DataContainer, DataContainer> fetchExistingSecToBeInactivated(DataContainer feedContainer,
			List<DataContainer> dbDataContainers) throws UdmBaseException {
		log.debug("fetchExistingSecToBeInactivated for feed container :{}", feedContainer);
		// Find the db container for feed container
		if (feedContainer == null) {
			return Collections.emptyMap();
		}
		DataContainer feedDbContainer = null;
		if (CollectionUtils.isNotEmpty(dbDataContainers)) {
			feedDbContainer = mergingService.getFeedEquivalentParentDbContainer(feedContainer, dbDataContainers);
		}
		List<DataContainer> evaluatedSecs = getSecurityPresentInFeedAndNotInDb(
				feedContainer.getChildDataContainers(DataLevel.SEC), feedDbContainer);
		Serializable ds = feedContainer.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getDatasourceAttribute(DataLevel.SEC));
		DomainType dtDs = null;
		if (ds instanceof DomainType) {
			dtDs = (DomainType) ds;
		}
		// For security to be evaluated, fetch the document from db where the
		// security is present.
		List<DataContainer> dbInstrumentContainerForSecurity = lookupService.getActiveDbInstrumentContainerForSecurity(dtDs,
				evaluatedSecs);
		if (CollectionUtils.isEmpty(dbInstrumentContainerForSecurity)) {
			return Collections.emptyMap();
		}
		// For each evaluated security, if we find db document, then create
		// dummy feed container with flagActive as Inactive
		// Create Map for evaluated security, whhich has db document
		return createMapForSecToBeInactivated(evaluatedSecs, dbInstrumentContainerForSecurity,dtDs);
	}

	/**
	 * Iterate over all the db containers security, and find the security for
	 * which evaluated security is present. Once found, create minimum attribute
	 * security and instrument data container with flag active as N
	 * 
	 * @param evaluatedSecs
	 * @param dbInstrumentContainerForSecurity
	 * @param dtDs 
	 * @return
	 * @throws UdmBaseException
	 */
	private Map<DataContainer, DataContainer> createMapForSecToBeInactivated(List<DataContainer> evaluatedSecs,
			List<DataContainer> dbInstrumentContainerForSecurity, DomainType dtDs) throws UdmBaseException {

		Map<DataContainer, DataContainer> map = new HashMap<>(dbInstrumentContainerForSecurity.size());

		for (DataContainer dbContainer : dbInstrumentContainerForSecurity) {

			DataContainer insContainer = createInstrumentContainerWithInactiveSecurity(evaluatedSecs, dbContainer,dtDs);

			if (insContainer != null) {
				map.put(insContainer, dbContainer);
			}
		}

		return map;
	}

	/**
	 * @param evaluatedSecs
	 * @param dbContainer
	 * @param dtDs 
	 * @return
	 * @throws UdmBaseException
	 */
	private DataContainer createInstrumentContainerWithInactiveSecurity(List<DataContainer> evaluatedSecs,
			DataContainer dbContainer, DomainType dtDs) throws UdmBaseException {

		List<DataContainer> inactiveSecContainers = new ArrayList<>();

		boolean allSecurityPresent = true;
		for (DataContainer secContainer : dbContainer.getChildDataContainers(DataLevel.SEC)) {
			DataContainer evaluatedSec = mergingService.getFeedEquivalentChildDbContainer(dbContainer, secContainer,
					evaluatedSecs);
			if (evaluatedSec == null) {
				allSecurityPresent = false;
			} else {
				inactiveSecContainers.add(cloneService.createMinimumAttributeInactiveSecurityDC(evaluatedSec,dtDs));
			}
		}

		DomainType dataSource = dbContainer.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute());
		String dataSourceInactivationLevel = dataRetrieval.getDataSourceInsInactivationLevel(dataSource.getVal());

		DataContainer insContainer = null;
        //This piece of code is getting called when same securitySourceUniqueId is comes up with different instrument.
		//In that case we inactivate existing security.Below piece of code decides logic to calculate instrument status 
		
		//If feed provides instrument inactivation(e.g. idcApex) then we keep instrument[existing] Active irrespective 
		//of whether it contains any other active security or not.
		if (dataSourceInactivationLevel != null && dataSourceInactivationLevel.equals(SdDataAttConstant.INSTRUMENT)) {
			insContainer = cloneService.createMinimumAttributeInstrumentDC(dbContainer);
		} else {
			//If feed doesn't provide instrument inactivation(e.g. trdse) then based on active security we decides instrument status.
			//If instrument is single listed & last security is inactivated then we inactivate instrument also.
			//If instrument is contains any other active security[multilisted] then we keep instrument active.
			//In case of active instrument we do not add status during cloning process 
			insContainer = allSecurityPresent ? cloneService.createMinimumAttributeInactiveInstrumentDC(dbContainer)
					: cloneService.createMinimumAttributeInstrumentDC(dbContainer);
		}

		for (DataContainer container : inactiveSecContainers) {
			insContainer.addDataContainer(container, DataLevel.SEC);
		}

		return insContainer;
	}

	/**
	 * If db container is present, then find the security (which are not present
	 * in db) to be evaluation. if db container is not present, then select all
	 * the security for evaluation
	 * 
	 * @param secContainers
	 * @param dbDataContainers
	 * @return
	 * @throws UdmTechnicalException
	 */
	private List<DataContainer> getSecurityPresentInFeedAndNotInDb(List<DataContainer> secContainers,
			DataContainer dbContainer) throws UdmTechnicalException {

		if (CollectionUtils.isEmpty(secContainers)) {
			return Collections.emptyList();
		}
		List<DataContainer> evaluatedSecContainers = new ArrayList<>(secContainers.size());
		if (dbContainer == null) {
			return secContainers;
		}
		for (DataContainer container : secContainers) {
			if (mergingService.getFeedEquivalentChildDbContainer(dbContainer, container,
					dbContainer.getChildDataContainers(DataLevel.SEC)) == null) {
				evaluatedSecContainers.add(container);
			}
		}

		return evaluatedSecContainers;
	}

}
