/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	TechnicalSecurityLastProcessedDatePopulator.java
 * Author:	Padgaonkar S
 * Date:	30-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.udl.enrichment;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.event.enrichment.services.EnrichmentService;
import com.smartstreamrdu.service.inactive.LastProcessedDatePopulationService;
import com.smartstreamrdu.service.udl.listener.UdlMergeEvent;
import com.smartstreamrdu.service.udl.listener.UdlMergeListener;
import com.smartstreamrdu.service.udl.listener.UdlProcessListenerInput;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;

import lombok.Setter;

/**
 * This listeners service populates lastProcessed date in technical security.
 * @author Padgaonkar
 *
 */
@Component("TechnicalSecurityLastProcessedDatePopulator")
public class TechnicalSecurityLastProcessedDatePopulator implements UdlMergeListener, EnrichmentService {

	@Autowired
	@Setter
	private EventConfiguration config;

	@Autowired
	@Setter
	private LastProcessedDatePopulationService service;

	@Override
	public void postMergingContainer(UdlProcessListenerInput input) {

		if (isContainerApplicable(input.getContainer())) {
			
			//populate lastProcessed Date
			service.populateLastProcessedDate(input.getContainer(), DataLevel.SEC);
		}
	}

	private boolean isContainerApplicable(DataContainer container) {
		List<DataContainer> allChildDataContainers = container.getAllChildDataContainers();
		return (container.getLevel() == DataLevel.INS && !allChildDataContainers.isEmpty()
				&& isActiveTechnicalSecurity(allChildDataContainers));
	}

	/**
	 * Validating is input security is technical security or not
	 * 
	 * @param allChildDataContainers
	 * @return
	 */
	private boolean isActiveTechnicalSecurity(List<DataContainer> allChildDataContainers) {

		for (DataContainer container : allChildDataContainers) {
			DomainType securityType = (DomainType) container
					.getHighestPriorityValue(SecurityAttrConstant.RDU_SECURITY_TYPE);
			DomainType securityStatus = (DomainType) container
					.getHighestPriorityValue(SecurityAttrConstant.SECURITY_STATUS);

			if (securityType != null && SdDataAttConstant.TECHNICAL.equals(securityType.getNormalizedValue())
					&& DomainStatus.ACTIVE.equals(securityStatus.getNormalizedValue())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setEventConfiguration(EventConfiguration eventConfig) {
		this.config = eventConfig;
	}

	@Override
	public boolean register(String eventName) {
		return UdlMergeEvent.POST_MERGING_CONTAINER.name().equals(eventName);	
	}
}
