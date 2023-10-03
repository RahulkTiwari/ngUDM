/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	NormalizedValueService.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.normalized;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DomainType;

/**
 * @author Jay Sangoi
 *
 */
public interface NormalizedValueService extends Serializable {
	/**
	 * Get the normalized data
	 * @param dataAttribute
	 * @param domainData
	 * @param dataSource
	 * @return
	 */
	Serializable getNormalizedValueForDomainValue(DataAttribute dataAttribute, DomainType domainData, String dataSource);
}
