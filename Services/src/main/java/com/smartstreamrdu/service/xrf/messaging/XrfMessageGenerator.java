/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfMessageGenerator.java
 * Author: Rushikesh Dedhia
 * Date: May 11, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.time.LocalDateTime;

import com.smartstreamrdu.commons.xrf.CrossRefBaseDocument;
import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;

/**
 * @author Dedhia
 *
 */
public interface XrfMessageGenerator {

	/**
	 * This method will generate a XRF JSON message from the ChangeEventInputPojo.
	 * 
	 * @param changeEventInput
	 * @return
	 */
	XrfMessage generateMessage(ChangeEventInputPojo changeEventInput, LocalDateTime staticDataUpdateDate);

	
	/***
	 * This method returns reprocessing xrf Message.
	 * @param doc
	 * @return
	 */
	XrfMessage generateReprocessingXrfMessage(CrossRefBaseDocument doc) ;
}
