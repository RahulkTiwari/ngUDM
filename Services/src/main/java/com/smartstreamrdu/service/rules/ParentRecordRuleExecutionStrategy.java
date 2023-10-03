/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ParentRecordRuleExecutionStrategy.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.InvalidDataTypeException;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleOutput;

public class ParentRecordRuleExecutionStrategy extends AbstractRuleExecutionStrategy {

	/**
	 * @param fileName
	 * @param prgm
	 */
	public ParentRecordRuleExecutionStrategy(String fileName, String prgm, LocalDateTime date ,String dataSource) {
		super(fileName, prgm, date,dataSource);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8701778197109597365L;
	
	private static final transient Logger _logger = LoggerFactory.getLogger(ParentRecordRuleExecutionStrategy.class);
	
	private Map<DataLevel, DataContainer> dataLevelVsDataContainer;
	

	
	@Override
	public List<DataContainer> executeStrategy(RecordWrapper recordWrapper, RduRuleEngine ruleEngine, List<RduRule> rules) {
		
		if (recordWrapper != null && rules != null) {
			
			ruleEngine.registerWithRuleEngine("dataContainer", getDataContainerForLevel(recordWrapper.getLevel()));
			
			executeParentRecordRules(recordWrapper, ruleEngine, rules);
			Record parentRecord = recordWrapper.getParentRecord();
			//prevent creation of redundant data container
			if (parentRecord.getRecordRawData().getRawDataLevel().equals(parentRecord.getLevel())) {
				executeRawDataIdRule(parentRecord);
			}
			clearBootStrapContext();
			
			ruleEngine.registerWithRuleEngine("dataContainer", null);
			
			return aggregateAvailableDataContainers();
		} else {
			_logger.debug("Unable to execute Praent record rule execution strategy because either the recordWrapper or the list of rules was {}.","null");
			return null;
		}
	}

	/**
	 *  This method will return a data container for the given level from the dataLevelVsDataContainer map.
	 *  If there is no DataContainer available for the specified level then this method creates a new DataContainer for that level and then puts it in the map and returns the same.
	 * @param dataLevel
	 * @param dataLevelVsDataContainer
	 * @return
	 */
	protected DataContainer getDataContainerForLevel(DataLevel dataLevel) {
		if (dataLevelVsDataContainer == null) {
			dataLevelVsDataContainer = new EnumMap<>(DataLevel.class);
		}

		return dataLevelVsDataContainer.computeIfAbsent(dataLevel, this::newDataContainer);
	}

	/**
	 * @param l
	 * @return
	 */
	protected DataContainer newDataContainer(DataLevel l) {
		return new DataContainer(l, DataContainerContext.builder().withFileName(fileName).withProgram(prgm).withUpdateDateTime(date).withDataSource(dataSource).build());
	}
	
	
	
	/**
	 * @param level
	 * @param rule
	 * @param ruleResult
	 */
	@Override
	protected void addDataAttributeAndDataValueToContainer(DataLevel level, RduRule rule, Serializable ruleResult) {
		DataContainer container = getDataContainerForLevel(level);
		addValueToDataContainer(rule, ruleResult, container);
	}

	protected void addValueToDataContainer(RduRule rule, Serializable ruleResult, DataContainer container) {
		RduRuleOutput ruleOutput = rule.getRuleOutput();

		if (StringUtils.isNotEmpty(ruleOutput.getParentAttributeName())) {
			return;
		}

		DataAttribute dataAttribute = getRuleDataAttibute(rule);
		if (dataAttribute == null) {
			return;
		}
		
		DataValue<Serializable> dataValue = createDataValueFromRuleResult(ruleResult, ruleOutput, dataAttribute);
		
		try {
			if(!isValueAlreadyExists(container,dataValue,dataAttribute,dataSource)) {
				container.addAttributeValue(dataAttribute, dataValue);		
			}
		} catch (InvalidDataTypeException dataTypeException) {
			_logger.debug(
					"The following exception occured {} while adding value : {} against dataAttribute : {} in the data container.",
					dataTypeException.getMessage(), dataValue, dataAttribute);
		}
	}

	@Override
	protected void addDataAttributeAndDataValueToContainer(DataAttribute complexDataAttribute, DataValue<Serializable> ruleResult) {
		DataContainer container = getDataContainerForLevel(complexDataAttribute.getAttributeLevel());
		container.addAttributeValue(complexDataAttribute, ruleResult);
	}
	
	
	/**
	 *  This method aggregates all the data containers available in the dataLevelVsDataContainer map based on the dataLevel.
	 *  For example, the security dataContainer should be put inside the instrument data container before returning the final value.
	 * @param dataLevelVsDataContainer
	 * @return
	 */
	protected ArrayList<DataContainer> aggregateAvailableDataContainers() {
		
		if(dataLevelVsDataContainer != null && !dataLevelVsDataContainer.isEmpty()){
			return new ArrayList<>(dataLevelVsDataContainer.values());
		}
		return null;
	}
	
	@Override
	protected Map<DataLevel, DataContainer> getContainers(){
		return dataLevelVsDataContainer;
	}

}
