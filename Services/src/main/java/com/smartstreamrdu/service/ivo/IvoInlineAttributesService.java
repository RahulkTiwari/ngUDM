/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoInlineAttributesService.java
 * Author : SaJadhav
 * Date : 22-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.xrf.XrfRuleAttributeDef;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.xrf.config.CrossRefRuleConfigService;

/**
 * Gives the sd attributes which should be stored inline
 * @author SaJadhav
 *
 */
@Component
public class IvoInlineAttributesService {
	
	@Autowired
	private CrossRefRuleConfigService crossRefRuleConfigService;
	
	private List<DataAttribute> inlineAttributes=new ArrayList<>();
	
	/**
	 * Checks whether the locks on attribute should be stored inline 
	 * @param attributeName
	 * @return
	 */
	public boolean isInlineAttribute(DataAttribute attributeName){
		if(CollectionUtils.isEmpty(inlineAttributes)){
			initInlineAttributes();
		}
		return inlineAttributes.contains(attributeName);
	}

	/**
	 * 
	 */
	private void initInlineAttributes() {
		List<XrfRuleAttributeDef> xrfAttributes = crossRefRuleConfigService.getMandatoryAndOptionalCrossRefAttributes();
		List<DataAttribute> listXrfMatchingAttributes = xrfAttributes.stream()
				.map(xrfAttribute -> DataAttributeFactory.getAttributeByNameAndLevel(xrfAttribute.getSdAttributeName(),
						DataLevel.valueOf(xrfAttribute.getSdAttributeLevel())))
				.collect(Collectors.toList());
		inlineAttributes.addAll(listXrfMatchingAttributes);
		
		inlineAttributes.add(InstrumentAttrConstant.INSTRUMENT_STATUS);
		inlineAttributes.add(SecurityAttrConstant.SECURITY_STATUS);
	}

}
