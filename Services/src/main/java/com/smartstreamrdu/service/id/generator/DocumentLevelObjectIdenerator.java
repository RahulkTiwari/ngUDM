/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DocumentLevelObjectIdenerator.java
 * Author:	Jay Sangoi
 * Date:	25-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.id.generator;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * @author Jay Sangoi
 *
 */
@Component	
public class DocumentLevelObjectIdenerator implements ObjectIdGenerator<Serializable>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1504962178807934850L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.id.generator.ObjectIdGenerator#generateUniqueId()
	 */
	@Override
	public String generateUniqueId() {
		return UUID.randomUUID().toString();
	}

}
