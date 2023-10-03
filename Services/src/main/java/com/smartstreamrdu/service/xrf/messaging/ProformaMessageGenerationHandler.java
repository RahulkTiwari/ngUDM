/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ProformaMessageGenerationHandler.java
 * Author: Rushikesh Dedhia
 * Date: October 04, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.proforma.ProformaMessage;

public interface ProformaMessageGenerationHandler {
	
	/**
	 *  Generates the proforma messages for the given data container.
	 * @param dataContainer
	 * @return
	 */
	List<ProformaMessage> createProformaMessages(DataContainer dataContainer);
	
}
