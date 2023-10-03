/*******************************************************************
*
* Copyright (c) 2009-2021 The SmartStream Reference Data Utility
* All rights reserved. 
*
* File:    RuleUtil.java
* Author:  Padgaonkar
* Date:    March 03,2021
*
*******************************************************************
*/
package com.smartstreamrdu.service.rules;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.rules.RduRule;

/**
 * 
 * @author Padgaonkar
 *
 */
public class RuleUtil {

	private RuleUtil() {
	}

	/**
	 * This method return ruleDataAttribute from rule output. If output level is not
	 * present then this assumes that rule is corresponding to sdData. Else based on
	 * level this returns rule attribute.
	 * 
	 * @param rule
	 * @param ruleAttributeName
	 * @return
	 */
	public static DataAttribute getRuleDataAttibute(RduRule rule, String ruleAttributeName) {
		String level = rule.getRuleOutput().getDataLevel();

		// Default implementation to handle cases where the dataLevel is not provided in the RduRule
		// This is implemented to keep a backward compatibility with the old rules until all rules are
		// back-filled with the dataLevel field.
		if (Objects.isNull(level) ) {
			return DataStorageEnum.SD.getAttributeByName(ruleAttributeName);
		}
		
		// For cases where the dataLevel field is populated in the RduRule, we will fetch the attribute
		// from the DataStorageEnum.
		DataLevel dataLevel = DataLevel.valueOf(level);
	
		if (!StringUtils.isEmpty(rule.getRuleOutput().getParentAttributeName()) && !rule.getRuleOutput().getParentAttributeName().equals(ruleAttributeName)) {
			return DataStorageEnum.getStorageByLevel(dataLevel).getAttributeByNameAndParent(ruleAttributeName, rule.getRuleOutput().getParentAttributeName());
		} else {
			return DataStorageEnum.getStorageByLevel(dataLevel).getAttributeByName(ruleAttributeName);
		}
		
	}
}
