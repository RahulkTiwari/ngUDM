/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionRouteContext.java
 * Author:	Rushikesh Dedhia
 * Date:	25-Feb-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.io.Serializable;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UdmExceptionRouteContext implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9107498830697877535L;
	
	private UdmExceptionDataHolder udmExceptionData;

	public UdmExceptionDataHolder getUdmExceptionData() {
		return udmExceptionData;
	}

	@Override
	public String toString() {
		return "UdmExceptionRouteContext [udmExceptionData=" + udmExceptionData + "]";
	}

	public void setUdmExceptionData(UdmExceptionDataHolder udmExceptionData) {
		this.udmExceptionData = udmExceptionData;
	}

}
