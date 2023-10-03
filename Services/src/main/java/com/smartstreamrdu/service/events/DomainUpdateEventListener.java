/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainUpdateEventListnener.java
 * Author : VRamani
 * Date : Jan 21, 2020
 * 
 */
package com.smartstreamrdu.service.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainColumnMetadata;
import com.smartstreamrdu.domain.DomainMaintenanceMetadata;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.message.ReprocessingData;
import com.smartstreamrdu.domain.message.ReprocessingMessage;
import com.smartstreamrdu.persistence.domain.autoconstants.DvDomainMapAttrConstant;
import com.smartstreamrdu.persistence.repository.service.DomainMetadataRepositoryService;
import com.smartstreamrdu.service.domain.DomainSearchService;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.Process;
import com.smartstreamrdu.util.DataAttributeConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * Event Listener for Domain Updates. Will check if the container is non
 * DvDomain and is inactivated, then it won't process further. If not, it will
 * find its corresponding VendorMappings and send a message to DEN. If the
 * container is DvDomain, it will generate a message from its vendor mappings
 * and send it to DEN.
 * 
 * @author VRamani
 *
 */
@Slf4j
@Component
public class DomainUpdateEventListener implements EventListener<DataContainer> {
	private static final Logger _logger = LoggerFactory.getLogger(DomainUpdateEventListener.class);

