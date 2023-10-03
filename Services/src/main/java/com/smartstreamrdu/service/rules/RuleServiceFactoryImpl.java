/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RuleServiceFactoryImpl.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Apr-2018
 *
 *******************************************************************/

package com.smartstreamrdu.service.rules;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RuleServiceFactoryImpl implements RuleServiceFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1216455951469047960L;
	
	@Value("${application.name}")
	private String appname;

	@Override
	public RuleService getRuleService(RuleComponentEnum component) {

		if (component == RuleComponentEnum.LOADER) {
			return new RduRuleServiceImpl(appname);
		}else {
			throw new IllegalArgumentException("No rule service available for the component :"+component);
		}
		
		
	}

}
