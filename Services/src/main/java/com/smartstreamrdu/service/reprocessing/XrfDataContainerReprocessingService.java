/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : XrfDataContainerReprocessingService.java
 * Author :SaJadhav
 * Date : 07-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainColumnMetadata;
import com.smartstreamrdu.domain.DomainMaintenanceMetadata;
import com.smartstreamrdu.domain.message.UdmMessageKey;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.persistence.repository.service.DomainMetadataRepositoryService;
import com.smartstreamrdu.service.message.XrfUdmMessageKeyGenerationService;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.openfigi.FeedUpdatesVfsOpenFigiRequestService;
import com.smartstreamrdu.service.xrf.messaging.XrfMessageGenerator;
import com.smartstreamrdu.util.Constant.Process;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reprocessing service for xrf dataSources dataContainers
 * 
 * @author SaJadhav
 *
 */
@Component
@Slf4j
public class XrfDataContainerReprocessingService implements DataContainerReprocessingService {
	
	@Autowired
	@Setter
	private XrfMessageGenerator xrfMessageGenerator;
	
	@Autowired
	@Setter
	private ProducerFactory producerFactory;
	
	@Autowired
	@Setter
	private XrfUdmMessageKeyGenerationService xrfUdmMessageKeyGenerationService;
	
	@Autowired
	private FeedUpdatesVfsOpenFigiRequestService feedUpdatesVfsOpenFigiRequestService;
	
	@Autowired
	private DomainMetadataRepositoryService metaDataRepoService;

	@Override
	public void reprocesDataContainer(DataContainer dataContainer, LocalDateTime staticDataUpdateDate, String rduDomain,
			List<String> changedDomainFieldNames) throws UdmBaseException {
		
		//Fetching the metadata for the domain provided
		DomainMaintenanceMetadata domainMetaData = metaDataRepoService.getDomainMetadataMap().get(rduDomain);
		if (domainMetaData!=null) {

			Predicate<DomainColumnMetadata> isFigiReq = DomainColumnMetadata::isFigiRequest;
			boolean isFigiRequest = checkIfEligible(changedDomainFieldNames, domainMetaData, isFigiReq);

			//Returns the value for isEligibleForFigiRequest flag from domainMetaData
			boolean domainEligibleForFigiReq = domainMetaData.isEligibleForFigiRequest();

			isEligibleToSendRequestToFigi(dataContainer, isFigiRequest, domainEligibleForFigiReq);

			Predicate<DomainColumnMetadata> isElgibleForReprocessing = DomainColumnMetadata::isReprocessing;
			boolean isReprocessing = checkIfEligible(changedDomainFieldNames, domainMetaData, isElgibleForReprocessing);

			ChangeEventInputPojo changeEventInput = new ChangeEventInputPojo();
			changeEventInput.setPostChangeContainer(dataContainer);
			if (DataLevel.INS.equals(dataContainer.getLevel()) && isReprocessing) {
				XrfMessage xrfMessage = xrfMessageGenerator.generateMessage(changeEventInput, staticDataUpdateDate);
				sendXrfMessage(xrfMessage, dataContainer);
			}
		}
	}

	
	/** 
	 * There are 2 scenarios for this method :-
	 * 
	 * 1) If there is an update from domainMap UUI page in that case changedFieldNames will be empty
	 *    and it will return true.
	 * 
	 * 2) If there is an update from domainMaintenance page UUI then changedFieldNames will be populated
	 * Here it checks if the field for the corresponding domain is present 
	 * in the changedFieldNames and returns the value of the columnToCheck flag.
	 * 
	 * @param changedFieldNames
	 * @param domainMetaData
	 * @param isFigiReq 
	 * @return
	 */
	private boolean checkIfEligible(List<String> changedFieldNames, DomainMaintenanceMetadata domainMetaData, 
			Predicate<DomainColumnMetadata> columnToCheck) {

		if(changedFieldNames !=null && !changedFieldNames.isEmpty()) {
			return domainMetaData.getColumns().stream()
					.filter(metaData -> changedFieldNames.contains(metaData.getField()))
					.anyMatch(columnToCheck);
		}
		return true;
	}


	/** 
	 * This method sends the message to FigiRequestQueue with the provided dataContainer
	 * if it is eligible to re-request to figi.
	 * 
	 * @param dataContainer
	 * @param isFigiRequest
	 * @param domainEligibleForFigiReq
	 */
	private void isEligibleToSendRequestToFigi(DataContainer dataContainer, boolean isFigiRequest, boolean domainEligibleForFigiReq) {	
		/*
		 * checking if the domainEligibleForFigiReq and isFigiRequest flag
		 * both are true then only send message to figi or else it won't send.
		 */
		if  (domainEligibleForFigiReq && isFigiRequest) {
			
			// Setting reprocessingFromUI to true as this is a UI driven event when a domain is added or updated 
			DataContainerContext containerContext = DataContainerContext.builder().reprocessingFromUI(true).build();
			dataContainer.updateDataContainerContext(containerContext);
			feedUpdatesVfsOpenFigiRequestService.sendRequestToVfsOpenFigi(dataContainer);
		}

	}

	/**
	 * @param xrfMessage
	 * @param dataContainer 
	 */
	private void sendXrfMessage(XrfMessage xrfMessage, DataContainer dataContainer) {
		if (null == xrfMessage) {
			log.info("Not sending message to XRF as the XrfMessage generated for dataContainer {} was null",
					dataContainer);
			return;
		}
		
		UdmMessageKey udmMessageKey = xrfUdmMessageKeyGenerationService.generateUdmMessageKey(xrfMessage);
		
		Message input = new DefaultMessage.Builder().data(xrfMessage).key(udmMessageKey).process(Process.FileLoad)
				.target(com.smartstreamrdu.util.Constant.Component.XRF).build();

		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);
		try {
			producer.sendMessage(input);
		} catch (Exception e) {
			log.error("Following error occured while sending messagee to XRF for DatContainer with id :: "
					+ dataContainer.get_id(), e);
		}
		log.debug("Message sent to XRF with details: {}", input);
	}

}
