/*******************************************************************
*
* Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	InsIdNotFoundInactiveContainerProcessListener.java
* Author:Padgaonkar S
* Date:	05-Jan-2022
*
*******************************************************************
*/
package com.smartstreamrdu.service.event.inactive.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.service.den.event.msg.generator.DenEventMessageGenerator;
import com.smartstreamrdu.service.event.enrichment.services.EnrichmentService;

import lombok.Setter;

/**
 * This class generates message for filtered inactive security & send that to
 * eventListenerQueue.
 * 
 * @author Padgaonkar
 *
 */
@Component("InsIdNotFoundInactiveContainerMessageSender")
public class InsIdNotFoundInactiveContainerProcessListener implements InsIdNotFoundContainerListener, EnrichmentService {

	@Setter
	@Autowired
	private EventConfiguration config;

	@Autowired
	@Qualifier("insIdNotFoundFilteredInactiveSecEventMessageGenerator")
	private DenEventMessageGenerator msgGenerator;
	
	@Override
	public void onFilteredInactiveContainer(InsIdNotFoundInactiveContainerListenerInput input) {

		// Generating event message for inactive security filter event & sending message
		// to denEventListenerQueue
		msgGenerator.generateAndSendMessage(input);

	}

	@Override
	public void setEventConfiguration(EventConfiguration eventConfig) {
		this.config = eventConfig;

	}

	@Override
	public boolean register(String eventName) {
		return InsIdNotFoundFilteredInactiveContainerEvent.ON_INACTIVE_SECURITY_FILTER.name().equals(eventName);
	}

}
