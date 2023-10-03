/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: VfsOpenFigiMessageGeneratorImpl.java
 * Author: Rushikesh Dedhia
 * Date: Jul 10, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.openfigi;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;
import com.smartstreamrdu.commons.openfigi.SuccessfulRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.persistence.domain.OpenFigiRequestMetrics;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.service.staticdata.StaticDataService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.OpenFigiContants;
import com.smartstreamrdu.util.VfsSystemPropertiesConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Dedhia
 * NOTE:This class provides services which is specific to sdData only.
 */
@Component
@Slf4j
public class VfsOpenFigiMessageGeneratorImpl implements VfsOpenFigiMessageGenerator {

	private static final String MATCHING_MIC = "MATCHINGMIC";

	private static final long serialVersionUID = 5675539402827394298L;
	
	private static final String DATASOURCE = "dataSource";
	
	@Autowired
	private NormalizedValueService normalizedService;
	
	@Autowired
	private OpenFigiIdentifierMapper openFigiIdentifierMapperService;
	
	@Autowired
	private transient OpenFigiRequestRuleOrderingService ruleOrderingService;
	
	private List<String> openFigiIdentifiers = Arrays.asList(OpenFigiContants.SEDOL, OpenFigiContants.ISIN, OpenFigiContants.CUSIP, OpenFigiContants.EX_CODE, OpenFigiContants.TRADE_CURR);
	
	@Autowired
	private transient StaticDataService staticDataService;
	
    @Autowired
    private transient UdmSystemPropertiesCache systemCache;
    
    @Autowired
    private transient VfsOpenFigiServicesUtil figiServicesUtil;
      
    private transient Object ruleToRuleAttributesMapMutex = new Object();
    
    private EnumMap<OpenFigiRequestRuleEnum, List<String>> ruleToRuleAttributesMap = new EnumMap<>(OpenFigiRequestRuleEnum.class);
	
