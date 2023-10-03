/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	AbstractRuleExecutionStrategy.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.HistorizedData;
import com.smartstreamrdu.domain.InvalidDataTypeException;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordWrapper;
import com.smartstreamrdu.domain.UdmErrorCodes;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleData;
import com.smartstreamrdu.rules.RduRuleOutput;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.DataTypeConversionUtil;

public abstract class AbstractRuleExecutionStrategy implements RuleExecutionStrategy {

	private static final String RDU_PREFIX = "RDU_";
	private static final String RDU_PREFIX_PATH = "$.RDU_";
	private static final String RDU_RAW_PREFIX_PATH = "$.RDU_RAW_";

	private static final Logger _logger = LoggerFactory.getLogger(AbstractRuleExecutionStrategy.class);


	/**
	 * 
	 */
	private static final long serialVersionUID = 8164152406738697811L;

	private Map<String, Serializable> boorstrapRulesContext = new HashMap<>();

	protected String fileName;
	
	protected String prgm;
	
	protected LocalDateTime date;
	
	protected String dataSource;
	
	/**
	 * @param fileName
	 * @param prgm
	 */
	public AbstractRuleExecutionStrategy(String fileName, String prgm, LocalDateTime date,String dataSource) {
		super();
		this.fileName = fileName;
		this.prgm = prgm;
		this.date = date;
		this.dataSource = dataSource;
	}

	/** 
	 *  This method returns a subset of the list of rdu rules  supplied that are applicable on a specific DataLevel.
	 *  This method compares the given data level against the data level of Data Attribute's of the rules.
	 * @param rules
	 * @param level
	 * @param isParent - this will allow Document level attributes to be added to rule list only when the call is for parents like INS/LE.
	 * @return
	 */
	List<RduRule> getRulesForLevel(List<RduRule> rules, DataLevel level, boolean isParent) {
		List<RduRule> rulesForLevel = new ArrayList<>();

		rules.forEach(rule -> {
			String ruleAttributeName = StringUtils.isNotEmpty(rule.getRuleOutput().getParentAttributeName()) ? rule.getRuleOutput().getParentAttributeName() : rule.getRuleOutput().getAttributeName();
			if (ruleAttributeName != null) {
				if(StringUtils.isNoneBlank(rule.getRuleOutput().getVariableName()) && 
						StringUtils.isBlank(rule.getRuleOutput().getParentAttributeName())){
					rulesForLevel.add(rule);
				}
				else{
					DataAttribute dataAttribute = RuleUtil.getRuleDataAttibute(rule,ruleAttributeName);
					if (isLevelMatched(level, dataAttribute, isParent)) {
						rulesForLevel.add(rule);
					}
				}
			}
		});
		return rulesForLevel;
	}

	/**
	 * This method is overridden for parent record to allow processing of document level rules.
	 * 
	 * @param level
	 * @param dataAttribute
	 * @param isParent - this will allow Document level attributes to be added to rule list only when the call is for parents like INS/LE.
	 * @return true if attribute level is same as for parent/child or Document.
	 */
	boolean isLevelMatched(DataLevel level, DataAttribute dataAttribute, boolean isParent) {
		return dataAttribute.getAttributeLevel() == level || (isParent && dataAttribute.getAttributeLevel() == DataLevel.Document);
	}

	List<RduRule> getRulesForAttributeType(List<RduRule>rules, String attributeType) {
		List<RduRule> rulesForAttributesOfType = new ArrayList<>();

		rules.forEach(rule -> {
			if (attributeType.equals(RuleConstants.NON_NESTED_ATTRIBUTES)) {
				if (rule.getRuleOutput().getParentAttributeName() == null || rule.getRuleOutput().getParentAttributeName().equals("")) {
					rulesForAttributesOfType.add(rule);
				}
			} else {
				if (rule.getRuleOutput().getParentAttributeName() != null && !rule.getRuleOutput().getParentAttributeName().equals("")) {
					rulesForAttributesOfType.add(rule);
				}
			}
		});
		return rulesForAttributesOfType;
	}

	/**
	 *  This method return the subset of rules from the supplied list of rdu rules that have the ruleType
	 *  same as the one specified.
	 * @param rules
	 * @param ruleType
	 * @return
	 */
	List<RduRule> getRulesOfType(List<RduRule> rules, String ruleType) {
		List<RduRule> rulesOftype = new ArrayList<>();

		rules.forEach(rule -> {
			if (rule.getRuleFilter().getRuleType().equals(ruleType)) {
				rulesOftype.add(rule);
			}
		});

		return rulesOftype;
	}


	protected void executeBootstrapRules(Record record, RduRuleEngine ruleEngine, List<RduRule> rules, DataLevel level) {
		if (rules != null && !rules.isEmpty()) {
			List<RduRule> rulesForRuleType = getRulesOfType(rules, RuleConstants.BOOTSTRAP);

			rulesForRuleType.forEach(rule -> executeBootStrapRule(record, ruleEngine, level, rule));

		}
	}

