/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: AbstractIvoSegregator.java
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
 *Template implementation for IvoSegregator
 *Concrete implementations should extend this class 
 * @author SaJadhav
 *
 */
public abstract class AbstractIvoSegregator implements IvoSegregator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void segregateIvos(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<Serializable> dataValue, Map<DataLevel,String> dataLevelVsObjectIdMap, Map<DataLevel,String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException {
		if (isApplicable(dataAttribute, dataValue, dataLevelVsObjectIdMap)){
			populateIvoContainer(ivoContainer, dataAttribute, dataValue, dataLevelVsObjectIdMap,dataLevelVsSourceUniqueIdMap);
		}
			

	}

	/**
	 * populates the dataAttribute value in respective DataContainer in IvoContainer
	 * 
	 * @param ivoContainer
	 * @param dataAttribute
	 * @param dataValue
	 * @param dataLevelVsObjectIdMap 
	 * @param dataLevelVsSourceUniqueIdMap 
	 */
	protected abstract  void populateIvoContainer(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap, Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException;

	/**
	 * Checks whether the seggregator is applicable for the dataAttribute and dataValue 
	 * 
	 * @param dataAttribute
	 * @param dataValue
	 * @param dataLevelVsObjectIdMap 
	 * @return
	 */
	protected abstract  boolean isApplicable(DataAttribute dataAttribute,DataValue<Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap);
}
