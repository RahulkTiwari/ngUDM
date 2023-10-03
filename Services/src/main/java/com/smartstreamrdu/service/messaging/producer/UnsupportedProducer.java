/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UnsupportedProducer.java
 * Author:	Jay Sangoi
 * Date:	15-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.messaging.producer;

import java.util.concurrent.Future;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.service.messaging.Message;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class UnsupportedProducer implements Producer<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8657070204991397068L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.messaging.producer.Producer#sendMessage(com.smartstreamrdu.service.messaging.Message)
	 */
	@Override
	public Future<Object> sendMessage(Message message) throws Exception {
		throw new UnsupportedOperationException("Producer");
	}

	@Override
	public Object sendMessageSync(Message message) throws Exception {
		throw new UnsupportedOperationException("Producer");
	}

}
