/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleCustomFunctions.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DomainMaintenanceMetadata;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.UdmErrorCodes;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.cache.CacheRepository;
import com.smartstreamrdu.persistence.cache.RuleNormalizationStrategyCache;
import com.smartstreamrdu.persistence.repository.service.DomainMetadataRepositoryService;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.Rule;
import com.smartstreamrdu.rules.RuleNormalizationStrategy;
import com.smartstreamrdu.service.domain.DomainLookupService;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Slf4j
public class RuleCustomFunctions implements Serializable {

	private static final String INS = "INS";

	static final Logger logger = LoggerFactory.getLogger(RuleCustomFunctions.class);

	private static final long serialVersionUID = -6334891350846620945L;

	private transient DomainLookupService domainService = SpringUtil.getBean(DomainLookupService.class);

	private transient CacheRepository cache = SpringUtil.getBean(CacheRepository.class);

	private transient DomainMetadataRepositoryService metaDataRepoService =SpringUtil.getBean(DomainMetadataRepositoryService.class);

	private transient CacheDataRetrieval dataRetrievalService = SpringUtil.getBean(CacheDataRetrieval.class);
	
	private transient NormalizedValueService normalizedValueService = SpringUtil.getBean(NormalizedValueService.class);

	private transient RuleNormalizationStrategyCache strategyCache =  SpringUtil.getBean(RuleNormalizationStrategyCache.class);
	
	public void initialize() {
		if (domainService == null) {
			domainService = SpringUtil.getBean(DomainLookupService.class);
		}
		
	}


	public Serializable feedValue(RduRule rduRule, Record record, String[] keys, Serializable defaultValue) {
		Serializable value = defaultValue;

		if (record != null) {
			for (String key : keys) {				
				Serializable recordValue = record.getDataAttribute(key);
				if (recordValue != null) {
					value = recordValue;
					break;
				}
			}
		}
		return value;
	}

	public Serializable feedValue(RduRule rduRule, Record record, String[] keys) {
		return feedValue(rduRule, record, keys, null);
	}

	public Serializable feedValueWithDefaultDateFallback(RduRule rduRule, Record record, String[] keys, String regex,
			String pattern, Serializable defaultValue) {
		// This is specific implementation for anna udm-39557
		if (record != null) {
			for (String key : keys) {
				Serializable recordValue = record.getDataAttribute(key);
				if (recordValue == null || "".equals(recordValue.toString())) {
					return null;// this is done to set errorCode 103
				}
				if (regex != null && !String.valueOf(recordValue).matches(regex)) {
					return LocalDate.parse(recordValue.toString(), DateTimeFormatter.ofPattern(pattern));
				}
			}
		}
		return LocalDate.parse(defaultValue.toString(), DateTimeFormatter.ofPattern(pattern));
	}

	public Serializable domainLookup(@NotNull String dataSource, @NotNull RduRule rduRule, @NotNull Record record,
			@NotNull String[] domainData) {
		Objects.requireNonNull(dataSource, "DataSource should not be null");
		Objects.requireNonNull(rduRule, "RduRule should not be null");
		Objects.requireNonNull(rduRule.getRuleOutput(), "RduRule.getRuleOutput should not be null");
		Objects.requireNonNull(record, "Record should not be null");
		Objects.requireNonNull(domainData, "domainData should not be null");
		initialize();
		DomainType domainType = new DomainType();


		String domainName = domainData[0];

		DataStorageEnum dataStorage = dataRetrievalService.getDataStorageFromDataSource(dataSource);
		DataAttribute attribute = dataStorage.getAttributeByName(rduRule.getRuleOutput().getAttributeName());	

		populateDomainDataobject(record, domainData, domainType);
		domainType.setDomain(domainName);

		String rduDomainName = DataAttributeFactory.getRduDomainForDomainDataAttribute(attribute);
		Serializable rduNormalizedValue = normalizedValueService.getNormalizedValueForDomainValue(attribute, domainType, dataSource);

		
		if (rduNormalizedValue == null) {
			domainType.setVal("".equals(domainType.getVal()) ? null : domainType.getVal());
			domainType.setVal2("".equals(domainType.getVal2()) ? null : domainType.getVal2());
			logger.debug(
					"No rdu normalized value found for the domain data : {} for vendor domain : {} and rduDomain : {}",
					domainType, domainName, rduDomainName);
		}
		
		// If val == null,then returning empty domain object.This check needs to ensure
		// that we will not violating mandatory condition for domainObject that is for
		// valid domain object val should non-null.By returning empty
		// objects from here we are ensuring that in such cases ErrorCode-103
		// will get populated.
		if (domainType.getVal() == null) {
			return new DomainType();
		}
		
		return domainType;
	}

