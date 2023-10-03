/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: DefaultProformaMessageGenerationHandler.java
 * Author: Rushikesh Dedhia
 * Date: October 04, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.proforma.ProformaMessage;

/**
 * @author Dedhia
 * 
 * This is the default message generation of the system.
 * It considers the data level of the incoming data container and returns the 
 *
 */
@Component
public class DefaultProformaMessageGenerationHandler implements ProformaMessageGenerationHandler {

	@Override
	public List<ProformaMessage> createProformaMessages(DataContainer dataContainer) {
		
		Objects.requireNonNull(dataContainer, "DataContainer cannot be null.");

		List<ProformaMessage> proformaMessages = new ArrayList<>();
		
		if (dataContainer.getLevel() == DataLevel.INS) {
			proformaMessages.add(ProformaMessageGeneratorUtil.createProformaMessage(dataContainer, DataLevel.INS));
			return proformaMessages;
		} else if (dataContainer.getLevel() == DataLevel.LE) {
			// Create and add the message for the original document.
			proformaMessages.add(ProformaMessageGeneratorUtil.createProformaMessage(dataContainer, DataLevel.LE));
		}else if (dataContainer.getLevel() == DataLevel.EN) {
			proformaMessages.add(ProformaMessageGeneratorUtil.createProformaMessage(dataContainer, DataLevel.EN));
		}
		
		return proformaMessages;
	}

}
