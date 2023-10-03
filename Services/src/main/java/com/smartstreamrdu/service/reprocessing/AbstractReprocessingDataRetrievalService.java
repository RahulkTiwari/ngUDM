/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : AbstractReprocessingDataRetrievalService.java
 * Author :SaJadhav
 * Date : 06-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaRDD;
import org.springframework.beans.factory.annotation.Autowired;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.message.ReprocessingData;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.cache.RuleNormalizationStrategyCache;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.Criteria.Operator;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.service.util.AttributeToCriteriaConverterUtility;
import com.smartstreamrdu.rules.RuleNormalizationStrategy;
import com.smartstreamrdu.service.retrieval.MongoSparkDataRetrieval;
import com.smartstreamrdu.service.spark.SparkUtil;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.Constant.StaticDataConstant;
import com.smartstreamrdu.util.DataAttributeConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract implementation for ReprocessingDataRetrievalService
 * 
 * @author SaJadhav
 *
 */
@Slf4j
public abstract class AbstractReprocessingDataRetrievalService implements ReprocessingDataRetrievalService {
	
	@Autowired
	@Setter
	private MongoSparkDataRetrieval mongoSparkDataRetrieval;
	
	@Autowired
	@Setter
	private SparkUtil sparkUtil;
	
	@Autowired
	@Setter
	private CacheDataRetrieval cacheDataRetrieval;
	
	@Autowired
	@Setter
	private AttributeToCriteriaConverterUtility attributeToCriteriaConverterUtility;
	
	@Autowired
	private RuleNormalizationStrategyCache ruleNormalizationStrategyCache;
	

