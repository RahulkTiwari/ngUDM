/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoLockRemovalHelper.java
* Author : Padgaonkar
* Date : April 12, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

public class IvoLockRemovalHelper {

	/**
	 * Added a private Constructor.
	 */
	private IvoLockRemovalHelper() {
	}

	/**
	 * The method will remove nestedArray Attribute Value From dbContainer for given
	 * Attribute from deletedContainer
	 * 
	 * @param deletedContainer
	 * @param dataAttribute
	 * @param dbContainer
	 */
	public static void handleForNestedArrayAttributes(DataContainer deletedContainer, DataAttribute dataAttribute,
			DataContainer dbContainer) {
		
		DataRow inDataRow = (DataRow) deletedContainer.getAttributeValue(dataAttribute);
		ArrayList<DataRow> inDataRowList = inDataRow.getValue().getValue();

		DataRow dbdataRow = (DataRow) dbContainer.getAttributeValue(dataAttribute);
		ArrayList<DataRow> dbdataRowList = dbdataRow.getValue().getValue();
		
		if(DataAttributeFactory.getRelationalParentAttributeList().contains(dataAttribute)) {
			handleRelationalNestedArrayAttributes(inDataRowList, dataAttribute, dbdataRowList);
		}else {
			handleNonRelationalNestedArrayAttributes(inDataRowList, dataAttribute, dbdataRowList);
		}

		

	}

	/**
	 * Handles lock removal for non relational nested array attributes
	 * 
	 * @param inDataRowList
	 * @param dataAttribute
	 * @param dbdataRowList
	 */
	private static void handleNonRelationalNestedArrayAttributes(ArrayList<DataRow> inDataRowList,
			DataAttribute dataAttribute, ArrayList<DataRow> dbdataRowList) {
		
		for(DataRow inDataRow:inDataRowList) {
			DataRow matchingDbDataRow = findMatchingDbDataRow(inDataRow,dbdataRowList,dataAttribute);
			if (matchingDbDataRow != null) {
				Map<DataAttribute, DataValue<Serializable>> inRowData = inDataRow.getRowData();
				Map<DataAttribute, DataValue<Serializable>> inputNonKeyAttributes = inRowData.entrySet().stream()
						.filter(entry -> !entry.getKey().isKeyAttribute())
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				removeAttributeFromNestedAttributes(inputNonKeyAttributes, matchingDbDataRow.getRowData());
			}
		}
		
	}

	/**
	 * Find the matching dbDataRow based on keyAttribute values
	 * 
	 * @param inDataRow 
	 * @param dbdataRowList
	 * @param dataAttribute
	 */
	private static DataRow findMatchingDbDataRow(DataRow inDataRow, ArrayList<DataRow> dbdataRowList,
			DataAttribute dataAttribute) {
		List<DataAttribute> keyAttributeForParentAttribute = DataAttributeFactory
				.getKeyAttributeForParentAttribute(dataAttribute);
		Optional<DataRow> matchingDbDataRow = dbdataRowList.stream().filter(dbDataRow -> keyAttributeForParentAttribute
				.stream().allMatch(keyAttribute -> isValueEqual(inDataRow, dbDataRow, keyAttribute))).findFirst();
		return matchingDbDataRow.orElse(null);
	}

	/**
	 * @param inDataRow
	 * @param dbDataRow
	 * @param keyAttribute
	 * @return
	 */
	private static boolean isValueEqual(DataRow inDataRow, DataRow dbDataRow, DataAttribute keyAttribute) {
		Serializable inValue = inDataRow.getAttributeValue(keyAttribute).getValue();
		Serializable dbValue = dbDataRow.getAttributeValue(keyAttribute).getValue();
		return inValue!=null && dbValue !=null && inValue.equals(dbValue);
	}

	/**
	 * Handles lock removal for relational attributes like instrumentRelations, instrumentLegalEntityRelations.
	 * 
	 * @param inDataRowList
	 * @param dataAttribute
	 * @param dbdataRowList
	 */
	private static void handleRelationalNestedArrayAttributes(ArrayList<DataRow> inDataRowList,
			DataAttribute dataAttribute, ArrayList<DataRow> dbdataRowList) {
		
		DataAttribute relationAttribute = getRelationAttribute(dataAttribute.getAttributeLevel());

		for (DataRow inputRow : inDataRowList) {
			removeNestedAttributeFromDbDataRow(dbdataRowList, relationAttribute, inputRow);
		}
	}

