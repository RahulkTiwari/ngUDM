/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataRetrievalService.java
 * Author:  Padgaonkar
 * Date:    Jul 19, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;


import org.json.simple.JSONObject;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;

/**
 * This service retrieves rawData from requested rawData collection.
 * @author Padgaonkar
 *
 */
public interface RawDataRetrievalService {


	/**
	 * This method returns raw data json for requested rawDataId.
	 * @param rawDataId
	 * @param dataSource
	 * @return
	 * @throws UdmTechnicalException
	 */
	public JSONObject retrieveRawDataJSON(String rawDataId, String dataSource) throws UdmTechnicalException;

	/**
	 * This method returns rawDataContainer for requested input.
	 * @param input
	 * @return
	 * @throws UdmTechnicalException
	 */
	public DataContainer retrieveRawDataContainer(DataRetrivalInput input) throws UdmTechnicalException;
	
}