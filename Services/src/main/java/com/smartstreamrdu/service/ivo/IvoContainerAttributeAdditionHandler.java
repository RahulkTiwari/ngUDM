/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoContainerAttributeAdditionHandler.java
 * Author : SaJadhav
 * Date : 12-Apr-2019
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
 * Service for adding attributes in IvoContainer object in respective dataContainers.
 * @author SaJadhav
 *
 */
public interface IvoContainerAttributeAdditionHandler {
	
	/**
	 * Adds attribute to the respective dataContainer in IvoContainer.
	 * If that dataContainer is not present then it will create new dataContainer and add to IvoContainer.
	 * <pre>e.g. If attribute to added at INS level and IvoContainer.sdContainer is null then 
	 * it creates new sdContainer and adds it to IvoContainer.</pre>
	 * 
	 * @param ivoContainer
	 * @param dataAttribute
	 * @param dataValue
	 * @param dataLevelVsObjectIdMap
	 * @param dataLevelVsSourceUniqueIdMap
	 * @throws UdmTechnicalException
	 */
	void addAttribute(IvoContainer ivoContainer,DataAttribute dataAttribute,DataValue<? extends Serializable> dataValue,
			Map<DataLevel,String> dataLevelVsObjectIdMap,Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException;
	
	/**
	 *  Adds attributes at  the NESTED_ARRAY level like instrumentRelations ,instrumentLegalEntityRelations
	 *  
	 * @param ivoContainer
	 * @param dataAttribute
	 * @param dataValue
	 * @param dataLevelVsObjectIdMap
	 * @param dataLevelVsSourceUniqueIdMap
	 * @throws UdmTechnicalException
	 */
	default void addAttributToEmbeddedRelation(IvoContainer ivoContainer,DataAttribute dataAttribute,DataValue<? extends Serializable> dataValue,
			Map<DataLevel,String> dataLevelVsObjectIdMap,Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException{
		//do nothing in default implementation
	}
}
