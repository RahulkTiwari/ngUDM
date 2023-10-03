/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FigiAdditionalXrfParameterTypePostProcessServiceImpl.java
 * Author:	Rushikesh Dedhia
 * Date:	14-June-2019
 *
 ********************************************************************/
package com.smartstreamrdu.service.postprocess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.domain.DomainLookupService;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdAttributeConstantValue;
import com.smartstreamrdu.util.DataSourceConstants;
import com.smartstreamrdu.util.EventListenerConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Dedhia
 * 
 * This class is responsible for handling the additionalXrfAttribute for figi records.
 * This class will ensure that the relationship/dependency between multi-listed figi records 
 * for logic around additionalXrfParameterType are catered to.
 * NOTE:This class provides services which is specific to sdData only.
 */
@Component
@Slf4j
@Order(2)
public class FigiAdditionalXrfParameterTypePostProcessServiceImpl extends AbstractDataContainerPostProcessorService {

	private static final Logger _logger = LoggerFactory.getLogger(FigiAdditionalXrfParameterTypePostProcessServiceImpl.class);
	
	@Autowired
	private NormalizedValueService normalizedValueServiceImpl;
	
	@Autowired
	private DomainLookupService domainService;
	
	public static final String DEFAULT_KEY="Default";
	public static final String SEC_TYPE_TECHNICAL = "Technical";
	

	@Override
	public boolean isServiceApplicable(String dataSource, List<DataContainer> dataContainers) throws UdmTechnicalException {
		Objects.requireNonNull(dataSource, "DataSource cannot null.");

		// This post processor is specific for records of figi data source.
		return DataSourceConstants.FIGI_DS.equals(dataSource);
	}

	@Override
	public void postProcessContainers(String dataSource, List<DataContainer> dataContainers) throws UdmTechnicalException {

		if (CollectionUtils.isEmpty(dataContainers) || Objects.isNull(dataSource)) {
			return;
		}

		for (DataContainer dataContainer : dataContainers) {
			
			populateAdditionaParametersForFigi(dataSource, dataContainer);
		}
	}

	private void populateAdditionaParametersForFigi(String datasource, DataContainer container) {
		
		// This work-flow is valid for only INS containers or for bbgMarketSectorDes == Equity .
		 
		if (!container.getLevel().equals(DataLevel.INS)) {
			return;
		}
		
		String bbgMarketSectorDesValue = container.getHighestPriorityValue(InstrumentAttrConstant.BBG_MARKET_SECTOR_DES);
		
		if(bbgMarketSectorDesValue!=null && !SdAttributeConstantValue.BBG_MARKET_SECTOR_EQUITY.equalsIgnoreCase(bbgMarketSectorDesValue)) {
			return;
		}
		
		

		// Get all active securities.
		List<DataContainer> activeSecurityContainer = getActiveSecurities(datasource, container);
		
		Objects.requireNonNull(activeSecurityContainer);

		Map<String, List<DataContainer>> groupedFigiSecurities = groupFigiSecuritiesBasedOnBbgIdBbUniqueAndExchangeCode(activeSecurityContainer);
		
		for (Entry<String, List<DataContainer>> entry : groupedFigiSecurities.entrySet()) {
			String uniqueKey = entry.getKey();
			List<DataContainer> dataContainers = entry.getValue();
			
			_logger.debug("Evaluating additionalXrfAttributes for figi securities {} with unique grouping key : {}", dataContainers, uniqueKey);
			
			evaluateSecurities(datasource, dataContainers);
		}

	}

	/**
	 *  Evaluate the given securities based on the logic for
	 *  FIGI additionaXrfParameterType handling for Security (Composite), etc.
	 * @param datasource
	 * @param dataContainers
	 */
	private void evaluateSecurities(String datasource, List<DataContainer> dataContainers) {
		String domainSource = getDomainSourceFromDataSource(datasource);
        String rduDomain = DataAttributeFactory.getRduDomainForDomainDataAttribute(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE);
		
        //Deciding whether to update xrfAdditionParameter or not
		if(shouldUpdateXrfAdditionalParameterType(dataContainers, domainSource, rduDomain)) {
			dataContainers.forEach(this::updateXrfAdditionalParameterValue);
		}
		
	}

