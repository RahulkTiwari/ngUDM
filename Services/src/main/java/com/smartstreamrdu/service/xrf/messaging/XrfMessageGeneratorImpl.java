/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfMessageGeneratorImpl.java
 * Author: Rushikesh Dedhia
 * Date: May 11, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.xrf.CrossRefBaseDocument;
import com.smartstreamrdu.commons.xrf.CrossRefChangeEntity;
import com.smartstreamrdu.commons.xrf.XrfAttributeValue;
import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.commons.xrf.XrfProcessMode;
import com.smartstreamrdu.commons.xrf.XrfRuleAttributeDef;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.service.staticdata.StaticDataService;
import com.smartstreamrdu.service.xrf.config.CrossRefRuleConfigService;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.EventListenerConstants;
import com.smartstreamrdu.util.SdDataAttributeConstant;

/**
 * @author Dedhia
 * NOTE:This class provides services which is specific to sdData only.
 */
@Component
public class XrfMessageGeneratorImpl implements XrfMessageGenerator {

	/**
	 * 
	 */
	private static final String UUI_PROGRAM_NAME = "NG-EquityWeb";
	private static final DataAttribute DD_EXCHANGE_REF_MATCHING_GROUP_EXCHANGE_CODE_DATA_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevel("matchingGroupExchangeCode", DataLevel.EXCHANGE_CODES);
	private static final DataAttribute MINOR_TO_MAJOR_CURRENCY_CODE_DATA_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevel("minorToMajorCurrencyCode", DataLevel.CURRENCY_CODES);

	static final Logger logger = LoggerFactory.getLogger(XrfMessageGeneratorImpl.class);
	
	@Autowired
	private NormalizedValueService normalizedService;
	
	@Autowired
	private CrossRefRuleConfigService configService;
	
	@Autowired
	private StaticDataService staticDataService;
	
