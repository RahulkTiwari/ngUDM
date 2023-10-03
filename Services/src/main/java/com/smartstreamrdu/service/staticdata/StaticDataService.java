/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StaticDataService.java
 * Author:	Jay Sangoi
 * Date:	08-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.staticdata;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Jay Sangoi
 *
 * Interface for Service related to Static Data,
 * It has methods like getting static data using code
 *
 */
public interface StaticDataService {

	/**
	 * @param level
	 * @param requiredAttributeName
	 * @return
	 * @throws UdmBaseException 
	 */
	Serializable getDataByCode(DataLevel level, String requiredAttributeName, Serializable value) throws UdmBaseException;
	
	/**
	 * @param level
	 * @param requiredAttributeName
	 * @return
	 * @throws UdmBaseException 
	 */
	Serializable getDataByCode(DataLevel level, DataAttribute requiredAttributeName, Serializable value) throws UdmBaseException;

	
	/** 
	 * @param dataSourceName
	 * @return
	 * Returns xrfSourcePriority for the input dataSourceName
	 */
	Optional<Integer> getXrfSourcePriority(String dataSourceName) throws UdmTechnicalException;
	
	/**
	 * @param exceptionType
	 * @return
	 * @throws UdmTechnicalException
	 * Returns all the string 
	 */
	List<String> getFieldsForExceptionType(String exceptionType) throws UdmTechnicalException;
	
	Boolean getIsExceptionAutoClosable(String exceptionTypes) throws UdmTechnicalException;

	boolean getIsExceptionValidForReprocessing(String exceptionType) throws UdmTechnicalException;

}