	public void initialize(){
		if(normalizedService == null){
			normalizedService = SpringUtil.getBean(NormalizedValueService.class);
		}
		if(openFigiIdentifierMapperService == null){
			openFigiIdentifierMapperService = SpringUtil.getBean(OpenFigiIdentifierMapper.class);
		}
		if(staticDataService == null){
			staticDataService = SpringUtil.getBean(StaticDataService.class);
		}
		if(ruleOrderingService == null) {
			ruleOrderingService = SpringUtil.getBean(OpenFigiRequestRuleOrderingService.class);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.openfigi.VfsOpenFigiMessaageGenerator#generateVfsOpenFigiMessaage(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public VfsFigiRequestMessage generateVfsOpenFigiMessaage(DataContainer childContainer, DataContainer parentContainer, VfsFigiRequestTypeEnum requestType) {
		initialize();
		return generateOpenFigiIdentifiersMessage(childContainer, parentContainer, requestType);	
		
	}

	/**
	 * @param childContainer
	 * @param parentContainer
	 * @return
	 */
	private VfsFigiRequestMessage generateOpenFigiIdentifiersMessage(DataContainer childContainer, DataContainer parentContainer, VfsFigiRequestTypeEnum requestType) {

		final VfsFigiRequestMessage vfsFigiRequestMessage = new VfsFigiRequestMessage();
		
		openFigiIdentifiers.forEach(attributeName ->
			populateOpenFigiIdentifiersObject(attributeName,vfsFigiRequestMessage,childContainer,parentContainer));
		
		// initialize the value of OpenFigiCompositeRuleEnum as DEFAULT
		vfsFigiRequestMessage.setOpenFigiRequestRuleEnum(OpenFigiRequestRuleEnum.DEFAULT); 
		vfsFigiRequestMessage.setPrimarySourceDocId(parentContainer.get_id());
		vfsFigiRequestMessage.setPrimarySourceSecurityId(childContainer.getHighestPriorityValue(SecurityAttrConstant._SECURITY_ID));
		vfsFigiRequestMessage.setRetryCount(-1);
		vfsFigiRequestMessage.setOpenFigiRequestOrderType(ruleOrderingService.getRequestOrderTypeForMic(vfsFigiRequestMessage.getExchangeCode()));
		vfsFigiRequestMessage.setRequestType(requestType);
		vfsFigiRequestMessage.setCreateTime(LocalDateTime.now());
		DomainType dataSource = parentContainer.getHighestPriorityValue(SdDataAttrConstant.DATA_SOURCE);		
		if(dataSource!=null) {
			vfsFigiRequestMessage.setDataSource(dataSource.getVal());
			vfsFigiRequestMessage.setSecurityStatus(getSecurityStatusValue(childContainer,dataSource.getVal()));
			vfsFigiRequestMessage.setInstrumentStatus(getInstrumentStatusValue(parentContainer,dataSource.getVal()));
		}				
		return vfsFigiRequestMessage;
	}
	
	/**
	 * @param childContainer
	 * @param string 
	 * @return
	 */
	private String getSecurityStatusValue(DataContainer childContainer, String dataSource) {
		DomainType statusValue = childContainer.getHighestPriorityValue(SecurityAttrConstant.SECURITY_STATUS);
		String securityStatus = (String) getNormalizedDomainValue(SecurityAttrConstant.SECURITY_STATUS, statusValue,dataSource);
		// in case domain mapping not available for the feed value then send
		// securityStatus as Active
		if (securityStatus == null) {
			securityStatus = DomainStatus.ACTIVE;
		}
		return securityStatus;
	}
	
	private String getInstrumentStatusValue(DataContainer parentContainer, String dataSource) {
        DomainType statusValue = parentContainer.getHighestPriorityValue(InstrumentAttrConstant.INSTRUMENT_STATUS);
        String instrumentStatus = (String) getNormalizedDomainValue(InstrumentAttrConstant.INSTRUMENT_STATUS, statusValue,dataSource);
        // in case domain mapping not available for the feed value then send
        // instrumentStatus as Active
        if (instrumentStatus == null) {
        	instrumentStatus = DomainStatus.ACTIVE;
        }
        return instrumentStatus;
    }

	
	/**
	 * @param attributeName
	 * @param openFigiIdentifiersObject
	 * @param childContainer
	 * @param parentContainer
	 */
	@SuppressWarnings("unchecked")
	private void populateOpenFigiIdentifiersObject(String attributeName, VfsFigiRequestMessage openFigiIdentifiersObject,
			DataContainer childContainer, DataContainer parentContainer) {
		DataAttribute dataAttribute = DataStorageEnum.SD.getAttributeByName(attributeName);
		DataContainer containerToBeUsedForAttribute = DataLevel.INS.equals(dataAttribute.getAttributeLevel())?parentContainer:childContainer;
		
		DomainType dataSourceValue = (DomainType) parentContainer.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getAttributeByNameAndLevel(DATASOURCE, DataLevel.Document));
		
		DataValue<Serializable> dataValue = (DataValue<Serializable>) containerToBeUsedForAttribute.getAttributeValue(dataAttribute);
		if (dataValue == null || dataValue.getValue()==null) {
			return;
		}
		Serializable value = dataValue.getValue();
		
		if (value instanceof DomainType) {
			value=getNormalizedDomainValue(dataAttribute, (DomainType) value, dataSourceValue.getVal());
		}
		if (attributeName.equals(OpenFigiContants.EX_CODE)) {
			// Special handling for exchangeCode as we want to send the mic for the normalized exchange code.
			value = checkAndReturnMicForExchangeCode(value);
			openFigiIdentifiersObject.resetMicIndex(); // initialize the value of micIndex as -1
			openFigiIdentifiersObject.setDependentMics(new ArrayList<>());
		}
		
		openFigiIdentifierMapperService.setFigiIdentifierValueByName(openFigiIdentifiersObject, attributeName, (String) value);
	}

	private Serializable getNormalizedDomainValue(DataAttribute dataAttribute, DomainType value,
			String dataSourceVal) {
		String normalizedValue = value.getNormalizedValue();
		return normalizedValue != null ? normalizedValue
				: normalizedService.getNormalizedValueForDomainValue(dataAttribute, value, dataSourceVal);
	}

