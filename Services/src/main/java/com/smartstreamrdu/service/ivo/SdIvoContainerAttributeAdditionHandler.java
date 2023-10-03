/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdIvoContainerAttributeAdditionHandler.java
 * Author : SaJadhav
 * Date : 12-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.IvoDocType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.IvoConstants;

/**
 * @author SaJadhav
 *
 */
@Component
public class SdIvoContainerAttributeAdditionHandler implements IvoContainerAttributeAdditionHandler {

	private final DataAttribute ivoDocTypeAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(IvoConstants.DOC_TYPE, DataLevel.IVO_DOC);

	private final DataAttribute sdIvoInsRelAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
	private final DataAttribute rduInstrumentIdAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.RDU_ID_INS_ATT_NAME, DataLevel.XRF_INS);
	private final DataAttribute ivoSecurityRelationsAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(IvoConstants.SECURITY_RELATIONS, DataLevel.IVO_SEC);
	private final DataAttribute xrfSecLinkStatusAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.XR_SEC_LINK_STATUS, DataLevel.XRF_SEC);

	private final DataAttribute xrfSecRefIdAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.SEC_REF_ID, DataLevel.XRF_SEC);
	
	private DataAttribute updDateAttribute=DataAttributeFactory
			.getAttributeByNameAndLevel("updDate", DataLevel.Document);

	private DataAttribute updUserAttribute=DataAttributeFactory
			.getAttributeByNameAndLevel("updUser", DataLevel.Document);

	@Autowired
	private IvoAttributeService ivoAttrService;

	@Autowired
	private IvoQueryService ivoQueryService;

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.ivo.IvoContainerAttributeAdditionHandler#
	 * addAttribute(com.smartstreamrdu.service.ivo.IvoContainer,
	 * com.smartstreamrdu.domain.DataAttribute,
	 * com.smartstreamrdu.domain.DataValue, java.util.Map, java.util.Map)
	 */
	@Override
	public void addAttribute(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<? extends Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap,
			Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException {
		Objects.requireNonNull(ivoContainer,"ivoContainer should be populated");
		Objects.requireNonNull(dataAttribute,"dataAttribute should be populated");
		
		Optional<DataContainer> sdIvoContainerOptional = ivoContainer.getSdIvoContainer();
		DataContainer sdIvoContainer = sdIvoContainerOptional.isPresent() ? sdIvoContainerOptional.get() : null;
		if (sdIvoContainer == null) {
			sdIvoContainer = initAndGetSdIvoContainer(dataLevelVsObjectIdMap.get(DataLevel.INS),
					dataLevelVsSourceUniqueIdMap.get(DataLevel.INS), ivoContainer);
		}
		DataAttribute ivoDataAttribute = ivoAttrService.getIvoAttributeForSdAttribute(dataAttribute);
		if (ivoDataAttribute.getAttributeLevel().equals(DataLevel.IVO_SEC)) {
			addToIvoSecurityContainer(ivoDataAttribute, dataValue, dataLevelVsObjectIdMap.get(DataLevel.SEC),
					sdIvoContainer, ivoContainer);
		} else {
			sdIvoContainer.addAttributeValue(ivoDataAttribute, dataValue);
		}
	}

	private DataContainer initAndGetSdIvoContainer(String objectId, String sourceUniqueId, IvoContainer ivoContainer)
			throws UdmTechnicalException {
		DataContainer sdIvoContainer = IvoContainerHelper.createNewDataContainer(null, DataLevel.IVO_INS, null, null, null, ivoContainer.getContainerContext());
		ivoContainer.setSdIvoContainer(sdIvoContainer);
		DataValue<String> docTypeValue = new DataValue<>();
		docTypeValue.setValue(LockLevel.RDU, IvoDocType.RDU.name());
		sdIvoContainer.addAttributeValue(ivoDocTypeAttribute, docTypeValue);
		// Populate the XRF relation Data
		DataContainer xrContainer = initAndGetXrDataContainer(objectId, ivoContainer);
		ivoContainer.setXrDataContainer(xrContainer);
		String rduInstrumentId = xrContainer.getHighestPriorityValue(rduInstrumentIdAttribute);
		// add relation at IVO_INS level
		addIvoRelationData(rduInstrumentId, sdIvoContainer, xrContainer.get_id(), sdIvoInsRelAttr);
		Optional<DataContainer> sdContainerOptional = ivoContainer.getSdContainer();
		if (!sdContainerOptional.isPresent()) {
			DataContainer sdContainer = IvoContainerHelper.createNewDataContainer(ivoContainer.getSdDocumentId(),
					DataLevel.INS, ivoContainer.getDataSourceId(), objectId, sourceUniqueId, ivoContainer.getContainerContext());
			populateUpdDateAndUpdUser(sdContainer,ivoContainer.getContainerContext());
			ivoContainer.setSdContainer(sdContainer);
			
		}
		return sdIvoContainer;
	}

	/**
	 * @param sdContainer
	 * @param containerContext
	 */
	private void populateUpdDateAndUpdUser(DataContainer sdContainer, DataContainerContext containerContext) {
		DataValue<LocalDateTime> updDateValue=new DataValue<>();
		updDateValue.setValue(LockLevel.RDU, containerContext.getUpdateDateTime());
		sdContainer.addAttributeValue(updDateAttribute, updDateValue);
		DataValue<String> updUserValue=new DataValue<>();
		updUserValue.setValue(LockLevel.RDU, containerContext.getUpdatedBy());
		sdContainer.addAttributeValue(updUserAttribute, updUserValue);
	}

	/**
	 * Initialize xrDataContainer
	 * 
	 * @param ivoContainer
	 * @throws UdmTechnicalException
	 */
	private DataContainer initAndGetXrDataContainer(String instrumentId, IvoContainer ivoContainer)
			throws UdmTechnicalException {
		List<DataContainer> xrDataContainers = ivoQueryService.getXrfDataContainer(instrumentId,
				ivoContainer.getSdDocumentId());
		if (CollectionUtils.isEmpty(xrDataContainers)) {
			throw new UdmTechnicalException("XRF documents not found for instrument id :" + instrumentId,
					new Exception());
		}
		return xrDataContainers.get(0);
	}

	/**
	 * @param dataAttribute
	 * @param dataValue
	 * @param sourceUniqueId
	 * @param objectId
	 * @param sdIvoContainer
	 * @param ivoContainer
	 */
	private void addToIvoSecurityContainer(DataAttribute dataAttribute, DataValue<? extends Serializable> dataValue,
			String objectId, DataContainer sdIvoContainer, IvoContainer ivoContainer) {
		DataContainer xrDataContainer = ivoContainer.getXrDataContainer();
		DataContainer childContainer = getIvoChildContainer(sdIvoContainer, objectId, xrDataContainer,
				ivoContainer.getSdDocumentId());
		if (childContainer == null) {
			childContainer = IvoContainerHelper.createNewDataContainer(null, DataLevel.IVO_SEC, null, null, null, ivoContainer.getContainerContext());
			sdIvoContainer.addDataContainer(childContainer, DataLevel.IVO_SEC);
			String rduSecurityId = getRduSecurityId(xrDataContainer, ivoContainer.getSdDocumentId(), objectId);
			addIvoRelationData(rduSecurityId, childContainer, xrDataContainer.get_id(), ivoSecurityRelationsAttribute);
		}

		childContainer.addAttributeValue(dataAttribute, dataValue);
	}

	/**
	 * Gets the matching ivo child container
	 * 
	 * @param ivoContainer
	 * @param dataLevel
	 * @param objectId
	 * @param xrDataContainer
	 * @param sdDocumentId
	 * @return
	 */
	private DataContainer getIvoChildContainer(DataContainer ivoContainer, String objectId,
			DataContainer xrDataContainer, String sdDocumentId) {
		Objects.requireNonNull(objectId, "objectId should be populated");
		DataContainer childContainer = null;
		String rduSecurityId = getRduSecurityId(xrDataContainer, sdDocumentId, objectId);
		List<DataContainer> childContainers = ivoContainer.getAllChildDataContainers();

		if (!CollectionUtils.isEmpty(childContainers)) {
			List<DataContainer> filteredChilds = childContainers.stream()
					.filter(dataContainer -> ivoChildMatchingPredicate(rduSecurityId, dataContainer))
					.collect(Collectors.toList());
			childContainer = CollectionUtils.isEmpty(filteredChilds) ? null : filteredChilds.get(0);
		}
		return childContainer;
	}

	/**
	 * Gets rduSecurityId from the list of xrf security containers
	 * 
	 * @param sdIvoChildContainer
	 * @param xrContainer
	 * @param sdDocumentId
	 * @param securityId
	 * @return
	 */
	private String getRduSecurityId(DataContainer xrContainer, String sdDocumentId, String securityId) {

		ReferenceId comparisonReferenceId = new ReferenceId(securityId, sdDocumentId);

		List<DataContainer> matchingXrSecs = xrContainer.getAllChildDataContainers().stream()
				.filter(xrChildContainer -> isMatchingXrfSecurity(comparisonReferenceId, xrChildContainer))
				.collect(Collectors.toList());

		DataContainer matchingXrSecurity = matchingXrSecs.get(0);
		DataAttribute rduSecIdAttr = DataAttributeFactory
				.getAttributeByNameAndLevel(CrossRefConstants.RDU_ID_SEC_ATT_NAME, DataLevel.XRF_SEC);
		return (String) matchingXrSecurity.getAttributeValueAtLevel(LockLevel.RDU, rduSecIdAttr);
	}

	/**
	 * Checks if the xrChildContainer is same based on comparisonReferenceId
	 * 
	 * @param comparisonReferenceId
	 * @param xrChildContainer
	 * @return
	 */
	private boolean isMatchingXrfSecurity(ReferenceId comparisonReferenceId, DataContainer xrChildContainer) {
		Serializable attributeValue = xrChildContainer.getAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_LINKS, DataLevel.XRF_SEC));
		DataRow dataRow = (DataRow) attributeValue;
		DataValue<ArrayList<DataRow>> value = dataRow.getValue();
		List<DataRow> secXrfLinks = value.getValue();
		return secXrfLinks.stream().anyMatch(link -> {
			DataValue<ReferenceId> referenceIdDV = link.getAttributeValue(xrfSecRefIdAttribute);
			DomainType secLinkStatus = link.getAttributeValueAtLevel(LockLevel.RDU, xrfSecLinkStatusAttribute);
			return comparisonReferenceId.equals(referenceIdDV.getValue())
					&& Constant.DomainStatus.ACTIVE.equals(secLinkStatus.getNormalizedValue());
		});
	}

	/**
	 * Checks whether the rduSecurityId matches with the objectId of ReferenceId
	 * value
	 * 
	 * @param rduSecurityId
	 * @param sdIvoSecRelAttr
	 * @param ivoRelationRefIdAttr
	 * @param ivoDataContainer
	 * @return
	 */
	private boolean ivoChildMatchingPredicate(String rduSecurityId, DataContainer ivoDataContainer) {
		DataRowIterator secRelationIterator = new DataRowIterator(ivoDataContainer, ivoSecurityRelationsAttribute);
		DataAttribute ivoRelationRefIdAttr = DataAttributeFactory
				.getRelationRefIdAttribute(ivoSecurityRelationsAttribute);
		ReferenceId secRelationRefId = secRelationIterator.next().getAttributeValueAtLevel(LockLevel.RDU,
				ivoRelationRefIdAttr);

		return rduSecurityId.equals(secRelationRefId.getObjectId());
	}

	/**
	 * Adds IvoRelation attribute at IVO_INS and IVO_SEC level
	 * 
	 * @param objectId
	 * @param dataContainer
	 * @param documentId
	 * @param relationAtrribute
	 */
	private void addIvoRelationData(String objectId, DataContainer dataContainer, String documentId,
			DataAttribute relationAtrribute) {
		DataRow ivoRelLink = new DataRow(relationAtrribute);
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(relationAtrribute);

		DataValue<String> relationTypeVal = new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoRelLink.addAttribute(relationTypeAttr, relationTypeVal);

		DataAttribute referenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(relationAtrribute);
		DataValue<ReferenceId> referenceIdVal = new DataValue<>();
		referenceIdVal.setValue(LockLevel.RDU, new ReferenceId(objectId, documentId));
		ivoRelLink.addAttribute(referenceIdAttr, referenceIdVal);

		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(ivoRelLink);
		DataValue<ArrayList<DataRow>> relDataValue = new DataValue<>();
		relDataValue.setValue(LockLevel.RDU, relLinkList);
		DataRow relDataRow = new DataRow(relationAtrribute, relDataValue);
		dataContainer.addAttributeValue(relationAtrribute, relDataRow);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.ivo.IvoContainerAttributeAdditionHandler#
	 * addAttributToEmbeddedRelation(com.smartstreamrdu.service.ivo.
	 * IvoContainer, com.smartstreamrdu.domain.DataAttribute,
	 * com.smartstreamrdu.domain.DataValue, java.util.Map, java.util.Map)
	 */
	@Override
	public void addAttributToEmbeddedRelation(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<? extends Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap,
			Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException {
		Optional<DataContainer> sdIvoContainerOptional = ivoContainer.getSdIvoContainer();
		DataContainer sdIvoContainer = sdIvoContainerOptional.isPresent() ? sdIvoContainerOptional.get() : null;
		if (sdIvoContainer == null) {
			sdIvoContainer = initAndGetSdIvoContainer(dataLevelVsObjectIdMap.get(DataLevel.INS),
					dataLevelVsSourceUniqueIdMap.get(DataLevel.INS), ivoContainer);
		}
		DataAttribute relationTypeAttribute = DataAttributeFactory.getRelationTypeAttribute(dataAttribute);
		DataAttribute relationRefDataAttribute = DataAttributeFactory.getRelationRefDataAttribute(dataAttribute);
		DataRow dataRow = (DataRow) dataValue;
		Iterator<DataRow> dataRowIterator = dataRow.getValue().getValue().iterator();
		while (dataRowIterator.hasNext()) {
			DataRow row = dataRowIterator.next();
			String relationType = row.getHighestPriorityAttributeValue(relationTypeAttribute);
			DataRow ivoRefData = IvoContainerHelper.getEmbeddedRelationRefData(sdIvoContainer,
					ivoAttrService.getIvoAttributeForSdAttribute(dataAttribute), relationType);
			DataRow refData = (DataRow) row.getAttributeValue(relationRefDataAttribute);
			mergeSdRefDataToIvoRefData(refData, ivoRefData, ivoAttrService);

		}
	}

	/**
	 * 
	 * @param sdRefData
	 * @param ivoRefData
	 * @param ivoAttrService
	 */
	private void mergeSdRefDataToIvoRefData(DataRow sdRefData, DataRow ivoRefData, IvoAttributeService ivoAttrService) {

		Map<DataAttribute, DataValue<Serializable>> rowData = sdRefData.getRowData();

		rowData.forEach((sdAttribute, value) -> {
			DataAttribute ivoDataAttribute = ivoAttrService.getIvoAttributeForSdAttribute(sdAttribute);
			DataValue<Serializable> ivoDataValue = new DataValue<>();
			ivoDataValue.setValue(LockLevel.RDU, value.getValue(LockLevel.RDU));

			ivoRefData.addAttribute(ivoDataAttribute, ivoDataValue);
		});
	}

}
