/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : IvoUpdatesVfsOpenFigiRequestService.java
 * Author :SaJadhav
 * Date : 30-Nov-2021
 */
package com.smartstreamrdu.service.openfigi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoSecurityAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoSecurityRelationAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.XrfInstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.XrfSecurityAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.service.ivo.IvoLockService;
import com.smartstreamrdu.service.ivo.IvoQueryService;

import com.smartstreamrdu.util.DataSourceConstants;
import com.smartstreamrdu.util.FullLoadBasedInactivationConstant;
import com.smartstreamrdu.util.LambdaExceptionUtil;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.MessagingConstant;
import com.smartstreamrdu.util.Constant.Process;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending VFS open figi requests in case of centralized IVO locks applied on the DataContainer
 * 
 * @author SaJadhav
 *
 */
@Component
@Slf4j
public class IvoUpdatesVfsOpenFigiRequestService implements VfsOpenFigiRequestService {
	
	private static final DataAttribute instrumentXrfRefIdAttr = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_REF_ID, DataLevel.XRF_INS);

	private static final DataAttribute instrumentXrfLinkDataSourceAttr = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_DATASOURCE, DataLevel.XRF_INS);

	private static final DataAttribute instrumentXrfLinkStatusAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.XR_INS_LINK_STATUS, DataLevel.XRF_INS);

	private static final DataAttribute ivoSecurityRelationTypeAttr = DataAttributeFactory
			.getAttributeByNameAndLevelAndParent(IvoSecurityRelationAttrConstant.COL_RELATION_TYPE, DataLevel.IVO_SEC, IvoSecurityAttrConstant.SECURITY_RELATIONS);
	private static final DataAttribute ivoSecurityRelationReferenceIdAttr=DataAttributeFactory
			.getAttributeByNameAndLevelAndParent(IvoSecurityRelationAttrConstant.COL_REFERENCE_ID, DataLevel.IVO_SEC, IvoSecurityAttrConstant.SECURITY_RELATIONS);

	private static final DataAttribute xrSecLinkStatusAttribute = DataAttributeFactory.
			getAttributeByNameAndLevelAndParent(CrossRefConstants.XR_SEC_LINK_STATUS, DataLevel.XRF_SEC,XrfSecurityAttrConstant.SECURITY_XRF_LINKS);

	private static final DataAttribute xrSecLinkDataSourceAttribute = DataAttributeFactory.
			getAttributeByNameAndLevelAndParent(CrossRefConstants.SEC_DATASOURCE, DataLevel.XRF_SEC,XrfSecurityAttrConstant.SECURITY_XRF_LINKS);

	private static final DataAttribute xrSecurityXrfRefIdAttribute = DataAttributeFactory.
			getAttributeByNameAndLevelAndParent(CrossRefConstants.SEC_REF_ID, DataLevel.XRF_SEC,XrfSecurityAttrConstant.SECURITY_XRF_LINKS);
	
	@Autowired
	private VfsOpenFigiEligibleAttributesService vfsOpenFigiEligibleAttributeService;
	
	@Autowired
	private DataRetrievalService dataRetrievalService;
	
	@Autowired
	private IvoLockService ivoLockService;
	
	@Autowired
	private IvoQueryService ivoQueryService;
	
	@Autowired
	private VfsMessageSender vfsMessageSender;
	
	@Autowired
	private VfsOpenFigiMessageGenerator vfsOpenFigiMessageGenerator;
	
	/**
	 * Sends message to VfsRequestQueue for the input centralized IVO DataContainer.
	 * Checks whether any non xrf attribute for VFS request has changed in the DataContainer.
	 * If changed then finds out all the non figi Active SD documents for input {@code dataContainer} , merges the lock values with 
	 * sdDataContainer and sends message to VfsRequestQueue for all changed Security documents
	 */
	@Override
	public void sendRequestToVfsOpenFigi(@NonNull DataContainer ivoDataContainer) {

		// check if postChangeContainer has any configured vfs non-xrf attribute changes
		List<String> changedRduSecurityIds = new ArrayList<>();
		boolean valueChanged = hasNonXrfVfsAttributeValueChangedInIvoContainer(ivoDataContainer, changedRduSecurityIds);

		if (!valueChanged) {
			return;
		}
		// query XRF to find the xrf document
		try {
			DataContainer xrContainer = ivoQueryService.getXrfDataContainerFromIvoDataContainer(ivoDataContainer);
			if (xrContainer != null) {
				List<DataContainer> sdDataContainers = getSdDataContainersFromXrfDataContainer(xrContainer,
						changedRduSecurityIds);
				sdDataContainers.forEach(LambdaExceptionUtil
						.rethrowConsumer(container -> sendVfsOpenFigiMessageForIvoContainer(container,
								xrContainer, ivoDataContainer)));
			}
		} catch (UdmTechnicalException e) {
			log.error(String.format(
					"Following error occured while sending message to FigiRequestQueue for IVO DataContainer Id %s",
					ivoDataContainer.get_id()), e);
		}

	}
	
	/**
	 * Returns true if the value for configured VFS non-xrf attributes has changed.
	 * If value changed at child container level then it populates changedRduSecurityIds list
	 * 
	 * @param ivoDataContainer
	 * @param changedRduSecurityIds 
	 * @return
	 */
	private boolean hasNonXrfVfsAttributeValueChangedInIvoContainer(DataContainer ivoDataContainer, List<String> changedRduSecurityIds) {
		boolean valueChanged = vfsOpenFigiEligibleAttributeService.getNonXrfIvoInsAttributesEligibleForVfsRequest().stream()
				.anyMatch(attribute -> hasValueChangedForDataAttribute(ivoDataContainer, attribute));
		if (!valueChanged
				&& !CollectionUtils.isEmpty(ivoDataContainer.getChildDataContainers(DataLevel.IVO_SEC))) {
			List<DataContainer> childDataContainers = ivoDataContainer.getChildDataContainers(DataLevel.IVO_SEC);

			List<String> changedRduSecIds = childDataContainers.stream()
					.filter(childContainer -> vfsOpenFigiEligibleAttributeService.getNonXrfIvoSecAttributesEligibleForVfsRequest().stream()
							.anyMatch(attribute -> hasValueChangedForDataAttribute(childContainer, attribute))).map(this :: getRduSecurityIdFromIvoContainer)
					.collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(changedRduSecIds)) {
				valueChanged=true;
				changedRduSecurityIds.addAll(changedRduSecIds);
			}
		}
		return valueChanged;
	}
	
	/**
	 * @param childContainer
	 * @return
	 */
	private String getRduSecurityIdFromIvoContainer(DataContainer childContainer) {
		DataRow dataRow = (DataRow) childContainer.getAttributeValue(IvoSecurityAttrConstant.SECURITY_RELATIONS);
		ArrayList<DataRow> listSecurityRelations = dataRow.getValue().getValue();
		Optional<ReferenceId> referenceIdOptional = listSecurityRelations.stream()
				.filter(secRelation -> RelationType.IVO.name()
						.equals(secRelation.getHighestPriorityAttributeValue(ivoSecurityRelationTypeAttr)))
				.map(secRelation -> (ReferenceId) secRelation
						.getHighestPriorityAttributeValue(ivoSecurityRelationReferenceIdAttr))
				.findFirst();
		if (referenceIdOptional.isPresent()) {
			return referenceIdOptional.get().getObjectId();
		}
		return "";
	}
	
	/**
	 * Retrieves SD dataContainers(except FIGI dataContainers) based on the instrument links in {@code xrContainer}.
	 * If {@code changedRduSecurityIds} is not empty then retrieves only affected securities from the SD dataContainer,
	 * Otherwise retrieves all the linked SD data documents.
	 * 
	 * @param xrContainer 
	 * @param changedRduSecurityIds 
	 * @return
	 * @throws UdmTechnicalException 
	 */
	private List<DataContainer> getSdDataContainersFromXrfDataContainer(DataContainer xrContainer,
			List<String> changedRduSecurityIds) throws UdmTechnicalException {
		DomainType xrfInstrumentStatusValue = xrContainer
				.getHighestPriorityValue(XrfInstrumentAttrConstant.XRF_INSTRUMENT_STATUS);
		if (!DomainStatus.ACTIVE.equals(xrfInstrumentStatusValue.getNormalizedValue())) {
			return Collections.emptyList();
		}

		DataRow dataRow = (DataRow) xrContainer.getAttributeValue(XrfInstrumentAttrConstant.INSTRUMENT_XRF_LINKS);
		
		ArrayList<DataRow> instrumentXrfLinks = dataRow.getValue().getValue();
		List<ReferenceId> insLinkRefIds = instrumentXrfLinks.stream()
				.filter(link -> isNonFigiActiveLink(link, instrumentXrfLinkStatusAttr, instrumentXrfLinkDataSourceAttr))
				.map(insXrfLink -> (ReferenceId) insXrfLink.getHighestPriorityAttributeValue(instrumentXrfRefIdAttr))
				.collect(Collectors.toList());

		Map<String, List<String>> mapOfSdDocumentIdVsChangedSecurityIds = null;
		
		if (!CollectionUtils.isEmpty(changedRduSecurityIds)) {
			mapOfSdDocumentIdVsChangedSecurityIds = getMapOfSdDocumentIdVsChangedSecurityIds(changedRduSecurityIds,
					xrContainer);
		}

		DataRetrivalInput retrievalInput = createDataRetrievalInput(insLinkRefIds,
				mapOfSdDocumentIdVsChangedSecurityIds);

		List<DataContainer> retrieveResult = dataRetrievalService.retrieve(Database.Mongodb, retrievalInput);
		if (CollectionUtils.isEmpty(retrieveResult)) {
			return Collections.emptyList();
		}

		return retrieveResult;
	}
	
	/**
	 * @param insLinkRefIds
	 * @param mapOfSdDocumentIdVsChangedSecurityIds
	 * @return
	 */
	private DataRetrivalInput createDataRetrievalInput(List<ReferenceId> insLinkRefIds,
			Map<String, List<String>> mapOfSdDocumentIdVsChangedSecurityIds) {
		Criteria criteria = createCriteriaForSdQuery(insLinkRefIds, mapOfSdDocumentIdVsChangedSecurityIds);

		DataRetrivalInput retrievalInput = new DataRetrivalInput();
		retrievalInput.setCriteria(criteria);
		retrievalInput.setOutputUnwindPath(FullLoadBasedInactivationConstant.SEC_UNWIND_PATH);
		return retrievalInput;
	}
	
	/**
	 * @param changedRduSecurityIds
	 * @param xrContainer
	 */
	private Map<String,List<String>> getMapOfSdDocumentIdVsChangedSecurityIds(List<String> changedRduSecurityIds,
			DataContainer xrContainer) {
		List<DataContainer> childDataContainers = xrContainer.getChildDataContainers(DataLevel.XRF_SEC);
		if (CollectionUtils.isEmpty(childDataContainers)) {
			return Collections.emptyMap();
		}

		final Map<String, List<String>> documentidVsSecurityIdsMap = new HashMap<>();

		childDataContainers.stream().filter(xrSecContainer -> changedRduSecurityIds
				.contains(xrSecContainer.getHighestPriorityValue(XrfSecurityAttrConstant.RDU_SECURITY_ID))
				&& ((DomainType) xrSecContainer.getHighestPriorityValue(XrfSecurityAttrConstant.XRF_SECURITY_STATUS))
						.getNormalizedValue().equals(DomainStatus.ACTIVE))
				.map(xrSecContainer -> (DataRow) xrSecContainer
						.getAttributeValue(XrfSecurityAttrConstant.SECURITY_XRF_LINKS))
				.forEach(xrSecContainer -> populateMapOfDocumentIdVsSecurityIds(xrSecContainer,
						documentidVsSecurityIdsMap));

		return documentidVsSecurityIdsMap;
	}

	/**
	 * @param xrSecLinkRow
	 * @param documentidVsSecurityIdsMap
	 * @return
	 */
	private void populateMapOfDocumentIdVsSecurityIds(DataRow dataRow,
			final Map<String, List<String>> documentidVsSecurityIdsMap) {
		ArrayList<DataRow> xrSecLinks = dataRow.getValue().getValue();
		xrSecLinks.stream().filter(
				xrSecLink -> isNonFigiActiveLink(xrSecLink, xrSecLinkStatusAttribute, xrSecLinkDataSourceAttribute))
				.forEach(xrSecLink -> {
					ReferenceId refId = xrSecLink.getHighestPriorityAttributeValue(xrSecurityXrfRefIdAttribute);
					documentidVsSecurityIdsMap.computeIfAbsent(refId.getDocumentId().toString(), k -> new ArrayList<>())
							.add(refId.getObjectId());
				});
	}

	/**
	 * @param listXrInsLinkRefId
	 * @param mapOfSdDocumentIdVsChangedSecurityIds 
	 * @return
	 */
	private static Criteria createCriteriaForSdQuery(List<ReferenceId> listXrInsLinkRefId,
			final Map<String, List<String>> mapOfSdDocumentIdVsChangedSecurityIds) {
		DataAttribute documetIdAttribute = DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.Document,
				false, null);
		List<Criteria> criteriaList = null;
		if (!MapUtils.isEmpty(mapOfSdDocumentIdVsChangedSecurityIds)) {
			criteriaList = mapOfSdDocumentIdVsChangedSecurityIds.entrySet().stream().map(entry -> {
				DataValue<String> dataVal = new DataValue<>();
				dataVal.setValue(LockLevel.FEED, entry.getKey());
				Criteria idCriteria = Criteria.where(documetIdAttribute).is(dataVal);
				return idCriteria.andOperator(
						Criteria.where(SecurityAttrConstant._SECURITY_ID).in(getDataValueList(entry.getValue())));
			}).collect(Collectors.toList());
		} else {
			criteriaList = listXrInsLinkRefId.stream().map(refId -> {
				DataValue<String> dataVal = new DataValue<>();
				dataVal.setValue(LockLevel.FEED, refId.getDocumentId().toString());
				return Criteria.where(documetIdAttribute).is(dataVal);
			}).collect(Collectors.toList());
		}

		if (criteriaList.size() == 1) {
			return criteriaList.get(0);
		} else {
			return new Criteria().orOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
		}
	}
	
	/**
	 * Merges sdDataContainer with IVO dataContainer and sends message to VfsRequestQueue
	 * 
	 * @param sdDataContainer
	 * @param ivoDataContainer 
	 * @param xrContainer 
	 * @return
	 * @throws UdmTechnicalException 
	 */
	private void sendVfsOpenFigiMessageForIvoContainer(DataContainer sdDataContainer, DataContainer xrContainer,
			DataContainer ivoDataContainer) throws UdmTechnicalException {
		DataContainer mergedContainer = ivoLockService.mergeContainers(sdDataContainer, ivoDataContainer, xrContainer);
		List<DataContainer> childDataContainers = mergedContainer.getChildDataContainers(DataLevel.SEC);

		if (CollectionUtils.isEmpty(childDataContainers)) {
			return;
		}
		childDataContainers.stream().forEach(childContainer -> generateAndSendMessageToVfsOpenFigi(mergedContainer,childContainer));
	}

	/**
	 * @param listValues
	 * @return
	 */
	private static Collection<DataValue<? extends Serializable>> getDataValueList(List<String> listValues) {
		return listValues.stream().map(value->{
			DataValue<String> dataValue=new DataValue<>();
			dataValue.setValue(LockLevel.FEED, value);
			return dataValue;
		}).collect(Collectors.toList());
	}

	/**
	 * Checks whether the link is active and dataSource is not figi
	 * @param xrLink
	 * @return
	 */
	private boolean isNonFigiActiveLink(DataRow xrLink,DataAttribute statusAttribute, DataAttribute dataSourceAttribute) {
		DomainType linkStatus = xrLink.getHighestPriorityAttributeValue(statusAttribute);
		DomainType dataSource = xrLink.getHighestPriorityAttributeValue(dataSourceAttribute);
		return DomainStatus.ACTIVE.equals(linkStatus.getNormalizedValue())
				&& !DataSourceConstants.FIGI_DS.equals(dataSource.getNormalizedValue());
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
