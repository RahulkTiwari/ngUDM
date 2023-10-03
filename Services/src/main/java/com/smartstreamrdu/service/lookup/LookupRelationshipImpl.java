/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LookupRelationshipImpl.java
 * Author:	Jay Sangoi
 * Date:	17-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.LegalEntityAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.util.AttributeToCriteriaConverterUtility;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.SdDataAttributeConstant;

/**
 * @author Jay Sangoi
 * NOTE:This class provides services which is specific to sdData only.
 */
@Component
public class LookupRelationshipImpl implements LookupRelationship {

	private static final String LOOKUP_LEVEL = "lookupLevel";

	@Autowired
	private NormalizedValueService normalizedService;
	
	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;

	@Autowired
	private DataRetrievalService service;
	
	@Autowired
	private AttributeToCriteriaConverterUtility searchRequestToCriteriaConverter;
	
	private static final Logger _logger = LoggerFactory.getLogger(LookupRelationshipImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.lookup.LookupRelationship#resolveLookup(com.
	 * smartstreamrdu.domain.DataContainer,
	 * com.smartstreamrdu.service.lookup.input.LookupAttributeInput)
	 */
	@Override
	public void resolveLookup(DataContainer feedContainer, LookupAttributeInput input) throws UdmBaseException {
		/**
		 * Resolve Instrument Le Relationship
		 */
		resolveInsLeLookup(feedContainer, input);

		/**
		 * Resolve Instrument Instrument relationship
		 */
		resolveInsInsLookup(feedContainer, input);
	}

	/**
	 * Resolve Ins Le Relationship
	 * 
	 * @param feedContainer
	 * @param input
	 * @throws UdmBaseException
	 */
	private void resolveInsLeLookup(DataContainer feedContainer, LookupAttributeInput input) throws UdmBaseException {

		lookup(feedContainer, input, DataAttributeFactory.getRelationAttributeForInsAndLe());

	}

	/**
	 * Resolve Ins Ins relationship
	 * 
	 * @param feedContainer
	 * @param input
	 * @throws UdmBaseException
	 */
	private void resolveInsInsLookup(DataContainer feedContainer, LookupAttributeInput input) throws UdmBaseException {

		lookup(feedContainer, input, DataAttributeFactory.getRelationAttributeForInsAndIns());

	}

	/**
	 * @param feedContainer
	 * @param input
	 * @param insLeAttr
	 * @throws UdmTechnicalException
	 */
	private void lookup(DataContainer feedContainer, LookupAttributeInput input, DataAttribute insLeAttr)
			throws UdmBaseException {
		DataRowIterator iterator = new DataRowIterator(feedContainer, insLeAttr);

		DomainType dataSourceDomainType = feedContainer.getHighestPriorityValue(SdDataAttributeConstant.DATASOURCE);

		boolean hasData = false;
		/**
		 * Iterate over all the relationship define and for each relationship, fetch the
		 * reference id
		 */
		while (iterator.hasNext()) {
			hasData = true;
			DataRow row = iterator.next();
			lookupRelationship(row, input, insLeAttr, dataSourceDomainType);
		}

		if (!hasData) {
			_logger.debug(
					"LookupServiceImpl->resolveInsLeLookup. Not doing lookup as there is no relation attribute defined for container {}",
					feedContainer);
		}
	}

	private void lookupRelationship(DataRow dv, LookupAttributeInput input, DataAttribute parentAttribute,
			DomainType dataSourceDomainType) throws UdmBaseException {

		Serializable val = dv.getAttributeValue(DataAttributeFactory.getRelationRefDataAttribute(parentAttribute));

		if (!(val instanceof DataValue)) {
			return;
		}

		DataRow refData = (DataRow) val;
		DataLevel relationLevel = DataAttributeFactory.getLevelForRelationAttribute(parentAttribute);
		if (DataLevel.LE.equals(relationLevel)) {
			// Lookup for LE
			_logger.debug("Fetching reference for INS to LE with input :{}", input);
			fetchReference(input, refData, DataLevel.LE, parentAttribute, dv, dataSourceDomainType);
		} else if (DataLevel.INS.equals(relationLevel)) {
			// Lookup for INS
			_logger.debug("Fetching reference for INS to INS with Input {}", input);
			fetchReference(input, refData, DataLevel.INS, parentAttribute, dv, dataSourceDomainType);
		}

	}