	/**
	 * @param record
	 * @param domainData
	 * @param domainType
	 */
	private void populateDomainDataobject(Record record, String[] domainData, DomainType domainType) {
		for (int j = 1; j < domainData.length; j++) {
			Serializable val = record.getDataAttribute(domainData[j]);
			if (j == 1) {
				domainType.setVal(val != null ? String.valueOf(val) : "");
			} else if (j == 2) {
				String val2ValueIfApplicable = "".equals(domainType.getVal()) ? "" : null;
				domainType.setVal2(val != null ? String.valueOf(val) : val2ValueIfApplicable);
			}
		}
	}

	/**
	 * @param dataSource
	 * @return
	 */
	protected String getDomainSourceFromDataSource(String dataSource) {

		try {
			return domainService.getDomainSourceFromDataSource(dataSource);
		} catch (Exception e) {
			logger.error("Following error occured while fetching domainSource for dataSource : {}", dataSource, e);
			return null;
		}

	}

	public Serializable normalizedValue(String dataSource, Rule rudRule, DataContainer dataContainer, String[] keys) {
		Serializable value = null;

		if (dataContainer != null) {
			for (String attributeName : keys) {
				DataStorageEnum dataStorage = dataRetrievalService.getDataStorageFromDataSource(dataSource);
				DataAttribute dataAttribute = dataStorage.getAttributeByName(attributeName);	
				value = dataContainer.getAttributeValueAtLevel(LockLevel.FEED, dataAttribute);
				try {
					if (!dataAttribute.isNull(value)) {
						break;
					}
				} catch (Exception e) {
					logger.error("Error occured while fetching the normalized value for attribute " + attributeName
							+ " from the data container", e);
				}
			}
		}

		return value;
	}

	/**
	 * This method fetches the domain value of the given data attribute from the
	 * data container and then returns the normalized rdu value if mapped in the
	 * dvDomainMap collection. This method accepts only works with data attributes
	 * of data type DomainType. If the passed data attribute is of any other type
	 * then this method will throw a IllegalArgumentException.
	 * 
	 * @param dataContainer
	 * @param domainInfo
	 * @return
	 */
	public Serializable normalizedDomainLookup(String dataSource, RduRule rduRule, DataContainer dataContainer,
			String[] domainInfo) {
		Serializable value = null;
		initialize();
		
		DataStorageEnum dataStorage = dataRetrievalService.getDataStorageFromDataSource(dataSource);

		String domainSource = getDomainSourceFromDataSource(dataSource);

		if (dataContainer != null) {
			String domainName = domainInfo[0];
			String attributeName = domainInfo[1];

			DataAttribute dataAttribute = dataStorage.getAttributeByName(attributeName);	

			if (dataAttribute.getDataType() == DataType.DOMAIN) {
				Serializable dataValue = dataContainer.getAttributeValueAtLevel(LockLevel.FEED, dataAttribute);
				if (dataValue instanceof DomainType) {
					value = ((DomainType) dataValue).getNormalizedValue();
					if (value == null) {
						value = domainService.getNormalizedValueForDomainValue(((DomainType) dataValue), domainSource,
								domainName, DataAttributeFactory.getRduDomainForDomainDataAttribute(dataAttribute));
					}
				}
			} else {
				logger.error(
						"The data attribute passed for normalized domain lookup is not of DomainType data type. DataAttribute : {}",
						attributeName);
				throw new IllegalArgumentException(
						"The data attribute provided " + dataAttribute + " is not a domain data type attribute.");
			}
		}

		return value;
	}

