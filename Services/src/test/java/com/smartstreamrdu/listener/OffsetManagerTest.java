/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	OffsetManagerTest.java
 * Author:	S Padgaonkar
 * Date:	11-Sept-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.listener;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.kafka.KafkaOffsetMessage;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.kafka.OffsetManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class OffsetManagerTest extends AbstractEmbeddedMongodbJunitParent{

	@Autowired
	private transient MongoTemplate mongoTemplate;

	@Autowired
	OffsetManager offsetManager;

	private void ensureMongoTemplate() {
		if (mongoTemplate == null) {
			mongoTemplate = SpringUtil.getBean(MongoTemplate.class);
		}
	}
	
	public long readOffsetFromExternalStore() {
		long digit = offsetManager.readOffsetFromExternalStore("testQueue", 0);
		return digit;
	}

	@Test
	@ModifiedCollections(collections = { "kafkaOffsetMessage" })
	public void readOffsetFromStorageTest() {
		long digit = readOffsetFromExternalStore();
		offsetManager.saveOffsetInExternalStore("testQueue", 0, digit);
		Assert.assertEquals(1, readOffsetFromExternalStore());
	}
	
	@Test
	@ModifiedCollections(collections = { "kafkaOffsetMessage" })
	public void getAllOffsetTest()
	{
		KafkaOffsetMessage message = new KafkaOffsetMessage("testQueue1", 0, 1);
		
		KafkaOffsetMessage message1 = new KafkaOffsetMessage("testQueue1", 0, 2);
		
		List<KafkaOffsetMessage> offsets = new ArrayList<KafkaOffsetMessage>();
		offsets.add(message);
		offsets.add(message1);
		offsetManager.saveOffsetInExternalStore(offsets);
		
		List<KafkaOffsetMessage> retriveOffset  = offsetManager.getAllOffset("testQueue1");
		
		Assert.assertNotNull(retriveOffset);
		Assert.assertEquals(2,retriveOffset.get(0).getOffset());
		
	}
}
