/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	PostRduRuleLastProcessedDatePopulator.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules.enrichment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.service.event.enrichment.services.EnrichmentService;
import com.smartstreamrdu.service.inactive.LastProcessedDatePopulationService;
import com.smartstreamrdu.service.rules.listeners.RduRuleEvent;
import com.smartstreamrdu.service.rules.listeners.RduRuleListenerInput;
import com.smartstreamrdu.service.rules.listeners.RduRuleProcessListener;

import lombok.Setter;

/**
 * This listener is responsible for populating lastProcessedDate in dataContiner
 * postRuleExecution.
 * 
 * @author Padgaonkar
 *
 */
@Component("RduRuleLastProcessedDatePopulator")
public class RduRuleLastProcessedDatePopulator implements RduRuleProcessListener, EnrichmentService {


	@Autowired
	@Setter
	private EventConfiguration eventConfig;

	@Autowired
	@Setter
	private LastProcessedDatePopulationService datePopultnService;

	@Override
	public void onRuleExecution(RduRuleListenerInput input) {
		datePopultnService.populateLastProcessedDateInFeedContainer(input.getFeedContainer(), eventConfig);
	}

	@Override
	public void setEventConfiguration(EventConfiguration eventConfig) {
		this.eventConfig = eventConfig;
	}

	@Override
	public boolean register(String eventName) {
		return RduRuleEvent.ON_RULE_EXECUTION.name().equals(eventName);
	}
}
