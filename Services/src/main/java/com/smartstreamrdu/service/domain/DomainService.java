/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DomainService.java
 * Author:	Jay Sangoi
 * Date:	04-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import java.util.List;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * This class contains all the service required for Domain
 * @author Jay Sangoi
 *
 */
public interface DomainService {
	
	/**
	 * Get the Domain value for Active status for the vendor
	 * @param datasource
	 * @return
	 * @throws Exception 
	 */
	List<DomainType> getActiveStatusValueForVendor(DomainType datasource, DataAttribute statusAtt ) throws UdmTechnicalException;

	List<DomainType> getInActiveStatusValueForVendor(DomainType datasource, DataAttribute statusAtt ) throws UdmTechnicalException;

	
}
