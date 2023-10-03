/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainMergeAndPersistServiceImpl.java
 * Author : SaJadhav
 * Date : 16-Aug-2019
 * 
 */
package com.smartstreamrdu.service.persist;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.cache.CacheException;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.IgniteHelperService;
import com.smartstreamrdu.persistence.cache.initializer.CacheOperation;
import com.smartstreamrdu.persistence.cache.initializer.DataContainerCacheStore;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.listener.DomainListenerService;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.merging.DataContainerMergingService;

/**
 * Class for merging of RDU domain with existing record in DB.
 * Performs the postprocessing after persisting merged container.
 * @author SaJadhav
 *
 */
@Component
public class DomainMergeAndPersistServiceImpl implements DomainMergeAndPersistService {

	private static final Logger _logger = LoggerFactory.getLogger(DomainMergeAndPersistServiceImpl.class);

	@Autowired
	private DataContainerMergingService mergingService;

	@Autowired
	private PersistenceService persistencService;

	@Autowired
	private DomainListenerService listenerService;

	@Autowired
	private IgniteHelperService igniteHelperService;

	@Autowired
	private DataContainerCacheStore dataContainerCacheStore;

	@Autowired
	private DataRetrievalService retrievalService;

	private static final int MAX_CACHE_UPDATE_RETRY_LIMIT = 3;

	private int cacheRetryCount = 1;

	@Override
	public void merge(DataContainer dataContainer, DataContainer dbDataContainer) throws DataContainerMergeException {

		List<DataContainer> dbDataContainers=new ArrayList<>();
		if (Objects.nonNull(dbDataContainer)) {
			dbDataContainers.add(dbDataContainer);
			dbDataContainers = mergingService.merge(dataContainer, dbDataContainers);
			sendMergeCompleteEvents(dbDataContainers);
		} else {
			dbDataContainers.add(dataContainer);
			listenerService.newDomainContainer(dataContainer);
		}

		processDataContainers( dbDataContainers );
	}


	/**
	 * This method processes DataContainer to persist in cache.
	 * @param dbDataContainers
	 * com.smartstreamrdu.service.persist
	 */
	private void processDataContainers ( List<DataContainer> dbDataContainers ) {
		dbDataContainers.forEach(dbc->{
			String docId = dbc.get_id();
			try {
				persistencService.persist(dbc);
				checkDataContainerLevelAndUpdateCache(dbc);
			}
			catch (CacheException e) {
				_logger.error("Failed to update cahche. Retry updateCache triggered. Retry count : {}" ,cacheRetryCount);
				retryCacheUpdate(dbc, cacheRetryCount++, docId);
			}
		});
 	}


	private void checkDataContainerLevelAndUpdateCache(DataContainer dbc) {
		DataLevel level = dbc.getLevel();
		if (level.isCacheable()) {
			updateCache(dbc);
			listenerService.domainUpdated(dbc, null);
			cacheRetryCount = 1;
		}
	}

	/**
	 * @param dbDataContainer
	 * com.smartstreamrdu.service.persist
	 */
	private void updateCache(DataContainer dbDataContainer) {
			CacheOperation operation = dbDataContainer.isNew() ? CacheOperation.INSERT : CacheOperation.UPDATE;
			dataContainerCacheStore.updateCacheEntry(igniteHelperService.getCacheStoreArguments(operation, dbDataContainer));
	}

	/**
	 * This method retry cache update operation when CacheException occurs.
	 * @param dbDataContainer
	 * @param cacheRetryCount
	 * com.smartstreamrdu.service.persist
	 */
	private void retryCacheUpdate ( DataContainer dbDataContainer, int retryCount, String objectId) {
		try {
			Thread.sleep(1000);
			DataAttribute documentIdAttribute = DataAttributeFactory.getIdDataAttributeForDataLevel(dbDataContainer.getLevel(), false,null);

			DataValue<ObjectId> idValue = new DataValue<>();
			idValue.setValue(LockLevel.RDU, new ObjectId(objectId));
			Criteria criteria = Criteria.where(documentIdAttribute).is(idValue);
			DataRetrivalInput input=new DataRetrivalInput();
			input.setCriteria(criteria);
			List<DataContainer> updatedDbDataContainer = retrievalService.retrieve(Database.Mongodb, input);

			if ( retryCount <= MAX_CACHE_UPDATE_RETRY_LIMIT ) {
				merge(dbDataContainer, updatedDbDataContainer.get(0));
				return;
			}
		}
		catch (InterruptedException ie) {
			_logger.error("Error occured in threed sleep", ie);
			Thread.currentThread().interrupt();
		}
		catch (UdmTechnicalException e) {
			_logger.error("Error occured while retryMergeAndPersistSd", e);
		}
		_logger.error("Max retry limit reached : {}", retryCount);

		// Initialized retry count to 1 when throwing exception
		cacheRetryCount=1;
		throw new CacheException();
	}


	/**
	 * @param dbDataContainers
	 */
	private void sendMergeCompleteEvents(List<DataContainer> dbDataContainers) {
		dbDataContainers.forEach(container -> listenerService.domainConainerMerged(container));
	}

}
