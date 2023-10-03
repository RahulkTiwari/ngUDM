/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdContainerAttributeAdditionHandler.java
 * Author : SaJadhav
 * Date : 12-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;

/**
 * Adds attribute at the IvoContainer.sdContainer 
 * @author SaJadhav
 *
 */
@Component
public class SdContainerAttributeAdditionHandler implements IvoContainerAttributeAdditionHandler {
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.IvoContainerAttributeAdditionHandler#addAttribute(com.smartstreamrdu.service.ivo.IvoContainer, com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataValue, java.util.Map, java.util.Map)
	 */
	@Override
	public void addAttribute(IvoContainer ivoContainer, DataAttribute dataAttribute,
			DataValue<? extends Serializable> dataValue, Map<DataLevel, String> dataLevelVsObjectIdMap,
			Map<DataLevel, String> dataLevelVsSourceUniqueIdMap) {
		Objects.requireNonNull(ivoContainer,"ivoContainer should be populated");
		Objects.requireNonNull(dataAttribute,"dataAttribute should be populated");
		
		Optional<DataContainer> sdContainerOptional = ivoContainer.getSdContainer();
		DataContainer sdContainer=sdContainerOptional.isPresent()?sdContainerOptional.get():null;
		if (sdContainer==null) {
			sdContainer=initAndGetSdContainer(dataLevelVsObjectIdMap.get(DataLevel.INS), dataLevelVsSourceUniqueIdMap.get(DataLevel.INS),
					ivoContainer);
		}

		if (dataAttribute.getAttributeLevel().equals(DataLevel.SEC)) {
			addToSdSecurityContainer(dataAttribute, dataValue, dataLevelVsObjectIdMap.get(DataLevel.SEC),
					dataLevelVsSourceUniqueIdMap.get(DataLevel.SEC), sdContainer, ivoContainer.getContainerContext());
		} else {
			sdContainer.addAttributeValue(dataAttribute, dataValue);
		}
	}
	
	private DataContainer initAndGetSdContainer(String objectId, String sourceUniqueId, IvoContainer ivoContainer) {
		DataContainer sdContainer = IvoContainerHelper.createNewDataContainer(ivoContainer.getSdDocumentId(), DataLevel.INS,
				ivoContainer.getDataSourceId(), objectId, sourceUniqueId, ivoContainer.getContainerContext());
		ivoContainer.setSdContainer(sdContainer);
		return sdContainer;
	}
	
	
	
	private void addToSdSecurityContainer(DataAttribute dataAttribute, DataValue<? extends Serializable> dataValue,
			String objectId, String sourceUniqueId, DataContainer sdContainer, DataContainerContext dataContainerContext) {
		DataContainer childContainer = getChildContainer(sdContainer, DataLevel.SEC, objectId);
		if (childContainer == null) {
			childContainer = IvoContainerHelper.createNewDataContainer(null, DataLevel.SEC, null, objectId, sourceUniqueId, dataContainerContext);
			sdContainer.addDataContainer(childContainer, DataLevel.SEC);
		}
		childContainer.addAttributeValue(dataAttribute, dataValue);
	}
	
	/**
	 * gets matching  security container based on {@code objectId}
	 */
	private DataContainer getChildContainer(DataContainer container, DataLevel dataLevel, String objectId) {
		Objects.requireNonNull(objectId, "objectId should be populated");
		DataContainer childContainer = null;
		List<DataContainer> childContainers = container.getChildDataContainers(dataLevel);
		if (!CollectionUtils.isEmpty(childContainers)) {
			List<DataContainer> filteredChilds = childContainers.stream().filter(dataContainer -> {
				String objectIdVal = container.getHighestPriorityValue(DataAttributeFactory.getObjectIdIdentifierForLevel(dataLevel));
				return objectId.equals(objectIdVal);
			}).collect(Collectors.toList());
			childContainer = CollectionUtils.isEmpty(filteredChilds) ? null : filteredChilds.get(0);
		}
		return childContainer;
	}

}
