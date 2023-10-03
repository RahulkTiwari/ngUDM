/**
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoContainer.java
 * Author : SaJadhav
 * Date : 12-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.service.SpringUtil;

import lombok.Data;

/**
 * Container for holding inline and centralized locks.
 *<p><tt>sdContainer</tt> : holds inline OPS locks on INS/SEC attributes</p>
 *<p><tt>sdIvoContainer</tt>: holds centralized OPS locks on INS/SEC and embedded Legal entity attributes</p>
 *<p><tt>sdLeContainer</tt>: holds inline OPS locks on Legal Entity attributes</p>
 * <p><tt>clientContainer</tt>: holds centralized client locks</p>
 * @author SaJadhav
 *
 */
@Data
public class IvoContainer {

	private DataContainer sdContainer;

	private DataContainer sdIvoContainer;
	private DataContainer sdLeContainer;
	
	private DataContainer enDataContainer;
	// document level attributes
	private final String sdDocumentId;
	private final String dataSourceId;
	// related XRF DataContainer
	private DataContainer xrDataContainer;
	
	private DataContainerContext containerContext;

	public IvoContainer(String documentId, String dataSourceId, DataContainerContext containerContext) {
		Objects.requireNonNull(dataSourceId, "dataSourceId should be populated");
		this.sdDocumentId = documentId;
		this.dataSourceId = dataSourceId;
		this.containerContext = containerContext;
	}

	/**
	 * @return the sdContainer
	 */
	public Optional<DataContainer> getSdContainer() {
		return Optional.ofNullable(sdContainer);
	}

	/**
	 * @return the sdIvoContainer
	 */
	public Optional<DataContainer> getSdIvoContainer() {
		return Optional.ofNullable(sdIvoContainer);
	}

	/**
	 * @return the sdLeContainer
	 */
	public Optional<DataContainer> getSdLeContainer() {
		return Optional.ofNullable(sdLeContainer);
	}
	
	/**
	 * @return the enDataContainer
	 */
	public Optional<DataContainer> getEnDataContainer(){
		return Optional.ofNullable(enDataContainer);
	}


	/**
	 * Adds attribute to the respective dataContainer in IvoContainer based on {@code dataLevelToAdd}.
	 * DataLevel.INS or DataLevel.SEC attributes will be added to IvoContainer.sdContainer.
	 * DataLevel.IVO_INS/IVO_SEC attributes will be added to IvoContainer.sdIvoContainer.
	 * DataLeve.LE will be added to IvoContainer.sdLeCOntainer
	 * 
	 * @param dataAttribute
	 * @param dataValue
	 * @param dataLevelVsObjectIdMap
	 * @param dataLevelVsSourceUniqueIdMap
	 * @param dataLevelToAdd
	 * @throws UdmTechnicalException
	 */
	public void addToDataContainer(DataAttribute dataAttribute, DataValue<? extends Serializable> dataValue,
			Map<DataLevel, String> dataLevelVsObjectIdMap, Map<DataLevel, String> dataLevelVsSourceUniqueIdMap,
			DataLevel dataLevelToAdd) throws UdmTechnicalException {
		IvoContainerAttributeAdditionHandler attributeAddHandler = getAttributeAdditionHandler(dataLevelToAdd);
		attributeAddHandler.addAttribute(this, dataAttribute, dataValue, dataLevelVsObjectIdMap,
				dataLevelVsSourceUniqueIdMap);
	}
	
	
	/**
	 * Adds dataContainer to IvoContainer based on dataContainer level
	 * 
	 * @param dataContainer
	 */
	public void setDataContainer(DataContainer dataContainer) {
		Objects.requireNonNull(dataContainer,"dataContainer should be populated");
		DataLevel level = dataContainer.getLevel();
		switch (level) {
		case EN:
			this.setEnDataContainer(dataContainer);
			break;
		case INS:
			this.setSdContainer(dataContainer);
			break;
		case LE:
			this.setSdLeContainer(dataContainer);
			break;
		default:
			break;
		}
	}

	/**
	 * Adds attributes at  the NESTED_ARRAY level like instrumentRelations ,instrumentLegalEntityRelations based on 
	 * {@code dataLevelToAdd}
	 * 
	 * @param dataAttribute
	 * @param dataValue
	 * @param dataLevelVsObjectIdMap
	 * @param dataLevelVsSourceUniqueIdMap
	 * @param dataLevelToAdd
	 * @throws UdmTechnicalException
	 */
	public void addToEmbeddedRelation(DataAttribute dataAttribute, DataValue<? extends Serializable> dataValue,
			Map<DataLevel, String> dataLevelVsObjectIdMap, Map<DataLevel, String> dataLevelVsSourceUniqueIdMap,
			DataLevel dataLevelToAdd) throws UdmTechnicalException {
		IvoContainerAttributeAdditionHandler attributeAddHandler = getAttributeAdditionHandler(dataLevelToAdd);
		attributeAddHandler.addAttributToEmbeddedRelation(this, dataAttribute, dataValue, dataLevelVsObjectIdMap,
				dataLevelVsSourceUniqueIdMap);
	}

	private IvoContainerAttributeAdditionHandler getAttributeAdditionHandler(DataLevel dataLevelToAdd) {
		IvoContainerAttributeAdditionHandlerFactory ivoContainerAttributeHandlerFactory = SpringUtil
				.getBean(IvoContainerAttributeAdditionHandlerFactory.class);
		return ivoContainerAttributeHandlerFactory.getIvoContainerAttributeAdditionHandler(dataLevelToAdd);
	}


}