	/**
	 * This method will return relation Attribute For DataContainer
	 * 
	 * @param dataContainer
	 * @return
	 */
	private static DataAttribute getRelationAttribute(DataLevel level) {

		if (level.equals(DataLevel.INS) || level.equals(DataLevel.IVO_INS)) {
			return DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, level);
		}

		if (level.equals(DataLevel.SEC) || level.equals(DataLevel.IVO_SEC)) {
			return DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.SECURITY_RELATIONS, level);
		}
		return null;

	}

	private static void removeNestedAttributeFromDbDataRow(ArrayList<DataRow> dbdataRowList,
			DataAttribute relationAttribute, DataRow inputRow) {
		for (DataRow dbRow : dbdataRowList) {

			Serializable inputRelationType = inputRow
					.getAttributeValue(DataAttributeFactory.getRelationTypeAttribute(relationAttribute)).getValue();
			Serializable dbRelationType = dbRow
					.getAttributeValue(DataAttributeFactory.getRelationTypeAttribute(relationAttribute)).getValue();
			if (!inputRelationType.equals(RelationType.IVO.getRelationTypeName())
					&& inputRelationType.equals(dbRelationType)) {

				Map<DataAttribute, DataValue<Serializable>> equivalentToBeDeleteNestedAttribute = getEquivalentNestedAttribute(
						inputRow);
				Map<DataAttribute, DataValue<Serializable>> equivalentDbNestedAttribute = getEquivalentNestedAttribute(
						dbRow);
				removeAttributeFromNestedAttributes(equivalentToBeDeleteNestedAttribute, equivalentDbNestedAttribute);

			}
		}
	}

	/**
	 * 
	 * @param equivalentToBeDeleteNestedAttribute
	 * @param equivalentDbNestedAttribute
	 */
	private static void removeAttributeFromNestedAttributes(
			Map<DataAttribute, DataValue<Serializable>> equivalentToBeDeleteNestedAttribute,
			Map<DataAttribute, DataValue<Serializable>> equivalentDbNestedAttribute) {

		if (equivalentToBeDeleteNestedAttribute == null || equivalentDbNestedAttribute == null) {
			return;
		}

		for (Entry<DataAttribute, DataValue<Serializable>> mapEntry : equivalentToBeDeleteNestedAttribute.entrySet()) {

			DataAttribute attributeTobeDeleted = mapEntry.getKey();
			DataValue<Serializable> dbdataValue = equivalentDbNestedAttribute.get(attributeTobeDeleted);
			if (dbdataValue != null) {
				DataValue<Serializable> toBeDeleteDataValue = mapEntry.getValue();
				Iterator<LockLevel> keyIterator = toBeDeleteDataValue.getLockData().keySet().iterator();
				while (keyIterator.hasNext()) {
					dbdataValue.removeLockValue(keyIterator.next());
				}

			}
		}
	}

	/**
	 * 
	 * @param dataRow
	 * @return
	 */
	private static Map<DataAttribute, DataValue<Serializable>> getEquivalentNestedAttribute(DataRow dataRow) {

		Map<DataAttribute, DataValue<Serializable>> nestedRowData = dataRow.getRowData();

		for (Entry<DataAttribute, DataValue<Serializable>> mapEntry1 : nestedRowData.entrySet()) {

			DataAttribute dataAttribute1 = mapEntry1.getKey();
			if (dataAttribute1.getDataType() == DataType.NESTED) {
				DataRow nestedDataRow = (DataRow) dataRow.getAttributeValue(dataAttribute1);
				return nestedDataRow.getRowData();
			}
		}
		return null;
	}

	/**
	 * For removing values from Nested Attributes this Method is called.
	 * 
	 * @param dataAttribute
	 * @param deletedContainer
	 * @param dbContainer
	 */
	public static void handleForNestedAttribute(DataAttribute dataAttribute, DataContainer deletedContainer,
			DataContainer dbContainer) {

		DataRow deletedDataRow = (DataRow) deletedContainer.getAttributeValue(dataAttribute);
		DataRow dbdataRow = (DataRow) dbContainer.getAttributeValue(dataAttribute);
		removeAttributeFromNestedAttributes(deletedDataRow.getRowData(), dbdataRow.getRowData());

	}

}
