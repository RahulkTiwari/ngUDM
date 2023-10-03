/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    RduTechnicalSecurityGenerationService.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.postprocess;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerContext.DataContainerContextBuilder;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.inactive.InstrumentInactiveUtil;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This method is evaluate provided insDataContainers & based on conditions it 
 * either creates / inActivates rduTechnicalSecurity.
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
@Order(3)
public class RduTechnicalSecurityGenerationService extends AbstractDataContainerPostProcessorService {

	@Autowired
	@Setter
	private CacheDataRetrieval cache;

	@Autowired
	@Setter
	private NormalizedValueService normalizedValService;
	
	@Autowired
	@Setter
	private RduSecurityTypePopulationService rduSecurityTypePopulationService;
	
	@Override
	public boolean isServiceApplicable(String dataSource, List<DataContainer> dataContainers)
			throws UdmTechnicalException {
		
		String dataSourceLevelFromCode = cache.getDataSourceLevelFromCode(dataSource);
		
		//This Service is currently applicable for INS level feeds only.
		return StringUtils.isNotEmpty(dataSourceLevelFromCode) && dataSourceLevelFromCode.equals(SdDataAttConstant.INSTRUMENT);
	}

	@Override
	public void postProcessContainers(String dataSource, List<DataContainer> dataContainers)
			throws UdmTechnicalException {
		for (DataContainer container : dataContainers) {
			
			//If instrument is InActivated no need to create technical Security
			if (!container.getLevel().equals(DataLevel.INS) || InstrumentInactiveUtil.isInstrumentStatusInActive(dataSource, container)) {
				log.debug("RduTechnicalSecurityGenerationService is not executed as requested instrument is inactive or"
						+ "requested container level {} is not applicable : {}",container.getLevel(),container);
				return;
			}

			List<String> feedSecuritiesStatusList = new ArrayList<>();
			String[] technicalSecurityStatus = new String[1];
			
			List<DataContainer> childDataContainers = container.getAllChildDataContainers();
			populateSecurityStatus(feedSecuritiesStatusList, technicalSecurityStatus, childDataContainers, dataSource);

			//If below mentioned conditions evaluates to true then create technical security
			if (canAddTechnicalSecurity(feedSecuritiesStatusList, technicalSecurityStatus[0], childDataContainers)) {		
				  populateRduTechnicalSecurity(container);	
			
		       } else if (shouldInActivateTechnicalSecurity(feedSecuritiesStatusList, technicalSecurityStatus) ) {
				inActivateRduTechnicalSecurity(childDataContainers);
		       }
			
		}
	}

	/**
	 * If below conditions evaluates to true then InActivate Technical security.
	 * @param feedSecuritiesStatusList
	 * @param technicalSecurityStatus
	 * @return
	 */
	private boolean shouldInActivateTechnicalSecurity(List<String> feedSecuritiesStatusList,
			String[] technicalSecurityStatus) {
		log.debug("Feed {} & technical{} seurities status are",feedSecuritiesStatusList,technicalSecurityStatus);
		//if any feed Security is Active && Technical security is also Active then return true
		//to inactivate technical security.
		return feedSecuritiesStatusList.contains(DomainStatus.ACTIVE) &&		
				StringUtils.isNotEmpty(technicalSecurityStatus[0]) && (DomainStatus.ACTIVE).equals(technicalSecurityStatus[0]);
	}

	/**
	 * This method returns true in following conditions:
	 * 1.If Ins doesn't contains any child Containers
	 * 2.If all feed securities are InActive & There is no active technical Security
	 * @param feedSecuritiesStatusList
	 * @param technicalSecurityStatus
	 * @param childDataContainers
	 * @return
	 */
	private boolean canAddTechnicalSecurity(List<String> feedSecuritiesStatusList, String technicalSecurityStatus,
			List<DataContainer> childDataContainers) {
		       
		       //No childConatiner is present
		return (CollectionUtils.isEmpty(childDataContainers)) ||
		        
				//All feed securities are Inactive && No Active technical security present
				(!feedSecuritiesStatusList.contains(DomainStatus.ACTIVE)  && !DomainStatus.ACTIVE.equals(technicalSecurityStatus));		 
	}

