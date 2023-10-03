/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaConfigurationImplTest.java
 * Author:	S Padgaonkar
 * Date:	25-Sept-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.Constant.Component;
import com.smartstreamrdu.util.Constant.Process;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class KafkaConfigurationImplTest {
	
	@Autowired
	KafkaConfiguration configuration;

	@Test
	public void testProducerConfiguration()
	{
		Properties producer =configuration.createProducerConfiguration();
		Assert.assertEquals("localhost:9092",producer.getProperty("bootstrap.servers"));
		Assert.assertEquals("org.apache.kafka.common.serialization.StringSerializer",producer.getProperty("key.serializer"));
		Assert.assertEquals("org.apache.kafka.common.serialization.StringSerializer",producer.getProperty("value.serializer"));
	}
	
	@Test
	public void testConsumerConfiguration()
	{
		Properties consumer =configuration.createConsumerConfiguration();
		Assert.assertEquals("localhost:9092",consumer.getProperty("bootstrap.servers"));
		Assert.assertEquals( org.apache.kafka.common.serialization.StringDeserializer.class,consumer.get("key.deserializer"));
		Assert.assertEquals(org.apache.kafka.common.serialization.StringDeserializer.class,consumer.get("value.deserializer"));
		Assert.assertEquals("earliest",consumer.getProperty("auto.offset.reset"));
		Assert.assertEquals(false,consumer.get("enable.auto.commit"));
		Assert.assertEquals("2000",consumer.getProperty("heartbeat.interval.ms"));
		Assert.assertEquals("86400000",consumer.getProperty("session.timeout.ms"));
	}
	
	@Test
	public void testGetTopicName()
	{
		Assert.assertEquals(null,configuration.getTopicName(Process.FileLoad, null, null));
		Assert.assertEquals("FigiRequestQueue",configuration.getTopicName(Process.FigiRequest, null, null));
		Assert.assertEquals("FigiPeriodicRefreshQueue",configuration.getTopicName(Process.PERIODIC_REFRESH, null, null));
		Assert.assertEquals("VfsRetryQueue",configuration.getTopicName(Process.VfsRetry, null, null));
		Assert.assertEquals("StreamingUdlQueue",configuration.getTopicName(Process.StreamingDataLoad, null, null));
		Assert.assertEquals("DisQueue",configuration.getTopicName(Process.DistributionStart, Component.UDL, Component.DISTRIBUTION));
		Assert.assertEquals("DataEnrichmentQueue", configuration.getTopicName(Process.LinkageJob, Component.DATA_ENRICHMENT, null));
		Assert.assertEquals("DataEnrichmentQueue", configuration.getTopicName(Process.RduLockRemovalJob, Component.DATA_ENRICHMENT, null));
		Assert.assertEquals("DomainChangeReprocessingQueue", configuration.getTopicName(Process.DOMAIN_CHANGE_REPROCESSING, Component.DATA_ENRICHMENT, null));
		Assert.assertEquals("LeUpdateProformaProcessingQueue", configuration.getTopicName(Process.LE_UPDATE_PROFORMA_PROCESSING, Component.DATA_ENRICHMENT, null));
		
	}
	
	@Test
	public void testGetPollTimeForComponent()
	{
		Assert.assertEquals(1000,configuration.getPollTimeForComponent(Component.DAQ));
	}
}
