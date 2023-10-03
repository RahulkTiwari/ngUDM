/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	OpenFigiIdentifierRulesServiceImpl.java
 * Author:	Shruti Arora
 * Date:	05-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.util.VfsSystemPropertiesConstant;

@Component
public class OpenFigiIdentifierRulesServiceImpl implements OpenFigiIdentifierRulesService {

	private static final String COMMA = ",";
	private static final String RULE_ATTRIBUTES = ".rule.attributes";
	private static final String FIGI = "figi.";

	/**
	 * 
	 */
	private static final long serialVersionUID = 2411222715361255925L;
	
	@Autowired
	private transient UdmSystemPropertiesCache systemCache;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getRuleAttributesForRuleEnum(OpenFigiRequestRuleEnum ruleEnum) {
		
		Optional<String> attributes = systemCache.getPropertiesValue(FIGI+ruleEnum.name()+RULE_ATTRIBUTES,VfsSystemPropertiesConstant.COMPONENT_VFS,DataLevel.VFS_SYSTEM_PROPERTIES);
		if (attributes.isPresent()) {
			return Arrays.asList(attributes.get().split(COMMA));
		}
		return Collections.emptyList();
	}
	

}
