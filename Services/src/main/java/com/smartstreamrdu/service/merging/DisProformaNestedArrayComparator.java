package com.smartstreamrdu.service.merging;

/*******************************************************************
*
* Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	DisProformaNestedArrayComparator.java
* Author:	S Padgaonkar
* Date:	22-August-2019
*
*******************************************************************
*/

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;

/**
 * Comparator for Nested Array Attributes in DisProformaNestedArrayComparator
*
*/

@Component("ProformaNestedComparator")
public class DisProformaNestedArrayComparator implements NestedArrayComparator {
	
	/**
	 * 
	 */
	private static final String RELATION_TYPE = "relationType";
	private static final String SOURCE = "source";	
	private static final String DOMAIN = "domain";
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = LoggerFactory.getLogger(DisProformaNestedArrayComparator.class);
	

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.NestedArrayComparator#compare(com.smartstreamrdu.domain.DataAttribute, com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataRowIterator)
	 */
	@Override
	public DataRow compare(NestedArrayComparatorInputPojo inputPojo) {
		DataAttribute arrayAttribute = inputPojo.getArrayAttribute();
		DataRow feed = inputPojo.getFeed();
		DataRowIterator dbIterator = inputPojo.getDbIterator();
		DataRow row = null;
		
		Map<DataAttribute, DataValue<Serializable>> feedRowData = feed.getRowData();
		
		if(feedRowData == null) {
			return null;
		}
		row = checkAttributeValueInRows(dbIterator, feedRowData,arrayAttribute);		
		return row;
	}

	private DataRow checkAttributeValueInRows(DataRowIterator dbIterator,
			Map<DataAttribute, DataValue<Serializable>> feedRowData, DataAttribute arrayAttribute) {
	
		while(dbIterator.hasNext()){
			DataRow dbRow = dbIterator.next();
			
		    Map<DataAttribute, DataValue<Serializable>> dbRowData = dbRow.getRowData();	

			if(dbRowData != null && compareDbAndFeedRow(dbRowData,feedRowData,arrayAttribute)) {
				return dbRow;
			}
		
		}
		return null;
	}
	

	private boolean compareDbAndFeedRow(Map<DataAttribute, DataValue<Serializable>> dbRowData,
			Map<DataAttribute, DataValue<Serializable>> feedRowData, DataAttribute arrayAttribute) {

		try {
			DataAttribute relationType = DataAttributeFactory.getAttributeByNameAndLevelAndParent(RELATION_TYPE,DataLevel.PROFORMA_INS, arrayAttribute);
			return feedRowData.get(relationType).equals(dbRowData.get(relationType));
		} catch (Exception e) {
			_logger.debug("{} attribute is not relational attribute",arrayAttribute);
		}
		
		DataAttribute source = DataAttributeFactory.getAttributeByNameAndLevelAndParent(SOURCE,
				DataLevel.PROFORMA_INS, arrayAttribute);
		
		DataAttribute domainAttribute = getDomainAttribute( arrayAttribute);

		// This attribute is always true for non-domainable Attribute if attribute is
		// domain attribute
		// this value will be overridden in next block.
		boolean domainEquality = true;

		if (domainAttribute!=null && dbRowData.get(domainAttribute) != null && feedRowData.get(domainAttribute) != null) {
			domainEquality = dbRowData.get(domainAttribute).equals(feedRowData.get(domainAttribute));
		}
		boolean sourceEquality = dbRowData.get(source).equals(feedRowData.get(source));

		return domainEquality && sourceEquality;

	}

	/**
	 * @param arrayAttribute
	 * @return 
	 */
	private DataAttribute getDomainAttribute(DataAttribute arrayAttribute) {
		try {
			return DataAttributeFactory.getAttributeByNameAndLevelAndParent(DOMAIN,DataLevel.PROFORMA_INS, arrayAttribute);
		} catch (Exception e) {
			_logger.debug("{} attribute is not domain attribute",arrayAttribute);
			return null;
		}
		
		
	}


}
