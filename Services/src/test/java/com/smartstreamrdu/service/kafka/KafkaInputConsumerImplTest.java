/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaInputConsumerImplTest.java
 * Author:	S Padgaonkar
 * Date:	25-Sept-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.kafka.KafkaInputParameter;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class KafkaInputConsumerImplTest {

	@Autowired
	KafkaInputConsumer kafkaConsumer;

	@Test
	public void start() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		new Thread(this::testConsumer).start();
		latch.await(30, TimeUnit.SECONDS);
	}

	public void testConsumer() {
		KafkaInputParameter input = new KafkaInputParameter(Set.of("test-topic"), "abc", (r) -> {});
		kafkaConsumer.startConsumer(input);
	}
}
