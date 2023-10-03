/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaProducer.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;

/**
 * @author Jay Sangoi
 *
 */
@Component("KafkaProducer")
public class KafkaProducer implements Producer<RecordMetadata> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2926445648674387988L;

	private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

	@Autowired
	private KafkaConfiguration configuration;

	volatile org.apache.kafka.clients.producer.Producer<String, String> producer;

	private void init() {
		Properties producerConfiguration = configuration.createProducerConfiguration();
		if (producer == null) {
			producer = new org.apache.kafka.clients.producer.KafkaProducer<>(producerConfiguration);
		}
		logger.info("Kafka Producer properties : {} ", producerConfiguration);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.messaging.producer.Producer#sendMessage(com.
	 * smartstreamrdu.service.messaging.Message)
	 */
	@Override
	public Future<RecordMetadata> sendMessage(Message message) throws Exception {
		init();
		ProducerRecord<String, String> record = new ProducerRecord<>(
				configuration.getTopicName(message.getProcess(), message.getSource(), message.getTarget()),
				message.getPartition(), message.getKey(), message.getData());
		return producer.send(record, (RecordMetadata metadata, Exception exception) -> {
			if (exception != null) {
				logger.error("Exception recieved while sending message [{}] to a kafka queue. Error : ", record, exception);
			} else {
				logger.trace("Message [{}] posted successfully with metadata {}", record, metadata);
			}

		});
	}

	@Override
	public RecordMetadata sendMessageSync(Message message) throws Exception {
		return sendMessage(message).get();
	}

}