	/**
	 * @param record
	 * @param ruleEngine
	 * @param level
	 * @param rule
	 */
	private void executeBootStrapRule(Record record, RduRuleEngine ruleEngine, DataLevel level, RduRule rule) {
		if (rule != null) {
			Serializable ruleResult = ruleEngine.executeRule(record, rule);

			// Add result value to normalized data container.
			if (rule.getRuleOutput().getAttributeName() != null && !rule.getRuleOutput().getAttributeName().equals("")) {
				DataAttribute dataAttribute = getRuleDataAttibute(rule);
				if (dataAttribute != null && dataAttribute.getAttributeLevel() == level) {
					addDataAttributeAndDataValueToContainer(level, rule, ruleResult);
				}
			}
			String variableName = rule.getRuleOutput().getVariableName();
			if(StringUtils.isNoneEmpty(variableName)){
				record.setAttribute(variableName, ruleResult);
				record.getRecordRawData().setRawAttribute(variableName, ruleResult);
			}
			// Add the outcome of the bootstrap rules to the bootstrapRulesContext.
			boorstrapRulesContext.put(rule.getRuleOutput().getVariableName(), ruleResult);
		}
	}

	
	/**
	 *  This method executes the feed rules from the supplied list of rules.
	 * @param record
	 * @param ruleEngine
	 * @param rules
	 * @param level
	 */
	protected void executeFeedRules(Record record, RduRuleEngine ruleEngine, List<RduRule> rules, DataLevel level) {
		if (rules != null && !rules.isEmpty()) {
			List<RduRule> rulesForRuleType = getRulesOfType(rules, RuleConstants.FEED);

			rulesForRuleType.forEach(rule -> {

				if (rule != null && isRuleApplicableAsPerBootstrapContext(rule)) {

					Serializable ruleResult = ruleEngine.executeRule(record, rule);

					// Add result value to normalized data container.
					try{
						addDataAttributeAndDataValueToContainer(level, rule, ruleResult);
					} catch (InvalidDataTypeException dataTypeException) {
						_logger.error("The following exception occured while adding value to data container. ", dataTypeException);
					}
				}
			});
		}
	}

	/**
	 *  This method compared whether the preConditions specified in a rule are satisfied by the 
	 *  result of the bootstrap rules triggered earlier. 
	 * @param rule
	 * @return
	 */
	protected boolean isRuleApplicableAsPerBootstrapContext(RduRule rule) {
		boolean isApplicable = true;

		if (rule != null) {
			RduRuleData ruleData = rule.getRuleData();
			Map<String, List<Serializable>> preConditions = ruleData.getPreConditions();
			if (preConditions != null) {
				for (Entry<String, List<Serializable>> entry : preConditions.entrySet()) {
					Serializable contextValue = boorstrapRulesContext.get(entry.getKey()); 
					isApplicable = isValueApplicable(entry, contextValue);
					if (isApplicable) {
						break;
					}
				}
			}
			return isApplicable;
		}
		return false;
	}

	/**
	 * @param entry
	 * @param contextValue
	 * @return
	 */
	private boolean isValueApplicable(Entry<String, List<Serializable>> entry, Serializable contextValue) {
		boolean isApplicable;
		if (contextValue != null) {
			if (entry.getValue().contains(contextValue)) {
				isApplicable = true;
			} else {
				isApplicable = false;
			}
		} else {
			isApplicable = false;
		}
		return isApplicable;
	}

	protected void clearBootStrapContext() {
		boorstrapRulesContext.clear();
	}

	/**
	 *  This method executes the Dependent rules from the supplied list of rules.
	 * @param record
	 * @param ruleEngine
	 * @param rules
	 * @param level
	 */
	protected void executeDependentRules(Record record, RduRuleEngine ruleEngine, List<RduRule> rules, DataLevel level) {
		if (CollectionUtils.isNotEmpty(rules)) {
			List<RduRule> rulesForRuleType = getRulesOfType(rules, RuleConstants.DEPENDENT);
			Map<DataLevel, DataContainer> dataLevelVsDataContainer = getContainers();
			//DataContainer container = get

			rulesForRuleType.forEach(rule -> 
			executeDependentRule(record, ruleEngine, level, dataLevelVsDataContainer, rule));
		}
	}

	/**
	 * @param record
	 * @param ruleEngine
	 * @param level
	 * @param dataLevelVsDataContainer
	 * @param rule
	 */
	private void executeDependentRule(Record record, RduRuleEngine ruleEngine, DataLevel level,
			Map<DataLevel, DataContainer> dataLevelVsDataContainer, RduRule rule) {
		if (rule != null) {
			boolean applicable = true;
			if(dataLevelVsDataContainer!= null && CollectionUtils.isNotEmpty(rule.getRuleFilter().getNormalizedAttributes())){
				applicable = isRuleApplicableForDataAttribute(dataLevelVsDataContainer.values(),rule, rule.getRuleFilter().getNormalizedAttributes());
			}
			if(!applicable){
				_logger.info("Not applying rule {} as the normalized attribute on which the rule depends, is not populated ",rule);
			}
			
			if (applicable && isRuleApplicableAsPerBootstrapContext(rule)) {
				Serializable ruleResult = ruleEngine.executeRule(record, rule);

				// Add result value to normalized data container.
				addDataAttributeAndDataValueToContainer(level, rule, ruleResult);
			}
		}
	}


