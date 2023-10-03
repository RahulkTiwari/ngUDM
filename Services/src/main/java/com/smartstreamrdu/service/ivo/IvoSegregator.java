/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoSegregator.java
 * Author : SaJadhav
 * Date : 22-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Map;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author SaJadhav
 *
 */
public interface IvoSegregator {

	/**
	 * Populates the {@code dataAttribute} and its {@code dataValue} in
	 * appropriate dataContainer in ivoContainer based on below mentioned
	 * conditions
	 * 
	 * <PRE>
	 * If {@code dataAttribute} is attribute used for XRF matching and
	 * {@code dataValue} contains value at <tt>LockLevel.RDU<tt> then populate
	 * <tt>ivoContainer.sdContainer<tt>
	 * 
	 * <PRE>
	 * If {@code dataAttribute} is attribute not used for XRF matching and
	 * {@code dataValue} contains value at <tt>LockLevel.RDU<tt> , OR
	 * {@code dataAttribute} is embedded Legal entity attribute then populate
	 * <tt>ivoContainer.sdIvoContainer<tt>
	 * 
	 * <PRE>
	 * If {@code dataAttribute} is Attribute of separate Legal Entity the
	 * populate <tt>ivoContainer.sdLeContainer<tt>
	 *
	 * @param ivoContainer
	 * @param dataAttribute
	 * @param dataValue
	 */
	void segregateIvos(IvoContainer ivoContainer, DataAttribute dataAttribute, DataValue<Serializable> dataValue,
			Map<DataLevel,String> dataLevelVsObjectIdMap, Map<DataLevel,String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException;

}
