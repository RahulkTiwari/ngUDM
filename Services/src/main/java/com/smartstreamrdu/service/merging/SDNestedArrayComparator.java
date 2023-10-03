/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	SDNestedArrayComparator.java
 * Author:	Jay Sangoi
 * Date:	07-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.util.Constant;

/**
 *  * Nested array comparator for SD dataLevel

 * @author Jay Sangoi
 *
 */
@Component("SDNestedComparator")
public class SDNestedArrayComparator extends AbstractNestedArrayComparator implements NestedArrayComparator {


	/**
	 * 
	 */
	private static final long serialVersionUID = -9032916568033096252L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.NestedArrayComparator#comprator(com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataRowIterator)
	 */
	@Override
	public DataRow compare(NestedArrayComparatorInputPojo inputPojo) {
		DataAttribute arrayAttribute = inputPojo.getArrayAttribute();
		DataRow feed = inputPojo.getFeed();
		DataRowIterator dbIterator = inputPojo.getDbIterator();
		DataContainer dbDataContianer = inputPojo.getDbDataContainer();
		
		DomainType dataSourceValue = (DomainType) dbDataContianer.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.dataSource,
						dbDataContianer.getLevel().getRootLevel()));
		
		DataRow row = null;
		if(arrayAttribute.getDataType().equals(DataType.NESTED_ARRAY) && (arrayAttribute.getAttributeLevel().getCollectionName().equals(Constant.SD_DATA))){
			
			while(dbIterator.hasNext()){
				DataRow dbRow = dbIterator.next();
				if(allKeysEquate(feed, dbRow, arrayAttribute, dataSourceValue)){
					row = dbRow;
					break;
				}
			}
		}
		return row;
	}


	

	
	
	

}
