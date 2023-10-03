/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	Message.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.messaging;

import java.io.Serializable;

import com.smartstreamrdu.util.Constant.Component;

/**
 * Message format that needs to be generated to be send to another component 
 * @author Jay Sangoi
 */
public interface Message extends Serializable{

	/**
	 * Actual data that needs to be send to components
	 * @return
	 */
	String getData();	
	
	
	Integer getPartition();
	
	/**
	 * 
	 * @return
	 */
	String getKey();
	
	/**
	 * Get the source component who has generated the message
	 * @return
	 */
	Component getSource();
	
	/**
	 * Get the target component on which message will be send
	 * @return
	 */
	Component getTarget();
	
	/**
	 * Get the Process for which message will be send
	 * @return
	 */
	com.smartstreamrdu.util.Constant.Process getProcess();
}
