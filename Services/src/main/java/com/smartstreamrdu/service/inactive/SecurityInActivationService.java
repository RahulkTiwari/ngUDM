/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    SecurityInActivationService.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.exception.UdmBusinessException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * This is default implementation of SecurityInActivationService.
 * This is applicable for all feeds.When instrument
 * is inActivated,then its all securities are getting inActivated in this
 * Service.
 */
@Component("DefaultSecurityInActive")
@Slf4j
public class SecurityInActivationService implements InactiveService {

	private static final long serialVersionUID = 1L;

	@Override
	public void inactivateIfRequired(InactiveBean inactive) throws UdmBusinessException, UdmTechnicalException {

		DataContainer container = inactive.getDbContainer();
		
		if(!container.getLevel().equals(DataLevel.INS)) {
			return;
		}
		
		//check if INS is inactive
		if (InstrumentInactiveUtil.isInstrumentStatusInActive(inactive.getDatasource(), container)) {		
			// Instrument Status inactive. Setting all the securities inactive
			List<DataContainer> childContainers = container.getChildDataContainers(DataLevel.SEC);
			inActivateChildContainer(childContainers,inactive.getDatasource());		
		}
	}

	/**
	 * This method inActivates all active securities.
	 * @param childContainers
	 * @param dataSource 
	 */
	private void inActivateChildContainer(List<DataContainer> childContainers, String dataSource) {
		if (CollectionUtils.isNotEmpty(childContainers)) {
		for(DataContainer childContainer : childContainers) {	
			 validateAndInactiveSecurity(dataSource, childContainer);
			}
		}
	}

	/**
	 * This method first checks if requested childContainer already is Inactive or not
	 * If No  - It Inactivate dataContainer.
	 * If yes - do nothing
	 * @param dataSource
	 * @param childContainer
	 */
	@SuppressWarnings("unchecked")
	private void validateAndInactiveSecurity(String dataSource, DataContainer childContainer) {
		if (InstrumentInactiveUtil.isSecurityInactive(childContainer, dataSource)) {
			log.debug("Requested security is already inactivated : {}",childContainer);
			return;
		}
		DomainType domainType = new DomainType(null, null, DomainStatus.INACTIVE);
		DataValue<Serializable> secStatusDataValue = (DataValue<Serializable>) childContainer
				.getAttributeValue(SdDataAttributeConstant.SEC_STATUS);
		secStatusDataValue.setValue(LockLevel.ENRICHED, domainType);
	}
}
