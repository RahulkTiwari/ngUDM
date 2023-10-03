/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdIvoSegregator.java
 * Author : SaJadhav
 * Date : 22-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.HistorizedData;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;


/**
 * OPS lock on non-xrf attributes and embedded Legal entity attributes should go to centralized collection
 * Populates IvoContainer.sdIvoContainer
 * @author SaJadhav
 *
 */
@Component
public class SdIvoSegregator extends AbstractIvoSegregator {
	
	@Autowired
	private IvoInlineAttributesService ivoInlineAttrService;
	
	

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.AbstractIvoSegragator#populateIvoContainer(com.smartstreamrdu.service.ivo.IvoContainer, com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataValue)
	 */
	@Override
	protected void populateIvoContainer(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<Serializable> dataValue, Map<DataLevel,String> dataLevelVsObjectIdMap, Map<DataLevel,String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException {
		if (isNestedArrayAttribute(dataAttribute)) {
			ivoContainer.addToEmbeddedRelation(dataAttribute, dataValue, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap, DataLevel.IVO_INS);
		} else {
			HistorizedData<Serializable> rduValue = dataValue.getLockValue(LockLevel.RDU);
			DataValue<Serializable> newValue = new DataValue<>();
			newValue.setValue(LockLevel.RDU, rduValue.getValue(), rduValue.getLockLevelInfo());
			ivoContainer.addToDataContainer(dataAttribute, dataValue, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap, DataLevel.IVO_INS);
		}

	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.AbstractIvoSegragator#isApplicable(com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataValue)
	 */
	@Override
	protected boolean isApplicable(DataAttribute dataAttribute, DataValue<Serializable> dataValue,Map<DataLevel,String> dataLevelVsObjectIdMap) {
		Objects.requireNonNull(dataAttribute, "DataAttribute should be populated");
		Objects.requireNonNull(dataValue, "dataValue should be populated");
		DataLevel attributeLevel = dataAttribute.getAttributeLevel();
		if(isNestedArrayAttribute(dataAttribute)){
			return DataLevel.INS.equals(attributeLevel);
		}else{
			HistorizedData<Serializable> rduValue = dataValue.getLockValue(LockLevel.RDU);
			return rduValue!=null && (!DataLevel.LE.equals(attributeLevel) && !isXrfMatchingAttribute(dataAttribute, attributeLevel));
		}
		
	}

	/**
	 * Checks whether the attribute is embedded i.e. it's type is NESTED_ARRAY
	 * @param dataAttribute
	 * @return
	 */
	private boolean isNestedArrayAttribute(DataAttribute dataAttribute) {
		return DataType.NESTED_ARRAY.equals(dataAttribute.getDataType());
	}


	private boolean isXrfMatchingAttribute(DataAttribute dataAttribute,
			DataLevel attributeLevel) {
		return (DataLevel.INS.equals(attributeLevel) || DataLevel.SEC.equals(attributeLevel))
				&& ivoInlineAttrService.isInlineAttribute(dataAttribute);
	}

}
