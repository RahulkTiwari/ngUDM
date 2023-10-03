/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleExecutionStrategyFactoryImpl.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules;

import java.time.LocalDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.RecordWrapper;


public class RuleExecutionStrategyFactoryImpl implements RuleExecutionStrategyFactory {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -889369467358297435L;
	private static final transient Logger _logger = LoggerFactory.getLogger(RuleExecutionStrategyFactoryImpl.class);

	private String appname;

	/**
	 * Date of single record. We always create new instance for each record, hence we are creating the date instance here. 
	 */
	private LocalDateTime date = LocalDateTime.now();
	
	
	/**
	 * @param appname
	 */
	public RuleExecutionStrategyFactoryImpl(String appname) {
		super();
		this.appname = appname;
	}


	@Override
	public RuleExecutionStrategy getRuleExecutionStrategy(RecordWrapper recordWrapper, String fileName, String dataSource) {

		Objects.requireNonNull(recordWrapper,
				"Unable to return a rule execution strategy because the supplied record wrapper is null");

		DataLevel dataLevel = recordWrapper.getLevel();

		if (dataLevel == null) {
			return null;
		}

		if (dataLevel.getParentLevel() == null) {
			_logger.debug(
					"Data level is a parent level thus returning ParentRecordRuleExecutionStrategy for the RecordWrapper : {}",
					recordWrapper);
			return new ParentRecordRuleExecutionStrategy(fileName, appname, date,dataSource);
		} else {
			_logger.debug(
					"Data level is a child level thus returning ChildRecordRuleExecutionStrategy for the RecordWrapper : {}",
					recordWrapper);
			return new ChildRecordRuleExecutionStrategy(fileName, appname, date,dataSource);
		}


	}

}