	/**
	 * @param value
	 */
	private Serializable checkAndReturnMicForExchangeCode(Serializable value) {
		Serializable micValue = value;
		if (micValue != null) {
			try {
				
				DataAttribute attributeByNameAndLevel = DataAttributeFactory.getAttributeByNameAndLevel("mic", DataLevel.EXCHANGE_CODES);
				
				Serializable dataByCode =  staticDataService.getDataByCode(DataLevel.EXCHANGE_CODES, attributeByNameAndLevel,value);
				
				String stringValue = attributeByNameAndLevel.stringValue(dataByCode);
				
				if(StringUtils.isNotBlank(stringValue)){
					micValue =  dataByCode;
				}
				
			} catch (Exception e) {
				log.error("Exception in checkAndReturnMicForExchangeCode", e);
			}
		}
		return micValue;
	}

	/**
	 * Generates OpenFigi Message from OpenFigiRequestMetrics
	 * If successfulRequest is available in OpenFigiRequestMetrics, will populate OpenFigiRequestRule based on last successful Request else will be set to DEFAULT
	 * @param metrics
	 * @param requestType
	 * @param retryCount
	 */
	@Override
	public VfsFigiRequestMessage generateVfsOpenFigiMessageFromRequestMetrics(OpenFigiRequestMetrics metrics, VfsFigiRequestTypeEnum requestType, int retryCount) {
        
        checkAndPopulateRuleToValue();
       
        SuccessfulRequestMessage message = JsonConverterUtil
                .convertFromJson(metrics.getSuccessfulRequest(), SuccessfulRequestMessage.class);

        if(message != null) {
            Map<String, String> idTypeMap = new HashMap<>();
            idTypeMap.put(OpenFigiContants.FIGI_ISIN, OpenFigiContants.ISIN);
            idTypeMap.put(OpenFigiContants.FIGI_SEDOL, OpenFigiContants.SEDOL);
            idTypeMap.put(OpenFigiContants.FIGI_CUSIP, OpenFigiContants.CUSIP);
            idTypeMap.put(OpenFigiContants.FIGI_MATURITY, OpenFigiContants.FIGI_MATURITY);
    
            String idType = idTypeMap.get(message.getIdType());
            String mic = message.getMicCode() != null ? OpenFigiContants.EX_CODE : null;
            String currency = message.getCurrency() != null ? OpenFigiContants.TRADE_CURR : null;
            //Compares mic available in success request with original parameters, if same populate null else return mic from success request
            String matchingMic = mic != null && !message.getMicCode().equals(metrics.getOriginalRequest().getExchangeCode()) ? message.getMicCode() : null;
            
            //These are mandatory parameters required to form a rule, when all parameters containing in this list are matched applicable rule is returned.
            List<String> requestParam = new ArrayList<>();
            CollectionUtils.addIgnoreNull(requestParam, idType);
            CollectionUtils.addIgnoreNull(requestParam, mic);
            CollectionUtils.addIgnoreNull(requestParam, currency);
    
            return enrichRequest(metrics, requestType, retryCount, requestParam, matchingMic);
        }
        return enrichRequest(metrics, requestType, retryCount, null, null);
    }

