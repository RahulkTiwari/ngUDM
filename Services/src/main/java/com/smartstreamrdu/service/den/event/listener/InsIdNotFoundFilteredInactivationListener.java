/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InsIdNotFoundFilteredInactivationListener.java
 * Author:	Padgaonkar S
 * Date:	05-Jan-2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.service.den.enrichment.service.InsIdNotFoundFilteredInactivationService;
import com.smartstreamrdu.service.event.enrichment.services.EnrichmentService;
import com.smartstreamrdu.util.FullLoadBasedInactivationConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This listener is responsible for processing inactivation of filtered
 * container.
 * 
 * Currently this service is applicable for security containers only. Following
 * are the cases when we filtered security from system.
 * 
 * If security status is Inactive & if that security is not available in
 * database. Then we filter that
 * security[As we do not insert inactive security into system] during udl flow.&
 * sends corresponding message to this service.
 * 
 * In this class based on vendor configuration we checked that if that filtered
 * security is part of some existing instrument or not..& if yes then we
 * inactivate that security based on vendor configuration.
 * 
 * This flow is completely vendor specific & hence not incorporated in standard
 * UDL workflow.
 * 
 * @author Padgaonkar
 *
 */
@Slf4j
@Component("InsIdNotFoundFilteredInactivationListener")
public class InsIdNotFoundFilteredInactivationListener implements DenProcessListener, EnrichmentService {

	@Setter
	@Autowired
	private EventConfiguration config;

	@Setter
	@Autowired
	private InsIdNotFoundFilteredInactivationService inactivationService;

	@Override
	public void onDenEventReceived(EventMessage eventMessage) {

		if (!config.getConditions().get(FullLoadBasedInactivationConstant.INACTIVATION_LEVEL)
				.equals(DataLevel.SEC.name())) {
			return;
		}

		try {
			inactivationService.inactivateContainer(eventMessage, DataLevel.SEC);
			log.info("Inactivation has been processed for eventMessgae :{}",eventMessage);
		} catch (UdmBaseException e1) {
			log.error("Following exception:{} occured while processing filtered container inactivation", e1);
		}
	}

	@Override
	public void setEventConfiguration(EventConfiguration eventConfig) {
		this.config = eventConfig;
	}

	@Override
	public boolean register(String eventName) {
		return DataEnrichmentEvent.INACTIVATION_OF_FILTERED_CONTAINER.getEventName().equals(eventName);

	}
}
