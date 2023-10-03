/**
 * *****************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionMessageHandlerImpl.java
 * Author:	Padgaonkar
 * Date:	28-Feb-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.exceptions;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.UdmSystemPropertiesConstant;
import com.smartstreamrdu.util.Constant.Process;

@Component
public class UdmExceptionMessageHandlerImpl implements UdmExceptionMessageHandler {

	private static final Logger _logger = LoggerFactory.getLogger(UdmExceptionMessageHandlerImpl.class);

	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	UdmSystemPropertiesCache systemCache;
	
	private boolean raiseException;
	
	@PostConstruct
	public void initialize() {
		Optional<String> raiseExceptionOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.ENABLE_RAISING_EXCEPTION,UdmSystemPropertiesConstant.COMPONENT_XRF,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(raiseExceptionOptional.isPresent()) {
			raiseException = Boolean.parseBoolean(raiseExceptionOptional.get());
	   }
	}
	
	@Override
	public void sendMessage(UdmExceptionDataHolder exceptionData) {

		if (null == exceptionData) {
			_logger.info("Not sending message to UdmExceptionQueue as the Message was null{}", "");
			return;
		}
		Message input = new DefaultMessage.Builder().data(exceptionData).target(com.smartstreamrdu.util.Constant.Component.UDM_EXCEPTION).process(Process.UdmException).build();

		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);

		try {
			_logger.debug("Sending Message to UdmExceptionQueue {}", input);
			if (raiseException) {
				producer.sendMessage(input);
			}
			_logger.debug("Message sent to UdmExceptionQueue with details: {}", input);
		} catch (Exception e) {
			_logger.error("Following error occured while sending message to UdmExceptionQueue", e);
		}

	}

}
