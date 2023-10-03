/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduRuleExecutor.java
 * Author:	Divya Bharadwaj
 * Date:	27-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstreamrdu.rules.Rule;
import com.smartstreamrdu.service.rules.jsonmerge.JsonArrayMergeFunctions;

/**
 * @author Bharadwaj
 *
 */
public class RduRuleExecutor extends JavascriptRuleExecutor {

	/**
	 * 
	 */
	private static final Logger _logger = LoggerFactory.getLogger(RduRuleExecutor.class);
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.rules.JavascriptRuleExecutor#executeRule(com.smartstreamrdu.service.rules.Rule, java.lang.Object)
	 */
	@Override
	public <T> Serializable executeRule(Rule rduRule, T rduRuleServiceInput) {
		RduRuleServiceInput input = (RduRuleServiceInput)rduRuleServiceInput;
		Serializable value = null;
		ScriptEngine engine = getEngine();
		engine.put(RuleConstants.RECORD, input.getRecord());
		engine.put(RuleConstants.RULE, rduRule);
		if (rduRule != null && rduRule.getRuleData() != null && rduRule.getRuleData().getRuleScript() != null) {
			Map<Rule, CompiledScript> compiledScripts = getScriptMap(engine);
			CompiledScript compiledScript = compiledScripts.get(rduRule);
			if (compiledScript == null) {
				_logger.debug("No compiled rule found for {}. Will compile the rule and add to the rule enginer", rduRule);
				compileRule(rduRule);
				compiledScript = compiledScripts.get(rduRule);
			}
			if (compiledScript != null) {
				try {
					value = executeCompiledRule(compiledScript);
				} catch (Exception exception) {
					_logger.error("Following error occured while executing the rule : {}",rduRule, exception);
				}
			}
		}
		return value;
	}
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.rules.JavascriptRuleExecutor#initializeRuleExecutor(java.util.List, com.smartstreamrdu.service.rules.RduRuleContext)
	 */
	@Override
	public void initializeRuleExecutor(List<? extends Rule> rules, RduRuleContext context) {
		String dataSource=(String) context.getRuleContext().get("dataSource");

		try {
			ScriptEngine engine = getEngine();
			engine.put("dataSource", dataSource);
			if(Objects.isNull(engine.get(RuleConstants.IS_INITIALIZED))) {
				initializeEngine(engine);
			}
		} catch (ScriptException scriptError) {
			_logger.error("The following error occured", scriptError);
		}

		// Compile all RduRules.
		for (Rule rule : rules) {
			compileRule(rule);
		}
		
	}
	
	
	
	
	/**
	 * 
	 * This function is responsible to initialize the Nashorn engine.
	 * 
	 * @param dataSource
	 * @param engine
	 * @throws ScriptException
	 */
	private void initializeEngine(ScriptEngine engine) throws ScriptException {
		engine.put(RuleConstants.IS_INITIALIZED,"isInitialized");
		engine.put(RuleConstants.FUNCTIONS, new RuleCustomFunctions());
		engine.put(RuleConstants.JSON_FUNCTION, JsonArrayMergeFunctions.builder().build());
		engine.eval(JavascriptRuleScriptConstants.VALUES_CLASS);
		engine.eval(JavascriptRuleScriptConstants.FEEDVALUE_CUSTOM_FUCTION_SCRIPT);
		engine.eval(JavascriptRuleScriptConstants.DOMAIN_LOOKUP_CUSTOM_FUNCTION_SCRIPT);
		engine.eval(JavascriptRuleScriptConstants.NORMALIZED_LOOKUP_FUNCTION_SCRIPT);
		engine.eval(JavascriptRuleScriptConstants.NORMALIZED_DOMAIN_LOOKUP_FUNCTION_SCRIPT);
		engine.eval(JavascriptRuleScriptConstants.GET_FROM_CODE_MAP);
		engine.eval(JavascriptRuleScriptConstants.RAW_FEED_VALUE);
		engine.eval(JavascriptRuleScriptConstants.FEEDVALUE_WITH_DEFAULT_DATE_FALLBACK);
		engine.eval(JavascriptRuleScriptConstants.NORMALIZED_DOMAIN_VALUE_FUNCTION_SCRIPT);
		engine.eval(JavascriptRuleScriptConstants.CURRENT_INDEX_FUNCTION_SCRIPT);
		engine.eval(JavascriptRuleScriptConstants.FIRST_VALUE);
		engine.eval(JavascriptRuleScriptConstants.INACTIVATION_BASED_ON_SECURITY_FUNCTION);
		engine.eval(JavascriptRuleScriptConstants.INACTIVATION_BASED_ON_ATTRIBUTE_FUNCTION);
		engine.eval(JavascriptRuleScriptConstants.GET_EARLIEST);
		engine.eval(JavascriptRuleScriptConstants.UDM_ERRORCODE);
		engine.eval(JavascriptRuleScriptConstants.CONVERT_JSON_ARRAY_TO_LIST_OF_STRINGS);
		engine.eval(JavascriptRuleScriptConstants.INITIATE_ARRAY_MERGE_FUNCTION);
	}
	

}