	/**
	 * This method populates feedSecurities Status & technicalSecurityStatus
	 * @param feedSecuritiesStatusList
	 * @param technicalSecurityStatus
	 * @param childDataContainers
	 * @param dataSource
	 */
	private void populateSecurityStatus(List<String> feedSecuritiesStatusList, String[] technicalSecurityStatus,
			List<DataContainer> childDataContainers, String dataSource) {
		for (DataContainer childContainer : childDataContainers) {
			if (isTechnicalSecurity(childContainer)) {
				technicalSecurityStatus[0] = getTechnicalSecurityStatus(childContainer);
			} else {
				String feedSecurityStatus = getFeedSecurityStatus(childContainer, dataSource);
				feedSecuritiesStatusList.add(feedSecurityStatus);
			}
		}

	}

	/**
	 * This method returns status value for requested feed Security.
	 * @param childContainer
	 * @param dataSource
	 * @return
	 */
	private String getFeedSecurityStatus(DataContainer childContainer, String dataSource) {
		DomainType statusValue = (DomainType) childContainer
				.getHighestPriorityValue(SdDataAttributeConstant.SEC_STATUS);

		if (statusValue == null) {
			log.error("following child container :{} found without any status value : {}",childContainer,statusValue);
			return null;
		}

		if (statusValue.getNormalizedValue() != null) {
			return statusValue.getNormalizedValue();
		}

		Serializable normalizedValueForDomainValue = normalizedValService
				.getNormalizedValueForDomainValue(SdDataAttributeConstant.SEC_STATUS, statusValue, dataSource);
		return (String) normalizedValueForDomainValue;
	}

	/**
	 * This methods returns status for technical Security.
	 * @param childContainer
	 * @return
	 */
	private String getTechnicalSecurityStatus(DataContainer childContainer) {
		DomainType statusValue = (DomainType) childContainer.getHighestPriorityValue(SdDataAttributeConstant.SEC_STATUS);
		if (statusValue != null ) {
			return statusValue.getNormalizedValue();
		}
		return null;
	}

	/**
	 * This method Inactivates RDU technical security.
	 * @param chlidContainers
	 */
	private void inActivateRduTechnicalSecurity(List<DataContainer> chlidContainers) {
		DataContainer technicalSecurity = getTechnicalSecurity(chlidContainers);

		if(technicalSecurity == null) {
			log.error("No valid technical Security is present in requested childDataContainers",chlidContainers);
			return;
		}
		
		DomainType value = new DomainType();
		value.setNormalizedValue(DomainStatus.INACTIVE);
		DataValue<DomainType> securityStatusDataValue = new DataValue<>();
	    securityStatusDataValue.setValue(LockLevel.ENRICHED, value);
		technicalSecurity.addAttributeValue(SdDataAttributeConstant.SEC_STATUS, securityStatusDataValue);
	}

	/**
	 * This method returns TechnicalSecurity from provided list of chlidContainers.
	 * @param chlidContainers
	 * @return
	 */
	private DataContainer getTechnicalSecurity(List<DataContainer> chlidContainers) {
		for (DataContainer container : chlidContainers) {
			if (isTechnicalSecurity(container)) {
				return container;
			}
		}
		return null;
	}

    /**
     * This method creates technical Security & populates required attributes in it.
     * @param container
     */
	private void populateRduTechnicalSecurity(DataContainer container) {
		DataContainerContextBuilder context = DataContainerContext.builder().withProgram(SdDataAttConstant.NG_UDL).withUpdateDateTime(LocalDateTime.now());
		DataContainer secContainer = new DataContainer(DataLevel.SEC, context.build());

		DomainType value = new DomainType();
		value.setNormalizedValue(DomainStatus.ACTIVE);
		DataValue<DomainType> domain = new DataValue<>();
		domain.setValue(LockLevel.ENRICHED, value);
		secContainer.addAttributeValue(SdDataAttributeConstant.SEC_STATUS, domain);

		rduSecurityTypePopulationService.populateSecurityType(SdDataAttConstant.TECHNICAL, secContainer);

		container.addDataContainer(secContainer, DataLevel.SEC);
	}


	
}
