/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdIvoToSdNestedArrayComparator.java
 * Author : SaJadhav
 * Date : 14-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;

/**
 * @author SaJadhav
 *
 */
@Component
public class SdIvoToSdNestedArrayComparator {
	/**
	 * Compares sdIvoDataRow with sdDataRows and returns the matching sdDataRow based on relationType value
	 * 
	 * @param sdIvoNestedDataAttribute
	 * @param sdIvoDataRow
	 * @param sdDataRowIterator
	 * @return
	 */
	public DataRow compareAndReturn(DataAttribute sdIvoNestedDataAttribute,DataRow sdIvoDataRow,DataRowIterator sdDataRowIterator){
		
		DataLevel normalizedLevel = DataLevel.getNormalizedDataLevelForIvoLevel(sdIvoNestedDataAttribute.getAttributeLevel());
		DataAttribute sdNestedDataAttribute=DataAttributeFactory.getAttributeByNameAndLevel(sdIvoNestedDataAttribute.getAttributeName(),normalizedLevel);
		
		DataRow row = null;
		if(DataAttributeFactory.getIvoRelationAttributeForInsAndLe().equals(sdIvoNestedDataAttribute) || DataAttributeFactory.getIvoRelationAttributeForInsAndIns().equals(sdIvoNestedDataAttribute)){
			Serializable attributeValue = sdIvoDataRow.getAttributeValue(DataAttributeFactory.getRelationTypeAttribute(sdIvoNestedDataAttribute)).getValue();
			if(attributeValue == null){
				return null;
			}
			while(sdDataRowIterator.hasNext()){
				DataRow sdDataRow = sdDataRowIterator.next();
				Serializable relationTypeAttributeValue = sdDataRow.getAttributeValue(DataAttributeFactory.getRelationTypeAttribute(sdNestedDataAttribute)).getValue();
				if(attributeValue.equals(relationTypeAttributeValue)){
					row = sdDataRow;
					break;
				}
			}
		}
		return row;
		
	}
}
