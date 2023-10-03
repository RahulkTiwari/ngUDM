/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataFullLoadFilter.java
 * Author:  Padgaonkar
 * Date:    May 28, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author Padgaonkar This filter is applicable when we are not filtering any
 *         raw Data records. All records are processed till sdData.
 */
public class RawDataFullLoadFilter implements RawDataFilter {

	private static final long serialVersionUID = 1L;
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean filter(DataContainer feedRawdataContainer, DataContainer dbDataContainer,
			RawDataFilterContext filterContext) {
		return false;
	}

}
