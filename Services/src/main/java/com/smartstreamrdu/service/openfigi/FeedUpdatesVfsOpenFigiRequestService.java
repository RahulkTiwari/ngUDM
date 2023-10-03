/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : FeedUpdatesVfsOpenFigiRequestService.java
 * Author :SaJadhav
 * Date : 30-Nov-2021
 */
package com.smartstreamrdu.service.openfigi;

import java.io.Serializable;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.events.XrfEligibilityEvaluator;
import com.smartstreamrdu.util.Constant.MessagingConstant;
import com.smartstreamrdu.util.Constant.Process;
import com.smartstreamrdu.util.DataSourceConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending VFS open figi requests in case of DataContainer updates from Feed
 * 
 * @author SaJadhav
 *
 */
@Component
@Slf4j
public class FeedUpdatesVfsOpenFigiRequestService implements VfsOpenFigiRequestService {
	
	@Autowired
	private VfsOpenFigiEligibleAttributesService vfsOpenFigiEligibleAttributeService;
	
	@Autowired
	private XrfEligibilityEvaluator xrfEligibilityEvaluator;
	
	@Autowired
	private VfsOpenFigiMessageGenerator vfsOpenFigiMessageGenerator;
	
	@Autowired
	private VfsMessageSender vfsMessageSender;

	/**
	 * Sends request to VfsRequestQueue for non IVO updates i.e. updates from feed or reprocessing from UI
	 */
	@Override
	public void sendRequestToVfsOpenFigi(DataContainer dataContainer) {

		boolean isPostChangeContainerApplicableForFigiRequest = validateIfApplicableForFigiRequest(dataContainer);
		
		// Check if the data  container is of BloombergFigi data source.
		//Check if postChangeContainer container having xrfSourcePriority =-1 then don't send request to vfsRequestQueue
		if (isFigiDataSource(dataContainer)||!isPostChangeContainerApplicableForFigiRequest) {
			// If true then do not process that data container for FIGI request.
			return;
		}
		Iterator<DataContainer> iterator = null;
		
		if (validateChildContainer(dataContainer)) {
			iterator = dataContainer.getChildDataContainers(DataLevel.SEC).iterator();
		}
		while (iterator != null && iterator.hasNext()) {

			DataContainer childContainer = iterator.next();
			
			// Send message to FIGI in case of new inserts or xrf attribute update or
			// configured non-xrf attribute updates
			if (isApplicableForFigiRequest(childContainer, dataContainer)) {
				generateAndSendMessageToVfsOpenFigi(dataContainer, childContainer);
			}
		}
	
	}
	
	
	private boolean validateIfApplicableForFigiRequest(DataContainer postChangeContainer) {
		boolean isPostChangeContainerApplicableForFigiRequest=false;
		
		try {
			isPostChangeContainerApplicableForFigiRequest = xrfEligibilityEvaluator.isDataContainerEligibleForXrf(postChangeContainer);
		} catch (UdmBaseException e1) {
			log.error("Exception occured while evaluating the postChangeContainer",e1);
		}
		return isPostChangeContainerApplicableForFigiRequest;
	}
	
	/**
	 *  This method accepts and Data Container and returns whether the Data Container is of BloombergFigi Data source.
	 * @param postChangeContainer
	 * @return
	 */
	private boolean isFigiDataSource(DataContainer postChangeContainer) {

		DomainType dataSourceValue = (DomainType) postChangeContainer
				.getHighestPriorityValue(SdDataAttrConstant.DATA_SOURCE);
		return dataSourceValue != null && dataSourceValue.getVal().equals(DataSourceConstants.FIGI_DS);
	}
	
	/**
	 * 
	 * @param postChangeContainer
	 * @return
	 */
	private boolean validateChildContainer(DataContainer postChangeContainer) {
		return postChangeContainer.getChildDataContainers(DataLevel.SEC) != null
				&& !postChangeContainer.getChildDataContainers(DataLevel.SEC).isEmpty();
	}
	