	/**
	 *  This method executes the rules for all the non-relational independent attributes from the list of the supplied rules.
	 *  Examples of such rules can be rules for attributes which are at the overlying level.
	 * @param record
	 * @param ruleEngine
	 * @param rules
	 * @param level
	 * @param attributeType
	 */
	protected void executeRulesForNonRelationalAttributes(Record record, RduRuleEngine ruleEngine, List<RduRule> rules, DataLevel level) {

		// TODO : Make changes to this method. This will need to handle the ALIAS based approach that we have though of.

		List<RduRule> attributeTypeSpecificRules = getRulesForAttributeType(rules, RuleConstants.NON_NESTED_ATTRIBUTES);

		// Get all the feed rules from the given rules and execute.
		executeFeedRules(record, ruleEngine, attributeTypeSpecificRules, level);

		// Get all the dependent rules from the given rules and execute.
		executeDependentRules(record, ruleEngine, attributeTypeSpecificRules, level);
	}


	/**
	 *  This method executes the rules for all the nested independent attributes from the list of the supplied rules.
	 *  Examples of such rules can be rules for attributes which are at the underlying level.  
	 * @param record
	 * @param ruleEngine
	 * @param rules
	 * @param level
	 * @param attributeType
	 */
	protected void executeRulesForNestedArrayAttributes(Record record, RduRuleEngine ruleEngine, List<RduRule> rules) {

		List<RduRule> attributeTypeSpecificRules = getRulesForAttributeType(rules, RuleConstants.NESTED_ATTRIBUTES);

		Map<String, Set<RduRule>> aggregatedRelationalRules = getAggregatedRelationalAttributeRules(attributeTypeSpecificRules);
		
		/* 
		 * Filter out only those nested array attributes that exist independently.
		 * 
		 * For example, attributes such are instrumentLegalEntityRelations, legalEntityRelations, callRedemptions, etc.
		 * are in existence independently.
		 * 
		 * Whereas, nested array attributes such as callSchedules exist inside other nested array attributes. 
		 */ 
		Map<String, Set<RduRule>> filteredAggregatedRelationalRules = filterMapByAttributeDepth(aggregatedRelationalRules);
		filteredAggregatedRelationalRules.forEach((parentAttributeName, parentAttributeAssociatedRules) -> {
 
			DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
			DataAttribute parentAttribute = getRuleDataAttributeFromSet(parentAttributeAssociatedRules,parentAttributeName);
			DataRow links = new DataRow(parentAttribute, valueList);
			ArrayList<DataRow> linkList = new ArrayList<>();
			valueList.setValue(LockLevel.FEED, linkList);
			
			Map<ParentAttributeNameRelationShipTypePojo, Set<RduRule>> parentAttributeNameRelationshipTypeRules = getParentAttributeWithRelationshipTypeSets(parentAttributeAssociatedRules);

			Iterator<Entry<ParentAttributeNameRelationShipTypePojo, Set<RduRule>>> parentAttributeIterator = parentAttributeNameRelationshipTypeRules.entrySet().iterator();
			
			executeRules(record, ruleEngine, parentAttribute, linkList, parentAttributeIterator, aggregatedRelationalRules);
			
			try {
				addDataAttributeAndDataValueToContainer(parentAttribute, links);
			} catch (Exception e) {
				_logger.error("Error occured while execution rule : ", e);
			}


		});

	}

