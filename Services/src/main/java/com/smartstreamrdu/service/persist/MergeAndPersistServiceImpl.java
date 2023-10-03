/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergeAndPersistServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	13-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.PersistenceType;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.audit.AuditService;
import com.smartstreamrdu.service.event.inactive.filter.InsIdNotFoundContainerListener;
import com.smartstreamrdu.service.event.inactive.filter.InsIdNotFoundFilteredInactiveContainerEvent;
import com.smartstreamrdu.service.event.inactive.filter.InsIdNotFoundInactiveContainerListenerInput;
import com.smartstreamrdu.service.event.process.listener.EventListenerFactory;
import com.smartstreamrdu.service.filter.DataFilterChainService;
import com.smartstreamrdu.service.filter.FilterInput;
import com.smartstreamrdu.service.filter.FilterOutput;
import com.smartstreamrdu.service.id.generator.ObjectIdGenerator;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.lookup.LookupService;
import com.smartstreamrdu.service.lookup.SecurityFetchService;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.merging.DataContainerMergingService;
import com.smartstreamrdu.service.merging.MergingStrategyFactory;
import com.smartstreamrdu.service.postprocess.DataContainerPostProcessorService;
import com.smartstreamrdu.service.rules.LoaderRuleExecutor;
import com.smartstreamrdu.service.udl.listener.UdlMergeEvent;
import com.smartstreamrdu.service.udl.listener.UdlMergeListener;
import com.smartstreamrdu.service.udl.listener.UdlProcessListenerInput;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class MergeAndPersistServiceImpl implements MergeAndPersistService {

	
	private static final Logger _logger = LoggerFactory.getLogger(MergeAndPersistServiceImpl.class);

	@Autowired
	private ListenerService listenerService;

	@Autowired
	List<DataContainerPostProcessorService> dataContainerPostProcessors;

	@Autowired
	private PersistenceService persistencService;

	@Autowired
	private ObjectIdGenerator<Serializable> generator;

	@Autowired
	private LookupService lookupService;

	@Autowired
	private DataFilterChainService filterSerivice;

	@Autowired
	private SecurityFetchService securityFetchService;

	@Autowired
	private AuditService auditService;
	
	@Autowired  
	private MergingStrategyFactory mergingStrategyFactory;
	
	@Autowired
	private LoaderRuleExecutor loaderRuleExecutor;
	
	@Autowired
	private List<DataContainerRejectionService> rejectionServices;
	
	@Autowired
	private EventListenerFactory listenerFactory;

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.persist.MergeAndPersistService#mergeAndPersistSd(
	 * com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	
	@Override
	public void mergeAndPersistSd(String dataSource, DataContainer container, List<DataContainer> dbDataContainers,
			FeedConfiguration feedConfiguration) throws DataContainerMergeException {
		boolean isNew = false;
		
		/*
		 * After handling the OptimisticLockingFailureException, failed container will have the latest time and it will posted to XRF,
		 * other container which are from the same feed will have old time compared to OptimisticLockingFailure container hence XRF will pick failed record as latest record ,
		 *  to resolve this updating all the containers with latest time before merge and persist so that all the container will have correct time sequence
		 *  and XRF will pick the correct message.
		 */
		
		container.getDataContainerContext().resetUpdateDateTime(LocalDateTime.now());
		
		container.getAllChildDataContainers().stream()
		.forEach(e -> e.getDataContainerContext().resetUpdateDateTime(LocalDateTime.now()));

		
		if (CollectionUtils.isNotEmpty(dbDataContainers)) {
			//// get default strategy or feed specific strategy
			DataContainerMergingService mergingService = mergingStrategyFactory.getMergingStrategy(container,
					dbDataContainers);

			dbDataContainers = mergingService.merge(container, dbDataContainers);
		} else if (feedConfiguration==null || !PersistenceType.UPDATE_ONLY.equals(feedConfiguration.getPersistenceType())) {

			dbDataContainers = new ArrayList<>();
			dbDataContainers.add(container);
			isNew = true;
		} else {
			_logger.info(
					"There is no corrosponding dbContainer available for feedContainer : {}, and this feed is updateOnly hence not persisting in db",
					container);
			return;
		}
		

		try {
			// Apply data container post processors if any have been configured.
			postProcessDataContainerBeforePersistence(dataSource, dbDataContainers);
		} catch (UdmTechnicalException udmTechnicalException) {
			_logger.error(
					"Following error occured while applying post processors on dataContainers : {} with dataSource : {}",
					dataSource, dbDataContainers, udmTechnicalException);
		}
 
		
		// invoking udlListeners on container
		List<UdlMergeListener> udlListeners = listenerFactory.getApplicableListeners(dataSource,UdlMergeEvent.POST_MERGING_CONTAINER.name());
		for (DataContainer con : dbDataContainers) {
			UdlProcessListenerInput input = UdlProcessListenerInput.builder().container(con).build();
			udlListeners.forEach(listnr -> listnr.postMergingContainer(input));
		}
		
		
		//This executor executes loader rule on dataContainers post Merging.
		loaderRuleExecutor.executeLoaderRules(dbDataContainers);
		
		//This Service validates & remove dataContainers from list based on conditions.
		for (DataContainerRejectionService rejectionService : rejectionServices) {
			rejectionService.validateAndRemoveInvalidContainerFromList(dbDataContainers);
		}

		if (CollectionUtils.isEmpty(dbDataContainers)) {
			_logger.warn("Output of data container merging is null or empty. Feed container is {}", container);
			return;
		}
		
		auditService.createAudit(dbDataContainers);
		// Send respective events
		sendContainerEvent(dataSource, container, dbDataContainers, isNew);
		dbDataContainers.forEach(sc -> {
			populateObjectId(sc);
			try {		
				   if(!verifyStatusOfDataContainer(sc)) {
				    	return;
				    }
					persistencService.persist(sc);	
					listenerService.dataContainerUpdated(null, container, sc, feedConfiguration);
						
			} catch (OptimisticLockingFailureException e) { // this catch block will handle the OptimisticLockingFailureException and make sure that failed container retries to persist
				_logger.info("Optimistic Failure - Occured for {}",sc.get_id());
				int count = 0;
				int maxRetry = 3;
				try {
					while (count < maxRetry) {
						_logger.info("Optimistic Failure - Retrying for object id {}", sc.get_id());
						retryMergeAndPersistSd(dataSource, container, feedConfiguration, sc.get_id()); // Retry to persist in case of OptimisticLockingFailureException
						count = maxRetry; // Successful hence exit
					}

				} catch (OptimisticLockingFailureException e1) {
					 ++count;
					_logger.error("Optimistic Failure - Retry count is {}", count,e1);
					
					if (count >= maxRetry) {
						_logger.error("Optimistic Failure - Maximum attempt of retry exceeded", e1);
						throw e1;
					}
				} catch (DataContainerMergeException e2) {
					_logger.error("Error occured in merge while retryMergeAndPersistSd ", e2);
				}
			}

		});
	}

	/**
	 * This method returns true if status dataAttribute with valid values present in container else it return false.
	 * @param dataContainer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean verifyStatusOfDataContainer(DataContainer dataContainer) {
		// This check is required as in sdIvo collection we are not storing status
		// Attribute.Inline lock is applicable
		// for status attribute.
		if (DataLevel.IVO_INS == dataContainer.getLevel() || DataLevel.IVO_LE == dataContainer.getLevel()) {
			return true;
		}
		DataValue<Serializable> statusAttributeValue = (DataValue<Serializable>) dataContainer
				.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(
						DataAttributeFactory.getStatusFlagForLevel(dataContainer.getLevel()),
						dataContainer.getLevel()));
		
		if (statusAttributeValue == null || statusAttributeValue.getValue() == null) {
			_logger.error("Status attribute is not present in container hence container is not persisted in db :  {} ",
					dataContainer);
			return false;
		}
		return true;
	}
	
	/**
	 * @param dataSource
	 * @param container
	 * @param feedConfiguration
	 * @throws DataContainerMergeException
	 * @throws UdmBaseException
	 * 
	 * This method will get the dbDataContainer based on the failed objectId and call the mergeAndPersistSd
	 * 
	 * When OptimisticLockingFailureException occurs while trying to inactivate the different security of same Instrument
	 * from singleFeed Then in that case after handling of OptimisticLockingFailureException we needed to reset the updateDateTime in
	 * the container else different XrfMessage will have same timestamp , that will cause the inconsistency on XRF side.
	 */
	private void retryMergeAndPersistSd(String dataSource, DataContainer container, FeedConfiguration feedConfiguration,
			String objectId) throws DataContainerMergeException  {
		List<DataContainer> dbDataContainers = null;
		
		try {
			dbDataContainers = lookupService.getDbDataContainersByObjectId(objectId, container.getLevel());

		} catch (OptimisticLockingFailureException e1) {
			_logger.error("Optimistic Failure - failed again for %s",objectId, e1);
		} catch (UdmTechnicalException e2) {
			_logger.error("Error occured while retryMergeAndPersistSd", e2);
		}

		mergeAndPersistSd(dataSource, container, dbDataContainers, feedConfiguration);

	}

	/**
	 * This method will invoke all the post processors that are available in the
	 * system on the data containers for the given data source.
	 * 
	 * @param dataSource
	 * @param dbDataContainers
	 * @throws UdmTechnicalException
	 */
	private void postProcessDataContainerBeforePersistence(String dataSource, List<DataContainer> dbDataContainers)
			throws UdmTechnicalException {
		//
		if (CollectionUtils.isEmpty(dataContainerPostProcessors)) {
			_logger.debug("No post processors defined in the system for the data source {} and data containers : {}",
					dataSource, dbDataContainers);
			return;
		}

		// Invoke post processors.
		for (DataContainerPostProcessorService postProcessService : dataContainerPostProcessors) {
			postProcessService.postProcess(dataSource, dbDataContainers);
		}
	}

	/**
	 * @param datasource
	 * @param container
	 * @param dbDataContainers
	 * @param isNew
	 */
	private void sendContainerEvent(String datasource, DataContainer container, List<DataContainer> dbDataContainers,
			boolean isNew) {
		if (isNew) {
			listenerService.newdataContainer(container);
			return;
		}
		dbDataContainers.forEach(sc -> {
			listenerService.dataContainerMerge(datasource, container, sc);
			listenerService.mergeComplete(datasource, container, sc);
		});
	}

	private void populateObjectId(DataContainer container) {
		if (container == null || DataLevel.IVO_INS.equals(container.getLevel())) {
			return;
		}
		DataValue<Serializable> value = new DataValue<>();
		value.setValue(LockLevel.FEED, generator.generateUniqueId());
		DataAttribute objectIdAttribute = DataAttributeFactory.getObjectIdIdentifierForLevel(container.getLevel());
		Serializable attributeValueAtLevel = container.getHighestPriorityValue(objectIdAttribute);
		if (attributeValueAtLevel == null) {
			container.addAttributeValue(objectIdAttribute, value);
		}
		if (!container.getLevel().equals(DataLevel.INS)) {
			return;
		}
		List<DataContainer> childDataContainers = container.getChildDataContainers(DataLevel.SEC);
		if (childDataContainers == null) {
			return;
		}
		for (int i = 0; i < childDataContainers.size(); i++) {
			DataAttribute childObjectIdAttr = DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC);
			Serializable objectId = childDataContainers.get(i).getAttributeValueAtLevel(LockLevel.FEED,
					childObjectIdAttr);
			if (objectId == null) {
				DataValue<Serializable> val1 = new DataValue<>();
				val1.setValue(LockLevel.FEED, String.valueOf(i + 1));
				childDataContainers.get(i).addAttributeValue(childObjectIdAttr, val1);
			}

		}

	}

	/**
	 * @param container
	 * @param input1
	 * @return
	 * @throws Exception
	 */
	public DataContainer lookupAndPersist(DataContainer container, LookupAttributeInput input1, String datasource,
			FeedConfiguration feedConfiguration) throws UdmBaseException {

		Serializable ds = container.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getDatasourceAttribute(container.getLevel()));
		DomainType dtDs = null;
		if (ds instanceof DomainType) {
			dtDs = (DomainType) ds;
		}

		// Get the db data container
		List<DataContainer> dbDataContainers = lookupService.getActiveDbDataContainersBySourceUniqueId(container, dtDs);
		if (CollectionUtils.isNotEmpty(dbDataContainers)) {
			_logger.debug("Merging db container and feed container. DB container is {} and feed container is {}",
					dbDataContainers, container);

		}
		// Perform the filtering
		FilterInput fInput = new FilterInput();
		fInput.setDbContainers(dbDataContainers);
		fInput.setFeedContainer(container);
		fInput.setDatasource(datasource);
		FilterOutput filterOutput = filterSerivice.applyFilterChain(fInput);
		if (StringUtils.isNotBlank(filterOutput.getMessage())) {
			_logger.debug("Output of filter {} for data container {}", filterOutput.getMessage(), container);
		}
		if (filterOutput.isPersist()) {
			performMergeAndPersist(container, dbDataContainers, input1, datasource, feedConfiguration);

		} else {
			_logger.warn(
					"Not persisting data container due to following reason {}. Data container source unique id is {}",
					filterOutput.getMessage(), container.getAttributeValue(
							DataAttributeFactory.getSourceUniqueIdentifierForLevel(container.getLevel())));
		}	
		
		//Invoking listener on filtered inactive dataContainers
		invokeFilterListener(datasource,filterOutput);
		return container;
	}

	/**
	 * Invoking listener on filtered inactive dataContainers
	 * @param datasource
	 * @param filterOutput
	 */
	private void invokeFilterListener(String datasource, FilterOutput filterOutput) {
		List<InsIdNotFoundContainerListener> filterListeners = listenerFactory.getApplicableListeners(datasource,
				InsIdNotFoundFilteredInactiveContainerEvent.ON_INACTIVE_SECURITY_FILTER.name());
		InsIdNotFoundInactiveContainerListenerInput filteredInactiveContainerListenerInput = InsIdNotFoundInactiveContainerListenerInput.builder()
				.securityDataContainers(filterOutput.getFilteredInactiveSecurities()).dataSource(datasource).build();
		filterListeners.forEach(listn -> listn.onFilteredInactiveContainer(filteredInactiveContainerListenerInput));
	}


	private void performMergeAndPersist(DataContainer container, List<DataContainer> dbDataContainers,
			LookupAttributeInput input1, String datasource, FeedConfiguration feedConfiguration)
			throws UdmBaseException {
		Map<DataContainer, DataContainer> inactiveSecuritiesInsDC = null;
		if (container.getLevel() == DataLevel.INS) {
			inactiveSecuritiesInsDC = securityFetchService.fetchExistingSecToBeInactivated(container, dbDataContainers);

		}
		// Resolve the lookup
		lookupService.resolveLookup(container, input1);
		// Merge the containers

		mergeAndPersistSd(datasource, container, dbDataContainers, feedConfiguration);

		mergeAndPersistInactiveSecurities(datasource, feedConfiguration, inactiveSecuritiesInsDC);

	}

	/**
	 * @param feedConfiguration
	 * @param datasource
	 * @param inactiveSecuritiesInsDC
	 * @throws DataContainerMergeException
	 */
	private void mergeAndPersistInactiveSecurities(String datasource, FeedConfiguration feedConfiguration,
			Map<DataContainer, DataContainer> inactiveSecuritiesInsDC) throws DataContainerMergeException {

		if (MapUtils.isEmpty(inactiveSecuritiesInsDC) || datasource == null) {
			return;
		}

		for (Map.Entry<DataContainer, DataContainer> entry : inactiveSecuritiesInsDC.entrySet()) {
			List<DataContainer> dbContainerList = new ArrayList<>(1);
			dbContainerList.add(entry.getValue());
			mergeAndPersistSd(datasource, entry.getKey(), dbContainerList, feedConfiguration);
		}

	}

}
