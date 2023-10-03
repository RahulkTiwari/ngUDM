/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DomainLookupService.java
 * Author:	Rushikesh Dedhia
 * Date:	07-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.domain;

import java.io.Serializable;

import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmTechnicalException;

public interface DomainLookupService {
	
	/**
	 *  This method searches in the database collection dvDomainMap for the normalized value that corresponds to the 
	 *  provided input information. It searches based on the domainName, rduDomainName, and the domain type data information. 
	 * @param domainData
	 * @param domainName
	 * @param rduDomainName
	 * @return
	 */
	Serializable getNormalizedValueForDomainValue(DomainType domainData, String domainSource, String domainName, String rduDomainName);

	
	/**
	 * Get the domain source from data source
	 * @param datasource
	 * @return
	 * @throws Exception 
	 */
	String getDomainSourceFromDataSource(String datasource) throws UdmTechnicalException;

	/**
	 * 
	 * @param domainData
	 * @param domainSource
	 * @param domainName
	 * @param rduDomainName
	 */
	Integer getNormalizedErrorCodeForDomainValue(DomainType domainData, String domainSource, String domainName, String rduDomainName);


	/**
	 * This method returns normalized value for input vendor values(domainData),it will search
	 * corresponding domain value based on pattern match & if value matches it
	 * returns its normalized value.
	 * 
	 * @param domainData
	 * @param domainSource
	 * @param rduDomainName
	 * @return
	 */
	public Serializable getPatternMatchNormalizedValueForDomainValue(DomainType domainData, String domainSource,
			String rduDomainName);
}
