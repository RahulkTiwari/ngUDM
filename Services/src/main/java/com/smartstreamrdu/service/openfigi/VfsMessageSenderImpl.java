/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	VfsMessageSenderImpl.java
 * Author:	Shruti Arora
 * Date:	05-Nov-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.Constant.Process;

import lombok.extern.slf4j.Slf4j;


/**
 * @author SArora
 * This service is responsible for sending the messages to the specified Queue and partition.
 */
@Component
@Slf4j
public class VfsMessageSenderImpl implements VfsMessageSender {
	
	@Autowired
	private ProducerFactory producerFactory;
	
	
	private void init() {
		if(producerFactory==null) {
			producerFactory=SpringUtil.getBean(ProducerFactory.class);
		}
	}


	@Override
	public void sendMessage(VfsFigiRequestMessage identifiers, Process process, Integer partition) {
		if(identifiers==null || process==null || partition==null) {
			return;
		}
		Message inputMsg = new DefaultMessage.Builder().data(identifiers).target(com.smartstreamrdu.util.Constant.Component.VFS_FIGI).process(process).partition(partition).build();
		log.debug("Sending message : {} to FigiRequestQueue on partition {} to retry", inputMsg,partition);
		sendMessage(inputMsg);
	}

	
	private void sendMessage(Message message) {
		init();
		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);
		try {
			producer.sendMessage(message);
			log.debug("Message sent to FIGI with details: {}",message);
		} catch (Exception e) {
			log.error("Following error occured while sending messagee to VFS Open Figi", e);
		}
	}


}
