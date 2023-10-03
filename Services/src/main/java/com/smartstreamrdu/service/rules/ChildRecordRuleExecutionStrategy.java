/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ChildRecordRuleExecutionStrategy.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

public class ChildRecordRuleExecutionStrategy extends AbstractRuleExecutionStrategy {
	
	/**
	 * @param fileName
	 * @param prgm
	 */
	public ChildRecordRuleExecutionStrategy(String fileName, String prgm, LocalDateTime date,String dataSource) {
		super(fileName, prgm, date,dataSource);
	}

	/**
	 * 
	 */
	private static final String DATA_CONTAINER = "dataContainer";

	/**
	 * 
	 */
	private static final long serialVersionUID = -107120916779375155L;

	private static final transient  Logger _logger = LoggerFactory.getLogger(ChildRecordRuleExecutionStrategy.class);

	protected Map<DataLevel, DataContainer> dataLevelVsDataContainer;
	
	protected List<DataContainer> childContainers = new ArrayList<>();
	
	DataContainer childContainer;

	@Override
	public ArrayList<DataContainer> executeStrategy(RecordWrapper recordWrapper, RduRuleEngine ruleEngine, List<RduRule> rules) {
		
		Objects.requireNonNull(ruleEngine, "ruleEngine cannot be null.");
		
		if (recordWrapper != null && rules != null) {
			
			ruleEngine.registerWithRuleEngine(DATA_CONTAINER, getDataContainerForLevel(recordWrapper.getLevel().getParentLevel()));
			
			executeParentRecordRules(recordWrapper, ruleEngine, rules);

			executeChildRecordRules(recordWrapper, ruleEngine, rules);

			clearBootStrapContext();
			
			ruleEngine.registerWithRuleEngine(DATA_CONTAINER, null);
			
			return aggregateAvailableDataContainers();
		} else {
			_logger.debug("Unable to execute record rule execution strategy because either the recordWrapper was {} and the list of rules was {}.", recordWrapper, rules);
			return null;
		}

	}

	protected void executeChildRecordRules(RecordWrapper recordWrapper, RduRuleEngine ruleEngine, List<RduRule> rules) {
		
		List<Record> childRecords = recordWrapper.getChildRecords();
		
		List<RduRule> childLevelRules = getRulesForLevel(rules, recordWrapper.getLevel(), false);
		
		childRecords.forEach(record -> {
			
			childContainer = createDataContainer(recordWrapper.getLevel());
			
			ruleEngine.registerWithRuleEngine(DATA_CONTAINER, childContainer);
			
			executeBootstrapRules(record, ruleEngine, rules, recordWrapper.getLevel());
			
			executeRulesForNonRelationalAttributes(record, ruleEngine, childLevelRules, recordWrapper.getLevel());
			
			executeRulesForNestedArrayAttributes(record, ruleEngine, childLevelRules);
			executeRawDataIdRule(record);
			childContainers.add(childContainer);
			
			ruleEngine.registerWithRuleEngine(DATA_CONTAINER, null);
		});
		
	}

	/**
	 * @param recordWrapper
	 * @return
	 */
	private DataContainer createDataContainer(DataLevel level) {
		return new DataContainer(level, DataContainerContext.builder().withFileName(fileName).withProgram(prgm).withUpdateDateTime(date).withDataSource(dataSource).build());
	}

	@Override
	protected void addDataAttributeAndDataValueToContainer(DataAttribute complexDataAttribute, DataValue<Serializable> ruleResult) {
		
		DataContainer container;
		
		if (complexDataAttribute.getAttributeLevel().getParentLevel() == null) {
			container = getDataContainerForLevel(complexDataAttribute.getAttributeLevel());
		} else {
			container = childContainer;
		}
		container.addAttributeValue(complexDataAttribute, ruleResult);
	}


	@Override
	protected void addDataAttributeAndDataValueToContainer(DataLevel level, RduRule rule, Serializable ruleResult) {
		RduRuleOutput ruleOutput = rule.getRuleOutput();

		if (StringUtils.isNotEmpty(ruleOutput.getParentAttributeName())) {
			return;
		}

		DataAttribute dataAttribute = getRuleDataAttibute(rule);
		if (dataAttribute == null) {
			return;
		}

		DataValue<Serializable> dataValue = createDataValueFromRuleResult(ruleResult, ruleOutput, dataAttribute);

		DataContainer container;

		if (level.getParentLevel() == null) {
			container = getDataContainerForLevel(level);
		} else {
			container = childContainer;
		}

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

	/**
	 *  This method will return a data container for the given level from the dataLevelVsDataContainer map.
	 *  If there is no DataContainer available for the specified level then this method creates a new DataContainer for that level and then puts it in the map and returns the same.
	 * @param dataLevel
	 * @param dataLevelVsDataContainer
	 * @return
	 */
	protected DataContainer getDataContainerForLevel(DataLevel dataLevel) {
		if (dataLevelVsDataContainer == null) {
			dataLevelVsDataContainer = new HashMap<>();
		}
		
		DataContainer dataContainer = dataLevelVsDataContainer.computeIfAbsent(dataLevel, k -> createDataContainer(dataLevel));
		
		if (dataContainer == null) {
			dataContainer = createDataContainer(dataLevel);
			dataLevelVsDataContainer.put(dataLevel, dataContainer);
		}
		return dataContainer;
	}

	
	/**
	 *  This method aggregates all the data containers available in the dataLevelVsDataContainer map based on the dataLevel.
	 *  For example, the security dataContainer should be put inside the instrument data container before returning the final value.
	 * @param dataLevelVsDataContainer
	 * @return
	 */
	protected ArrayList<DataContainer> aggregateAvailableDataContainers() {
		
		ArrayList<DataContainer> containers =  new ArrayList<>();

		DataContainer insDataContainer = getDataContainerForLevel(DataLevel.INS);
		
		childContainers.forEach(container -> insDataContainer.addDataContainer(container, DataLevel.SEC) );
		
		containers.add(insDataContainer);
		
		if (dataLevelVsDataContainer.get(DataLevel.LE) != null) {
			DataContainer legalEntityContainer = getDataContainerForLevel(DataLevel.LE);
			containers.add(legalEntityContainer);
		}
		return containers;
	}
	
	@Override
	protected Map<DataLevel, DataContainer> getContainers(){
		return dataLevelVsDataContainer;
	}
}
