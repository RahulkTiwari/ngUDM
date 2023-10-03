/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    LoaderRuleExecutor.java
 * Author:  Padgaonkar
 * Date:    Feb 02, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * This interface is responsible for executing loader rules.
 * @author Padgaonkar
 *
 */
public interface LoaderRuleExecutor {
	
	/**
	 * This method executes loader rules on input dataContainers.
	 * @param containers
	 */
	public void executeLoaderRules(List<DataContainer> containers);
}
