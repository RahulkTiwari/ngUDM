/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	BatchHandler.java
 * Author:	Jay Sangoi
 * Date:	11-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.io.Serializable;
import java.util.Set;

import org.apache.spark.api.java.JavaPairRDD;

import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * Callback Interface when spark streaming receives a message 
 * @author Jay Sangoi
 *
 */
public interface BatchHandler<K extends Serializable,V extends Serializable> extends Serializable {
	
	/**
	 * Method that will be called with the rdd record
	 * @param rdd
	 * @param topicName
	 * @throws UdmTechnicalException  
	 */
	void handleBatch(JavaPairRDD<K, V>  rdd, Set<String> topicName) throws UdmTechnicalException;
}