	private static final DataAttribute securityIdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.securityId, DataLevel.SEC);
	private static final DataAttribute instrumentIdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.instrumentId, DataLevel.INS);
	private static final DataAttribute insertDate = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.insertDate, DataLevel.Document);
	private static final DataAttribute updateDate = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.updateDate, DataLevel.Document);
	
	@Override
	public XrfMessage generateMessage(ChangeEventInputPojo changeEventInput, LocalDateTime staticDataUpdateDate) {
		Objects.requireNonNull(changeEventInput, "changeEventInput should be populated");
		try {
			return generateXrfMessageFromEventInputPojo(changeEventInput, staticDataUpdateDate);
		} catch (Exception exception) {
			logger.error("Following error occured while generating XRF message for object with ID : {}",
					changeEventInput.getPostChangeContainer(), exception);
			return null;
		}
	}

	/**
	 * @param changeEventInput
	 * @param staticDataUpdateDate 
	 * @return
	 */
	private XrfMessage generateXrfMessageFromEventInputPojo(ChangeEventInputPojo changeEventInput, final LocalDateTime staticDataUpdateDate) {
		DataContainer postChangeContainer = changeEventInput.getPostChangeContainer();
		Objects.requireNonNull(changeEventInput.getPostChangeContainer(),
				"changeEventInput.getPostChangeContainer should be populated");
		//XRF message should be generated only from SD collection updates.
		//Ignore the other levels like IVO_INS,LE 
		if(!DataLevel.INS.equals(postChangeContainer.getLevel())){
			return null;
		}
		List<DataContainer> childContainers = postChangeContainer.getChildDataContainers(DataLevel.SEC);
		DomainType dataSourceValue = (DomainType) postChangeContainer.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.dataSource,
						DataLevel.Document));
		
		if(CollectionUtils.isEmpty(childContainers)){
			logger.error("No securities present for instrument with  documentId : {}", postChangeContainer.get_id());
			return null;
		}

		
		List<XrfRuleAttributeDef> secLevelAttributes = configService.getAttributesBySdLevel(DataLevel.SEC);
		List<XrfRuleAttributeDef> insLevelAttributes = configService.getAttributesBySdLevel(DataLevel.INS);

		Map<String, XrfAttributeValue> crossRefInsAttributeMapPostChange = getCrossRefAttributeMap(postChangeContainer,dataSourceValue,insLevelAttributes);
		XrfMessage message = new XrfMessage();
		DataContainer pre = changeEventInput.getPreChangeContainer();
		if(pre != null && pre.getDataContainerContext() != null) {
			message.setTraceInfo(pre.getDataContainerContext().getTraceInfo());
		}
		XrfProcessMode xrfProcessMode = (XrfProcessMode) changeEventInput.getFromMessageContext(EventListenerConstants.XRF_PROCESS_MODE);
		childContainers.forEach(secContainer -> message
					.addCrossRefChangeEntity(createCrossRefChangeEntity(secContainer, postChangeContainer,
							dataSourceValue, crossRefInsAttributeMapPostChange, secLevelAttributes,staticDataUpdateDate, xrfProcessMode)));
		return message;
	}

	/**
	 * @param secContainer
	 * @param postChangeContainer
	 * @param dataSourceValue
	 * @param crossRefInsAttributeMapPostChange 
	 * @param secLevelAttributes 
	 * @param xrfProcessMode 
	 * @return
	 */
	private CrossRefChangeEntity createCrossRefChangeEntity(DataContainer secContainer, DataContainer postChangeContainer,
			DomainType dataSourceValue, Map<String, XrfAttributeValue> crossRefInsAttributeMapPostChange, List<XrfRuleAttributeDef> secLevelAttributes,
			LocalDateTime staticDataUpdateDate, XrfProcessMode xrfProcessMode) {
		Objects.requireNonNull(secContainer,"secContainer should be populated");
		Objects.requireNonNull(postChangeContainer,"postChangeContainer should be populated");
		
		CrossRefBaseDocument postChangeBase = new CrossRefBaseDocument();
		addBasicInformationToCrossRefBaseDocument(postChangeBase, secContainer, postChangeContainer, dataSourceValue, staticDataUpdateDate, xrfProcessMode);
		Map<String, XrfAttributeValue> crossRefAttributesMap=new HashMap<>();
		crossRefAttributesMap.putAll(crossRefInsAttributeMapPostChange);
		crossRefAttributesMap.putAll(getCrossRefAttributeMap(secContainer, dataSourceValue,secLevelAttributes));
		postChangeBase.setCrossRefAttributes(crossRefAttributesMap);
		CrossRefChangeEntity changeEntity = new CrossRefChangeEntity();
		changeEntity.setPostChangeDocument(postChangeBase);
		return changeEntity;
	}

	/**
	 * @param changeBaseDocument
	 * @param secDataContainer
	 * @param changeContainer
	 * @param dataSource 
	 * @param xrfProcessMode 
	 */
	private void addBasicInformationToCrossRefBaseDocument(CrossRefBaseDocument changeBaseDocument, DataContainer secDataContainer, DataContainer changeContainer,DomainType dataSource
			,LocalDateTime staticDataUpdateDate, XrfProcessMode xrfProcessMode) {

		String securityIdValue = secDataContainer.getHighestPriorityValue(securityIdDataAttribute);
		String instrumentIdValue = changeContainer.getHighestPriorityValue(instrumentIdDataAttribute);
		LocalDateTime insDate = changeContainer.getHighestPriorityValue(insertDate);
		LocalDateTime upDate = changeContainer.getHighestPriorityValue(updateDate);
		String documentId =  changeContainer.get_id();

		changeBaseDocument.set_instrumentId(instrumentIdValue);
		changeBaseDocument.set_securityId(securityIdValue);
		changeBaseDocument.set_documentId(documentId);
		changeBaseDocument.setLastModifiedDate(getTimestamp(insDate, upDate,staticDataUpdateDate));
		DataAttribute secStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.SEC),DataLevel.SEC);
		populateStatusAttribute(secDataContainer, secStatusAttribute, dataSource, changeBaseDocument);
		DataAttribute insStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(DataLevel.INS),DataLevel.INS);
		populateStatusAttribute(changeContainer, insStatusAttribute, dataSource, changeBaseDocument);
		populateDataApprovedByAndXrfProcessMode(changeContainer,secDataContainer,changeBaseDocument, xrfProcessMode);
		
		DomainType sectype = secDataContainer.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		if (sectype != null) {
			changeBaseDocument.setRduSecurityType(sectype.getNormalizedValue());
		}
		
	}

	/**
	 * @param changeContainer
	 * @param changeBaseDocument
	 * @param xrfProcessMode 
	 */
	private void populateDataApprovedByAndXrfProcessMode(DataContainer changeContainer, DataContainer secDataContainer,
			CrossRefBaseDocument changeBaseDocument, XrfProcessMode xrfProcessMode) {
		DataContainerContext containerContext = changeContainer.getDataContainerContext();
		if (UUI_PROGRAM_NAME.equals(containerContext.getProgram())
				&& (secDataContainer.hasContainerChanged() || changeContainer.hasContainerChanged())) {
			changeBaseDocument.setDataApprovedBy(containerContext.getUpdatedBy());
			setXrfProcessMode(changeBaseDocument, xrfProcessMode);
		}
	}

	private void setXrfProcessMode(CrossRefBaseDocument changeBaseDocument, XrfProcessMode xrfProcessMode) {
		if (xrfProcessMode != null) {
			changeBaseDocument.setXrfProcessMode(xrfProcessMode);
		} else {
			changeBaseDocument.setXrfProcessMode(XrfProcessMode.RDUEDIT);
		}
	}

	/**
	 * @param insDate
	 * @param upDate
	 * @param staticDataUpdateDate 
	 * @return
	 */
	private Timestamp getTimestamp(LocalDateTime insDate, LocalDateTime upDate, LocalDateTime staticDataUpdateDate) {
		Timestamp staticDataUpdateTs = getTimestamp(staticDataUpdateDate);
		if(staticDataUpdateTs!=null){
			return staticDataUpdateTs;
		}
		Timestamp updDate = getTimestamp(upDate);
		return updDate != null ? updDate : getTimestamp(insDate);
	}
	
	private Timestamp getTimestamp(LocalDateTime date) {
		return date!=null?Timestamp.valueOf(date):null;
	}
	
	/**
	 * @param dataContainer
	 * @param statusAttribute
	 * @param dataSource
	 * @param changeBaseDocument
	 */
	private void populateStatusAttribute(DataContainer dataContainer, DataAttribute statusAttribute,
			DomainType dataSource, CrossRefBaseDocument changeBaseDocument) {
		DomainType value = dataContainer.getHighestPriorityValue(statusAttribute);
		String normalizedValueForDomainValue = getNormalizedDomainValue(statusAttribute, value,
				dataSource.getVal());
		if (statusAttribute.getAttributeLevel().equals(DataLevel.INS)) {
			changeBaseDocument.setInstrumentStatus(normalizedValueForDomainValue);
		} else if (statusAttribute.getAttributeLevel().equals(DataLevel.SEC)) {
			changeBaseDocument.setSecurityStatus(normalizedValueForDomainValue);
		}
	}

	/**
	 *  Method to handle and populate domain data type attributes in the XrfMessage.
	 * @param dataSource
	 * @param attribute
	 * @param dataAttribute
	 * @param value
	 * @return
	 */
	private Serializable handleDomainTypeDataAttributes(DomainType dataSource, XrfRuleAttributeDef attribute,
			DataAttribute dataAttribute, DomainType value) {
		Serializable normalizedDomainValue = null;
		if (dataSource != null) {
			normalizedDomainValue = getNormalizedDomainValue(dataAttribute, value, dataSource.getVal());
		}
		if (normalizedDomainValue != null) {
			if (attribute.getSdAttributeName().equals(CrossRefConstants.EX_CODE)) {
				normalizedDomainValue = getStaticDataByCode(normalizedDomainValue, DataLevel.EXCHANGE_CODES,
						DD_EXCHANGE_REF_MATCHING_GROUP_EXCHANGE_CODE_DATA_ATTRIBUTE);
			} else if (attribute.getSdAttributeName().equals(CrossRefConstants.TRADE_CURR)) {
				normalizedDomainValue = getStaticDataByCode(normalizedDomainValue, DataLevel.CURRENCY_CODES,
						MINOR_TO_MAJOR_CURRENCY_CODE_DATA_ATTRIBUTE);
			}
		}
		return normalizedDomainValue;
	}

	private Serializable getStaticDataByCode(Serializable code, DataLevel staticDataLevel, DataAttribute dataAttribute) {
		Serializable dataAttributeValue=null;
		try {
			Serializable dataByCode = staticDataService.getDataByCode(staticDataLevel, dataAttribute,code);
			
			String stringValue = dataAttribute.stringValue(dataByCode);
			dataAttributeValue=StringUtils.isNotBlank(stringValue)?stringValue:code;
		} catch (Exception e) {
			logger.error("Following error occured while fetching the Matching group exchange code for normalized value {} : {}", code, e.getMessage());
		}
		return dataAttributeValue;
	}
	
	/**
	 * @param changeContainer
	 * @param xrfRuleAttributes
	 * @param dataSource 
	 * @return
	 */
	private Map<String, XrfAttributeValue> getCrossRefAttributeMap(DataContainer changeContainer,
			DomainType dataSource,List<XrfRuleAttributeDef> xrfRuleAttributes) {
		if (CollectionUtils.isEmpty(xrfRuleAttributes)) {
			return Collections.emptyMap();
		}
		final Map<String, XrfAttributeValue> xrfAttributeMap = new HashMap<>();
		xrfRuleAttributes.forEach(xrfRuleAttributeDef -> xrfAttributeMap.put(xrfRuleAttributeDef.getSdAttributeName(),
				createXrfAttributeValue(xrfRuleAttributeDef, changeContainer, dataSource)));
		return xrfAttributeMap;
	}

	/**
	 * @param xrfRuleAttributeDef
	 * @param changeContainer
	 * @param dataSource
	 * @return
	 */
	private XrfAttributeValue createXrfAttributeValue(XrfRuleAttributeDef xrfRuleAttributeDef,
			DataContainer changeContainer, DomainType dataSource) {
		DataAttribute dataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(xrfRuleAttributeDef.getSdAttributeName(), DataLevel.valueOf(xrfRuleAttributeDef.getSdAttributeLevel()));
		Serializable value =  changeContainer.getHighestPriorityValue(dataAttribute);
		XrfAttributeValue newAttribute = new XrfAttributeValue();

		newAttribute.setName(xrfRuleAttributeDef.getSdAttributeName());
		
		String dataSourceVal = dataSource == null ? null : dataSource.getVal();
		
		if (value instanceof DomainType) {
			value = handleDomainTypeDataAttributes(dataSource, xrfRuleAttributeDef, dataAttribute, (DomainType)value);
		}
		newAttribute.setValue(value);
		newAttribute.setDataSourceId(dataSourceVal);
		return newAttribute;
	}

	private String getNormalizedDomainValue(DataAttribute dataAttribute, DomainType value,
			String dataSourceVal) {
		String normalizedValue = value.getNormalizedValue();
		return normalizedValue != null ? normalizedValue
				: (String) normalizedService.getNormalizedValueForDomainValue(dataAttribute, value, dataSourceVal);
	}
	
	
	/**
	 * This method generates reprocessing xrf message
	 */
	@Override
	public XrfMessage generateReprocessingXrfMessage(CrossRefBaseDocument doc) {
		XrfMessage message = new XrfMessage();
		CrossRefChangeEntity entity = new CrossRefChangeEntity();
		CrossRefBaseDocument document = new CrossRefBaseDocument();
		document.setSecurityRduId(doc.getSecurityRduId());
		document.setInstrumentRduId(doc.getInstrumentRduId());
		document.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
		document.setXrfProcessMode(XrfProcessMode.XRFREPROCESSING);
		entity.setPostChangeDocument(document);
		message.addCrossRefChangeEntity(entity);
		return message;
	}


}
