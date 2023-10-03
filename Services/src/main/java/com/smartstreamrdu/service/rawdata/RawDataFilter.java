/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataFilter.java
 * Author:  Padgaonkar
 * Date:    May 28, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataContainer;

/**
 * This interface is used to filter rawData record based on Conditions.
 * 
 * @author Padgaonkar
 *
 */
public interface RawDataFilter extends Serializable {

	/**
	 * This method return true if record needs to be filtered(does not send for
	 * further processing) else return false.
	 * 
	 * @param feedRawdataContainer
	 * @param dbDataContainer
	 * @param filterContext
	 * @return
	 */
	public boolean filter(DataContainer feedRawdataContainer, DataContainer dbDataContainer,
			RawDataFilterContext filterContext);
}
