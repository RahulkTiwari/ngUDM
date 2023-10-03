/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaSparkConsumerInput.java
 * Author:	Jay Sangoi
 * Date:	11-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Input for Kafka Spark Consumer
 * @author Jay Sangoi
 *
 */
public class KafkaSparkConsumerInput<K extends Serializable,V extends Serializable> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8327399314043413186L;

	private String groupId;
	
	private Collection<String> topics;
	
	/**
	 * 
	 */
	private BatchHandler<K,V> handler;


	/**
	 * @return the topics
	 */
	public Collection<String> getTopics() {
		return topics;
	}
	
	public void addTopic(String topic){
		if(topics == null){
			topics = new ArrayList<>();
		}
		topics.add(topic);
	}


	/**
	 * @return the handler
	 */
	public BatchHandler<K, V> getHandler() {
		return handler;
	}


	/**
	 * @param handler the handler to set
	 */
	public void setHandler(BatchHandler<K, V> handler) {
		this.handler = handler;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	
}
