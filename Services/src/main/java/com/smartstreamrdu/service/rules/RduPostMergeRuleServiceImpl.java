/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduPostMergeRuleServiceImpl.java
 * Author:	RKaithwas
 * Date:	Feb 02, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.rules.RduRuleOutput;
import com.smartstreamrdu.rules.Rule;

import lombok.extern.slf4j.Slf4j;
/**
 * Apply rules for post merge activity, apply rules where ruleType=LoaderRules.
 * 
 * @author RKaithwas
 *
 */
@Slf4j
public class RduPostMergeRuleServiceImpl implements RuleService<RduRuleServiceInput> {

	private RduRuleEngine ruleEngine = new RduRuleEngine();


	/**
	 * This method is responsible for applying loader rules on given
	 * ruleServiceInput.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T applyRules(RduRuleServiceInput rduRuleServiceInput, List<? extends Rule> rules, String fileName) {
		if (rduRuleServiceInput != null) {
			// create ruleContext
			RduRuleContext context = rduRuleServiceInput.getRduRuleContext();
			ruleEngine.initializeRuleEngine(rules, context);

			if (rules != null && !rules.isEmpty()) {
				// initialy we have added support to execute parent rules only.this
				// functionality needs to be extend when required.
				applyParentRules(rduRuleServiceInput, (List<RduRule>) rules);
			}

		} else {
			log.debug("Unable to execute rule because either the input data or the list of rules was empty. rules : {}", rules);
			return null;
		}
		return (T) rduRuleServiceInput.getDataContainer();
	}
	
	/**
	 * apply rules for Instrument Level attributes.
	 * @param input
	 * @param rules
	 */

	private void applyParentRules(RduRuleServiceInput input, List<RduRule> rules) {
		DataLevel level = input.getDataContainer().getLevel();

		// get loader rules for container level.
		List<RduRule> filterRules = getRulesForLevel(level, input, rules);

		//If there is no applicable rule -return. 
		if (filterRules.isEmpty()) {
			return;
		}

		filterRules.forEach(rule -> {
			if (rule != null) {
				ruleEngine.registerWithRuleEngine(RuleConstants.SD_DATA_CONTAINER, input.getDataContainer());
				
				//based on container level this method return strategy
				LoaderRuleExecutionStrategy dataContainerStrategy = getRuleExecutionStrategy(level);

				if (!Objects.isNull(dataContainerStrategy)) {
					log.info("Executing dataContainerStrategy :{}",dataContainerStrategy);
					dataContainerStrategy.executeStrategy(input, ruleEngine, rule);
				}
				ruleEngine.registerWithRuleEngine(RuleConstants.SD_DATA_CONTAINER, null);
			}
		});

	}

	private LoaderRuleExecutionStrategy getRuleExecutionStrategy(DataLevel level) {
		
		//currently strategy is added for only parent level.
		if (level == DataLevel.INS) {
			return new PostMergeParentRuleExecutionStrategy(null, null, null, null);
		}
		return null;
	}

	/**
	 * This method returns applicable rule for input dataContainer.
	 * @param level
	 * @param input
	 * @param rules
	 * @return
	 */
	private List<RduRule> getRulesForLevel(DataLevel level, RduRuleServiceInput input, List<RduRule> rules) {
		DataContainer dataContainer = input.getDataContainer();
		DomainType dataSourceDomain = dataContainer
				.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute(dataContainer.getLevel()));
		String dataSource = dataSourceDomain.getVal();
		return filterApplicableRules(rules, level, dataSource);
	}

	/**
	 * This method returns dataAttribute based on rule output attribute.
	 * 
	 * @param rule
	 * @return
	 */
	private DataAttribute getDataAttribute(RduRule rule) {
		RduRuleOutput ruleOutput = rule.getRuleOutput();
		String ruleAttributeName = StringUtils.isNotEmpty(ruleOutput.getParentAttributeName())
				? ruleOutput.getParentAttributeName()
				: ruleOutput.getAttributeName();
	
		if (ruleAttributeName != null) {
			return RuleUtil.getRuleDataAttibute(rule,ruleAttributeName);
		}
		return null;

	}
	
	/**
	 * This method filter rules based on input level & dataSource. If rule is of
	 * default Type (feedName=Default) then it will be applicable for all dataSources.
	 * @param rules
	 * @param level
	 * @param dataSource
	 * @return
	 */
	private List<RduRule> filterApplicableRules(List<RduRule> rules, DataLevel level, String dataSource) {
		List<RduRule> applicableRules = new ArrayList<>();
		rules.forEach(rule -> {
			DataAttribute dataAttribute = getDataAttribute(rule);

			if ((dataAttribute.getAttributeLevel() == level)
					&& (rule.getRuleFilter().getFeedName().equals(dataSource) || isDefaultRule(rule))) {
				applicableRules.add(rule);
			}
		});
		return applicableRules;

	}

	/**
	 * if feedName is default then these rules are applicable for all dataSources.
	 * @param rule
	 * @return
	 */
	private boolean isDefaultRule(RduRule rule) {
		return RuleConstants.FEEDNAME_DEFAULT.equals(rule.getRuleFilter().getFeedName());

	}

	

}