	private DataLevel getLowestLevelForLookupAttribute(List<String> lookup) {
		DataLevel level = null;
		if (lookup == null) {
			return null;
		}

		for (String sc : lookup) {

			DataLevel attributeLevel = DataStorageEnum.SD.getAttributeByName(sc).getAttributeLevel();
			if (level == null || (!attributeLevel.equals(level)) && level.equals(attributeLevel.getParentLevel())) {
				level = attributeLevel;
			}
		}
		return level;

	}

	/**
	 * @param input
	 * @param refDataRow
	 * @param level
	 * @throws UdmTechnicalException
	 */
	private void fetchReference(LookupAttributeInput input, DataRow refDataRow, DataLevel level, DataAttribute parentAttribute,
			DataRow rootDataRow, DomainType dataSourceDomainType) throws UdmBaseException {
		List<List<String>> lookupAttributes = null;
		// Get the lookup attributes
		lookupAttributes = getLookupAttributes(input, level);
		// Fetch the reference using attributes
		fetchAndPopulateReference(refDataRow, level, lookupAttributes, dataSourceDomainType, rootDataRow, parentAttribute);
	}

	/**
	 * @param ref
	 * @param level
	 * @param lookupAttributes
	 * @param root 
	 * @param reference
	 * @return
	 * @throws UdmBaseException
	 * @throws UdmTechnicalException
	 */
	private void fetchAndPopulateReference(DataRow ref, DataLevel level, List<List<String>> lookupAttributes,
			DomainType dataSourceDomainType, DataRow rootDataRow, DataAttribute parentAttribute) throws UdmBaseException {
		ReferenceId reference = null;
		DataValue<String> lookupLevelDataValue = new DataValue<>();
		if (CollectionUtils.isNotEmpty(lookupAttributes)) {
			// Lookup LE in db
			AtomicReference<Criteria> criteria = new AtomicReference<>();
			
			reference = lookupUsingAttributesDefined(ref, criteria, level, lookupAttributes, dataSourceDomainType, lookupLevelDataValue);
			// Add lookup resolution level to root data row.
		} else {
			// Get the source unique id from embedded object
			reference = lookupUsingSourceUniqueIdAtribute(ref, level);
			if (null != reference) {
				lookupLevelDataValue.setValue(LockLevel.RDU, level.name());
			} else {
				lookupLevelDataValue.setValue(LockLevel.RDU, null);
			}
			// Lookup on source unique id of
		}
		// populate look up level if applicable.
		populateLookupLevelIfApplicable(rootDataRow, lookupLevelDataValue, parentAttribute);
		// Populate the reference id
		populateReferenceId(rootDataRow, reference, parentAttribute);
	}

	/**
	 *  This method populates the lookup level if it is applicable for the 
	 *  given parent attribute.
	 *  
	 *  Currently this attribute of lookupLevel is supported only for the 
	 *  instrumentRelations nested attribute in our data model.
	 * 
	 * @param rootDataRow
	 * @param lookupLevelDataValue
	 * @param parentDataAttribute
	 */
	private void populateLookupLevelIfApplicable(DataRow rootDataRow, DataValue<String> lookupLevelDataValue, DataAttribute parentDataAttribute) {
			Optional<DataAttribute> lookupAttribute = DataAttributeFactory.getAttributeByNameAndLevelAndParentOptional(LOOKUP_LEVEL, DataLevel.INS, parentDataAttribute);
			
			if (lookupAttribute.isPresent()) {
				rootDataRow.addAttribute(lookupAttribute.get(), lookupLevelDataValue);
			}
	}

