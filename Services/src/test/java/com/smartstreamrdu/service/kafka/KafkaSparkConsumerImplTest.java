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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.spark.SparkUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class KafkaSparkConsumerImplTest {
	
	private static final Logger _logger = LoggerFactory.getLogger(KafkaSparkConsumerImplTest.class);
	
	@Autowired
	KafkaSparkConsumerImpl consumer;
	
	@Autowired
	private SparkUtil sparkUtil;
	
	@Test
	public void test() throws InterruptedException
	{
			CountDownLatch latch = new CountDownLatch(1);
			Runnable r = new Runnable() {
		         public void run() {
		        
		        		 try {
							testKafkaSparkConsumer();
						} catch (InterruptedException e) {
							_logger.error("Error Occured in KafkaSparkInputConsumerTest", e);
						}				
		         }
		     };
		     new Thread(r).start();
		   latch.await(60,TimeUnit.SECONDS);
	}	
	
	@SuppressWarnings({ "unchecked" })
	public void testKafkaSparkConsumer() throws InterruptedException
	{
		
		@SuppressWarnings("rawtypes")
		KafkaSparkConsumerInput input = new KafkaSparkConsumerInput();
		input.setGroupId("abc");
		input.addTopic("DisQueue");
		input.setHandler((rdd, topicName) -> {

		});
		consumer.start(input);
	}	
	
	@After
	public void closeSparkSession(){
		SparkSession sparkSession = sparkUtil.getSparkContext();
		if(!sparkSession.sparkContext().isStopped()){
			sparkSession.close();
		}
	}
}
