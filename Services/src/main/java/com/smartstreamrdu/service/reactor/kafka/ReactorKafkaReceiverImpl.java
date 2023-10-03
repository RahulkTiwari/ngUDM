/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ReactorKafkaReceiverImpl.java
 * Author:  Padgaonkar
 * Date:    April 5, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.reactor.kafka;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;
import com.smartstreamrdu.api.response.PushApiResponseWrapper;
import com.smartstreamrdu.api.response.PushApiResponseWrapper.PushApiResponseWrapperBuilder;
import com.smartstreamrdu.api.response.ResponseContent;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class ReactorKafkaReceiverImpl implements ReactorKafkaReceiver {

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flux<Serializable> receive(ReactorKafkaReceiverInput input) {
		ReceiverOptions<Integer, String> receiverOptions = input.getReceiverOptions();

		ReceiverOptions<Integer, String> options = receiverOptions
				.subscription(Collections.singleton(input.getTopicName()))
				.addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
				.addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

		Flux<ReceiverRecord<Integer, String>> kafkaFlux = kafkaReceiver(options);
		return kafkaFlux.map(e -> {
			// committing offset
			e.receiverOffset().commit();
			return e.value();
		});
	}

	@Override
	public Flux<PushApiResponseWrapper<ResponseContent>> processPushNotificationEvents(
			ReactorKafkaReceiverInput input) {
		
		Flux<ReceiverRecord<Integer, String>> kafkaRecordReceiver = kafkaReceiver(input.getReceiverOptions());	
		return kafkaRecordReceiver.map(record -> {
			Map<String, Object> convertFromJson = JsonConverterUtil.convertFromJson(record.value(),
					new TypeToken<Map<String, Object>>() {
					}.getType());
			ResponseContent responseContent = new ResponseContent();
			responseContent.putAll(convertFromJson);
			record.receiverOffset().commit(); // commit to kafka offset
			record.receiverOffset().acknowledge();
			PushApiResponseWrapperBuilder<ResponseContent> responseBuilder = PushApiResponseWrapper.successBuilder(record.receiverOffset().offset());
			responseBuilder.content(responseContent);
			return responseBuilder.build();
		}).onErrorContinue(
				(ex, i) -> log.error("Error occured while processing notification {} , with exception : {}", i, ex));
	}


	protected Flux<ReceiverRecord<Integer, String>> kafkaReceiver(ReceiverOptions<Integer, String> receiverOptions) {
		return KafkaReceiver.create(receiverOptions).receive();
	}

}
