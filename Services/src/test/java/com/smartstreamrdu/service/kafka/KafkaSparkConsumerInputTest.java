/*******************************************************************
*
* Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	KafkaSparkConsumerInputTest.java
* Author:	S Padgaonkar
* Date:	25-Sept-2018
*
*******************************************************************
*/
package com.smartstreamrdu.service.kafka;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KafkaSparkConsumerInputTest {

	@SuppressWarnings("rawtypes")
	KafkaSparkConsumerInput input;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void init() {
		input = new KafkaSparkConsumerInput();
		input.setGroupId("abc");
		input.addTopic("test_topic");
		input.setHandler(getBatchHandler());
	}

	@Test
	public void testConsumer() {
		List<String> topics = new ArrayList<>();
		topics.add("test_topic");
		Assert.assertNotNull(input.getHandler());
		Assert.assertNotNull(input.getTopics());
		Assert.assertNotNull(input.getGroupId());
		Assert.assertEquals("abc", input.getGroupId());
		Assert.assertEquals(topics, input.getTopics());

	}

	@SuppressWarnings({ "rawtypes" })
	public BatchHandler getBatchHandler() {
		return ((rdd, topicName) -> {
		});
	}
}
