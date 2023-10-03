/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ProformaMessageGenerator.java
 * Author: Rushikesh Dedhia
 * Date: August 14, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.util.List;

import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;

public interface ProformaMessageGenerator {

	/**
	 * This method will generate a Proforma message from the ChangeEventInputPojo.
	 * 
	 * @param inputPojo
	 * @return ProformaMessage
	 */
	List<ProformaMessage> generateMessage(ChangeEventInputPojo inputPojo);

}
