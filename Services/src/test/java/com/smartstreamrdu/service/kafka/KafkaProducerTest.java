/*******************************************************************
*
* Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	KafkaProducerTest.java
* Author:	S Padgaonkar
* Date:	25-Sept-2018
*
*******************************************************************
*/
package com.smartstreamrdu.service.kafka;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.message.UdmMessageKey;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.util.Constant.Component;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class KafkaProducerTest {

	MockProducer<String, String> producer;

	KafkaProducer kafkaProducer;

	@Autowired
	KafkaConfiguration configuration;

	@Before
	public void init() {
		kafkaProducer = SpringUtil.getBean(KafkaProducer.class);
		producer = new MockProducer<String, String>(true, new StringSerializer(), new StringSerializer());
		kafkaProducer.producer = producer;
	}

	@Test
	public void TestMessage() throws Exception {
		UdmMessageKey udmMessageKey = UdmMessageKey.builder().action("udl").variableAttributeValue("val").build();
		DefaultMessage message = (DefaultMessage) new DefaultMessage.Builder().key(udmMessageKey).source(Component.UDL)
				.target(Component.XRF).data("{\"key\":\"val\"}").build();
     	Assert.assertNotNull(kafkaProducer.sendMessage(message).get());
		Assert.assertNotNull(kafkaProducer.sendMessageSync(message));
	}
}
