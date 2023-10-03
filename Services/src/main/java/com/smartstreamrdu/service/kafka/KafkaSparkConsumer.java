/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaSparkConsumer.java
 * Author:	Jay Sangoi
 * Date:	15-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.io.Serializable;

/**
 * @author Jay Sangoi
 *
 */
public interface KafkaSparkConsumer extends Serializable{


	/**
	 *  Start reading from Kafka topic
	 * @param input
	 * @throws InterruptedException
	 */
	<K extends Serializable,V  extends Serializable> void start(KafkaSparkConsumerInput<K, V> input) throws InterruptedException;

}