	public boolean isNormalizedValueAavailable(String dataSource, RduRule rduRule, DomainType domainData,
			String[] domainInformation) {
		initialize();
		boolean isAvailable = false;

		if(domainData.getDomain() == null) {
			domainData.setDomain(domainInformation[0]);
		}
		
		DataStorageEnum dataStorage = dataRetrievalService.getDataStorageFromDataSource(dataSource);
		DataAttribute attribute = dataStorage.getAttributeByName(rduRule.getRuleOutput().getAttributeName());	

		RuleNormalizationStrategy strategy = strategyCache.getStrategy(dataSource,
				attribute.getAttributeName());

		if (strategy == null) {
			log.error("No normalization strategy found for dataSource : {} , rduRule : {}", dataSource, rduRule);
			return false;
		}

		String domainSource = getDomainSourceFromDataSource(dataSource);
        String rduDomain = DataAttributeFactory.getRduDomainForDomainDataAttribute(attribute);
		
        Serializable rduNormalizedValue = getNormalizedValue(domainData, strategy, domainSource, rduDomain);
		
		if (rduNormalizedValue != null) {
			isAvailable = true;
		}

		return isAvailable;
	}


	/**
	 * @param domainData
	 * @param strategy
	 * @param domainSource
	 * @param rduDomain
	 * @return
	 */
	private Serializable getNormalizedValue(DomainType domainData, RuleNormalizationStrategy strategy,
			String domainSource, String rduDomain) {
		Serializable rduNormalizedValue = null ;
		switch (strategy) {
		case DOMAIN_REGEX_RESOLVER:
			return domainService.getPatternMatchNormalizedValueForDomainValue(domainData, domainSource,
					rduDomain);
		case DEFAULT:
			return  domainService.getNormalizedValueForDomainValue(domainData, domainSource, null, rduDomain);
		default:
			log.error("Specified ruleNormalizationStrategy : {} is not configured in system:", strategy);
		}
		return rduNormalizedValue;
	}

	public Serializable getFromCodeForMap(String[] arguments) {
		String collectionName = arguments[1].replaceFirst(arguments[1].substring(0, 1),
				arguments[1].substring(0, 1).toUpperCase());
		DataLevel level = DataLevel.getDataLevel(collectionName);
		String fromCode = null;
		if (level != null && arguments[0] != null) {
			fromCode = cache.getFromCodeForCollection(arguments[0], level, arguments[2]);
		}

		return fromCode;
	}
	
	/**
	 * This method will extract errorCode from map output value.
	 */
	public Serializable getErrorCodeFromMap(String[] arguments) {
		String fromCode = (String) getFromCodeForMap(arguments);
		String errorCode = StringUtils.substringBetween(fromCode, RuleConstants.ERROR_CODE, RuleConstants.ERROR_CODE_SEPARATOR);
		
		if(errorCode == null) {
			return null;
		}
		
		//Checking if there is valid errorCode is associated with errorCode return from map
		UdmErrorCodes udmErrorCode = UdmErrorCodes.getByErrorCode(Ints.tryParse(errorCode));

		if (udmErrorCode == null) {
			log.error("Returning null for errorCodeMap value :{} as this errorCode is not supported by udmErrorCodes.", errorCode);
			return null;
		}
		
		return errorCode;
	}
	
	
	/**
	 *
	 * This function accept JSONArray and maps it to aList of Strings.
	 * if the passed JsonArray has objects other than String it will throw
	 * IllegalArgumentException.
	 * 
	 * This method is to be used specifically when the input of the feedValue function 
	 * or any operation that returns a JSONArray needs to be mapped to a 
	 * List<String>
	 * 
	 * @param jsonResult
	 * @return List<String>
	 * @throws IllegalArgumentException
	 */
	public List<String> convertJsonArrayToListOfStrings(Serializable jsonResult) {

		ArrayList<String> listOfString = new ArrayList<>();

		// If jsonResult Contain Single String it will directly add to List
		// This is done due to implementation in Record.java method convertJsonArrayToObject for cases where we expect the Jayway 
		// library to return an JSONArray with a single element.
		// That method returns a single object if the returned value is a
		// JSONArray containing a single element.
		if (jsonResult instanceof String) {
			listOfString.add(jsonResult.toString());
			return listOfString;
		} else if (jsonResult instanceof JSONArray) {
			JSONArray jsonArray  = (JSONArray) jsonResult;
			// stream the JSONArray to access each element and add it to the list.
			jsonArray.stream().forEach(dataElement -> {
				// Add the element only if it is of type String.
				if (dataElement instanceof String) {
					listOfString.add((String) dataElement);
				}
				else {
					// For any other type we will throw in IllegalArgumentException.
					throw new IllegalArgumentException("The data in the JSONArray was not of type string. JSONArray is : "+jsonArray);
				}
			});
			return listOfString;
		} else {
			throw new IllegalArgumentException("The jsonResult was not of type JSONArray. jsonResult is : "+jsonResult);
		}
		
		
	}

