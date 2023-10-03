/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ReactorKafkaReceiver.java
 * Author:  Padgaonkar
 * Date:    April 5, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.reactor.kafka;

import java.io.Serializable;

import com.smartstreamrdu.api.response.PushApiResponseWrapper;
import com.smartstreamrdu.api.response.ResponseContent;

import reactor.core.publisher.Flux;

/**
 * This is generic implementation of Reactor Kafka Receiver.
 * Based on subscribed topic this this will return 
 * @author Padgaonkar
 *
 */
public interface ReactorKafkaReceiver {
	
	/**
	 * This method returns flux<Serializable> from requested Kafka topics.
	 * @param input
	 * @return
	 */
	Flux<Serializable> receive(ReactorKafkaReceiverInput input); 
	
	/**
	 * This method is defined to process on receiver(ReceiverOptions) endpoint and generates flux of PushApiResponseWrapper.
	 *  <br>
	 * It creates KafkaReceiver based on ReceiverOptions attributes and process each records which will be received until client gets disconnected. 
	 *  <br>
	 * Each records are being process for defined consumer group and those offset are also maintained.
	 *  <br>
	 * It wraps each received object into PushApiResponseWrapper before returning.
	 * 
	 *  <br>
	 * @param kafkaRecordReceiver
	 * @return
	 */
	Flux<PushApiResponseWrapper<ResponseContent>> processPushNotificationEvents(
			ReactorKafkaReceiverInput input);
}
