/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ReactorKafkaReceiverInput.java
 * Author:  Padgaonkar
 * Date:    April 5, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.reactor.kafka;

import java.util.Properties;

import lombok.Data;
import reactor.kafka.receiver.ReceiverOptions;

/**
 * Input class for reactive Kafka Receiver.
 * 
 * @author Padgaonkar
 *
 */

@Data
public class ReactorKafkaReceiverInput {

	//Kafka topic name from which we wanted to read messages
	private String topicName;

	private ReceiverOptions<Integer, String> receiverOptions;
	

	public ReactorKafkaReceiverInput(String topicName, Properties consumerProperties) {
		this.topicName = topicName;
		this.receiverOptions = ReceiverOptions.create(consumerProperties);
	}

	public ReactorKafkaReceiverInput(String topicName,ReceiverOptions<Integer, String> receiverOptions) {
		this.topicName = topicName;
		this.receiverOptions = receiverOptions;
	}

}
