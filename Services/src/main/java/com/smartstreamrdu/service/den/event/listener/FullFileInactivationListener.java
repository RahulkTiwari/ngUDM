/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FullFileInactivationListener.java
 * Author:	Padgaonkar S
 * Date:	30-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.service.den.enrichment.service.FullFileInactivationService;
import com.smartstreamrdu.service.event.enrichment.services.EnrichmentService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Den listeners which does the job of inactivating securities on back of full file load.
 */

@Slf4j
@Component("FullFileInactivationListener")
public class FullFileInactivationListener implements DenProcessListener,EnrichmentService{

	@Setter
	@Autowired
	private EventConfiguration config;
	
	
	@Setter
	@Autowired
	private FullFileInactivationService service;
	

	@Override
	public void onDenEventReceived(EventMessage eventMessage) {
		
		//inactivate securities which are not part of the full file load
		try {
			log.info("inactivation service  based on full file load is started");

			service.inactivationBasedOnFullLoad(config, eventMessage);

			log.info("inactivation service based on full file load is completes");
		} catch (UdmTechnicalException e) {
			log.error("Following error occured while processing inactivation message:{}",e);
		}
		
	}

	@Override
	public void setEventConfiguration(EventConfiguration config) {
		this.config = config;	
	}

	@Override
	public boolean register(String eventName) {
		return DataEnrichmentEvent.INACTIVATION_BASED_ON_FULL_LOAD.getEventName().equals(eventName);
	}
}
