/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ProformaMessageGeneratorImpl.java
 * Author: Rushikesh Dedhia
 * Date: August 14, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;

@Component
public class ProformaMessageGeneratorImpl implements ProformaMessageGenerator {

	@Autowired
	private ProformaMessageGenerationHandlerFactory generationHandlerFactory;

	@Override
	public List<ProformaMessage> generateMessage(ChangeEventInputPojo inputPojo) {
		Objects.requireNonNull(inputPojo, "ChangeEventInputPojo cannot be null.");

		DataContainer postChangeDataContainer = inputPojo.getPostChangeContainer();

		Objects.requireNonNull(postChangeDataContainer, "DataContainer cannot be null.");
		
		DomainType dataSource = (DomainType) ProformaMessageGeneratorUtil.getValueForDataAttribute(DataAttributeFactory.getDatasourceAttribute(postChangeDataContainer.getLevel())
				, postChangeDataContainer);
		
		ProformaMessageGenerationHandler generationHandler = generationHandlerFactory.getProformaMessageGenerationHandler(dataSource.getVal());

		return generationHandler.createProformaMessages(postChangeDataContainer);
	}

}