	@Override
	public Optional<JavaRDD<DataContainer>> getDataContainersToReprocess(String rduDomain,
			Map<String, List<ReprocessingData>> mapOfDomainSourceVsListOfReprocessingData,
			Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources, String normalizedValue)
			throws UdmTechnicalException {
		Objects.requireNonNull(rduDomain, "rduDomain should be populated");

		List<DataAttribute> dataAttributesForRduDomain = DataAttributeFactory.getDataAttributesForRduDomain(rduDomain);

		// applicable dataAttributes for current applicable data levels
		List<DataAttribute> applicableDataAttributes = getApplicableDataAttributes(dataAttributesForRduDomain);
		// applicable domain sources for current dataStorage level
		List<String> applicableDomainSources = mapDataStorageVsDomainSources.get(getDataStorageLevel());

		if (CollectionUtils.isEmpty(applicableDataAttributes)) {
			return Optional.empty();
		}

		Map<String, List<ReprocessingData>> filteredDomainSourceVsListOfReprocessingData = Collections.emptyMap();

		if (!CollectionUtils.isEmpty(applicableDomainSources)) {
			filteredDomainSourceVsListOfReprocessingData = mapOfDomainSourceVsListOfReprocessingData.entrySet().stream()
					.filter(entry -> applicableDomainSources.contains(entry.getKey()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		}

		Optional<DataRetrivalInput> input = getDataRetrivalInput(applicableDataAttributes,
				filteredDomainSourceVsListOfReprocessingData, normalizedValue);

		if (input.isPresent()) {
			return Optional.of(mongoSparkDataRetrieval.retrieveSparkRDD(input.get(), sparkUtil.getSparkContext()));
		}

		return Optional.empty();

	}
	

	private Optional<DataRetrivalInput> getDataRetrivalInput(List<DataAttribute> dataAttributes,
			Map<String, List<ReprocessingData>> mapOfDomainSourceVsListOfReprocessingData, String normalizedValue) {

		if (MapUtils.isEmpty(mapOfDomainSourceVsListOfReprocessingData) && StringUtils.isEmpty(normalizedValue)) {
			return Optional.empty();
		}

		DataRetrivalInput input = new DataRetrivalInput();
		List<Criteria> criteriaList = new ArrayList<>();

		for (Entry<String, List<ReprocessingData>> entry : mapOfDomainSourceVsListOfReprocessingData.entrySet()) {
			String domainSource = entry.getKey();
			Criteria statusCriteria = getStatusCriteria(domainSource);

			Criteria c = Criteria.where(getDataStorageLevel().getAttributeByName(SdAttributeNames.DATASOURCE_ATTRIBUTE))
					.in(getDataSourceDataValue(domainSource)).andOperator(
							setCriteriaForDataAttribute(entry.getValue(), dataAttributes,
									mapOfDomainSourceVsListOfReprocessingData.keySet()),
							statusCriteria);
			criteriaList.add(c);
		}

		if (!StringUtils.isEmpty(normalizedValue)) {
			dataAttributes.stream().forEach(
					dataAttribute -> criteriaList.add(getCriteriaForNormalizedValue(normalizedValue, dataAttribute)));
		}

		Criteria criteria = new Criteria(criteriaList, Operator.OR);
		log.debug("Criteria to fetch affected documents for dataStorage {}  : {}", getDataStorageLevel(), criteria);
		input.setCriteria(criteria);
		input.setLevel(getApplicableDataLevels().get(0));
		return Optional.of(input);
	}

	/**
	 * @return
	 */
	private Criteria getStatusCriteria(final String domainSource) {
		List<DataAttribute> statusAttributes = getApplicableDataLevels().stream()
				.map(dataLevel -> getDataStorageLevel()
						.getAttributeByName(DataAttributeFactory.getStatusFlagForLevel(dataLevel)))
				.collect(Collectors.toList());
		List<Criteria> statusCriterias = statusAttributes.stream()
				.map(statusAttribute -> getStatusCriteriaForAttributeAndSource(statusAttribute, domainSource))
				.collect(Collectors.toList());
		if (statusCriterias.size() == 1) {
			return statusCriterias.get(0);
		} else {
			return new Criteria().andOperator(statusCriterias.toArray(new Criteria[statusCriterias.size()]));
		}

	}

	private Criteria getStatusCriteriaForAttributeAndSource(DataAttribute statusAttribute, String domainSource) {

		return attributeToCriteriaConverterUtility.createStatusAttributeCriteria(statusAttribute, DomainStatus.ACTIVE,
				domainSource);
	}
	
	/**
	 * 
	 * @param reprocessDataList
	 * @param dataAttributes
	 * @param domainSources
	 * @param normalizedValue 
	 * @return
	 */
	private Criteria setCriteriaForDataAttribute(List<ReprocessingData> reprocessDataList, List<DataAttribute> dataAttributes, Set<String> domainSources) {
		List<Criteria> criteriaList = new ArrayList<>();

		Set<DataAttribute> regexQueryAttributes = new HashSet<>();
		Set<DataAttribute> exactMatchQueryAttributes = new HashSet<>();
		
		segregateAttributesUsingNormalizationStrategy(dataAttributes, domainSources, regexQueryAttributes, exactMatchQueryAttributes);
		
		exactMatchQueryAttributes.forEach(dataAttribute ->
			criteriaList.add(Criteria.where(dataAttribute).in(getDomainTypeDataValueList(reprocessDataList)))
		
		);
		
		List<Criteria> criteriaListRegexMatchAttributes = getCriteriaForRegexMatchAttributes(regexQueryAttributes, reprocessDataList);
		criteriaList.addAll(criteriaListRegexMatchAttributes);
		
		return new Criteria(criteriaList, Operator.OR);
	}
	
	private List<Criteria> getCriteriaForRegexMatchAttributes(Set<DataAttribute> regexQueryAttributes,
			List<ReprocessingData> reprocessDataList) {
		
		List<Criteria> regexCriterias = new ArrayList<>();
		for (DataAttribute attr : regexQueryAttributes) {
			for (ReprocessingData data : reprocessDataList) {
				Criteria cri = Criteria.where(attr).regex(createDataValue(data.getDomainType(), LockLevel.FEED));
				regexCriterias.add(cri);
			}
		}
		return regexCriterias;
	}
	
	
	/**
	 * Create criteria for normalizedValue at rdu locklevels (i.g. RDU and ENRICHED locklevels)
	 * 
	 * @param normalizedValue
	 * @param dataAttribute
	 * @return
	 */
	private Criteria getCriteriaForNormalizedValue(String normalizedValue, DataAttribute dataAttribute) {
		List<Criteria> criteriaList = new ArrayList<>();

		LockLevel.rduLockLevels.stream().forEach(lockLevel -> criteriaList.add(Criteria.where(dataAttribute)
				.is(createDataValue(new DomainType(null, null, normalizedValue), lockLevel))));
		
		DataAttribute statusAttribute = getDataStorageLevel()
		.getAttributeByName(DataAttributeFactory.getStatusFlagForLevel(dataAttribute.getAttributeLevel()));

		return new Criteria(criteriaList, Operator.OR)
				.andOperator(getStatusCriteriaForAttributeAndSource(statusAttribute, null));
	}

	/**
	 * Segregate each attribute using RuleNormalizationStrategy 
	 * 
	 * @param domainSources 
	 */
	private void segregateAttributesUsingNormalizationStrategy(List<DataAttribute> dataAttributes, Set<String> domainSources, Set<DataAttribute> regexQueryAttributes 
			,Set<DataAttribute> exactMatchQueryAttribute) {

		Set<String> dataSources = new HashSet<>();
				for (String domainSource : domainSources) {
					
					Set<String> ds = cacheDataRetrieval.getListOfDataFromCache(StaticDataConstant.CODE_ATT_NAME,
							DataLevel.DATA_SOURCES.getCollectionName(), DataAttributeConstant.DOMAIN_SOURCE, domainSource);
					dataSources.addAll(ds);
				}
				
		dataSources.stream().forEach(ds->
			dataAttributes.forEach( attr -> {
				RuleNormalizationStrategy ruleNormalizationStrategy = ruleNormalizationStrategyCache.getStrategy(ds, attr.getAttributeName());
				if(ruleNormalizationStrategy ==RuleNormalizationStrategy.DOMAIN_REGEX_RESOLVER)		{
					regexQueryAttributes.add(attr);
				}else {
					exactMatchQueryAttribute.add(attr); 
				}
			}));

		//if the same attribute is present in both regex and exact then remove it from exactMatchQueryAttribute
		exactMatchQueryAttribute.removeAll(regexQueryAttributes);

	}

	private Collection<DataValue<? extends Serializable>> getDomainTypeDataValueList(List<ReprocessingData> reprocessDataList){
		return reprocessDataList.stream().map(reprocessData -> 
			 createDataValue(reprocessData.getDomainType(), LockLevel.FEED)
		).collect(Collectors.toList());
	}
	
	/**
	 * This method returns list<DataValue<DomainType>> for requested domainSource.
	 * @param domainSource
	 * @return
	 */
	private List<DataValue<? extends Serializable>> getDataSourceDataValue(String domainSource) {
		Set<String> listOfDataFromCache = cacheDataRetrieval.getListOfDataFromCache(StaticDataConstant.CODE_ATT_NAME,
				DataLevel.DATA_SOURCES.getCollectionName(), DataAttributeConstant.DOMAIN_SOURCE, domainSource);

		List<DataValue<? extends Serializable>> valueList = new ArrayList<>();
		for (String dataSource : listOfDataFromCache) {
			DomainType dataSourceDomainType = new DomainType(dataSource);
			valueList.add(createDataValue(dataSourceDomainType, LockLevel.FEED));

		}
		return valueList;
	}
	
	/**
	 * This methods create dataValue of DoaminType.
	 */
	private DataValue<DomainType> createDataValue(DomainType value, LockLevel lockLevel) {
		DataValue<DomainType> dataValue = new DataValue<>();
		dataValue.setValue(lockLevel, value);
		return dataValue;
	}

	/**
	 * Returns applicable dataAttributes for currrent dataStorage level
	 * 
	 * @param dataAttributesForRduDomain 
	 * @return
	 */
	private List<DataAttribute> getApplicableDataAttributes(List<DataAttribute> dataAttributesForRduDomain) {
		return dataAttributesForRduDomain.stream()
				.filter(attribute -> getApplicableDataLevels().contains(attribute.getAttributeLevel()))
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns DataStorage level for the implementation
	 * 
	 * @return
	 */
	protected abstract DataStorageEnum getDataStorageLevel();
	
	/**
	 * Returns the applicable levels for current implementation
	 *  
	 * @return
	 */
	protected abstract List<DataLevel> getApplicableDataLevels();

}
