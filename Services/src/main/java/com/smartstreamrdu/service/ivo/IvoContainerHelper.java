/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoContainerHelper.java
 * Author : SaJadhav
 * Date : 12-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;

/**
 * @author SaJadhav
 *
 */
public class IvoContainerHelper {
	
	private IvoContainerHelper(){
		
	}
	

	/**
	 * @param sdDocumentId2
	 * @param ins
	 * @param dataSourceId2
	 * @param objectId
	 * @param sourceUniqueId
	 * @return
	 */
	public  static DataContainer createNewDataContainer(String sdDocumentId, DataLevel dataLevel, String dataSourceId,
			String objectId, String sourceUniqueId, DataContainerContext dataContainerContext) {
		DataContainer container = new DataContainer(dataLevel, dataContainerContext);
		container.set_id(sdDocumentId);
		if (StringUtils.isNotEmpty(dataSourceId)) {
			DataValue<DomainType> dataSrcVal = new DataValue<>();
			dataSrcVal.setValue(LockLevel.FEED, new DomainType(dataSourceId));
			DataAttribute dataSourceAttribute = DataAttributeFactory.getDatasourceAttribute(dataLevel);
			container.addAttributeValue(dataSourceAttribute, dataSrcVal);
		}
		if (StringUtils.isNotEmpty(objectId)) {
			DataAttribute objectIdAttr = DataAttributeFactory.getObjectIdIdentifierForLevel(dataLevel);
			DataValue<String> objectIdVal = new DataValue<>();
			objectIdVal.setValue(LockLevel.FEED, objectId);
			container.addAttributeValue(objectIdAttr, objectIdVal);
		}
		if (StringUtils.isNotEmpty(sourceUniqueId)) {
			DataAttribute objectIdAttr = DataAttributeFactory.getSourceUniqueIdentifierForLevel(dataLevel);
			DataValue<String> sourceUniqueIdVal = new DataValue<>();
			sourceUniqueIdVal.setValue(LockLevel.FEED, sourceUniqueId);
			container.addAttributeValue(objectIdAttr, sourceUniqueIdVal);
		}
		return container;
	}
	
	/**
	 * Adds  a embedded relation in a Datacontainer for a relationType and for relation data attribute
	 * e.g instrumentRelations, instrumentLegalEntityRalations.
	 * 
	 * 
	 * @param sdIvoContainer
	 * @param relDataAttribute
	 * @param relationType
	 * @return
	 */
	public static  DataRow getEmbeddedRelationRefData(DataContainer sdIvoContainer, DataAttribute relDataAttribute,
			String relationType) {
		DataRow ivoRelValue = (DataRow) sdIvoContainer.getAttributeValue(relDataAttribute);
		DataAttribute relationTypeAttribute = DataAttributeFactory.getRelationTypeAttribute(relDataAttribute);
		DataAttribute relationRefDataAttribute = DataAttributeFactory.getRelationRefDataAttribute(relDataAttribute);
		DataRow ivoRefData = null;
		if (ivoRelValue != null && ivoRelValue.getValue() != null
				&& !CollectionUtils.isEmpty(ivoRelValue.getValue().getValue())) {
			ivoRefData=getMatchingRefData(sdIvoContainer, relDataAttribute, relationType, relationTypeAttribute,
					relationRefDataAttribute);
			if(ivoRefData==null){
				ivoRefData=addRelationToRelationsList(ivoRelValue, relDataAttribute, relationType);
			}
		} else {
			ivoRefData = addRelationAndGetRefData(relDataAttribute, relationType, sdIvoContainer);
		}
		return ivoRefData;
	}

	private static DataRow getMatchingRefData(DataContainer sdIvoContainer, DataAttribute relDataAttribute, String relationType,
			DataAttribute relationTypeAttribute, DataAttribute relationRefDataAttribute) {
		DataRowIterator iterator = new DataRowIterator(sdIvoContainer, relDataAttribute);
		while (iterator.hasNext()) {
			DataRow row = iterator.next();
			String ivoRelType = row.getHighestPriorityAttributeValue(relationTypeAttribute);
			if (relationType.equals(ivoRelType)) {
				return (DataRow) row.getAttributeValue(relationRefDataAttribute);
			}
		}
		return null;
	}

	/**
	 * @param relDataAttribute
	 * @param relationType
	 * @param sdIvoContainer2
	 * @return
	 */
	private static DataRow addRelationAndGetRefData(DataAttribute relDataAttribute, String relationType,
			DataContainer sdIvoContainer) {

		DataRow relLink = new DataRow(relDataAttribute);
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(relDataAttribute);

		DataValue<String> relationTypeVal = new DataValue<>();
		relationTypeVal.setValue(LockLevel.FEED, relationType);
		relLink.addAttribute(relationTypeAttr, relationTypeVal);

		DataAttribute refDataAttribute = DataAttributeFactory.getRelationRefDataAttribute(relDataAttribute);
		DataRow refDataRow = new DataRow(refDataAttribute);
		relLink.addAttribute(refDataAttribute, refDataRow);

		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(relLink);
		DataValue<ArrayList<DataRow>> relDataValue = new DataValue<>();
		relDataValue.setValue(LockLevel.FEED, relLinkList);
		DataRow relDataRow = new DataRow(relDataAttribute, relDataValue);
		sdIvoContainer.addAttributeValue(relDataAttribute, relDataRow);

		return refDataRow;
	}

	/**
	 * Adds new relation in the existing relation .
	 * 
	 * <pre>
	 * e.g.
	 * instrumentRelations :[
	 * 	{
	 * 		"relationType":"IVO",
	 * 		"referenceId":{
	 * 			"objectId":"123",
	 * 			"documentId":"djhdh"
	 * 		}
	 * }
	 * 
	 * This method  adds a new relation Type in instrumentRelations array
	 * 
	 * </pre>
	 * 
	 * ]
	 * 
	 * @param ivoRelValue
	 * @param relDataAttribute
	 * @param relationType
	 * @return
	 */
	private static DataRow addRelationToRelationsList(DataRow ivoRelValue, DataAttribute relDataAttribute,
			String relationType) {
		ArrayList<DataRow> listValues = ivoRelValue.getValue().getValue();
		DataRow newRelationRow = new DataRow(relDataAttribute);
		DataAttribute relationTypeAttribute = DataAttributeFactory.getRelationTypeAttribute(relDataAttribute);
		DataValue<String> relationTypeVal = new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, relationType);
		newRelationRow.addAttribute(relationTypeAttribute, relationTypeVal);

		DataAttribute relationRefDataAttribute = DataAttributeFactory.getRelationRefDataAttribute(relDataAttribute);
		DataRow refDataVal = new DataRow(relationRefDataAttribute);
		newRelationRow.addAttribute(relationRefDataAttribute, refDataVal);

		listValues.add(newRelationRow);
		return refDataVal;
	}

}
