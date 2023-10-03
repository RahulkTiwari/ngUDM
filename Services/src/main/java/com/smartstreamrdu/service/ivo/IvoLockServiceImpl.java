/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoLockServiceImpl.java
* Author : VRamani
* Date : Feb 12, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.events.XrfEligibilityEvaluator;
import com.smartstreamrdu.util.LambdaExceptionUtil;

/**
 * @author VRamani
 *
 */
@Component
public class IvoLockServiceImpl implements IvoLockService {

	private static final Logger _logger = LoggerFactory.getLogger(IvoLockServiceImpl.class);

	@Autowired
	private IvoMergeAndPersistService ivoMergeService;
	@Autowired
	private List<IvoSegregator> ivoSegragator;

	@Autowired
	private IvoAggregationHandlerFactory aggregationHandlerFactory;

	@Autowired
	private SdIvoToSdContainerChildComparator childComparator;

	@Autowired
	private IvoQueryService queryService;

	@Autowired
	private IvoLockRemovalService lockRemovalService;
	
	@Autowired
	private XrfEligibilityEvaluator xrfEligibilityEvaluator;
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.ivo.IvoLockService#applyLocks(java.util.List)
	 */
	@Override
	public void persist(List<DataContainer> editedDataContainers) throws UdmBaseException {
		editedDataContainers.forEach(LambdaExceptionUtil.rethrowConsumer(this::segragateDataContainersAndPersist));
	}

	/**
	 * It segregates the DataContainer into centralized(sdIVO) and inline containers
	 * based on LockLevel,DataLevel and xrf/non-xrf attribute
	 * 
	 * @param inDataContainer
	 * @throws UdmBaseException 
	 */
	private void segragateDataContainersAndPersist(final DataContainer inDataContainer) throws UdmBaseException {
		final IvoContainer ivoContainer = getIvoContainer(inDataContainer);
		// merge and persist
		ivoMergeService.mergeAndPersist(ivoContainer);
	}

	/**
	 * It segregates the DataContainer into centralized(sdIVO) and inline containers
	 * based on LockLevel,DataLevel and xrf/non-xrf attribute. Populates
	 * IvoContainer Object
	 * 
	 * @param inDataContainer
	 * @return
	 * @throws UdmBaseException 
	 */
	private IvoContainer getIvoContainer(final DataContainer inDataContainer) throws UdmBaseException {
		DomainType dataSource = (DomainType) inDataContainer.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getDatasourceAttribute(inDataContainer.getLevel()));

		boolean isDataContainerEligibleForXrf = xrfEligibilityEvaluator.isDataContainerEligibleForXrf(inDataContainer);

		final IvoContainer ivoContainer = new IvoContainer(inDataContainer.get_id(), dataSource.getVal(),
				inDataContainer.getDataContainerContext());
		
		//For non-xrf dataSources segregation of locks is not required, since locks will be inline
		if(!isDataContainerEligibleForXrf || DataLevel.LE.equals(inDataContainer.getLevel())) {
			ivoContainer.setDataContainer(inDataContainer);
			return ivoContainer;
		}
		
		final Map<DataLevel, String> dataLevelVsObjectIdMap = new EnumMap<>(DataLevel.class);
		final Map<DataLevel, String> dataLevelVsSourceUniqueIdMap = new EnumMap<>(DataLevel.class);

		segregate(ivoContainer, inDataContainer, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap);

		List<DataContainer> childContainers = inDataContainer.getChildDataContainers(DataLevel.SEC);

