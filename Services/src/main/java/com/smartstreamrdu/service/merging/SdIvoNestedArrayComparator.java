/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: SdIvoNestedArrayComparator.java
* Author : VRamani
* Date : Apr 24, 2019
* 
*/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;

/**
 * Comparator for Nested Array Attributes in SdIvo
* @author VRamani
*
*/
@Component("IVONestedComparator")
public class SdIvoNestedArrayComparator implements NestedArrayComparator {

	private static final long serialVersionUID = 1L;
	
	private final List<DataAttribute> ivoRelationAttributes=new ArrayList<>();
	
	public SdIvoNestedArrayComparator(){
		ivoRelationAttributes.add(DataAttributeFactory.getIvoRelationAttributeForInsAndIns());
		ivoRelationAttributes.add(DataAttributeFactory.getIvoRelationAttributeForInsAndLe());
		ivoRelationAttributes.add(DataAttributeFactory.getIvoRelationAttributeForSecAndSec());
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.NestedArrayComparator#compare(com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataRowIterator)
	 */
	@Override
	public DataRow compare(NestedArrayComparatorInputPojo inputPojo) {
		DataAttribute arrayAttribute = inputPojo.getArrayAttribute();
		DataRow feed = inputPojo.getFeed();
		DataRowIterator dbIterator = inputPojo.getDbIterator();
		DataRow row = null;
		if(ivoRelationAttributes.contains(arrayAttribute)){
			Serializable attributeValue = feed.getAttributeValue(DataAttributeFactory.getRelationTypeAttribute(arrayAttribute));
			if(attributeValue == null){
				return null;
			}
			row = checkAttributeValueInRows(arrayAttribute, dbIterator, attributeValue);
			
		}
		return row;
	}

	private DataRow checkAttributeValueInRows(DataAttribute arrayAttribute, DataRowIterator dbIterator,
			Serializable attributeValue) {
		while(dbIterator.hasNext()){
			DataRow dbRow = dbIterator.next();
			if(attributeValue.equals(dbRow.getAttributeValue(DataAttributeFactory.getRelationTypeAttribute(arrayAttribute)))){
				return dbRow;
			}
		}
		return null;
	}

}
