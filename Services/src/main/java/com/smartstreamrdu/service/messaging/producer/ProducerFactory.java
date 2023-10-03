/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ProducerFactory.java
 * Author:	Jay Sangoi
 * Date:	15-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.messaging.producer;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.util.Constant;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class ProducerFactory implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4099359875870729867L;
	@Autowired
	private Map<String, Producer<?>> producers;
	
	public Producer<?> getProducer(ProducerEnum producerEnum){
		return producers.get(producerEnum.name() + Constant.MessagingConstant.PRODUCER_BEAN_SUFFIX);
	}
	
}
