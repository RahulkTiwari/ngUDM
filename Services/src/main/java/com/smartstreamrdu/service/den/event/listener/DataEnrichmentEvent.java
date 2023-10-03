/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataEnrichmentEvent.java
 * Author:	Padgaonkar S
 * Date:	30-Nov-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.event.listener;

import lombok.Getter;

/*
 * Generic enum class to hold name of all den events.
 */
public enum DataEnrichmentEvent {

	INACTIVATION_BASED_ON_FULL_LOAD("inactivationBasedOnFullLoad"),
	INACTIVATION_OF_FILTERED_CONTAINER("FilteredContainerInactivation");

	@Getter
	private String eventName;

	private DataEnrichmentEvent(String eventName) {
		this.eventName = eventName;
	}
}
