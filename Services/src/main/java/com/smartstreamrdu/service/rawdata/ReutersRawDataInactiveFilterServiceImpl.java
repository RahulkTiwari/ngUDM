/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataInactiveFilterServiceImpl.java
 * Author:	Shruti Arora
 * Date:	21-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.util.Constant.RecordType;
import com.smartstreamrdu.util.Constant.TradingStatus;

@Component("reutersRDInactive")
public class ReutersRawDataInactiveFilterServiceImpl implements RawDataInactiveFilterService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4771032615447436673L;

	/** (non-Javadoc)
	 * This Method will return false only when the trading status of incoming record is Inactive
	 */
	@Override
	public boolean shouldPersistRawData(Record record,  List<DataContainer> dbContainers) {
		
		if(CollectionUtils.isNotEmpty(dbContainers)){
			return true;
		}
		Serializable tradingStatus=record.getRecordRawData().getRawDataAttribute(TradingStatus.TRADING_STATUS);
		Serializable recordType = record.getRecordRawData().getRawDataAttribute(RecordType.RECORD_TYPE);
		if (RecordType.M4_RECORD_TYPE.equals(String.valueOf(recordType)) ||
				RecordType.M3_RECORD_TYPE.equals(String.valueOf(recordType))) {
			return false;
		}
		if(tradingStatus == null) {
			return true;
		}
		return !TradingStatus.INACTIVE.equals(String.valueOf(tradingStatus));
	}

}
