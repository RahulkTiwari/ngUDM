/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    RduSecurityTypePopulationServiceImpl.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.postprocess;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Order(1)
public class RduSecurityTypePopulationService  extends AbstractDataContainerPostProcessorService {

	public void populateSecurityType(String securityType, DataContainer container) {

		if (!container.getLevel().equals(DataLevel.SEC)) {
			log.error("RduSecurityTypePopulationService is applicable for sec level dataConatiners only."
					+ "hence rejecting this {} dataContainer", container);
			return;
		}

		DomainType attributeValue = (DomainType) container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);

		// If its new Security & doesn't contains securityType then populate it.
		if (attributeValue == null) {
			DataValue<DomainType> domainValue = getDomainObject(securityType);
			container.addAttributeValue(SdDataAttributeConstant.RDU_SEC_TYPE, domainValue);
		} else {
			
			// If security already contains same securityType then don't override it.else
			// update securityType
			if (!attributeValue.getNormalizedValue().equals(securityType)) {
				log.debug("For requested security{} type{} is same as earlier",container,securityType);
				attributeValue.setNormalizedValue(securityType);
				container.setHasChanged(true);
			}
		}
	} 

	/**
	 * This method returns DomainType objects for securityType.
	 * @param securityType
	 * @return
	 */
	private DataValue<DomainType> getDomainObject(String securityType) {
		DomainType domain = new DomainType();
		domain.setNormalizedValue(securityType);
		DataValue<DomainType> domainValue = new DataValue<>();
		domainValue.setValue(LockLevel.ENRICHED, domain);
		return domainValue;
	}

	@Override
	public boolean isServiceApplicable(String dataSource, List<DataContainer> dataContainers)
			throws UdmTechnicalException {
		//This service is applicable for all feeds hence returning true
		return true;
	}

	@Override
	public void postProcessContainers(String dataSource, List<DataContainer> dataContainers)
			throws UdmTechnicalException {
		for(DataContainer container : dataContainers) {
			List<DataContainer> allChildDataContainers = container.getAllChildDataContainers();
			for (DataContainer childContainer : allChildDataContainers) {
				if (isTechnicalSecurity(childContainer)) {
					populateSecurityType(SdDataAttConstant.TECHNICAL, childContainer);
				}else {
					populateSecurityType(SdDataAttConstant.REGULAR, childContainer);
				}
			}
		}	
	}
	
}