	/**
	 * This function will return value from rawRecordObject.
	 * 
	 * @param record
	 * @param keys
	 * @param defaultValue
	 * @return
	 * @throws UdmTechnicalException
	 */
	public Serializable rawFeedValue(RduRule rduRule, Record record, String[] keys, Serializable defaultValue)
			throws UdmTechnicalException {
		Serializable value = defaultValue;

		if (record != null) {
			for (String key : keys) {
				Serializable recordValue = record.getRecordRawData().getRawDataAttribute(key);
				if (recordValue != null && StringUtils.isNotEmpty(recordValue.toString())) {
					value = recordValue;
					if (value instanceof JSONArray  && ((JSONArray) value).isEmpty()) {
						continue;
					}
					break;
				}
			}
		}
		if (value instanceof JSONArray) {
			JSONArray val = (JSONArray) value;

			if (val.size() > 1) {
				throw new UdmTechnicalException("for given attribute " + keys + "multiple values are found" + value,
						null);
			}
			return (Serializable) val.get(0);
		}
		return value;

	}

	public Serializable rawFeedValue(RduRule rduRule, Record record, String[] keys) throws UdmTechnicalException {
		return rawFeedValue(rduRule, record, keys, null);
	}
	
	/**
	 * This function fetches the DataAttribute of the rule output attribute and
	 * fetches its rduDomain. Using that rduDomain it gets the domain's primarykey
	 * from DomainMaintenanceMetadata Collection and then looks for the lookupValue
	 * provided int the script int the respective collection.If the value is present
	 * then returns a domainType object with normalized value specified for lookup
	 * in the rulescript.
	 * 
	 * @param rule
	 * @param lookupValue
	 * @return
	 */
	public Serializable normalizedDomainValue(RduRule rule, String lookupValue) {
		String attributeName = rule.getRuleOutput().getAttributeName();
		DataAttribute outputDataAttribute = RuleUtil.getRuleDataAttibute(rule,attributeName);
		String rduDomainForDomainDataAttribute = DataAttributeFactory
				.getRduDomainForDomainDataAttribute(outputDataAttribute);

		DomainMaintenanceMetadata domainMaintenanceMetadata = metaDataRepoService.getDomainMetadataMap()
				.get(rduDomainForDomainDataAttribute);
		String primaryKey = domainMaintenanceMetadata.getPrimaryKey();

		String resultValue = dataRetrievalService.getDataFromCache(primaryKey, rduDomainForDomainDataAttribute,primaryKey,lookupValue);

		if (StringUtils.isNotBlank(resultValue) && resultValue.equals(lookupValue))
			return new DomainType(null, null, resultValue);

		return null;

	}
	
	
	/**
	 * Takes Json Array as input and sort it in natural order then returns first value. 
	 * if the input is not json array then it return the input as is.
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Serializable firstValue(Serializable value) {
		if(value instanceof org.json.simple.JSONArray || value instanceof JSONArray) {
			 Optional<Object> first = ((List<Object>)value).stream().sorted().findFirst();
			 return first.isPresent()?(Serializable)first.get():null;
		}else {
			return value;
			
		}
		
	}

	/**
	 * This method provides rduInactivationDate based on supplied inputAttribute.Currently function support
	 * only security level attribute.
	 * 
	 * If InstrumentInactivationLevel is INS -(meaning feed itself provides instrument
	 * inactivation like idcApex) then rduInactivationDate == LocalDate.of(9999, 12,
	 * 31)
	 * 
	 * Else It compares attributeValue in all childContainers & based on following logic &  it returns value.
	 * If multiple active securities-pick latest value
	 * If one sec is Active & another is inactive - pick value from active security
	 * If one sec is Active(without value) & another is inactive - pick value from inactive security
	 * If all securities are inactive - pick latest value
	 * 
	 * @param noOfDays
	 * @param attributeName
	 * @param dataContainer
	 * @return
	 */
	public Serializable inactivateBasedOnAttribute(String noOfDays, String attributeName, DataContainer dataContainer) {

		DataAttribute datasourceAttribute = DataAttributeFactory.getDatasourceAttribute(dataContainer.getLevel());
		DomainType dataSourceVal = dataContainer.getHighestPriorityValue(datasourceAttribute);
		String dataSource = dataSourceVal.getVal();

		// If feed is insLevel feed(meaning feed itself provides instrument inactivation
		// like idcApex) then returning maxDate.
		String dataSourceInsInactivationLevel = dataRetrievalService.getDataSourceInsInactivationLevel(dataSource);
		if (dataSourceInsInactivationLevel != null && dataSourceInsInactivationLevel.equals(INS)) {
			return LocalDate.of(RuleConstants.MAX_YEAR, RuleConstants.MAX_MONTH, RuleConstants.MAX_DATE);
		}

		DataStorageEnum dataStorage = dataRetrievalService.getDataStorageFromDataSource(dataSource);
		DataAttribute attribute = dataStorage.getAttributeByName(attributeName);
		DataAttribute securityStatus = SdDataAttributeConstant.SEC_STATUS;
		List<DataContainer> allChildDataContainers = dataContainer.getAllChildDataContainers();
		Map<String, LocalDate> statusVsExpirationDateMap = new HashMap<>();

		for (DataContainer childContainer : allChildDataContainers) {
			LocalDate expirationDate = childContainer.getHighestPriorityValue(attribute);
			String normalizedStatus = getSecurityStatus(securityStatus, dataSource, childContainer);
			
			//population map of securityStatusVsLatestValue
			addLatestValueToMap(statusVsExpirationDateMap, normalizedStatus, expirationDate);
		}

		//This returns date from statusVsExpirationDateMap
		LocalDate expirationDate = getExpirationDate(statusVsExpirationDateMap);
		if (Objects.isNull(expirationDate)) {
			return LocalDate.of(RuleConstants.MAX_YEAR, RuleConstants.MAX_MONTH, RuleConstants.MAX_DATE);
		}
		
		Long additionalPeriod = Long.valueOf(noOfDays);
		return expirationDate.plusDays(additionalPeriod);
	}