    private VfsFigiRequestMessage enrichRequest(OpenFigiRequestMetrics metrics, VfsFigiRequestTypeEnum requestType, int retryCount, List<String> requestParam, String matchingMic) {
        VfsFigiRequestMessage enrichedRequest = new VfsFigiRequestMessage();
        enrichedRequest.setPrimarySourceDocId((String) metrics.getPrimarySourceReferenceId().getDocumentId());
        enrichedRequest.setPrimarySourceSecurityId(metrics.getPrimarySourceReferenceId().getObjectId());
        enrichedRequest.setIsin(metrics.getOriginalRequest().getIsin());
        enrichedRequest.setSedol(metrics.getOriginalRequest().getSedol());
        enrichedRequest.setCusip(metrics.getOriginalRequest().getCusip());
        enrichedRequest.setTradeCurrencyCode(metrics.getOriginalRequest().getTradeCurrencyCode());
        enrichedRequest.setExchangeCode(metrics.getOriginalRequest().getExchangeCode());
        enrichedRequest.setCusip(metrics.getOriginalRequest().getCusip());
        enrichedRequest.setOpenFigiRequestRuleEnum(requestParam != null ? getMatchingEnum(requestParam, ruleToRuleAttributesMap, matchingMic, enrichedRequest) : OpenFigiRequestRuleEnum.DEFAULT);
        enrichedRequest.setRequestType(requestType);
        enrichedRequest.setRetryCount(retryCount);
        enrichedRequest.setDataSource(metrics.getDataSource());
        enrichedRequest.setOpenFigiRequestOrderType(ruleOrderingService.getRequestOrderTypeForMic(metrics.getOriginalRequest().getExchangeCode()));
        enrichedRequest.setSecurityStatus(DomainStatus.ACTIVE);
        enrichedRequest.setInstrumentStatus(DomainStatus.ACTIVE);
		enrichedRequest.setMaturity(metrics.getOriginalRequest().getMaturity() != null ? updateMaturityDate() : null);
		enrichedRequest.setCreateTime(LocalDateTime.now());
        return enrichedRequest;
        
    }
    
    private List<String> updateMaturityDate() {
		return Arrays.asList((LocalDate.now().minusDays(figiServicesUtil.getFigiRequestParameterMaturityDay())).toString(), null);
	}

	private void checkAndPopulateRuleToValue() {
		if (ruleToRuleAttributesMap.isEmpty()) {
			synchronized (ruleToRuleAttributesMapMutex) {
				if (ruleToRuleAttributesMap.isEmpty()) {
					populateRuleToValueMap();
				}
			}
		}
	}

	/**
	 * Populates ruleToRuleAttributesMap with list of identifiers for each rule(OpenFigiRequestRuleEnum) from VfsSystemProperties
	 */
	private void populateRuleToValueMap() {
		for (OpenFigiRequestRuleEnum rule : OpenFigiRequestRuleEnum.values()) {
			String key = "figi." + rule.name() + ".rule.attributes";
			Optional<String> ruleValues = systemCache.getPropertiesValue(key,
					VfsSystemPropertiesConstant.COMPONENT_VFS, DataLevel.VFS_SYSTEM_PROPERTIES);
			List<String> components = null;
			if (ruleValues.isPresent()) {
				components = Arrays.asList(ruleValues.get().split(","));
				ruleToRuleAttributesMap.put(rule, components);
			}
		}
	}

	/**
	 * Returns OpenFigiRequestRule if all identifiers from successful request matches with a particular RequestRule else returns DEFAULT
	 * @param requestParam
	 * @param ruleToValue
	 * @param matchingMic
	 * @param enrichedRequest
	 * @return
	 */
	private OpenFigiRequestRuleEnum getMatchingEnum(List<String> requestParam,
			EnumMap<OpenFigiRequestRuleEnum, List<String>> ruleToValue, String matchingMic,
			VfsFigiRequestMessage enrichedRequest) {

		for (Map.Entry<OpenFigiRequestRuleEnum, List<String>> entry : ruleToValue.entrySet()) {
			List<String> values = entry.getValue();
			if (requestParam.equals(values)) {
				
				//here it is checking if the rule consist's of MATCHING_MIC or not 
				if (matchingMic == null && !entry.getKey().name().contains(MATCHING_MIC)) {
					return entry.getKey();
				} else if (matchingMic != null && entry.getKey().name().contains(MATCHING_MIC)) {
					/*
					 * if MATCHING_MIC is present populate dependent mic and mic_index
					 *  and select rule containing 
					 */
					List<String> list = Arrays.asList(matchingMic);
					enrichedRequest.setDependentMics(list);
					enrichedRequest.setMicIndex(0);
					return entry.getKey();
				}
			}
		}
		return OpenFigiRequestRuleEnum.DEFAULT;

	}

	@Override
	public VfsFigiRequestMessage generateDefaultOpenFigiMessageFromRequestMetrics(OpenFigiRequestMetrics metrics,
			VfsFigiRequestTypeEnum requestType, int retryCount) {
		return enrichRequest(metrics, requestType, retryCount, null, null);
	}
}
