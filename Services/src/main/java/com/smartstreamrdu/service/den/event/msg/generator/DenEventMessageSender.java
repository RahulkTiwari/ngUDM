/*******************************************************************
*
* Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	DenEventMessageSender.java
* Author:Padgaonkar S
* Date:	05-Jan-2022
*
*******************************************************************
*/
package com.smartstreamrdu.service.den.event.msg.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.Constant.Process;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Den event message sender class.
 * @author Padgaonkar
 *
 */
@Slf4j
@Component
public class DenEventMessageSender {

	@Autowired
	@Setter
	private ProducerFactory factory;

	/**
	 * This method sends message to denEventListenerQueue.
	 * 
	 * @param message
	 */
	public void createAndsendMessageToEventListenerQueue(EventMessage message) {
		// Build the kafka message.
		Message input = new DefaultMessage.Builder().data(message).process(Process.DEN_EVENT)
				.target(com.smartstreamrdu.util.Constant.Component.DATA_ENRICHMENT).build();

		Producer<?> producer = factory.getProducer(ProducerEnum.Kafka);

		try {
			producer.sendMessage(input);
			log.debug("Message sent to den eventListenerQueue with details: {}", input);
		} catch (Exception e) {
			log.error("Following error occured while sending message to den eventListenerQueue", e);
		}
		
	}

}
