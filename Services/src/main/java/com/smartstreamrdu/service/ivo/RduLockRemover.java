/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: RduLockRemover.java
* Author : VRamani
* Date : Mar 5, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;

/**
* @author VRamani
*
*/
@Component
public class RduLockRemover extends AbstractLockRemover implements LockRemover {

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.LockRemover#removeLockValue(com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataValue)
	 */
	@Override
	public void removeLockValue(DataAttribute dataAttribute, DataValue<Serializable> feedVal,
			DataValue<Serializable> dataValue,DomainType dataSource) {
		Serializable feedValue = dataValue.getValue(LockLevel.FEED);
		Serializable rduValue = dataValue.getValue(LockLevel.RDU);
		if (feedValue != null && rduValue != null && feedValue.equals(rduValue)) {
			dataValue.removeLockValue(LockLevel.RDU);
		}
	    checkAndRemoveLockForDomainAttribute(dataAttribute,dataValue,LockLevel.RDU,dataSource);
	}

}
