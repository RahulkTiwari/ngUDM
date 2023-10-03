/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaConfiguration.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.io.Serializable;
import java.util.Properties;

import com.smartstreamrdu.util.Constant.Component;

/**
 * @author Jay Sangoi
 *
 */
public interface KafkaConfiguration extends Serializable{

	/**
	 * create the Kafka Producer configuration
	 * @return
	 */
	Properties createProducerConfiguration();

	/**
	 * create the Kafka Consumer configuration
	 * @return
	 */
	Properties createConsumerConfiguration();

	
	/**
	 * Get the topic name for the source and target component
	 * @param source
	 * @param target
	 * @return
	 */
	String getTopicName(com.smartstreamrdu.util.Constant.Process process, Component source, Component target);
	
	/**
	 * Return the time for polling the kafka topic
	 * @param component
	 * @return
	 */
	long getPollTimeForComponent(Component component);

}