	/**
	 * This method decides whether we should update xrfAdditional parameter or not.
	 * @param dataContainers
	 * @param domainSource
	 * @param rduDomain
	 * @return
	 */
	private boolean shouldUpdateXrfAdditionalParameterType(List<DataContainer> dataContainers, String domainSource,
			String rduDomain) {
		for (DataContainer con : dataContainers) {
			// If RDU lock applied on additionalXrfParameterType then we should not update
			// additionalXrfParameterType for any of the securities
			if (isRduLockApplied(con, SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE)) {
				return false;
			}
			
			DomainType domainData = con.getHighestPriorityValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE);
			// It means at least one security already has Default .Hence no need to mark
			// another as Default.
			if (domainData.getNormalizedValue() != null
					&& domainData.getNormalizedValue().equals(EventListenerConstants.DEFAULT)) {
				return false;
			}

			DomainType defaultDomain = new DomainType(domainData.getVal(),
					domainData.getVal2() + EventListenerConstants.SEPARATOR_DEFAULT, null, domainData.getDomain());
			Serializable normalizedValue = domainService.getPatternMatchNormalizedValueForDomainValue(defaultDomain,
					domainSource, rduDomain);
			// For all special cases [composite figi & similar one] we have added domain map
			// with val2 = val2+"-Default".If this mapping is not present
			// it means that security is not a figi special case. Hence it is normal
			// security..no need to override another security to Default.
			if (normalizedValue == null) {
				return false;
			}
		}
		// If none of the securities as Default then we need to override value for all
		// special case securities.
		return true;
	}
	
	/**
	 * @param con
	 * @param additionalXrfParameterType
	 */
	private boolean isRduLockApplied(DataContainer con, DataAttribute additionalXrfParameterType) {
		DomainType value = (DomainType) con.getAttributeValueAtLevel(LockLevel.RDU, additionalXrfParameterType);
		return value!=null && value.getNormalizedValue()!=null;
	}

	/**
	 * @param dataSource
	 * @return
	 */
	protected String getDomainSourceFromDataSource(String dataSource) {

		try {
			return domainService.getDomainSourceFromDataSource(dataSource);
		} catch (Exception e) {
			log.error("Following error occured while fetching domainSource for dataSource : {}", dataSource, e);
			return null;
		}

	}
	/**
	 * @param xrfAdditionalAttrDefault
	 * @param con
	 */
	private void updateXrfAdditionalParameterValue(DataContainer con) {
		DomainType xrfAdditionalType = con.getHighestPriorityValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE);
		xrfAdditionalType.setVal2(xrfAdditionalType.getVal2() +EventListenerConstants.SEPARATOR_DEFAULT); 
	}

	/**
	 *  Groups the FIGI securities on the basis of a unique key generated by
	 *  concatenating bbgIdBbUnique and exchangeCode.
	 * @param activeSecurityContainer
	 * @return
	 */
	private Map<String, List<DataContainer>> groupFigiSecuritiesBasedOnBbgIdBbUniqueAndExchangeCode(List<DataContainer> activeSecurityContainer) {
		
		Map<String, List<DataContainer>> groupedFigiSecurities = new HashMap<>();
		
		for (DataContainer dataContainer : activeSecurityContainer) {
			String uniqueKey = getUniqueKeyForContainer(dataContainer);
			
			if (groupedFigiSecurities.containsKey(uniqueKey)) {
				groupedFigiSecurities.get(uniqueKey).add(dataContainer);
			} else {
				List<DataContainer> tempList = new ArrayList<>();
				tempList.add(dataContainer);
				groupedFigiSecurities.put(uniqueKey, tempList);
			}
			
		}
		return groupedFigiSecurities;
	}

	/**
	 *  Returns a unique key for the given FIGI security which is a combination of 
	 *  bbgCompositeIdBbGlobalValue and exchangeCode
	 * @param dataContainer
	 * @return
	 */
	private String getUniqueKeyForContainer(DataContainer dataContainer) {
		
		String bbgCompositeIdBbGlobalValue = dataContainer.getHighestPriorityValue(SecurityAttrConstant.BBG_COMPOSITE_ID_BB_GLOBAL);
		Serializable exchangeCodeDomainData = dataContainer.getHighestPriorityValue(SecurityAttrConstant.EXCHANGE_CODE);
		String exchangeCodeNormalizedValue = (String) normalizedValueServiceImpl.getNormalizedValueForDomainValue(SecurityAttrConstant.EXCHANGE_CODE, (DomainType) exchangeCodeDomainData, DataSourceConstants.FIGI_DS);
		return bbgCompositeIdBbGlobalValue+"|"+exchangeCodeNormalizedValue;
	}
	
	/**
	 *  Returns all the active securities present in the given data container.
	 * @param datasource
	 * @param container
	 * @return
	 */
	private List<DataContainer> getActiveSecurities(String datasource, DataContainer container) {	        
		List<DataContainer> securityContainers = container.getChildDataContainers(DataLevel.SEC);
		List<DataContainer> activeSecurityContainer = new ArrayList<>();
		DataAttribute secStatusDataAttribute = DataStorageEnum.SD.getAttributeByName(DataAttributeFactory.getStatusFlagForLevel(DataLevel.SEC));
		for (DataContainer dataContainer : securityContainers) {
			DomainType domainType = dataContainer.getHighestPriorityValue(secStatusDataAttribute);
			DomainType rduSecType = dataContainer.getHighestPriorityValue(SecurityAttrConstant.RDU_SECURITY_TYPE);
			String normalizedValue = domainType.getNormalizedValue();
			String rduSecTypeValue = rduSecType.getNormalizedValue();
			if (StringUtils.isEmpty(normalizedValue)) {
				normalizedValue = (String) normalizedValueServiceImpl
						.getNormalizedValueForDomainValue(secStatusDataAttribute, domainType, datasource);
			}
			if (DomainStatus.ACTIVE.equals(normalizedValue) && !rduSecTypeValue.equals(SEC_TYPE_TECHNICAL)) {
				activeSecurityContainer.add(dataContainer);
			}
		}
		return activeSecurityContainer;
	}

}
