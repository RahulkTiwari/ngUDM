/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: LockRemover.java
* Author : VRamani
* Date : Mar 5, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;

/**
* @author VRamani
*
*/
public interface LockRemover {
	
	/**
	 * Used to remove value at lock level of XRF attributes if it matches with the FEED value.
	 * @param dataAttribute 
	 * 
	 * @param dataValue
	 * @param dbValue 
	 * @param dataSource 
	 */
	void removeLockValue(DataAttribute dataAttribute, DataValue<Serializable> feedValue, DataValue<Serializable> dbValue, DomainType dataSource);
}
