/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: SdDataContainerComparator.java
 * Author: Rushikesh Dedhia
 * Date: May 31, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import static com.smartstreamrdu.domain.DataAttributeFactory.getSourceUniqueIdentifierForLevel;
import static com.smartstreamrdu.domain.DataAttributeFactory.getObjectIdIdentifierForLevel;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.domain.DomainService;
import com.smartstreamrdu.util.Constant.DomainStatus;

public class SdDataContainerComparator implements Serializable {

	@Autowired
	private transient DomainService domainService;

	@Autowired
	private transient CacheDataRetrieval cacheDataRetrieval;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1029772234828101552L;

	public DataContainer compareContainersAndReturn(DataContainer parent, DataContainer feedDContainer,
			List<DataContainer> dbContainers) throws UdmTechnicalException {

		if (feedDContainer == null || CollectionUtils.isEmpty(dbContainers)) {
			throw new IllegalArgumentException(
					"Exception while merging SD data containers. Neither the feed container nor the database containers can be empty");
		}

		DataContainer dsContainer = parent != null ? parent : feedDContainer;

		Serializable ds = dsContainer.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute(dsContainer.getLevel()));
		DomainType datasource = (DomainType) ds;
		DataStorageEnum dataStorage = cacheDataRetrieval.getDataStorageFromDataSource(datasource.getVal());
		DataAttribute statusAtt = dataStorage.getAttributeByName(
				DataAttributeFactory.getStatusFlagForLevel(feedDContainer.getLevel()));
		List<DomainType> activeStatusValueForVendor = domainService.getActiveStatusValueForVendor(datasource, statusAtt);
		return dbContainers.stream().filter(dbContainer -> {
			DomainType dbStatus = dbStatus(statusAtt, dbContainer);
			return checkValue(feedDContainer, dbContainer, activeStatusValueForVendor, dbStatus);
		}).findFirst().orElseGet(() -> null);

	}


	/**
	 *
	 * @param statusAtt
	 * @param dbContainer
	 * @return The highest priority lock level value for the attribute
	 */
	protected DomainType dbStatus(DataAttribute statusAtt, DataContainer dbContainer) {
		return dbContainer.getHighestPriorityValue(statusAtt);
	}

	/**
	 * @param feedDContainer
	 * @param dbContainer
	 * @param activeStatusValueForVendor
	 * @param dbStatus
	 * @param statusAtt 
	 * @param datasource 
	 * @return
	 */
	protected boolean checkValue(DataContainer feedDContainer, DataContainer dbContainer,
			List<DomainType> activeStatusValueForVendor, DomainType dbStatus) {
		// FIXME: This check should be replaced with but LE doesn't have a status
		// (dbStatus != null && (dbStatus.getVal().equals(activeStatusValueForVendor.getVal()))
		
		return ((dbStatus == null || checkStatus(activeStatusValueForVendor, dbStatus))
				&& checkValue(feedDContainer, dbContainer));

	}
	
	private boolean checkStatus(List<DomainType> activeStatusValueForVendor, DomainType dbStatus) {
		
		if (dbStatus.getNormalizedValue() != null) { // Give first preference to normalized value for checks, as they might have been overridden by Ops
			return dbStatus.getNormalizedValue().equals(DomainStatus.ACTIVE);
		} else if(CollectionUtils.isNotEmpty(activeStatusValueForVendor)) {
			return activeStatusValueForVendor.stream().map(DomainType::getVal).collect(Collectors.toSet())
					.contains(dbStatus.getVal());
		}
		
		return false;
	}

	/**
	 * @param feed
	 * @param db
	 * @return
	 */
	protected boolean checkValue(DataContainer feed, DataContainer db) {
		
		Serializable feedObjectIdIdentifier = geObjectIdIdentifier(feed);
		Serializable dbObjectIdIdentifier = geObjectIdIdentifier(db);

		/**
		 * If ObjectIdIdentifier is contains in both container then we should compare
		 * container based on these identifier instead of SourceUniqueId. As
		 * ObjectIdIdentifier are system generated & doesn't depend on feed & also
		 * ObjectIdIdentifier are getting used in xrf,Comparing based on these more
		 * logical. 
		 * 
		 * This logic needs to be used more generically in
		 * MergeAndPersistence Service So any two container[coming from feed or UI] must
		 * be compare&Merge based on ObjectIdIdentifier.
		 */
		if(feedObjectIdIdentifier != null && dbObjectIdIdentifier != null) {
			return Objects.equals(feedObjectIdIdentifier, dbObjectIdIdentifier);
		}
		
		Serializable feedSourceUniqueId = getUniqueId(feed);
		Serializable dbSourceUniqueId = getUniqueId(db);

		// if the feed dataContainer and the dbDataContainer have the
		// same sourceUniqueId and are of the same level then this
		// information is
		// sufficient for proving equality of the containers.
		return ((feed.getLevel() == db.getLevel())
				&& (Objects.equals(feedSourceUniqueId, dbSourceUniqueId)));
	}

	/**
	 * This method returns value of ObjectIdIdentifier for Level.
	 */
	private Serializable geObjectIdIdentifier(DataContainer container) {
		DataLevel level = container.getLevel();
		DataAttribute identifierForLevel = getObjectIdIdentifierForLevel(level);
		return container.getHighestPriorityValue(identifierForLevel);
	}
	/**
	 * @param dc
	 * @return
	 */
	protected Serializable getUniqueId(DataContainer dc) {
		DataLevel level = dc.getLevel();
		DataAttribute identifierForLevel = getSourceUniqueIdentifierForLevel(level);
		return dc.getHighestPriorityValue(identifierForLevel);
	}

}