	private ReferenceId lookupUsingSourceUniqueIdAtribute(DataRow ref, DataLevel level) throws UdmTechnicalException {
		DataContainer lookupContainer;
		Serializable objectId;
		ReferenceId reference = null;
		Serializable attributeValueAtLevel2 = ref.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getSourceUniqueIdentifierForLevel(level));
		lookupContainer = processUsingSourceUniqieId(attributeValueAtLevel2, level);
		if (lookupContainer != null) {
			objectId = lookupContainer.getAttributeValueAtLevel(LockLevel.FEED,
					DataAttributeFactory.getObjectIdIdentifierForLevel(lookupContainer.getLevel()));

			reference = new ReferenceId(objectId != null ? objectId.toString() : null,
					new ObjectId(lookupContainer.get_id()));
		}
		return reference;
	}

	/**
	 * @param ref
	 * @param criteria
	 * @param dcIds
	 * @param level
	 * @param lookupAttributes
	 * @param dataSourceDomainType
	 * @return
	 * @throws UdmBaseException
	 * 
	 * This method flow is as follows:
	 * 
	 * 1. For each lookup attributes create criteria with following condition:
	 *     a. lookupAttribute 
	 *     b. DataSources ( @dataSourceDomainType )
	 *     c. InstrumentStatus or LegalEntityStatus should be active
	 *     
	 * 2. Based on above prepared criteria get the resultset ( @retrieve )
	 * 3. Filter child container if applicable based on SecurityStatus and lookupAttribute
	 * 4. If for lookupAttribute ReferenceId is null then return ReferenceId as null 
	 * 5. If reference id is not null add ReferenceId into a LinkedHashSet @setOfReferenceId  so that we can get the child referenceId.
	 * 6. If size of dcIds are greater than 1 , it means we have found two document based on lookup condition and filter criteria , log error and return ReferenceId as null.
	 *   6.1 If size of dcIds are not greater than 1 , return the referenceId
	 * 
	 * 
	 */
	private ReferenceId lookupUsingAttributesDefined(DataRow ref, AtomicReference<Criteria> criteria, DataLevel level,
			List<List<String>> lookupAttributes, DomainType dataSourceDomainType, DataValue<String> lookupLevelDataValue) throws UdmBaseException {

		ReferenceId reference = null;
		Set<ReferenceId> setOfReferenceId = new LinkedHashSet<>();
		Set<String> dcIds = new HashSet<>();
		List<String> allAttributes = new ArrayList<>();

		lookupAttributes.removeIf(Objects::isNull);

		for (List<String> atts : lookupAttributes) {

			String attName = atts.get(0);
			DataAttribute lookupAttribute = DataStorageEnum.SD.getAttributeByName(attName);
			Serializable attributeValueAtLevel2 = ref.getAttributeValueAtLevel(LockLevel.FEED, lookupAttribute);

			if (lookupAttribute.isNull(attributeValueAtLevel2)) {
				continue;
			}

			boolean stopped = createCriteria(ref, attributeValueAtLevel2, criteria, attName, atts, dataSourceDomainType,
					level);
			if (stopped) {
				continue;
			}
			DataRetrivalInput input = new DataRetrivalInput();
			input.setCriteria(criteria.get());
			List<DataContainer> retrieve = service.retrieve(Database.Mongodb, input);
			if (CollectionUtils.isEmpty(retrieve)) {
				continue;
			}

			DataLevel lowestLevelForLookupAttribute = getLowestLevelForLookupAttribute(atts);

			// We have used the DataContainerFilterService here to filter out
			// only those child data containers
			// that match the child level criteria. This is a fix implemented as
			// a part of JIRA UDM-37148.

			reference = getActiveReferenceIdFromDataContainers(retrieve, lowestLevelForLookupAttribute,
					dataSourceDomainType, lookupAttribute, attributeValueAtLevel2, dcIds);

			if (null != reference) {
				setOfReferenceId.add(reference);
				allAttributes.add(atts.get(0));
			} else {
				lookupLevelDataValue.setValue(LockLevel.RDU, null);
				return null;
			}

		}

		if (dcIds.size() > 1) {
			// Failed lookup so populate null.
			lookupLevelDataValue.setValue(LockLevel.RDU, null);
			// throw error, we have multiple lookup result
			_logger.error(
					"Multiple data containers returned for the lookup criteria defined for level {} with attributes-{}. Hence we will not be linking it to reference id-{}",
					level, lookupAttributes, dcIds);
			return null;
		}

		// Once a reference is established, we will use the attributes used for the 
		// linkage to populate the lookupLevel.
		lookupLevelDataValue.setValue(LockLevel.RDU, getApplicableLookUpLevel(allAttributes));
		// This is make sure that the container retrieved by earlier lookup attribute,
		// which MUST BE a child level container, is preserved and returned as a
		// relation reference.
		return setOfReferenceId.stream().findFirst().orElse(null);

	}
	
	/**
	 *  This method checks what all attributes are used for the lookup.
	 *  based on the level of all the attributes present, we will
	 *  decide whether the lookup was based on the INS level or the 
	 *  SEC level.
	 * 
	 * @param atts
	 * @return
	 */
	private String getApplicableLookUpLevel(List<String> atts) {
		
		Set<DataLevel> allLevels = atts.stream().map(attribute -> DataStorageEnum.SD.getAttributeByName(attribute).getAttributeLevel()).collect(Collectors.toSet());
		
		if (allLevels.contains(DataLevel.SEC)) {
			return DataLevel.SEC.name();
		} else if (allLevels.contains(DataLevel.INS)) {
			return DataLevel.INS.name();
		} else {
			return null;
		}
	}

	/**
	 * @param retrieve
	 * @param lowestLevelForLookupAttribute
	 * @param dataSourceDomainType
	 * @param lookupAttribute
	 * @param attributeValueAtLevel2
	 * @param documentContainerIds
	 * @return
	 * @throws UdmTechnicalException
	 * 
	 * This method flow is as follows:
	 * 
	 * 1. Get the child container for each resultSet
	 * 2. For each child container filter out the child container based on lookup attributes and securityStatus(highest lock value will be only considered)
	 * 3. If for a result contains multiple child container( @applicableChildContainers ) which satisfy above criteria then we will log error and return 
	 *    ReferenceId as null(since we can't have multiple reference)
	 * 4. If among multiple result set ( @retrieve ) multiple active child container is filtered then also we will log error and return ReferenceId as null
	 *    (since we can't have multiple referenceId)
	 * 5. If only one reference id is found for each result set then we will add id into @documentContainerIds 
	 */
	private ReferenceId getActiveReferenceIdFromDataContainers(List<DataContainer> retrieve,
			DataLevel lowestLevelForLookupAttribute, DomainType dataSourceDomainType, DataAttribute lookupAttribute,
			Serializable attributeValueAtLevel2, Set<String> documentContainerIds) throws UdmTechnicalException {
		Serializable objectId = null;
		ReferenceId reference = null;
		Set<ReferenceId> setOfReferenceId = new HashSet<>();
		for (DataContainer lookupContainer : retrieve) {
			if (!lookupContainer.getLevel().equals(lowestLevelForLookupAttribute)) {

				List<DataContainer> applicableChildContainers = filterDataContainerBasedOnLookupAttributeAndActiveStatus(
						dataSourceDomainType.getVal(), lookupAttribute, attributeValueAtLevel2, lookupContainer,
						lowestLevelForLookupAttribute);
				if (applicableChildContainers.size() > 1) {
					_logger.error(
							"Multiple datacontainers returned while filtering at level {} dataContainer for attributes {} and status as Active",
							lowestLevelForLookupAttribute, lookupAttribute);
					return null;
				}
				if (!applicableChildContainers.isEmpty()) {
					DataContainer applicableChildContainer = applicableChildContainers.get(0);
					objectId = applicableChildContainer.getAttributeValueAtLevel(LockLevel.FEED,
							DataAttributeFactory.getObjectIdIdentifierForLevel(applicableChildContainer.getLevel()));
					documentContainerIds.add(lookupContainer.get_id());
					reference = evaluateReferenceId(objectId, lookupContainer);
					setOfReferenceId.add(reference);
				}

			}

			else {
				objectId = lookupContainer.getAttributeValueAtLevel(LockLevel.FEED,
						DataAttributeFactory.getObjectIdIdentifierForLevel(lookupContainer.getLevel()));
				documentContainerIds.add(lookupContainer.get_id());
				reference = evaluateReferenceId(objectId, lookupContainer);
				setOfReferenceId.add(reference);
			}

			if (setOfReferenceId.size() > 1) {
				// throw error, we have multiple lookup result
				_logger.error(
						"Multiple child data containers returned for the lookup criteria defined for level {} with attributes-{}. Hence we will not be linking it to reference id-{}",
						lowestLevelForLookupAttribute, lookupAttribute, setOfReferenceId);
				return null;
			}

		}

		return reference;
	}

	/**
	 * @param reference
	 * @param objectId
	 * @param lookupContainer
	 * @return
	 */
	private ReferenceId evaluateReferenceId(Serializable objectId, DataContainer lookupContainer) {
		return new ReferenceId(objectId != null ? objectId.toString() : null, new ObjectId(lookupContainer.get_id()));
	}

	/**
	 * @param dataSource
	 * @param filterAttribute
	 * @param attributeValueAtLevel2ForFilter
	 * @param childDataContainers
	 * @param lowestLevelForLookupAttribute
	 * @return
	 * @throws UdmTechnicalException
	 * 
	 * This method filters out the childContainer based on lookupAttribute and status.
	 * 
	 * Note: This method will be removed when filterService.filterDataContainerRecords supports EXISTS in created criteria.
	 * 
	 * 
	 */
	private List<DataContainer> filterDataContainerBasedOnLookupAttributeAndActiveStatus(String dataSource,
			DataAttribute filterAttribute, Serializable attributeValueAtLevel2ForFilter, DataContainer lookupContainer,
			DataLevel lowestLevelForLookupAttribute) throws UdmTechnicalException {

		List<DataContainer> filteredDataContainer = new ArrayList<>();
		List<DataContainer> childDataContainers = lookupContainer.getChildDataContainers(lowestLevelForLookupAttribute);

		if (childDataContainers == null || childDataContainers.isEmpty()) {
			throw new IllegalStateException(
					String.format("Lookup attribute is defined at %s and no containers avaialble at that level",
							lowestLevelForLookupAttribute));
		}

		for (DataContainer dataContainer : childDataContainers) {
			Serializable attributeValue = dataContainer.getAttributeValueAtLevel(LockLevel.FEED, filterAttribute);
			if (attributeValueAtLevel2ForFilter.equals(attributeValue)) {
				String containerStatus = getContainerStatus(dataContainer, dataSource);
				if (DomainStatus.ACTIVE.equals(containerStatus)) {
					filteredDataContainer.add(dataContainer);
				}
			}

		}

		return filteredDataContainer;
	}
	
	/**
	 * @param container
	 * @param dataSource
	 * @return
	 * @throws UdmTechnicalException
	 * 
	 * It returns the container highest priority status based on @dataSource
	 */
	private String getContainerStatus(DataContainer container, String dataSource) throws UdmTechnicalException {

		DataLevel level = container.getLevel();
		DataAttribute statusAttribute = DataAttributeFactory
				.getAttributeByNameAndLevel(DataAttributeFactory.getStatusFlagForLevel(level), level);

		DomainType currentContainerStatus = container.getHighestPriorityValue(statusAttribute);
		String normalizedValueForStatus = currentContainerStatus.getNormalizedValue();
		if (normalizedValueForStatus != null) {
			return normalizedValueForStatus;
		}

		String statusValue = (String) normalizedService.getNormalizedValueForDomainValue(statusAttribute,
				currentContainerStatus, dataSource);

		if (statusValue == null) {
			_logger.error(
					"normalized status value are not found for given input status {} for feed {} for documentId {} ",
					currentContainerStatus, dataSource, container.get_id());
		}
		return statusValue;
	}


	/**
	 * @param rootDataRow
	 * @param lookupContainer
	 * @param objectId
	 */
	private void populateReferenceId(DataRow rootDataRow, ReferenceId reference, DataAttribute parentAttribute) {
		DataValue<ReferenceId> val = new DataValue<>();
		val.setValue(LockLevel.FEED, reference);
		rootDataRow.addAttribute(DataAttributeFactory.getRelationRefIdAttribute(parentAttribute), val);
	}

	/**
	 * @param ref
	 * @param attributeValueAtLevel2
	 * @param criteria
	 * @param attName
	 * @param atts
	 * @param dataSourceDomainType
	 * @return
	 * @throws UdmBaseException
	 * 
	 *                          Based on lookupAttributeName ,dataSource and legalEntityStatus and instrumentStatus create
	 *                          criteria to get the relational reference
	 * 
	 *                          LE and Instrument both for the lookupRelationship
	 *                          LockLevel Enriched , feed and rdu
	 */
	private boolean createCriteria(DataRow ref, Serializable attributeValueAtLevel2, AtomicReference<Criteria> criteria,
			String attName, List<String> atts, DomainType dataSourceDomainType, DataLevel level)
			throws UdmBaseException {

		boolean stopped = false;

		DataValue<Serializable> value = new DataValue<>();
		value.setValue(LockLevel.FEED, attributeValueAtLevel2);
		Criteria lookupAttributeNameCriteria = Criteria
				.where(DataStorageEnum.SD.getAttributeByName(attName)).is(value);

		DataValue<DomainType> dataValue = new DataValue<>();
		dataValue.setValue(LockLevel.FEED, dataSourceDomainType);
		Criteria dataSourceLookupCriteria = Criteria.where(SdDataAttributeConstant.DATASOURCE).is(dataValue);

		criteria.set(lookupAttributeNameCriteria.andOperator(dataSourceLookupCriteria));

		String domainSource = cacheDataRetrieval.getDataSourceDomainSourceFromCode(dataSourceDomainType.getVal());

		if (level.equals(DataLevel.LE)) {
			Criteria createLegalEntityStatusAttributeCriteria = searchRequestToCriteriaConverter
					.createStatusAttributeCriteria(LegalEntityAttrConstant.LEGAL_ENTITY_STATUS, DomainStatus.ACTIVE, domainSource);
			criteria.set(criteria.get().andOperator(createLegalEntityStatusAttributeCriteria));
		}

		else if (level.equals(DataLevel.INS)) {
			Criteria createInstrumentStatusAttributeCriteria = searchRequestToCriteriaConverter
					.createStatusAttributeCriteria(InstrumentAttrConstant.INSTRUMENT_STATUS, DomainStatus.ACTIVE, domainSource);
			criteria.set(criteria.get().andOperator(createInstrumentStatusAttributeCriteria));
		}

		for (int i = 1; i < atts.size(); i++) {
			value = new DataValue<>();
			DataAttribute attribute = DataStorageEnum.SD.getAttributeByName(atts.get(i));
			attributeValueAtLevel2 = ref.getAttributeValueAtLevel(LockLevel.FEED, attribute);

			if (attribute.isNull(attributeValueAtLevel2)) {
				stopped = true;
				break;
			}
			value.setValue(LockLevel.FEED, attributeValueAtLevel2);
			criteria.set(criteria.get().andOperator(
					Criteria.where(DataStorageEnum.SD.getAttributeByName(atts.get(i))).is(value)));
		}
		return stopped;
	}


	private DataContainer processUsingSourceUniqieId(Serializable attributeValueAtLevel2, DataLevel level)
			throws UdmTechnicalException {
		if (attributeValueAtLevel2 == null) {
			return null;
		}
		DataValue<Serializable> value = new DataValue<>();
		value.setValue(LockLevel.FEED, attributeValueAtLevel2);
		Criteria criteria = Criteria.where(DataAttributeFactory.getSourceUniqueIdentifierForLevel(level)).is(value);
		DataRetrivalInput input = new DataRetrivalInput();
		input.setCriteria(criteria);
		List<? extends DataContainer> retrieve = service.retrieve(Database.Mongodb, input);
		if (CollectionUtils.isNotEmpty(retrieve)) {
			return retrieve.get(0);
		}
		return null;
	}

	/**
	 * @param input
	 * @param level
	 * @param lookupAttributes
	 * @return
	 */
	private List<List<String>> getLookupAttributes(LookupAttributeInput input, DataLevel level) {
		if (input == null) {
			return Collections.emptyList();
		}
		List<List<String>> lookup = null;
		if (DataLevel.INS.equals(level)) {
			lookup = input.getInsInsAttributes();
		} else if (DataLevel.LE.equals(level)) {
			lookup = input.getInsLeAttributes();
		}
		return lookup;
	}

}
