/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerService.java
 * Author:	Ravi Shah
 * Date:	10-Jul-2018
 *
 *******************************************************************
 */

package com.smartstreamrdu.service.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalInputOptions;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

/**
 * A service class for DataContainer, which provides helper methods to get data
 * in different cases.
 */
public class DataContainerService {
	
	private static final String LOOKUP_LEVEL = "lookupLevel";

	private static final Logger logger = LoggerFactory.getLogger(DataContainerService.class);

	private static DataRetrievalService retrivalService = SpringUtil.getBean(DataRetrievalService.class);
	
	private DataContainerService() {
		
	}

	/**
	 * 
	 * @param relationType
	 * @param container
	 * @param outputFields 
	 * @return
	 */
	public static DataContainer getInsInsRelation(String relationType, DataContainer container, List<DataAttribute> outputFields) {
		if (container == null) {
			return null;
		}

		DataAttribute insInsRelAttr = DataAttributeFactory.getRelationAttributeForInsAndIns();
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(insInsRelAttr);
		DataRowIterator iterator = new DataRowIterator(container, insInsRelAttr);

		if (iterator.hasNext()) {
			DataRow insInsRel = iterator.next();
			DataValue<String> attributeValue = insInsRel.getAttributeValue(relationTypeAttr);
			if (relationType.equalsIgnoreCase(attributeValue.getValue())) {
				DataValue<ReferenceId> referenceIdDv = insInsRel
						.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insInsRelAttr));
				
				String lookupLevelString = getLookupLevel(insInsRel, insInsRelAttr);
				
				if (referenceIdDv != null && referenceIdDv.getValue() != null) {
					// Found a reference
					return getContainerFromReferencedRelation(referenceIdDv, lookupLevelString, DataLevel.INS,outputFields);
				} else { // Embedded object
					return getContainerFromEmbeddedRelation(insInsRelAttr, insInsRel, DataLevel.INS);
				}
			}
		}
		return null;
	}

	/**
	 * @param insInsRel
	 * @param insInsRelAttr 
	 * @param lookupLevelDataAttribute
	 * @return
	 */
	private static String getLookupLevel(DataRow insInsRel, DataAttribute insInsRelAttr) {
		Optional<DataAttribute> lookupLevelDataAttribute = DataAttributeFactory.getAttributeByNameAndLevelAndParentOptional(LOOKUP_LEVEL, DataLevel.INS, insInsRelAttr);
		String lookupLevelString = null;
		DataValue<String> lookupLevel = null;
		if (lookupLevelDataAttribute.isPresent()) {
			lookupLevel = insInsRel.getAttributeValue(lookupLevelDataAttribute.get());
			lookupLevelString = lookupLevel.getValue();
		}
		return lookupLevelString;
	}

	private static DataContainer getContainerFromEmbeddedRelation(DataAttribute relAttr, DataRow insInsRel,
			DataLevel dataLevel) {
		DataRow insToInsRelRefData = (DataRow) insInsRel
				.getAttributeValue(DataAttributeFactory.getRelationRefDataAttribute(relAttr));
		if (insToInsRelRefData != null) {
			DataContainer embededContainer = new DataContainer(dataLevel, DataContainerContext.builder().build());
			if (embededContainer.getChildDataContainers().isEmpty()) {
				DataLevel childDataLevel = getChildDataLevel(dataLevel);
				if (childDataLevel != null) {
					DataContainer childContainer = getChildContainer(insToInsRelRefData, childDataLevel);
					embededContainer.addDataContainer(childContainer, childDataLevel);
				}
			}			
			Map<DataAttribute, DataValue<Serializable>> rowData = insToInsRelRefData.getRowData();
			Set<DataAttribute> entrySet = rowData.keySet();
			for(DataAttribute attribute : entrySet) {
				embededContainer.addAttributeValue(attribute, rowData.get(attribute));
			}
			return embededContainer;
		}
		return null;
	}

	private static DataContainer getChildContainer(DataRow insToInsRelRefData, DataLevel childDataLevel) {
		DataContainer childContainer = new DataContainer(childDataLevel, DataContainerContext.builder().build());
		List<DataAttribute> dataAttributes = insToInsRelRefData.getDataAttributesAtLevel(childDataLevel);
		dataAttributes.forEach(attr -> {
			DataValue<Serializable> v = insToInsRelRefData.getAttributeValue(attr);
			childContainer.addAttributeValue(attr, v);
		});
		return childContainer;
	}

	private static DataLevel getChildDataLevel(DataLevel dataLevel) {
		DataLevel[] allLevels = DataLevel.values();
		for (DataLevel level : allLevels) {
			if (level.getParentLevel() != null && level.getParentLevel().equals(dataLevel))
				return level;
		}
		return null;
	}

	private static DataContainer getContainerFromReferencedRelation( DataValue<ReferenceId> referenceIdDv, String lookupLevel, DataLevel dataLevel, List<DataAttribute> outputFields) {
		Criteria criteria = getReferenceCriteria(referenceIdDv, lookupLevel, dataLevel);
		try {
			DataRetrivalInput input=new DataRetrivalInput();
			input.setCriteria(criteria);
			input.withOptions(DataRetrievalInputOptions.builder().shouldFilterChildRecords(true).build());
			if(CollectionUtils.isNotEmpty(outputFields)) {
				input.setOutputFields(outputFields.toArray(new DataAttribute[outputFields.size()]));
			}
			List<DataContainer> sdRelatedDocument = retrivalService.retrieve(Database.Mongodb, input);
			if (!sdRelatedDocument.isEmpty())
				return checkLookupLevelAndReturnContainer(sdRelatedDocument.get(0), lookupLevel); // Returning first as we do not expect multiple relations as of now in
													// system.
		} catch (Exception e) {
			logger.error("Error while fetching related record for the given criteria : {}", criteria, e);
		}
		return null;
	}
	
	/**
	 *  This method checks if the lookupLevel was INS or SEC.
	 *  If the lookupLevel is INS, we will replace the existing securities with an
	 *  empty list and return the container.
	 * 
	 * @param dataContainer
	 * @param lookupLevel
	 * @return
	 */
	private static DataContainer checkLookupLevelAndReturnContainer(DataContainer dataContainer, String lookupLevel) {
		
		if (DataLevel.INS.name().equals(lookupLevel)) {
			// For cases where the linkage is established using INS level data attributes, we do not
			// wish to distribute the securities of that INS in that case.
			// Hence replacing the securities in the data container with an empty list.
			dataContainer.replaceChildDataContainersForLevel(new ArrayList<>(), DataLevel.SEC);
		}
		
		return dataContainer;
	}

	/**
	 * Get criteria based on underlying security/instrument mapped in overlying. If
	 * its LE, document id based criteria will be returned.
	 * 
	 * @param referenceIdDv
	 * @param lookupLevel 
	 * @param dataLevel
	 * @return
	 */
	private static Criteria getReferenceCriteria(DataValue<ReferenceId> referenceIdDv, String lookupLevel, DataLevel dataLevel) {
		ReferenceId referenceId = referenceIdDv.getValue();

		DataValue<String> docIdValue = new DataValue<>();
		docIdValue.setValue(LockLevel.FEED, referenceId.getDocumentId().toString());
		DataAttribute docIdDataAttribute = DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.Document, false,null);
		Criteria docIdCriteria = Criteria.where(docIdDataAttribute).is(docIdValue);

		if (DataLevel.LE.equals(dataLevel)) {
			return docIdCriteria;
		}

		DataValue<String> objIdValue = new DataValue<>();
		objIdValue.setValue(LockLevel.FEED, referenceId.getObjectId());
		
		DataAttribute objIdDataAttribute = null; 
		
		if (lookupLevel != null && DataLevel.INS.name().equals(lookupLevel)) {
			objIdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INS_ID, DataLevel.INS);
		} else {
			objIdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.SEC_ID, DataLevel.SEC);
		}
		

		Criteria objIdCriteria = Criteria.where(objIdDataAttribute).is(objIdValue);

		Criteria finalCriteria = new Criteria();
		finalCriteria = finalCriteria.andOperator(docIdCriteria, objIdCriteria);

		return finalCriteria;
	}


	public static DataContainer getInsLeRelation(String relationType, DataContainer container) {
		if (container == null) {
			return null;
		}

		DataAttribute insLeRelAttr = DataAttributeFactory.getRelationAttributeForInsAndLe();
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(insLeRelAttr);
		DataRowIterator iterator = new DataRowIterator(container, insLeRelAttr);
		if (iterator.hasNext()) {
			DataRow insLeRel = iterator.next();
			DataValue<String> attributeValue = insLeRel.getAttributeValue(relationTypeAttr);
			if (relationType.equalsIgnoreCase(attributeValue.getValue())) {
				DataValue<ReferenceId> referenceIdDv = insLeRel
						.getAttributeValue(DataAttributeFactory.getRelationRefIdAttribute(insLeRelAttr));
				if (referenceIdDv != null && referenceIdDv.getValue() != null) {
					// Found a reference
					return getContainerFromReferencedRelation(referenceIdDv, null, DataLevel.LE,Collections.emptyList());
				} else { // Embedded object
					return getContainerFromEmbeddedRelation(insLeRelAttr, insLeRel, DataLevel.LE);
				}
			}
		}
		return null;
	}
}