	/**
	 * This method returns value of requested attribute from dataContainer.
	 * @param statusVsExpirationDateMap
	 * @return
	 */
	private LocalDate getExpirationDate(Map<String, LocalDate> statusVsExpirationDateMap) {
		if (statusVsExpirationDateMap.isEmpty()) {
			return null;
		}

		if (statusVsExpirationDateMap.containsKey(DomainStatus.ACTIVE)) {
			return statusVsExpirationDateMap.get(DomainStatus.ACTIVE);
		} else {
			return statusVsExpirationDateMap.get(DomainStatus.INACTIVE);
		}
	}

	/**
	 * This method returns security status value for dataConatiner.
	 * 
	 * @param securityStatus
	 * @param dataSource
	 * @param childContainer
	 * @return
	 */
	private String getSecurityStatus(DataAttribute securityStatus, String dataSource, DataContainer childContainer) {
		DomainType securityStatusValue = childContainer.getHighestPriorityValue(securityStatus);

		if (securityStatusValue.getNormalizedValue() != null) {
			return securityStatusValue.getNormalizedValue();
		}
		String rduDomainForDomainDataAttribute = DataAttributeFactory
				.getRduDomainForDomainDataAttribute(securityStatus);
		String domainSource = getDomainSourceFromDataSource(dataSource);
		return (String) domainService.getNormalizedValueForDomainValue(securityStatusValue, domainSource,
				securityStatusValue.getDomain(), rduDomainForDomainDataAttribute);
	}

