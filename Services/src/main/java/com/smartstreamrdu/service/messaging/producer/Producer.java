/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	Produceer.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.messaging.producer;

import java.io.Serializable;
import java.util.concurrent.Future;

import com.smartstreamrdu.service.messaging.Message;

/**
 * Generic interface to 
 * @author Jay Sangoi
 *
 */
public interface Producer<T> extends Serializable{
	/**
	 * Sends  the message 
	 * @param message
	 * @return
	 */
	Future<T> sendMessage(Message message) throws Exception;

	T sendMessageSync(Message message) throws Exception;

}
