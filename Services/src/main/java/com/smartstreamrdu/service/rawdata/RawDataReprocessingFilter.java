/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataReprocessingFilter.java
 * Author:  Padgaonkar
 * Date:    May 28, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.time.LocalDateTime;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.persistence.domain.autoconstants.SdRawDataAttrConstant;

/**
 * This filter is child filter of {@link RawDataDeltaProcessingFilter} It is
 * applicable in case when we are reprocessing pending files from udlMetrics
 * collection. In this we check codeHash of records if they doesn't matches we
 * return false. If codeHash of records are matches we additionally check that,
 * if records inserted/updated after fileProcessingStartDate.
 * 
 * @author Padgaonkar
 *
 */
public class RawDataReprocessingFilter extends RawDataDeltaProcessingFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean filter(DataContainer feedRawdataContainer, DataContainer dbDataContainer,
			RawDataFilterContext filterContext) {

		// if codeHash of records doesn't matches we send as false means do not filter
		// this record.
		if (!codeHashMatched(feedRawdataContainer, dbDataContainer)) {
			return false;
		}

		//if codeHash of record matches we additionally check for fileProcessingStartDate
		return filterbasedOnModifiedDate(dbDataContainer, filterContext);
	}

	/**
	 * This check is applicable for pending Message from UdlMetrics collection when
	 * starts Re-Processeing.In this case it may happen that record entry is get
	 * added in sdRawData but not in sdData due to abrupt shutdown of engine.
	 * 
	 * We need check entries in sdRawData which having upDate or insertDate greater
	 * that startDate of file for last Execution(i.e. insDate/upDate > startDate for
	 * file from UdlMetrics Collection)
	 * 
	 * If this condition is true we need to send those records for further
	 * processing.
	 * 
	 * @param dbDataContainer
	 * @param pendingMessageMetrics
	 * @return
	 */
	private boolean filterbasedOnModifiedDate(DataContainer dbDataContainer, RawDataFilterContext filterContext) {

		DataAttribute rawDataInsDate = SdRawDataAttrConstant.INS_DATE;
		DataAttribute rawDataUpdateDate = SdRawDataAttrConstant.UPD_DATE;
		LocalDateTime insDate = getDateAttributeValue(rawDataInsDate, dbDataContainer);
		LocalDateTime upDate = getDateAttributeValue(rawDataUpdateDate, dbDataContainer);
		LocalDateTime fileProcessingStartDate = filterContext.getPendingFileProcessingStartDate();

		if (upDate != null) {
			return upDate.isBefore(fileProcessingStartDate);
		}
		if (insDate != null) {
			return insDate.isBefore(fileProcessingStartDate);
		}
		return false;
	}

	/**
	 * this method return value for requested attribute.
	 * 
	 * @param attribute
	 * @param dbDataContainer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private LocalDateTime getDateAttributeValue(DataAttribute attribute, DataContainer dbDataContainer) {
		DataValue<LocalDateTime> insDateVal = (DataValue<LocalDateTime>) dbDataContainer.getAttributeValue(attribute);
		if (insDateVal != null) {
			return insDateVal.getValue();
		}
		return null;
	}
}
