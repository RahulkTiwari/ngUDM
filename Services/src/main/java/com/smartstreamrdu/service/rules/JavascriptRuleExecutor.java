/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	JavascriptRuleExecutor.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstreamrdu.rules.Rule;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.SneakyThrows;


@ThreadSafe
public abstract class JavascriptRuleExecutor implements RuleExecutor {
	
	private static final String KEY_COMPILED_SCRIPTS = "COMPILED_SCRIPTS";


	private static final Logger _logger = LoggerFactory.getLogger(JavascriptRuleExecutor.class);


	private static final ThreadLocal<ScriptEngine> engine = ThreadLocal
			.withInitial(() -> new NashornScriptEngineFactory().getScriptEngine());

	public JavascriptRuleExecutor() {
		
	}

	/**
	 * @param compiledScript
	 */
	protected Serializable executeCompiledRule(CompiledScript compiledScript) {
		try {
			return (Serializable) compiledScript.eval(getEngine().getContext());
		} catch (ScriptException e) {
			_logger.error("The following error occured", e);
			return null;
		}
	}
	
	protected ScriptEngine getEngine() {
		return engine.get();
	}

	@Override
	public void registerWithRuleExecutor(String key, Object value) {
		getEngine().put(key, value);
	}

	/**
	 *  This method will compile the rule with the JavaScript engine and add the compiled rule to the map
	 *  compiledScripts with the ruleScript string itself being the key.  
	 * @param rule
	 */
	protected void compileRule(Rule rule) {
		if (rule != null && rule.getRuleData() != null && rule.getRuleData().getRuleScript() != null) {
			ScriptEngine thisEngine = getEngine();
			Map<Rule, CompiledScript> compiledScripts = getScriptMap(thisEngine);
			if (thisEngine instanceof Compilable) {
				Compilable compilingEngine = (Compilable) thisEngine;
				try {
					compiledScripts.computeIfAbsent(rule, r -> compileRule(r, compilingEngine));
				} catch (Exception scriptError) {
					_logger.error("The following error occured", scriptError);
				}
			}
		}
	}

	protected Map<Rule, CompiledScript> getScriptMap(ScriptEngine thisEngine) {
		Map<Rule, CompiledScript> compiledScripts = getEngineFromMap(thisEngine);
		if (compiledScripts == null) {
			compiledScripts = new HashMap<>();
			thisEngine.put(KEY_COMPILED_SCRIPTS, compiledScripts);
		}
		return compiledScripts;
	}

	@SuppressWarnings("unchecked")
	private <T> T getEngineFromMap(ScriptEngine thisEngine) {
		return (T) thisEngine.get(KEY_COMPILED_SCRIPTS);
	}

	@SneakyThrows(ScriptException.class)
	private CompiledScript compileRule(Rule rule, Compilable compilingEngine) {
		return compilingEngine.compile(rule.getRuleData().getRuleScript());
	}
	

}