	/**
	 * This method filter map by depth of attribute.The ones with the 0 depth will
	 * be return. This will be done with the help of the parent attribute
	 * information available in the core DataAttribute framework.
	 * 
	 * Ex: instrumentMoodysRatings has no parent dataAttribute so depth is 0 and
	 * will added to map. callSchdeules has parent callRedemptions and
	 * callRedemptions has no parent, So depth of callSchedules is 1 and
	 * callRedemptions is 0. it means callSchdeules will be filtered out and
	 * callRedemptions will added to map;
	 * 
	 * @param aggregatedRelationalRules
	 * @return
	 */
	private Map<String, Set<RduRule>> filterMapByAttributeDepth(Map<String, Set<RduRule>> aggregatedRelationalRules) {
		return aggregatedRelationalRules.entrySet().stream().filter(entry -> getAttributeDepth(entry) == 0)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						HashMap::new));
	}

	/**
	 * This method will calculate the depth of given DataAttribute;
	 * 
	 * @param entry
	 * @return
	 */
	private Integer getAttributeDepth(Entry<String, Set<RduRule>> entry) {
		int depth = 0;
		DataAttribute dataAttribute = getRuleDataAttributeFromSet(entry.getValue(), entry.getKey());
		if (null != dataAttribute) {
			while (dataAttribute.getParent() != null) {
				depth = depth + 1;
				dataAttribute = dataAttribute.getParent();
			}
			return depth;
		}
		return -1;
	}

	/**
	 * This method returns ruleDataAttribute from input set.
	 * @param parentAttributeAssociatedRules
	 * @param parentAttributeName
	 * @return
	 */
	private DataAttribute getRuleDataAttributeFromSet(Set<RduRule> parentAttributeAssociatedRules, String parentAttributeName) {
		Iterator<RduRule> ruleIterator = parentAttributeAssociatedRules.iterator();
		if(ruleIterator.hasNext()) {
			RduRule rule = ruleIterator.next();
			return RuleUtil.getRuleDataAttibute(rule,parentAttributeName);
		}else {
			return null;
		}
	}
	
	
	protected DataAttribute getRuleDataAttibute(RduRule rule) {
		return RuleUtil.getRuleDataAttibute(rule,rule.getRuleOutput().getAttributeName());
	}

	/**
	 * @param record
	 * @param ruleEngine
	 * @param parentAttribute
	 * @param linkList
	 * @param parentAttributeIterator
	 * @param allRelationalRules 
	 */
	private void executeRules(Record record, RduRuleEngine ruleEngine, DataAttribute parentAttribute,
			ArrayList<DataRow> linkList, Iterator<Entry<ParentAttributeNameRelationShipTypePojo, Set<RduRule>>> parentAttributeIterator, Map<String, Set<RduRule>> allRelationalRules) {

		while (parentAttributeIterator.hasNext()) {
			
			Entry<ParentAttributeNameRelationShipTypePojo, Set<RduRule>> entry = parentAttributeIterator.next();

			Set<RduRule> nestedAttributeRules = entry.getValue();

			DataValue<String> relationShipTypeValue = new DataValue<>();

			handleNestedRules(record, ruleEngine, parentAttribute, linkList, nestedAttributeRules, relationShipTypeValue, allRelationalRules);			
		}
	}

	/**
	 * This method executes rules for the nested attributes.
	 * 
	 * @param record
	 * @param ruleEngine
	 * @param parentAttribute
	 * @param linkList
	 * @param nestedAttributeRules
	 * @param relationShipTypeValue
	 * @param allNestedRules 
	 */
	private void handleNestedRules(Record record, RduRuleEngine ruleEngine, DataAttribute parentAttribute,
			ArrayList<DataRow> linkList, Set<RduRule> nestedAttributeRules, DataValue<String> relationShipTypeValue, 
			Map<String, Set<RduRule>> allNestedRules) {
		
		ArrayList<Object> feedNestedArray = shouldExecuteRulesOnArrayOfInput(record, parentAttribute.getAttributeName(), nestedAttributeRules);

		if (!feedNestedArray.isEmpty()) {
			// These means that there are multiple elements at the specified path.
			// So we will iterate on each element and populates its sub-complex and then add it to the data container.
			iterateOverNestedObjects(record, ruleEngine, parentAttribute, linkList, nestedAttributeRules, feedNestedArray, allNestedRules);
		} else if(isRelationalAttributeRules(nestedAttributeRules)) {
			DataRow link = executeNestedAttributeRule(nestedAttributeRules, record, ruleEngine, parentAttribute, relationShipTypeValue);
			linkList.add(link);
		}
	}

	/** 
	 *  Returns true if the set of rules have a relation shipt type populated.
	 * 
	 * @param nestedAttributeRules
	 * @return
	 */
	private boolean isRelationalAttributeRules(Set<RduRule> nestedAttributeRules) {
		return nestedAttributeRules.stream().anyMatch(rule -> StringUtils.isNotEmpty(rule.getRuleOutput().getRelationshipType()));
	}

	/**
	 *  Checks whether the given data attribute has mapping rules available
	 *  from the RduRules collection.
	 * 
	 * @param childNestedArrayAttribute
	 * @param allRelationalRules
	 * @return
	 */
	private boolean mappingRulesAvailableForAttribute(DataAttribute childNestedArrayAttribute,
			Map<String, Set<RduRule>> allRelationalRules) {
		return allRelationalRules.entrySet().stream().anyMatch(entry -> entry.getKey().equals(childNestedArrayAttribute.getAttributeName()));
	}

	/**
	 *  Returns list of DataAttributes that are child of the given
	 *  data attribute and are of type NESTED_ARRAY.
	 * 
	 * @param parentAttribute
	 * @return
	 */
	private List<DataAttribute> getChildNestedArrayAttributesForParentAttribute(DataAttribute parentAttribute) {
		return DataAttributeFactory.getAllAttributes().entrySet().stream().filter(entry -> 
			parentAttribute.equals(entry.getKey().getParent()) && DataType.NESTED_ARRAY.equals(entry.getValue().getDataType())
		).map(Entry::getValue).collect(Collectors.toList());
	}

	/**
	 *  This method iterates over each element in the specified feed JSON array and 
	 *  populates a data row for each of them using the specified rules.
	 * 
	 * @param record
	 * @param ruleEngine
	 * @param parentAttribute
	 * @param linkList
	 * @param nestedAttributeRules
	 * @param relationShipTypeValue
	 * @param array
	 * @param allNestedRules 
	 */
	private void iterateOverNestedObjects(Record record, RduRuleEngine ruleEngine, DataAttribute parentAttribute, ArrayList<DataRow> linkList, Set<RduRule> nestedAttributeRules,
			ArrayList<Object> array, Map<String, Set<RduRule>> allNestedRules) {

		Optional<RduRule> parentPathRule = nestedAttributeRules.stream().filter(rule -> getParentAttrFeedRule(rule, parentAttribute.getAttributeName())).findFirst();
		List<Object> rawNestedArray = new ArrayList<>();
		if (parentPathRule.isPresent()) {
			 rawNestedArray = convertNestedResultToArray(record.getRecordRawData().getRawDataAttribute(parentPathRule.get().getRuleData().getRuleScript()));
		}
		
		for (int currentIndex=0; currentIndex< array.size(); currentIndex++) {

			Serializable nestedObject = (Serializable) array.get(currentIndex);

			// Populate the element in the record object at the specific path.
			record.setAttribute(RDU_PREFIX_PATH+parentAttribute.getAttributeName(), nestedObject);
			
			if (CollectionUtils.isNotEmpty(rawNestedArray)) {
				Serializable rawNestedObject = (Serializable) rawNestedArray.get(currentIndex);
				// populate the raw element in the record object at the specific path.
				record.getRecordRawData().setRawAttribute(RDU_RAW_PREFIX_PATH+parentAttribute.getAttributeName(), rawNestedObject);
			}

			DataValue<String> relationShipTypeValue = new DataValue<>();
			DataRow link = executeNestedAttributeRule(nestedAttributeRules, record, ruleEngine, parentAttribute, relationShipTypeValue);
			
			linkList.add(link);
			
			// Check if any child data attributes exist for the current parent data attribute that are
			// of type NESTED_ARRAY.
			processSubAttributes(record, ruleEngine, parentAttribute, allNestedRules, link);
			
			record.setAttribute(RDU_PREFIX_PATH+parentAttribute.getAttributeName(), null);
			record.getRecordRawData().setRawAttribute(RDU_RAW_PREFIX_PATH+parentAttribute.getAttributeName(), null);
		}
	}

	/** 
	 *  Checks if the given data attribute has any child attributes that are nested arrays in their data type.
	 *  If found, we will iterate the entire process over for those attributes and repeat the process to 
	 *  populate thier sub-complex data rows in the existing data row.
	 * @param record
	 * @param ruleEngine
	 * @param parentAttribute
	 * @param allNestedRules
	 * @param link
	 */
	private void processSubAttributes(Record record, RduRuleEngine ruleEngine,
			DataAttribute parentAttribute, Map<String, Set<RduRule>> allNestedRules, DataRow link) {
		
		List<DataAttribute> childNestedArrayAttributes = getChildNestedArrayAttributesForParentAttribute(parentAttribute);

		for (DataAttribute childNestedArrayAttribute : childNestedArrayAttributes) {
			
			if (mappingRulesAvailableForAttribute(childNestedArrayAttribute, allNestedRules)) {
				
				// Create new data row structure for the new child nested array attribute.
				DataValue<String> childRelationShipTypeValue = new DataValue<>();
				DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
				DataRow links = new DataRow(parentAttribute, valueList);
				ArrayList<DataRow> childLinkList = new ArrayList<>();
				valueList.setValue(LockLevel.FEED, childLinkList);
				
				// Fetch rules for all the attributes that are to be populated inside the child nested attribute.
				Set<RduRule> rulesForChildNestedArrayAttribute = allNestedRules.get(childNestedArrayAttribute.getAttributeName());
				// Execute rules for all child attributes.
				handleNestedRules(record, ruleEngine, childNestedArrayAttribute, childLinkList, rulesForChildNestedArrayAttribute, childRelationShipTypeValue, allNestedRules);

				link.addAttribute(childNestedArrayAttribute, links);
			}
			
		}
	}

	/**
	 *  Converts the result of the given nested array rule in the format
	 *  of ArrayList<Object>
	 * 
	 * @param rawNestedArray
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<Object> convertNestedResultToArray(Object rawNestedArray) {
		if (rawNestedArray instanceof JSONArray) {
				return (JSONArray) rawNestedArray;
			} else if (rawNestedArray instanceof net.minidev.json.JSONArray) {
				return (net.minidev.json.JSONArray) rawNestedArray;
			} else if (rawNestedArray instanceof JSONObject || rawNestedArray instanceof net.minidev.json.JSONObject) {
				JSONArray jsonArray = new JSONArray();
				jsonArray.add(rawNestedArray);
				return jsonArray;
			} else {
				return new ArrayList<>();
			}
	}

	/**
	 *  When there is a set of rules that need to be executed multiple times over a array of inputs, 
	 *  there should be a one rule that will define JSONPath for that array and then this method will fetch that JSONPath from record object.
	 *  This rule will have 'attributeName' as empty, 'parentAttributeName' will be the parent array name 
	 *  and 'variableName' should contain a value with given convention : 'RDU_parentAttributeName'. 
	 *  
	 *  This method checks if that JSONPath is available in {@link Record}, If yes then It returns that array. Else
	 *  returns null.
	 * 
	 * @param record
	 * @param parentAttributeName
	 * @param nestedAttributeRules
	 * @return
	 */
	private ArrayList<Object> shouldExecuteRulesOnArrayOfInput(Record record, String parentAttributeName, Set<RduRule> nestedAttributeRules) {
		Optional<RduRule> parentAttrFeedRuleO = nestedAttributeRules.stream().filter(rule -> getParentAttrFeedRule(rule, parentAttributeName)).findFirst();
		if(parentAttrFeedRuleO.isPresent()) {
			Object value = record.getDataAttribute(parentAttrFeedRuleO.get().getRuleData().getRuleScript());
			return convertNestedResultToArray(value);
		}
		return new ArrayList<>();
	}
	
	/**
	 * Get a feed rule from a set of rules for same parent attribute, that matches the below conditions.
	 * <ul>
	 * <li>Should match parent attribute name And </li>
	 * <li>variableName should be equals to 'RDU_parentAttributeName' </li>
	 * </ul>  
	 * @param rule
	 * @param parentAttributeName
	 * @return
	 */
	private boolean getParentAttrFeedRule(RduRule rule, String parentAttributeName) {
		return parentAttributeName.equals(rule.getRuleOutput().getParentAttributeName()) && 
				((RDU_PREFIX+parentAttributeName).equals(rule.getRuleOutput().getVariableName()));
	}

	private DataRow executeNestedAttributeRule(Set<RduRule> nestedAttributeRules, Record record, RduRuleEngine ruleEngine, DataAttribute parentAttribute,
			DataValue<String> relationShipTypeValue) {

		DataRow link = new DataRow(parentAttribute);

		DataRow ref1 = null;
		DataAttribute refDataAttr = getRefDataAttributeIfAvailable(parentAttribute);

		if (null != refDataAttr) {
			ref1 = new DataRow(refDataAttr);
			link.addAttribute(refDataAttr, ref1);
		}
		
		for (RduRule rule : nestedAttributeRules) {
			
			if (StringUtils.isEmpty(rule.getRuleOutput().getAttributeName())) {
				continue;
			}
			
			LockLevel levelForName = LockLevel.getLevelForName(rule.getRuleOutput().getLockLevel());
			if (relationShipTypeValue.getValue(levelForName) == null && !StringUtils.isEmpty(rule.getRuleOutput().getRelationshipType())) {
				relationShipTypeValue.setValue(levelForName, rule.getRuleOutput().getRelationshipType());
				link.addAttribute(DataAttributeFactory.getRelationTypeAttribute(parentAttribute), relationShipTypeValue);
			}

			// Execute the rule.
			DataAttribute dataAttribute = getRuleDataAttibute(rule);

			Serializable ruleResult = executeSingleRule(record, ruleEngine, rule);

			DataValue<Serializable> dataValue = createDataValueFromRuleResult(ruleResult, rule.getRuleOutput(), dataAttribute);
			// Add attribute value to the relevant level.
			addAttributeValue(link, ref1, refDataAttr, dataAttribute, dataValue);
		}
		
		return link;
	}

	/**
	 * @param link
	 * @param ref1
	 * @param refDataAttr
	 * @param dataAttribute
	 * @param dataValue
	 */
	private void addAttributeValue(DataRow link, DataRow ref1, DataAttribute refDataAttr, DataAttribute dataAttribute,
			DataValue<Serializable> dataValue) {
		if (null != refDataAttr) {
			ref1.addAttribute(dataAttribute, dataValue);						
		} else {
			link.addAttribute(dataAttribute, dataValue);
		}
	}

	/**
	 * @param parentAttribute
	 * @return
	 */
	protected DataAttribute getRefDataAttributeIfAvailable(DataAttribute parentAttribute) {
		try {
			return DataAttributeFactory.getRelationRefDataAttribute(parentAttribute);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private Map<ParentAttributeNameRelationShipTypePojo, Set<RduRule>> getParentAttributeWithRelationshipTypeSets(Set<RduRule> parentAttributeAssociatedRules) {

		Map<ParentAttributeNameRelationShipTypePojo, Set<RduRule>> someMap = new HashMap<>();

		parentAttributeAssociatedRules.forEach(rduRule -> {

			if ((rduRule.getRuleOutput().getParentAttributeName() != null && !rduRule.getRuleOutput().getParentAttributeName().equals(""))) {
				ParentAttributeNameRelationShipTypePojo pojo = new ParentAttributeNameRelationShipTypePojo(rduRule.getRuleOutput().getParentAttributeName(), rduRule.getRuleOutput().getRelationshipType());

				if (someMap.containsKey(pojo)) {
					Set<RduRule> someRules = someMap.get(pojo);
					if (someRules == null) {
						someRules = new HashSet<>();
						someRules.add(rduRule);
						someMap.put(pojo, someRules);
					} else {
						someRules.add(rduRule);
					}
				} else {
					Set<RduRule> someRules = new HashSet<>();
					someRules.add(rduRule);
					someMap.put(pojo, someRules);
				}
			}

		});

		return someMap;
	}

	protected Serializable executeSingleRule(Record record, RduRuleEngine ruleEngine, RduRule rule) {
		Serializable ruleResult = null;
		if (isRuleApplicableAsPerBootstrapContext(rule)) {
			ruleResult = ruleEngine.executeRule(record, rule);
		}
		return ruleResult;
	}


	private Map<String, Set<RduRule>> getAggregatedRelationalAttributeRules(List<RduRule> attributeTypeSpecificRules) {
		Map<String, Set<RduRule>> aggregatedSet = new HashMap<>();

		attributeTypeSpecificRules.forEach(rule -> {

			String parentAttributeName = rule.getRuleOutput().getParentAttributeName();
			if (parentAttributeName != null && !parentAttributeName.equals("")) {
				if (aggregatedSet.containsKey(parentAttributeName)) {
					Set<RduRule> setOfRduRules = aggregatedSet.get(parentAttributeName);
					if (setOfRduRules != null) {
						setOfRduRules.add(rule);
					} else {
						setOfRduRules = new HashSet<>();
						setOfRduRules.add(rule);
						aggregatedSet.put(parentAttributeName, setOfRduRules);
					}
				} else {
					Set<RduRule> setOfRduRules = new HashSet<>();
					setOfRduRules.add(rule);
					aggregatedSet.put(parentAttributeName, setOfRduRules);
				}
			}

		});

		return aggregatedSet;
	}

	
	
	private boolean isRuleApplicableForDataAttribute(Collection<DataContainer> containers, RduRule rule, List<String> normalizedAttributes){
		
		if(CollectionUtils.isEmpty(containers)){
			return false;
		}
		
		for(DataContainer container: containers){
			for (String key : normalizedAttributes) {
				Serializable attributeValue = container.getAttributeValue(RuleUtil.getRuleDataAttibute(rule, key));
				if(attributeValue != null){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param recordWrapper
	 * @param ruleEngine
	 * @param rules
	 */
	protected void executeParentRecordRules(RecordWrapper recordWrapper, RduRuleEngine ruleEngine, List<RduRule> rules) {
		Record parentRecord = recordWrapper.getParentRecord();

		DataLevel level = recordWrapper.getLevel();
		
		if (level.getParentLevel() != null) {
			level = level.getParentLevel();
		}

		List<RduRule> parentLevelRules = getRulesForLevel(rules, level, true);

		// Get all bootstrap rules from the given rules and execute.
		executeBootstrapRules(parentRecord, ruleEngine, rules, level);

		// Execute rules for all non-relational attributes.
		executeRulesForNonRelationalAttributes(parentRecord, ruleEngine, parentLevelRules, level);

		// Execute rules for all relational attributes.
		executeRulesForNestedArrayAttributes(parentRecord, ruleEngine, parentLevelRules);
	}
	
	protected void executeRawDataIdRule(Record record) {
		DataLevel level = DataLevel.valueOf(record.getRecordRawData().getRawDataLevel());
		Optional<String> rawDataIdAttributeNameOptional = DataAttributeFactory.getRawDataIdAttributeNameForLevel(level);
		if (StringUtils.isNotEmpty(record.getRecordRawData().getRawDataId()) && rawDataIdAttributeNameOptional.isPresent()) {
			RduRule rule = new RduRule();
			RduRuleOutput ruleOutput = new RduRuleOutput();
			ruleOutput.setAttributeName(rawDataIdAttributeNameOptional.get());
			ruleOutput.setLockLevel(LockLevel.FEED.getLockLevelName());
			ruleOutput.setDataLevel(level.name());
			rule.setRuleOutput(ruleOutput);
			addDataAttributeAndDataValueToContainer(level, rule, record.getRecordRawData().getRawDataId());
		} else if (StringUtils.isEmpty(record.getRecordRawData().getRawDataId())) {
			_logger.error(String.format("Raw Data ID not found for Record %s", record));
		} else {
			_logger.error(String.format("Raw Data ID attribute not found for level %s", level));
		}
	}


	protected abstract void addDataAttributeAndDataValueToContainer(DataLevel level, RduRule rule, Serializable ruleResult);

	protected abstract void addDataAttributeAndDataValueToContainer(DataAttribute complexDataAttribute, DataValue<Serializable> ruleResult);


	class ParentAttributeNameRelationShipTypePojo {

		String parentAttributeName;

		String relationShipType;

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof ParentAttributeNameRelationShipTypePojo)) {
				return false;
			}
			ParentAttributeNameRelationShipTypePojo castOther = (ParentAttributeNameRelationShipTypePojo) other;
			return new EqualsBuilder().append(parentAttributeName, castOther.parentAttributeName)
					.append(relationShipType, castOther.relationShipType).isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(parentAttributeName).append(relationShipType).toHashCode();
		}



		ParentAttributeNameRelationShipTypePojo(String parentAttribute, String relationShip) {
			this.parentAttributeName = parentAttribute;
			this.relationShipType = relationShip;
		}

	}

	/**
	 * @return the boorstrapRulesContext
	 */
	protected Map<String, Serializable> getBoorstrapRulesContext() {
		return boorstrapRulesContext;
	}

	/**
	 * @return
	 */
	protected abstract Map<DataLevel, DataContainer> getContainers() ;

	/**
	 * Checks whether the supplied value is null or not. In case of DomainType
	 * object, we will treat it as null when all val/val2/normalizedValue are null.
	 * 
	 * @param value
	 * @return
	 */
	protected boolean isNullValue(Serializable value) {
		if (value == null)
			return true;

		if (value instanceof DomainType) {
			DomainType domain = (DomainType) value;
			if (StringUtils.isEmpty(domain.getVal()) && StringUtils.isEmpty(domain.getVal2()) && StringUtils.isEmpty(domain.getNormalizedValue())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method checks in requested dataContainer whether dataAttribute is already 
	 * exists or not at FEED lockLevel.If it exists, method checks whether dataValue is
	 * same as new calculated value.
	 * If values are different it returns false means calculted values are different. 
	 * else it return true saying same value is already exists with FEED lock level.	 	
	 * @param container 
	 * @param dataValue
	 * @param dataAttribute
	 * @param dataSource
	 * @return
	 */
	protected boolean isValueAlreadyExists(DataContainer container, DataValue<Serializable> dataValue,
			DataAttribute dataAttribute, String dataSource) {

		Serializable feedValue = container.getAttributeValueAtLevel(LockLevel.FEED, dataAttribute);

		// If we do not have any existing value at FEED lock level value then add new
		// value in container
		if (Objects.isNull(feedValue)) {
			return false;
		}

		List<LockLevel> lockLevelList = Arrays.asList(LockLevel.class.getEnumConstants());
		for (LockLevel level : lockLevelList) {
          
			if (!level.equals(LockLevel.FEED) && level.isAutoRemovable()) {
        	   HistorizedData<Serializable> historizedData = dataValue.getLockValue(level);	
        	   
        	   if (!Objects.isNull(historizedData) && !Objects.isNull(historizedData.getValue()) ) {			
					Serializable lockValue = historizedData.getValue();
					return validateValue(dataAttribute, feedValue, lockValue,dataSource);
				}
			}
		}

		return false;
	}

	/**
	 * This method validates value & returns true if:
	 * 1.both values are equal 
	 * 2.if it is a domain attribute it checks normalized value & if both are equal it returns true.
	 * 
	 * @param dataAttribute
	 * @param feedValue
	 * @param lockValue
	 * @param dataSource
	 * @return
	 */
	protected boolean validateValue(DataAttribute dataAttribute, Serializable feedValue, Serializable lockValue, String dataSource) {

		if(feedValue.equals(lockValue)) {
			return true;
		}
		
		//If its domainAttribute check normalized values are matches are not.
		if (dataAttribute.getDataType().equals(DataType.DOMAIN)) {			
			NormalizedValueService normalizedValueService = SpringUtil.getBean(NormalizedValueService.class);
			Serializable normalizedValueForDomainValue = normalizedValueService
					.getNormalizedValueForDomainValue(dataAttribute, (DomainType) feedValue,dataSource);
			return ((DomainType) lockValue).getNormalizedValue().equals(normalizedValueForDomainValue);
		}

		return false;
	}
	
	/**
	 * Create dataValue from ruleResult. If ruleResult is instanceof UdmErrorCodes
	 * or null then it populates DataValue.errorCode. Otherwise populates
	 * DataValue.value
	 * 
	 * @param ruleResult
	 * @param ruleOutput
	 * @param dataAttribute
	 * @return
	 */
	protected DataValue<Serializable> createDataValueFromRuleResult(Serializable ruleResult, RduRuleOutput ruleOutput,
			DataAttribute dataAttribute) {
		DataValue<Serializable> dataValue = new DataValue<>();
		LockLevel levelForName = LockLevel.getLevelForName(ruleOutput.getLockLevel());
		
		//If ruleResult is UdmErrorCodes then populate errorCode in dataValue and return.
		if(ruleResult instanceof UdmErrorCodes) {
			dataValue.setErrorCode(levelForName, (UdmErrorCodes) ruleResult);
			return dataValue;
		}
		
		Serializable value = DataTypeConversionUtil.convertSerializableTo((Class<? extends Serializable>) dataAttribute.getDataType().getTypeClass(), ruleResult);
		
		if (!isNullValue(value)) {
			dataValue.setValue(levelForName, value);
		} else {
			dataValue.setErrorCode(levelForName, UdmErrorCodes.NULL_VALUE);
		}
		return dataValue;
	}

}