	/**
	 * This method add latest value for requestedDate in map
	 * @param statusVsExpirationDateMap
	 * @param normalizedStatus
	 * @param expirationDate
	 */
	private void addLatestValueToMap(Map<String, LocalDate> statusVsExpirationDateMap, String normalizedStatus,
			LocalDate expirationDate) {

		if (expirationDate == null) {
			return;
		}
		// check whether map already contains key or not
		if (statusVsExpirationDateMap.containsKey(normalizedStatus)) {

			// if yes check whether new value isAfter existing one
			LocalDate existingdate = statusVsExpirationDateMap.get(normalizedStatus);
			if (expirationDate.isAfter(existingdate)) {
				// if yes.add latest value into map.
				statusVsExpirationDateMap.put(normalizedStatus, expirationDate);
			}
		} else {
			// if map doesn't contains key add entry into map.
			statusVsExpirationDateMap.put(normalizedStatus, expirationDate);
		}
	}

	/**
	 * This method returns rduInactivationDate based on securityStatus. If
	 * InstrumentInactivationLevel is INS -(meaning feed itself provides instrument
	 * inactivation like idcApex) then rduInactivationDate == LocalDate.of(9999, 12,
	 * 31)
	 * 
	 * If all feed securities are inactive & only technical security is active then
	 * this method returns rduInactivationDate == currentDate+noOfDays
	 * when input dataSource is null or input dataSource matches with the current dataSource
	 * 
	 * If any feed security is active then this method returns then
	 * rduInactivationDate == LocalDate.of(9999, 12, 31)
	 * 
	 * @param noOfDays
	 * @param dataSourceValue
	 * @param dataContainer
	 * @return
	 */
	public LocalDate inactivationBasedOnSecurity(String noOfDays, String dataSourceValue, DataContainer dataContainer) {
		DataAttribute datasourceAttribute = DataAttributeFactory.getDatasourceAttribute(dataContainer.getLevel());
		DomainType dataSourceVal = dataContainer.getHighestPriorityValue(datasourceAttribute);
		String dataSource = dataSourceVal.getVal();

		// If feed is insLevel feed(meaning feed itself provides instrument inactivation
		// like idcApex) then returning maxDate.
		String dataSourceInsInactivationLevel = dataRetrievalService.getDataSourceInsInactivationLevel(dataSource);
		if (dataSourceInsInactivationLevel == null || dataSourceInsInactivationLevel.equals(INS)) {
			return LocalDate.of(RuleConstants.MAX_YEAR, RuleConstants.MAX_MONTH, RuleConstants.MAX_DATE);
		}

		List<DataContainer> allChildDataContainers = dataContainer.getAllChildDataContainers();
		for (DataContainer childContainer : allChildDataContainers) {
			DomainType securityType = (DomainType) childContainer
					.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
			String normalizedStatus = getSecurityStatus(SdDataAttributeConstant.SEC_STATUS, dataSource, childContainer);

			// If technical Security is active then return rduInactivationDate =
			// currentDate+noOfDays
			if (securityType!= null && SdDataAttConstant.TECHNICAL.equals(securityType.getNormalizedValue())
					&& DomainStatus.ACTIVE.equals(normalizedStatus) && (dataSourceValue == null || dataSourceValue.equals(dataSource))) {
				return LocalDate.now().plusDays(Long.valueOf(noOfDays));
			}
		}
		return LocalDate.of(RuleConstants.MAX_YEAR, RuleConstants.MAX_MONTH, RuleConstants.MAX_DATE);
	}

	/**
	 * This method returns earliest localDate value from input parameters.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public LocalDate getEarliest(LocalDate o1, LocalDate o2) {
		if (Objects.isNull(o1)) {
			return o2;

		} else if (Objects.isNull(o2)) {
			return o1;

		} else if (o1.isBefore(o2)) {
			return o1;

		} else {
			return o2;
		}
	}
}
