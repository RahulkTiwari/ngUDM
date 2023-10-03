/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	SparkOperation.java
 * Author:	Jay Sangoi
 * Date:	24-Oct-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.spark;

/**
 * This is the interface for all the Spark function
 * @author Jay Sangoi
 *
 */
public interface SparkFunction<I,O> {

	/**
	 * Actual implementation of the operation
	 * @param input
	 * @return
	 */
	O call(I input) throws Exception;
	
}
