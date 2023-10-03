/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	CreateDataContainerAudit.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.audit;


import com.smartstreamrdu.domain.DataContainer;

/**
 * @author Jay Sangoi
 *
 */
@FunctionalInterface
public interface CreateDataContainerAudit {

	/**
	 * Create the audit info based on data container isNew and hasChanged atributes
	 * @param mergedContainer
	 * @return
	 */
	void create(DataContainer mergedContainer);
	
}
