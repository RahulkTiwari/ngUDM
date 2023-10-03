/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerEnrichmentRuleService.java
 * Author :SaJadhav
 * Date : 31-Mar-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.rules.DataContainerEnrichmentRule;
import com.smartstreamrdu.rules.DataContainerEnrichmentRuleOutput;
import com.smartstreamrdu.rules.Rule;
import com.smartstreamrdu.service.rules.RduRuleContext;
import com.smartstreamrdu.service.rules.RuleEngine;
import com.smartstreamrdu.service.rules.RuleService;
import com.smartstreamrdu.util.DataTypeConversionUtil;

import lombok.NonNull;

/**
 * Rule service to apply enrichment rules on DataContainer
 * @author SaJadhav
 *
 */
@Component
public class DataContainerEnrichmentRuleService implements RuleService<EnrichmentRuleServiceInput> {
	
	private RuleEngine enrichmentRuleEngine=new DataContainerEnrichmentRuleEngine();

	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T applyRules(@NonNull EnrichmentRuleServiceInput input,@NonNull List<? extends Rule> rules,
			String fileName) {
		
		final DataContainer dataContainer = input.getDataContainer();
		
		enrichmentRuleEngine.initializeRuleEngine(rules, new RduRuleContext());
		
		rules.stream().forEach(rule -> {
			DataContainerEnrichmentRule enrichmentRule = (DataContainerEnrichmentRule) rule;
			DataContainerEnrichmentRuleOutput ruleOutput = enrichmentRule.getRuleOutput();
			DataAttribute dataAttribute = ruleOutput.getDataAttribute();
			if (dataAttribute.getParent() != null
					&& DataType.NESTED_ARRAY.equals(dataAttribute.getParent().getDataType())) {
				applyRulesForNestedArrayAttributes(rule, ruleOutput, input, dataContainer);
			} else {
				Serializable ruleResult = enrichmentRuleEngine.executeRule(input, rule);
				addRuleResultToDataContainer(ruleResult, dataContainer, ruleOutput);
			}

		});
		
		return (T) dataContainer;
	}

	/**
	 * @param rule
	 * @param ruleOutput
	 * @param input
	 * @param dataContainer
	 */
	private void applyRulesForNestedArrayAttributes(Rule rule, DataContainerEnrichmentRuleOutput ruleOutput,
			EnrichmentRuleServiceInput input, DataContainer dataContainer) {
		DataRow dataRow = (DataRow) dataContainer.getAttributeValue(ruleOutput.getDataAttribute().getParent());
		
		if(dataRow!=null && dataRow.getValue()!=null && !CollectionUtils.isEmpty(dataRow.getValue().getValue())) {
			ArrayList<DataRow> listDataRow = dataRow.getValue().getValue();
			listDataRow.forEach(row -> {
				Serializable ruleResult = enrichmentRuleEngine.executeRule(input, rule);
				addRuleResultToDataRow(row, ruleResult, ruleOutput);

			});
		}
		
		
	}

	/**
	 * @param row
	 * @param ruleResult
	 * @param ruleOutput
	 */
	private void addRuleResultToDataRow(DataRow row, Serializable ruleResult, DataContainerEnrichmentRuleOutput ruleOutput) {
		if(ruleResult!=null) {
			DataValue<Serializable> dataValue = createDataValue(ruleResult, ruleOutput);
			row.addAttribute(ruleOutput.getDataAttribute(), dataValue);
		}
	}

	/**
	 * @param ruleResult
	 * @param ruleOutput
	 * @return
	 */
	private DataValue<Serializable> createDataValue(Serializable ruleResult, DataContainerEnrichmentRuleOutput ruleOutput) {
		DataValue<Serializable> dataValue = new DataValue<>();
		Serializable convertedValue = DataTypeConversionUtil.convertSerializableTo(
				(Class<? extends Serializable>) ruleOutput.getDataAttribute().getDataType().getTypeClass(), ruleResult);
		dataValue.setValue(ruleOutput.getLockLevel(), convertedValue);
		return dataValue;
	}

	/**
	 * @param ruleResult
	 * @param dataContainer
	 * @param ruleOutput 
	 */
	private void addRuleResultToDataContainer(Serializable ruleResult, DataContainer dataContainer, DataContainerEnrichmentRuleOutput ruleOutput) {
		if (ruleResult != null) {
			DataValue<Serializable> dataValue = createDataValue(ruleResult,ruleOutput);
			dataContainer.addAttributeValue(ruleOutput.getDataAttribute(), dataValue);
		}
		
	}
}
