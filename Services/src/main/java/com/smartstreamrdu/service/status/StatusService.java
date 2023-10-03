/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StatusService.java
 * Author:	Jay Sangoi
 * Date:	12-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.status;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataContainer;

/**
 * Service which is related to Level Status, for example instrument status, security status, etc
 * @author Jay Sangoi
 *
 */
public interface StatusService extends Serializable {
	
	/**
	 * Checks whether the data container is inactive for the datasource
	 * @param container
	 * @return
	 */
	boolean isStatusInactive(DataContainer container, String datasource);
		
}
