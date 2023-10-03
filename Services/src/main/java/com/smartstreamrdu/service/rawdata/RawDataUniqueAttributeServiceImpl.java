/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataUniqueAttributeService.java
 * Author:	Padgaonkar S
 * Date:	29-Aug-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.service.rules.JavascriptRuleExecutor;
import com.smartstreamrdu.service.rules.RduRuleContext;
import com.smartstreamrdu.service.rules.RduRuleExecutor;
import com.smartstreamrdu.service.rules.RduRuleServiceInput;

import net.minidev.json.JSONArray;

@ThreadSafe
@Component
public class RawDataUniqueAttributeServiceImpl implements RawDataUniqueAttributeService {

	private final JavascriptRuleExecutor executor = new RduRuleExecutor();

	@Override
	public Serializable getRawDataUniqueAttributeValue(String dataSource, Record record, RduRule rule) {

		RduRuleServiceInput input = new RduRuleServiceInput();
		RduRuleContext ruleContext = new RduRuleContext();
		ruleContext.addToRuleContext("dataSource", dataSource);
		input.setRduRuleContext(ruleContext);
		input.setRecord(record);
		List<RduRule> ruleList = new ArrayList<>();
        ruleList.add(rule);
		executor.initializeRuleExecutor(ruleList, ruleContext);
		Serializable value = executor.executeRule(rule, input);

		if (value instanceof JSONArray) {
			JSONArray val = (JSONArray) value;
			return (Serializable) val.get(0);
		}
		
		return value;
	}
}
