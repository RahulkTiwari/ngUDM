/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataUtil.java
 * Author:	RKaithwas
 * Date:	13-Nov-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;

import com.smartstreamrdu.domain.Record;

/**
 * @author RKaithwas
 *
 */

public final class RawDataUtil {
	
	private RawDataUtil(){
		
	}
	/**
	 * create codeHash from rawData only where rawDate.field matches deltaFields else if deltaFields=null then take all rawData fields and calculate codeHash.
	 * @param record
	 * @param deltaFields
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static String createCodeHash(Record record, List<String> deltaFields) {
        JSONObject rawDataRecord = record.getRecordRawData().getRawData();
        JSONObject finalMap = new JSONObject();
        if (deltaFields != null && !deltaFields.isEmpty()) {
            deltaFields.forEach(field -> finalMap.put(field, record.getRecordRawData().getRawDataAttribute(field)));
            return createHash(finalMap.toString());
        }
        return createHash(rawDataRecord.toString());
    }
	
	private static String createHash(String record) {
		return DigestUtils.md5Hex(record);
	}

}