		if (CollectionUtils.isNotEmpty(childContainers)) {
			childContainers.forEach(LambdaExceptionUtil.rethrowConsumer(childContainer -> segregate(ivoContainer,
					childContainer, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap)));
		}
		return ivoContainer;
	}

	/**
	 * @param ivoContainer
	 * @param inDataContainer
	 * @param dataLevelVsObjectIdMap
	 * @param dataLevelVsSourceUniqueIdMap
	 * @throws UdmTechnicalException
	 */

	private void segregate(final IvoContainer ivoContainer, DataContainer inDataContainer,
			Map<DataLevel, String> dataLevelVsObjectIdMap, Map<DataLevel, String> dataLevelVsSourceUniqueIdMap)
			throws UdmTechnicalException {
		Map<DataAttribute, DataValue<Serializable>> rowData = inDataContainer.getRecordData();

		final String objectId = inDataContainer.getHighestPriorityValue(
				DataAttributeFactory.getObjectIdIdentifierForLevel(inDataContainer.getLevel()));

		dataLevelVsObjectIdMap.put(inDataContainer.getLevel(), objectId);

		final String sourceUniqeId = inDataContainer.getHighestPriorityValue(
				DataAttributeFactory.getSourceUniqueIdentifierForLevel(inDataContainer.getLevel()));

		dataLevelVsSourceUniqueIdMap.put(inDataContainer.getLevel(), sourceUniqeId);

		for (Map.Entry<DataAttribute, DataValue<Serializable>> e : rowData.entrySet()) {
			DataValue<Serializable> val = e.getValue();
			DataAttribute dataAttribute = e.getKey();
			ivoSegragator
					.forEach(LambdaExceptionUtil.rethrowConsumer(segragator -> segragator.segregateIvos(ivoContainer,
							dataAttribute, val, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.ivo.IvoLockService#retrieveWithLocks(com.
	 * smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer,
	 * com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer mergeContainers(DataContainer sdDataContainer, DataContainer sdIvoDataContainer,
			DataContainer xrDataContainer) throws UdmTechnicalException {
		Objects.requireNonNull(sdDataContainer, "sdDataContainer should be populated");
		Objects.requireNonNull(sdIvoDataContainer, "sdIvoDataContainers should be populated");
		Objects.requireNonNull(xrDataContainer, "xrDataContainer should be populated");

		// Merges attributes like ISIN
		mergeTopLevelAttributes(sdDataContainer, sdIvoDataContainer);

		// Merges attributes at child level such as attributes at Security
		// levels
		// Since this is one-many relationship xrDataContainer is required
		mergeChildAttributes(sdDataContainer, sdIvoDataContainer, xrDataContainer);

		return sdDataContainer;
	}

	/**
	 * Merges attributes at child level such as attributes at Security levels. Since
	 * this is one-many relationship xrDataContainer is required
	 * 
	 * @param sdDataContainer
	 * @param sdIvoDataContainer
	 * @param xrDataContainer
	 */
	private void mergeChildAttributes(DataContainer sdDataContainer, DataContainer sdIvoDataContainer,
			DataContainer xrDataContainer) {
		List<DataContainer> sdIvoChildContainers = sdIvoDataContainer.getChildDataContainers(DataLevel.IVO_SEC);

		if (CollectionUtils.isEmpty(sdIvoChildContainers)) {
			// No need to merge since child IVO container is not present
			return;
		}
		String sdDocumentId = sdDataContainer.get_id();

		for (DataContainer sdIvoChildContainer : sdIvoChildContainers) {
			Optional<DataContainer> matchedSdChildContainer = childComparator.findMatchingContainerByIvo(
					sdIvoChildContainer, sdDataContainer.getChildDataContainers(DataLevel.SEC), xrDataContainer,
					sdDocumentId);
			if (matchedSdChildContainer.isPresent()) {
				mergeTopLevelAttributes(matchedSdChildContainer.get(), sdIvoChildContainer);
			}
		}
	}

	/**
	 * Merges top level attributes such as ISIN for a DataContainer This is also
	 * called to merge top level attributes within child containers
	 * 
	 * @param sdDataContainer
	 * @param sdIvoDataContainer2
	 */
	private void mergeTopLevelAttributes(DataContainer sdDataContainer, DataContainer sdIvoDataContainer) {
		Map<DataAttribute, DataValue<Serializable>> ivoRecordData = sdIvoDataContainer.getRecordData();
		Set<DataAttribute> ivoRecordKeySet = ivoRecordData.keySet();
		for (DataAttribute dataAttribute : ivoRecordKeySet) {
			IvoAggregationHandler ivoAggregationHandler = aggregationHandlerFactory
					.getIvoAggregationHandler(dataAttribute);
			ivoAggregationHandler.handleAttributeMerge(sdDataContainer, sdIvoDataContainer, dataAttribute);
		}
	}

	
	@Override
	public void removeIvoLocks(List<DataContainer> deletedDataContainerList) throws UdmBaseException {
		if (CollectionUtils.isEmpty(deletedDataContainerList)) {
			return;
		}
		for (DataContainer deletedContainer : deletedDataContainerList) {

			IvoContainer ivoContainer = getIvoContainer(deletedContainer);

			removeLockValuesForSdContainer(deletedContainer, ivoContainer);

			removeLockValuesForSdIvoContainer(ivoContainer);

			removeLockValuesForLeContainer(ivoContainer);
			removeLockValuesFromEnContainer(ivoContainer);
		}
	}

	/**
	 * @param ivoContainer
	 * @throws UdmTechnicalException 
	 */
	private void removeLockValuesFromEnContainer(IvoContainer ivoContainer) throws UdmTechnicalException {
		Optional<DataContainer> enDataContainerOptional = ivoContainer.getEnDataContainer();
		if(enDataContainerOptional.isPresent()) {
			DataContainer enDataContainer = enDataContainerOptional.get();
			List<DataContainer> enDataContainers = queryService.getMatchingDataContainerByDocumentId(enDataContainer);
			removeLockValues(enDataContainer, enDataContainers,ivoContainer.getDataSourceId());
		}
	}

	/**
	 * The method will remove lockValues from Legal Entity Container.
	 * @param deletedContainer
	 * @param ivoContainer
	 * @throws UdmTechnicalException
	 */
	private void removeLockValuesForLeContainer(IvoContainer ivoContainer)
			throws UdmTechnicalException {
		Optional<DataContainer> sdLeContainerOptional = ivoContainer.getSdLeContainer();
		if (sdLeContainerOptional.isPresent()) {
			DataContainer sdLeContainer = sdLeContainerOptional.get();
			List<DataContainer> legalEntityDataContainers = queryService
					.getMatchingLegalEntityDataContainerFromSdData(sdLeContainer);
			removeLockValues(sdLeContainer, legalEntityDataContainers,ivoContainer.getDataSourceId());
		}
	}

	/**
	 * This method will remove Lock values For SdIvo Container.
	 * @param deletedContainer
	 * @param ivoContainer
	 * @throws UdmTechnicalException
	 */
	private void removeLockValuesForSdIvoContainer(IvoContainer ivoContainer)
			throws UdmTechnicalException {
		Optional<DataContainer> sdIvoContainerOptional = ivoContainer.getSdIvoContainer();
		if (sdIvoContainerOptional.isPresent()) {
			DataContainer sdIvoContainer = sdIvoContainerOptional.get();
			List<DataContainer> sdIvoDataContainer = queryService.getMatchingDataContainerFromSdIvo(sdIvoContainer);
			removeLockValues(sdIvoContainer, sdIvoDataContainer,ivoContainer.getDataSourceId());
		}
	}

/**
 * This method will remove Lock values from SdData Container.
 * @param deletedContainer
 * @param ivoContainer
 * @throws UdmTechnicalException
 */
	private void removeLockValuesForSdContainer(DataContainer deletedContainer, IvoContainer ivoContainer)
			throws UdmTechnicalException {
		Optional<DataContainer> sdDataContainerOptional = ivoContainer.getSdContainer();
		if (sdDataContainerOptional.isPresent()) {
			DataContainer sdDataContainer = sdDataContainerOptional.get();
			List<DataContainer> sdDataContainers = queryService.getMatchingDataContainerByDocumentId(sdDataContainer);
			 
			DataAttribute dataSourceAttr = DataAttributeFactory.getDatasourceAttribute(sdDataContainer.getLevel());
			DataValue<DomainType> datasourceVal = new DataValue<>();
			datasourceVal.setValue(LockLevel.FEED, new DomainType(ivoContainer.getDataSourceId()));
			deletedContainer.addAttributeValue(dataSourceAttr, datasourceVal);

			removeLockValues(sdDataContainer, sdDataContainers,ivoContainer.getDataSourceId());
		}
	}

	/**
	 * This method will take list of  deletedContainer and dbContaines and pass values to Lock Removal Service.
	 * @param deletedContainer
	 * @param dbContaines
	 * @param dataSource 
	 */
	private void removeLockValues(DataContainer deletedContainer, List<DataContainer> dbContaines, String dataSource) {

		if (dbContaines.size() > 1) {
			throw new IllegalArgumentException("multiple dataContainers are found from databse ");
		}
		try {
			lockRemovalService.removeLockFromDataContainer(deletedContainer, dbContaines.get(0),dataSource);
		} catch (UdmTechnicalException e) {
			_logger.error("Exception occured while removing lock from container:{}", e);
		}
	}
}
