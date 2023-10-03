/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdLeSegregator.java
 * Author : SaJadhav
 * Date : 24-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.HistorizedData;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * RDU locks on Legal entity will be stored inline.
 * Populates IvoContainer.sdLeContainer
 * @author SaJadhav
 *
 */
@Component
public class SdLeSegregator extends AbstractIvoSegregator {

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	protected void populateIvoContainer(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap,
			Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException {
		if (isNestedArrayAttribute(dataAttribute)) {
			ivoContainer.addToEmbeddedRelation(dataAttribute, dataValue, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap, DataLevel.LE);
		}else{
			HistorizedData<Serializable> rduValue = dataValue.getLockValue(LockLevel.RDU);
			DataValue<Serializable> newValue = new DataValue<>();
			newValue.setValue(LockLevel.RDU, rduValue.getValue(), rduValue.getLockLevelInfo());
			ivoContainer.addToDataContainer(dataAttribute, dataValue, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap, DataLevel.LE);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.ivo.AbstractIvoSegragator#isApplicable(com.
	 * smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataValue,
	 * java.lang.String)
	 */
	@Override
	protected boolean isApplicable(DataAttribute dataAttribute, DataValue<Serializable> dataValue,
			Map<DataLevel, String> dataLevelVsObjectIdMap) {
		Objects.requireNonNull(dataAttribute, "DataAttribute should be populated");
		Objects.requireNonNull(dataValue, "dataValue should be populated");
		DataLevel attributeLevel = dataAttribute.getAttributeLevel();
		if(isNestedArrayAttribute(dataAttribute)){
			return DataLevel.LE.equals(attributeLevel);
		}else{
			HistorizedData<Serializable> rduValue = dataValue.getLockValue(LockLevel.RDU);
			return rduValue != null && DataLevel.LE.equals(attributeLevel);
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

}