	/**
	 * Checks whether childContaineris eligible for figi request.
	 * </p>
	 * returns true if childContainer is new OR
	 * </p> 
	 * any of the XRF attribute changed OR
	 * </P>
	 * is triggered from UI reprocessing
	 * 
	 * @param childContainer
	 * @param postChangeContainer
	 * @return
	 * @throws UdmTechnicalException 
	 */
	private boolean isApplicableForFigiRequest(DataContainer childContainer, DataContainer postChangeContainer) {
		DataContainerContext dataContainerContext = postChangeContainer.getDataContainerContext();
		return dataContainerContext.isReprocessingFromUI() || childContainer.isNew()
				|| hasXrfIdentifierValueChanged(childContainer, postChangeContainer)
				|| hasNonXrfAttributeChanged(childContainer,postChangeContainer)
				|| hasValueChangedForDataAttribute(childContainer,SecurityAttrConstant.SECURITY_STATUS);

	}
	
	/**
	 * 
	 * @param childContainer
	 * @param postChangeContainer
	 * @return
	 * @throws UdmTechnicalException 
	 */
	private boolean hasNonXrfAttributeChanged(DataContainer childContainer, DataContainer postChangeContainer){
		return vfsOpenFigiEligibleAttributeService.getNonXrfAttributesEligibleForVfsRequest().stream()
				.anyMatch(attribute -> hasIdentifierValueChanged(attribute, childContainer, postChangeContainer));
	}
	
	/**
	 * This method will check xrfAttributesList is empty or not.If list is empty
	 * then it will fetch XRF attributes from crossReferenceRules collection and add
	 * into the list. From list it will get attribute name and data level and check
	 * attribute value is updated or not.
	 * 
	 * 
	 * @param childContainer
	 * @param postChangeContainer
	 * @return
	 */
	private boolean hasXrfIdentifierValueChanged(DataContainer childContainer, DataContainer postChangeContainer) {
		return vfsOpenFigiEligibleAttributeService.getXrfAttributesEligibleForVfsRequest().stream()
				.anyMatch(attribute -> hasIdentifierValueChanged(attribute, childContainer, postChangeContainer));
	}
	
	/**
	 * @param dataAttribute
	 * @param childContainer
	 * @param instrumentContainer
	 * @param dataLevel
	 * @return
	 */
	private boolean hasIdentifierValueChanged(DataAttribute dataAttribute, DataContainer childContainer, DataContainer instrumentContainer) {
		
		if (DataLevel.SEC.equals(dataAttribute.getAttributeLevel())) {
			return hasValueChangedForDataAttribute(childContainer, dataAttribute);
		} else {
			return hasValueChangedForDataAttribute(instrumentContainer, dataAttribute);
		}
	}

	/**
	 * This method will check for attribute value is change or not.If value is
	 * change then return true else false.
	 * 
	 * @param postChangeContainer
	 * @param attributeName
	 * @param dataLevel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean hasValueChangedForDataAttribute(DataContainer postChangeContainer, DataAttribute dataAttribute) {

		DataValue<Serializable> dataValue = (DataValue<Serializable>) postChangeContainer
				.getAttributeValue(dataAttribute);
		return dataValue != null && dataValue.hasValueChanged();
	}
	
	/**
	 * @param postChangeContainer
	 * @param childContainer
	 */
	private void generateAndSendMessageToVfsOpenFigi(DataContainer postChangeContainer, DataContainer childContainer) {
		VfsFigiRequestMessage openFigiIdentifiersObject = vfsOpenFigiMessageGenerator
				.generateVfsOpenFigiMessaage(childContainer, postChangeContainer, VfsFigiRequestTypeEnum.NEW_OR_UPDATE);
		vfsMessageSender.sendMessage(openFigiIdentifiersObject, Process.FigiRequest,
				MessagingConstant.PRODUCER_PARTITION_0);
	}

}
