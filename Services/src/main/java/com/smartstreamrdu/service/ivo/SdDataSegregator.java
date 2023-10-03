/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: sDIvoSegragator.java
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
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.HistorizedData;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * OPS lock on Instrument/Security attributes used for XRF matching to be stored inline
 * Populates IvoContainer.sdContainer
 * @author SaJadhav
 *
 */
@Component
public class SdDataSegregator extends AbstractIvoSegregator {
	
	@Autowired
	private IvoInlineAttributesService ivoInlineAttributeService;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.AbstractIvoSegragator#populateIvoContainer(com.smartstreamrdu.service.ivo.IvoContainer, com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataValue)
	 */
	@Override
	protected void populateIvoContainer(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<Serializable> dataValue,Map<DataLevel,String> dataLevelVsObjectIdMap,Map<DataLevel,String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException {
		Objects.requireNonNull(dataAttribute, "ivoContainer should be populated");
		HistorizedData<Serializable> rduValue = dataValue.getLockValue(LockLevel.RDU);
		DataValue<Serializable> newValue=new DataValue<>();
		newValue.setValue(LockLevel.RDU, rduValue.getValue(),rduValue.getLockLevelInfo());
		ivoContainer.addToDataContainer(dataAttribute, dataValue, dataLevelVsObjectIdMap, dataLevelVsSourceUniqueIdMap, DataLevel.INS);

	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.AbstractIvoSegragator#isApplicable(com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataValue)
	 */
	@Override
	protected boolean isApplicable(DataAttribute dataAttribute, DataValue<Serializable> dataValue,Map<DataLevel,String> dataLevelVsObjectIdMap) {
		Objects.requireNonNull(dataAttribute, "DataAttribute should be populated");
		Objects.requireNonNull(dataValue, "dataValue should be populated");
		DataLevel attributeLevel = dataAttribute.getAttributeLevel();
		 HistorizedData<Serializable> rduValue = dataValue.getLockValue(LockLevel.RDU);
		return rduValue!=null && (DataLevel.INS.equals(attributeLevel) || DataLevel.SEC.equals(attributeLevel))
				&& ivoInlineAttributeService.isInlineAttribute(dataAttribute);
	}

}