	private static final DataAttribute RDU_DOMAIN = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeConstant.RDU_DOMAIN, DataLevel.DV_DOMAIN_MAP);
	private static final DataAttribute VENDOR_MAPPINGS = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeConstant.VENDOR_MAPPINGS, DataLevel.DV_DOMAIN_MAP);
	private static final DataAttribute DOMAIN_VALUE = DataAttributeFactory.getAttributeByNameAndLevelAndParent(DataAttributeConstant.DOMAIN_VALUE, DataLevel.DV_DOMAIN_MAP, VENDOR_MAPPINGS);
	private static final DataAttribute DOMAIN_NAME = DataAttributeFactory.getAttributeByNameAndLevelAndParent(DataAttributeConstant.DOMAIN_NAME, DataLevel.DV_DOMAIN_MAP, VENDOR_MAPPINGS);
	private static final DataAttribute DOMAIN_SOURCE = DataAttributeFactory.getAttributeByNameAndLevelAndParent(DataAttributeConstant.DOMAIN_SOURCE, DataLevel.DV_DOMAIN_MAP, VENDOR_MAPPINGS);

	
	@Autowired
	private DomainSearchService domainSearchService;
	
	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private DomainMetadataRepositoryService metaDataRepoService;

	@Override
	public void propogateEvent(DataContainer dataContainer) {
		
		// DO NOT process Domain Inactivation since those will be handled by DomainInactivationEvent
		if (!dataContainer.getLevel().equals(DataLevel.DV_DOMAIN_MAP) && checkStatus(dataContainer)) {
			return;
		}
		
		String rduDomain = domainSearchService.getRduDomainFromDataLevel(dataContainer.getLevel());
		Map<DataAttribute, DataValue<Serializable>> allAttributes = dataContainer.getRecordData();
			// If DvDomain use the Vendor Mappings from the Container and send the message
		ReprocessingMessage message = null;
		if (dataContainer.getLevel().equals(DataLevel.DV_DOMAIN_MAP)) {
			message = getReprocessingMessageFromVendorMappings(dataContainer, false);
		} else {
			// if Non-DvDomain first find the corresponding Vendor Mappings and then send the message
					
			if(isDataContainerEligibleForReprocessing(rduDomain, allAttributes)){
				DataContainer dvDomainContainer = domainSearchService.findVendorMappingsFromRduDomain(dataContainer);
				
				// Here we are creating a map of DataAttribute vs DataValue
				Map<DataAttribute, DataValue<Serializable>> dataAttrMap = dataContainer.getDataRow().getRowData();
				 List<DataAttribute> changedDomainAttributes = getChangedDomainAttributes(dataAttrMap);
					
				//Here we are collecting distinct attributeNames from the changedDoaminAttributes 
				List<String> changedDomainFieldNames = changedDomainAttributes.stream().map(DataAttribute::getAttributeName)
						.distinct().collect(Collectors.toList());	
			    message = generateMessageForReprocessing(dataContainer, message, dvDomainContainer);
				if(message!= null && !CollectionUtils.isEmpty(changedDomainFieldNames)) {
					message.setChangedDomainFieldNames(changedDomainFieldNames);
				}
			}
		}
		
		sendMessageToDen(message);
	}

	/**
	 * This method it will only return attributes for which the value hasChanged.
	 * 
	 * @param dataAttrMap
	 * @return
	 */
	private List<DataAttribute> getChangedDomainAttributes(Map<DataAttribute, DataValue<Serializable>> dataAttrMap) {
		List<DataAttribute> changedAttrNames = new ArrayList<>();
			dataAttrMap.entrySet().forEach(attr -> {
				DataAttribute dataAttr = attr.getKey();
				DataValue<Serializable> value = dataAttrMap.get(dataAttr);
				// Checking if the value for the attribute has changed
				if (value.hasValueChanged()) {
					changedAttrNames.add(dataAttr);
				}
			});
		return changedAttrNames;
	}

	private ReprocessingMessage generateMessageForReprocessing(DataContainer dataContainer, ReprocessingMessage message,
			DataContainer dvDomainContainer) {
		if (dvDomainContainer != null) {
			dvDomainContainer.updateDataContainerContext(dataContainer.getDataContainerContext());
			message = getReprocessingMessageFromVendorMappings(dvDomainContainer, true);
			message.setNormalizedValue(
					dvDomainContainer.getHighestPriorityValue(DvDomainMapAttrConstant.NORMALIZED_VALUE));
		}
		
		return message;
	}

	private boolean checkStatus(DataContainer dataContainer) {
		if (!dataContainer.getLevel().equals(DataLevel.DV_DOMAIN_MAP)) {
			DataAttribute statusAttribute = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeConstant.STATUS,
					dataContainer.getLevel());
			String statusValue = dataContainer.getHighestPriorityValue(statusAttribute);
			return !DomainStatus.ACTIVE.equals(statusValue);
		}
		return false;
	}

	private void sendMessageToDen(ReprocessingMessage message) {
		if(message == null) {
			return;
		}
		Message input = new DefaultMessage.Builder().data(message)
				.target(com.smartstreamrdu.util.Constant.Component.DATA_ENRICHMENT).process(Process.DOMAIN_CHANGE_REPROCESSING)
				.build();

		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);

		try {
			producer.sendMessage(input);
			_logger.info("Message for reprocessing sent to Data Enrichment with details: {}", input);
		} catch (Exception e) {
			_logger.error("Following error occured while sending message to Data Enrichment", e);
		}
	}

	@SuppressWarnings("unchecked")
	private ReprocessingMessage getReprocessingMessageFromVendorMappings(DataContainer dataContainer, boolean addAllVendorMappings) {
		ReprocessingMessage message = new ReprocessingMessage();
		
		DataRowIterator iterator = new DataRowIterator(dataContainer, VENDOR_MAPPINGS);
		DataValue<String> rduDomain = (DataValue<String>) dataContainer.getAttributeValue(RDU_DOMAIN);
		String rduDomainValue = rduDomain.getValue();
		
		message.setRduDomain(rduDomainValue);
		
		List<ReprocessingData> reprocessingDataList = new ArrayList<>();
		
		while (iterator.hasNext()) {
			DataRow row = iterator.next();
			addToReprocessingDataList(addAllVendorMappings, reprocessingDataList, row);
		}
		message.setUpdDate(dataContainer.getDataContainerContext().getUpdateDateTime());
		
		message.setMapOfDomainSourceVsListOfReprocessingData(groupReprocessingDataOnDomainSource(reprocessingDataList));
		return message;
	}

	private void addToReprocessingDataList(boolean addAllVendorMappings, List<ReprocessingData> reprocessingDataList, DataRow row) {
		if (row.hasValueChanged() || addAllVendorMappings) {
			ReprocessingData reprocessingData = new ReprocessingData();
			DataValue<String> domainSourceValue = row.getAttributeValue(DOMAIN_SOURCE);
			reprocessingData.setDomainSource(domainSourceValue.getValue());

			DataValue<String> domainName = row.getAttributeValue(DOMAIN_NAME);
			reprocessingData.setDomainName(domainName.getValue());

			DataValue<DomainType> domainValue = row.getAttributeValue(DOMAIN_VALUE);
			domainValue.getValue().setDomain(domainName.getValue());
			reprocessingData.setDomainType(domainValue.getValue());
			reprocessingDataList.add(reprocessingData);
		}
	}

	private Map<String, List<ReprocessingData>> groupReprocessingDataOnDomainSource(
			List<ReprocessingData> reprocessingDataList) {
		return reprocessingDataList.stream().collect(Collectors.groupingBy(ReprocessingData::getDomainSource));
	}

	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return event.equals(ListenerEvent.DomainContainerUpdate);
	}

	@Override
	public DataContainer createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		if (!(inputCreationContext instanceof DomainContainerChangeEventListenerInputCreationContext)) {
			_logger.error(
					"{} requires a input creation context of type DomainContainerChangeEventListenerInputCreationContext. Supplied input creation context was of type {}",
					this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException(
					"Argument of incorrect type supplied input creation is not supported for "
							+ this.getClass().getName());
		}
		DomainContainerChangeEventListenerInputCreationContext context = (DomainContainerChangeEventListenerInputCreationContext) inputCreationContext;
		return context.getDomainContainer();
	}

	/**
	 * If there is any attribute changed and that attribute is marked as reprocessing=true
	 * or requestToFigi=true in domainMaintenanceMetadata then that is eligible for reprocessing 
	 * 
	 * @param rduDomain
	 * @param allAttributes
	 * @return true if attribute is used in xrf or proforma
	 */
	private boolean isDataContainerEligibleForReprocessing(String rduDomain,
			Map<DataAttribute, DataValue<Serializable>> allAttributes) {

		return allAttributes.entrySet().stream().anyMatch(entry -> entry.getValue().hasValueChanged()
				&& isReprocessable(rduDomain, entry.getKey().getAttributeName()));

	}
	
	/**
	 * Search domainMaintenanceMetadata collection using rduDomain 
	 * return false if rduDomain is not present in domainMaintenanceMetadata collection.
	 * Else compare the attributeName i.e changed Attribute with that of matched record
	 * from domainMaintenanceMetadata and return flag for the changed attribute
	 * isFigiRequest or isReprocessing field from domainMaintenanceMetadata for that attribute.
	 * 
	 * @param rduDomain
	 * @param attributeName
	 * @return
	 */
	private boolean isReprocessable(String rduDomain, String attributeName) {
		DomainMaintenanceMetadata domainMetadata = metaDataRepoService.getDomainMetadataMap().get(rduDomain);

		if (domainMetadata == null) {
			log.error("There is no entry present in domainMaintenanceMetadata for rduDomain "+ rduDomain);
			return false;
		}
		Predicate<DomainColumnMetadata> reprocessingPredicate = DomainColumnMetadata::isReprocessing;
		Predicate<DomainColumnMetadata> figiReqPredicate = DomainColumnMetadata::isFigiRequest;

		//Return the field value for figiRequest or reprocessing field from domainMaintenanceMetadata for that attributeName
		return domainMetadata.getColumns().stream().filter(column -> column.getField().equals(attributeName))
				.anyMatch(reprocessingPredicate.or(figiReqPredicate));
	}
}
