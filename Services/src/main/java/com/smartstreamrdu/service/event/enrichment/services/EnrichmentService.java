/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	EnrichmentService.java
 * Author:	Padgaonkar S
 * Date:	30-Nov-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.event.enrichment.services;

import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.service.event.process.listener.ProcessListener;

/**
 * Generic interface to add all dataSourceSpecifc enrichment service
 * @author Padgaonkar
 *
 */
public interface EnrichmentService extends ProcessListener  {

	/**
	 * Setter method for specific event configuration
	 */
	public void setEventConfiguration(EventConfiguration eventConfig);
	
	
	/**
	 * This methods return true if enrichment service is applicable for
	 * input event name.
	 * @param rawDataListeners
	 */
	public boolean register(String eventName);
}
