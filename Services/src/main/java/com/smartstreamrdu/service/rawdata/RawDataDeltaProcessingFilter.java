/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataDeltaProcessingFilter.java
 * Author:  Padgaonkar
 * Date:    May 28, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.RawDataConstants;

/**
 * @author Padgaonkar This filter is applicable when we need to filter record
 *         based on codeHash. This filter checks if codeHash of records is same
 *         it return true(i.e. filter record) else returns false.
 */
public class RawDataDeltaProcessingFilter implements RawDataFilter {

	
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean filter(DataContainer feedRawdataContainer, DataContainer dbDataContainer,
			RawDataFilterContext filterContext) {
		return codeHashMatched(feedRawdataContainer, dbDataContainer);
	}

	/**
	 * compare codeHash of feed vs db container
	 * @return true only if codeHash is matched otherwise return false
	 */
	protected boolean codeHashMatched(DataContainer feedRawdataContainer, DataContainer dbDataContainer) {
		boolean returnValue = false;
		if (dbDataContainer == null || feedRawdataContainer == null) {
			return returnValue;
		}

		DataValue<?> feedCodeHash = (DataValue<?>) feedRawdataContainer.getAttributeValue(DataAttributeFactory
				.getAttributeByNameAndLevel(RawDataConstants.CODE_HASH_ATTRIBUTE, feedRawdataContainer.getLevel()));
		DataValue<?> dbCodeHash = (DataValue<?>) dbDataContainer.getAttributeValue(DataAttributeFactory
				.getAttributeByNameAndLevel(RawDataConstants.CODE_HASH_ATTRIBUTE,  feedRawdataContainer.getLevel()));
		return feedCodeHash.getValue().equals(dbCodeHash.getValue());

	}
}
