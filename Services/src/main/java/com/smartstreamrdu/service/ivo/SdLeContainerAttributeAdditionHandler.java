/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdLeContainerAttributeAdditionHandler.java
 * Author : SaJadhav
 * Date : 12-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author SaJadhav
 *
 */
@Component
public class SdLeContainerAttributeAdditionHandler implements IvoContainerAttributeAdditionHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAttribute(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<? extends Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap,
			Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) {
		Objects.requireNonNull(ivoContainer,"ivoContainer should be populated");
		Objects.requireNonNull(dataAttribute,"dataAttribute should be populated");
		Optional<DataContainer> sdLeContainerOptional = ivoContainer.getSdLeContainer();
		DataContainer sdLeContainer=sdLeContainerOptional.isPresent()?sdLeContainerOptional.get():null;
		if (sdLeContainer==null) {
			sdLeContainer=initAndGetSdLeContainer(dataLevelVsObjectIdMap.get(DataLevel.LE), dataLevelVsSourceUniqueIdMap.get(DataLevel.LE),
					ivoContainer);
		} 
		sdLeContainer.addAttributeValue(dataAttribute, dataValue);

	}

	private DataContainer initAndGetSdLeContainer(String objectId, String sourceUniqueId, IvoContainer ivoContainer) {
		DataContainer sdLeContainer = IvoContainerHelper.createNewDataContainer(null, DataLevel.LE,
				ivoContainer.getDataSourceId(), objectId, sourceUniqueId, ivoContainer.getContainerContext());
		ivoContainer.setSdLeContainer(sdLeContainer);
		return sdLeContainer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAttributToEmbeddedRelation(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<? extends Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap,
			Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) throws UdmTechnicalException {
		Optional<DataContainer> sdLeContainerOptional = ivoContainer.getSdLeContainer();
		DataContainer sdLeContainer=sdLeContainerOptional.orElse(null);
		if(sdLeContainer==null){
			sdLeContainer=initAndGetSdLeContainer(dataLevelVsObjectIdMap.get(DataLevel.LE), dataLevelVsSourceUniqueIdMap.get(DataLevel.LE),
					ivoContainer);
		}
		DataAttribute relationTypeAttribute = DataAttributeFactory.getRelationTypeAttribute(dataAttribute);
		DataAttribute relationRefDataAttribute = DataAttributeFactory.getRelationRefDataAttribute(dataAttribute);
		DataRow dataRow = (DataRow) dataValue;
		Iterator<DataRow> dataRowIterator = dataRow.getValue().getValue().iterator();
		while (dataRowIterator.hasNext()) {
			DataRow row = dataRowIterator.next();
			String relationType = row.getHighestPriorityAttributeValue(relationTypeAttribute);
			DataRow ivoRefData = IvoContainerHelper.getEmbeddedRelationRefData(sdLeContainer,
					dataAttribute, relationType);
			DataRow refData = (DataRow) row.getAttributeValue(relationRefDataAttribute);
			mergeSdRefDataToIvoRefData(refData, ivoRefData);

		}
		
	}
	
	/**
	 * 
	 * @param sdRefData
	 * @param ivoRefData
	 * @param ivoAttrService
	 */
	private void mergeSdRefDataToIvoRefData(DataRow sdRefData, DataRow ivoRefData) {

		Map<DataAttribute, DataValue<Serializable>> rowData = sdRefData.getRowData();

		rowData.forEach((sdAttribute, value) -> {
			DataValue<Serializable> ivoDataValue = new DataValue<>();
			ivoDataValue.setValue(LockLevel.RDU, value.getValue(LockLevel.RDU));

			ivoRefData.addAttribute(sdAttribute, ivoDataValue);
		});
	}

}
