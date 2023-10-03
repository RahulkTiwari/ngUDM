/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataBasedLastProcessedDatePopulator.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata.enrichment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.persistence.repository.EventConfigurationRepository;
import com.smartstreamrdu.service.event.enrichment.services.EnrichmentService;
import com.smartstreamrdu.service.inactive.LastProcessedDatePopulationService;
import com.smartstreamrdu.service.rawdata.listeners.RawDataEvent;
import com.smartstreamrdu.service.rawdata.listeners.RawDataListenerInput;
import com.smartstreamrdu.service.rawdata.listeners.RawDataProcessListener;

import lombok.Setter;

/**
 * This class is responsible for populating lastProcessedDate when record is
 * getting rejected in rawData. This class reads eventConfiguration from
 * database & based on config it populates lastProcessedDate at specified level.
 * 
 * @author Padgaonkar
 *
 */
@Component("RawDataBasedLastProcessedDatePopulator")
public class RawDataBasedLastProcessedDatePopulator implements EnrichmentService, RawDataProcessListener {

	@Autowired
	@Setter
	private EventConfiguration config;

	@Autowired
	@Setter
	private EventConfigurationRepository repo;

	@Autowired
	private LastProcessedDatePopulationService service;

	@Override
	public void onRawRecordFiltered(RawDataListenerInput input) {
		service.populateLastProcessedDateBasedOnRawContainer(input.getFilePath(), input.getDbDataContainer(), config);
	}

	
	@Override
	public boolean register(String eventName) {	
		return RawDataEvent.ON_RAW_RECORD_FILTERD.name().equals(eventName);
	}

	@Override
	public void setEventConfiguration(EventConfiguration eventConfig) {
		this.config = eventConfig;
	}

}
