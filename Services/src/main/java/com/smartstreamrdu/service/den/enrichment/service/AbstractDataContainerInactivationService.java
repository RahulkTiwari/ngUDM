/*******************************************************************
*
* Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	AbstractDataContainerInactivationService.java
* Author:Padgaonkar S
* Date:	05-Jan-2022
*
*******************************************************************
*/
package com.smartstreamrdu.service.den.enrichment.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.service.domain.DataContainerCloneService;
import com.smartstreamrdu.util.EventListenerConstants;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.Setter;

/**
 * Abstract class consist of  common methods used across inactivation
 * services.
 * 
 * @author Padgaonkar
 *
 */
@Component
public abstract class AbstractDataContainerInactivationService {

	@Setter
	@Autowired
	private DataContainerCloneService cloneService;

	/**
	 * Creating minimum attribute securityDataContainer
	 * 
	 * @param sec
	 * @param dataSource
	 * @return
	 * @throws UdmBaseException
	 */
	public DataContainer getMinAttSecDataContainer(DataContainer sec, DomainType dataSource) throws UdmBaseException {
		DataContainer minInSecDataContainer = cloneService.createMinimumAttributeInactiveSecurityDC(sec, dataSource);
		minInSecDataContainer.updateDataContainerContext(DataContainerContext.builder()
				.withDataSource(dataSource.getVal()).withUpdateDateTime(LocalDateTime.now()).withProgram(EventListenerConstants.PROGRAME_DEN).build());
		return minInSecDataContainer;
	}

	/**
	 * Creating minimum attribute instrument dataContainer.
	 * 
	 * @param sdDataContainer
	 * @param dataSource
	 * @return
	 * @throws UdmBaseException
	 */
	@SuppressWarnings("unchecked")
	public DataContainer getMinimumAttributeInsDataContainer(DataContainer sdDataContainer, DomainType dataSource)
			throws UdmBaseException {
		DataValue<DomainType> insStatusVal = (DataValue<DomainType>) sdDataContainer
				.getAttributeValue(SdDataAttributeConstant.INS_STATUS);
		DataContainer minInsDataContainer = cloneService.createMinimumAttributeInstrumentDC(sdDataContainer);
		minInsDataContainer.updateDataContainerContext(DataContainerContext.builder()
				.withDataSource(dataSource.getVal()).withUpdateDateTime(LocalDateTime.now()).withProgram(EventListenerConstants.PROGRAME_DEN).build());
		minInsDataContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, insStatusVal);
		return minInsDataContainer;

	}
}
