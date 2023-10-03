/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	Auditservice.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.audit;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * Service which is related to audit. It has operation like creating object level audit etc.
 * 
 * @author Jay Sangoi
 *
 */
public interface AuditService {

	/**
	 * This will create audit at object level. {@link - Audit}
	 * It will check if the container is new, if yes then it will add an audit object with index 0
	 * If the Data container is not new, then it will check, if there is any update, if yes then it will append new ausit in audit array 
	 * @param mergedContainer
	 */
	void createAudit(DataContainer mergedContainer);
	
	/**
	 * This will create audit at object level. {@link - Audit}
	 * It will check if the container is new, if yes then it will add an audit object with index 0
	 * If the Data container is not new, then it will check, if there is any update, if yes then it will append new ausit in audit array 
	 * @param mergedContainers
	 */
	void createAudit(List<DataContainer> mergedContainers);
	
}
