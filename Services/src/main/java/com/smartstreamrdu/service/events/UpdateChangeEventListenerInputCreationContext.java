/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ProformaChangeEventListenerInputCreationContext.java
 * Author: Rushikesh Dedhia
 * Date: August 21, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.events;

import com.smartstreamrdu.commons.xrf.XrfProcessMode;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.FeedConfiguration;

import lombok.Data;

@Data
public class UpdateChangeEventListenerInputCreationContext implements ChangeEventListenerInputCreationContext {
	
	private DataContainer feedDataContaier;
	
	private DataContainer dbDataContainer;
	
	private FeedConfiguration feedConfiguration;
	
	private XrfProcessMode xrfProcessMode;

}
