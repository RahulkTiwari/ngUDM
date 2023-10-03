/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerEnrichmentRuleExecutor.java
 * Author :SaJadhav
 * Date : 31-Mar-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.smartstreamrdu.rules.DataContainerEnrichmentRule;
import com.smartstreamrdu.rules.DataContainerEnrichmentRuleOutput;
import com.smartstreamrdu.rules.Rule;
import com.smartstreamrdu.service.rules.JavascriptRuleExecutor;
import com.smartstreamrdu.service.rules.JavascriptRuleScriptConstants;
import com.smartstreamrdu.service.rules.RduRuleContext;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to execute DataContainer enrichment rules
 * 
 * @author SaJadhav
 *
 */
@Slf4j
public class DataContainerEnrichmentRuleExecutor extends JavascriptRuleExecutor {

	@Override
	public <T> Serializable executeRule(@NonNull Rule rduRule,@NonNull T ruleInput) {
		DataContainerEnrichmentRule rule=(DataContainerEnrichmentRule) rduRule;
		DataContainerEnrichmentRuleOutput enrichmentRuleOutput = rule.getRuleOutput();
		
		Objects.requireNonNull(enrichmentRuleOutput,"ruleOutput should be populated");
		Objects.requireNonNull(enrichmentRuleOutput.getDataAttribute(),"ruleOutput DataAttribute should be populated");
		
		EnrichmentRuleServiceInput input = (EnrichmentRuleServiceInput) ruleInput;
		ScriptEngine engine = getEngine();
		engine.put(DataContainerEnrichmentRuleConstant.DATACONTAINER, input.getDataContainer());
		engine.put(DataContainerEnrichmentRuleConstant.DATAATTRIBUTE, enrichmentRuleOutput.getDataAttribute());
		
		return executeRuleInternal(rduRule, input, engine);
	}

	/**
	 * @param rduRule
	 * @param input
	 * @param engine
	 * @return
	 */
	private Serializable executeRuleInternal(Rule rduRule, EnrichmentRuleServiceInput input, ScriptEngine engine) {
		Serializable value = null;
		if (rduRule.getRuleData() != null && rduRule.getRuleData().getRuleScript() != null) {
			Map<Rule, CompiledScript> compiledScripts = getScriptMap(engine);
			CompiledScript compiledScript = compiledScripts.get(rduRule);

			if (compiledScript == null) {
				log.debug("No compiled rule found for {}. Will compile the rule and add to the rule engine", rduRule);
				compileRule(rduRule);
				compiledScript = compiledScripts.get(rduRule);
			}
			if (compiledScript != null) {
				try {
					value = executeCompiledRule(compiledScript);
				} catch (Exception exception) {
					log.error("Following error occured while executing the rule : {}: {} for dataContainer:{}",
							rduRule.getRuleData().getRuleScript(), exception, input.getDataContainer());
				}
			}
		}
		return value;
	}

	@Override
	public void initializeRuleExecutor(List<? extends Rule> rules, RduRuleContext context) {
		try {
			ScriptEngine engine = getEngine();

			if (Objects.isNull(engine.get(DataContainerEnrichmentRuleConstant.IS_INITIALIZED))) {
				initializeEngine(engine);
			}

		} catch (ScriptException scriptError) {
			log.error("The following error occured", scriptError);
		}
		// Compile all RduRules.
		for (Rule rule : rules) {
			compileRule(rule);
		}

	}

	/**
	 * @param engine
	 */
	private void initializeEngine(ScriptEngine engine) throws ScriptException {
		engine.put(DataContainerEnrichmentRuleConstant.IS_INITIALIZED, "isInitialized");
		engine.put(DataContainerEnrichmentRuleConstant.DATA_CONTAINER_ENRICHMENT_FUNCTIONS, new DataContainerEnrichmentRuleCustomFunctions());
		engine.eval(JavascriptRuleScriptConstants.DOMAIN_VALUE);
		engine.eval(JavascriptRuleScriptConstants.GET_NORMALIZED_DOMAIN_VALUE_ENRICHMENT_FUNCTION);
		engine.eval(JavascriptRuleScriptConstants.GET_ATTRIBUTE_VALUE_FROM_DATA_CONTAINER);
		engine.eval(JavascriptRuleScriptConstants.GET_NORMALIZED_ATTRIBUTE_VALUE_FROM_DATA_CONTAINER);
		engine.eval(JavascriptRuleScriptConstants.FORMAT_DATE);
		engine.eval(JavascriptRuleScriptConstants.GET_NESTED_ARRAY_COUNTER);
		engine.eval(JavascriptRuleScriptConstants.ENRICH_EVENT_FILE_NAMES);
		engine.eval(JavascriptRuleScriptConstants.GET_CURRENT_DATE_UTC);
	}

}
