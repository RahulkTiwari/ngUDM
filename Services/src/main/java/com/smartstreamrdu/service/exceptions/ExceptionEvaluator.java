/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ExceptionEvaluator.java
 * Author:	Padgaonkar
 * Date:	23-March-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.util.UdmSystemPropertiesConstant;

import lombok.Data;


@Component
@Data
public class ExceptionEvaluator implements Function<Object, Object> {

	private boolean raiseException;
	
	@Autowired
	UdmSystemPropertiesCache systemCache;
	
	@PostConstruct
	public void initialize() {
		Optional<String> raiseExceptionOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.ENABLE_RAISING_EXCEPTION,UdmSystemPropertiesConstant.COMPONENT_XRF,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(raiseExceptionOptional.isPresent()) {
			raiseException = Boolean.parseBoolean(raiseExceptionOptional.get());
	   }
	}


	@Override
	public Object apply(Object t) {
		return raiseException;
	}
	
	@PostConstruct
	public void init() {
		this.raiseException = true;
	}
}
