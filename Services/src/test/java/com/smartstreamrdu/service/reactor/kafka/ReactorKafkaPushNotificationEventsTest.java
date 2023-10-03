/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    ReactorKafkaTest.java
 * Author:	RKaithwas
 * Date:	24-June-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.reactor.kafka;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.api.response.PushApiResponseWrapper;
import com.smartstreamrdu.api.response.ResponseContent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ReactorKafkaReceiverImplTestConfig.class })
public class ReactorKafkaPushNotificationEventsTest {

	private static final String TOPIC = "test";
	@Autowired
	private ReactorKafkaReceiverImpl kafkaReceiver;
	private ReceiverOptions<Integer, String> receiverOptions;

	@SuppressWarnings("rawtypes")
	@Autowired
	KafkaReceiver receiver;
	ReceiverOptions<Integer, String> subscription;

	private static String MESSAGE = "{ \"exchangeCode\": \"NYSE\", \"exchangeName\": \"HONaG KONG FUTURES EXCHANGE LIMITED\", \"segmentMic\": \"HKFE99\", \"newExchangeCode\": \"XOSE1\", \"eventInsertDate\": \"2020-02-20\", \"eventSubject\": \"Tradinag \", \"eventSummaryText\": \"Consolidation\" }";

	@Before
	public void setUp() {

		receiverOptions = ReceiverOptions.<Integer, String>create()
				.consumerProperty(ConsumerConfig.GROUP_ID_CONFIG, "groupId")
				.consumerProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
				.consumerProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class)
				.consumerProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		subscription = receiverOptions.subscription(Collections.singleton(TOPIC));

		ReceiverOffset committableOffset = new ReceiverOffset() {

			@Override
			public TopicPartition topicPartition() {

				return null;
			}

			@Override
			public long offset() {
				return 0;
			}

			@Override
			public Mono<Void> commit() {
				return null;
			}

			@Override
			public void acknowledge() {

			}
		};
		kafkaReceiver = new ReactorKafkaReceiverImpl() {
			@Override
			public Flux<ReceiverRecord<Integer, String>> kafkaReceiver(
					ReceiverOptions<Integer, String> receiverOptions) {
				ReceiverRecord<Integer, String> data = new ReceiverRecord<Integer, String>(
						new ConsumerRecord<Integer, String>(TOPIC, 1, 0, 1, MESSAGE), committableOffset);
				Flux<ReceiverRecord<Integer, String>> value = Flux.just(data);
				return value;
			}
		};
	}

	@Test
	public void testKafkaReceiver() {
		ReactorKafkaReceiverInput input = new ReactorKafkaReceiverInput("test",receiverOptions);
		Flux<PushApiResponseWrapper<ResponseContent>> events = kafkaReceiver
				.processPushNotificationEvents(input);

		assertEquals("NYSE", events.blockFirst().getContent().get("exchangeCode"));

	}

}
