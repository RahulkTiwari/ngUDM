/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: StaticNestedArrayComparator.java
 * Author : SaJadhav
 * Date : 20-Aug-2019
 * 
 */
package com.smartstreamrdu.service.merging;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * COmparator for Nested array in static domain collections and dvDomainMap collection
 * @author SaJadhav
 *
 */
@Component("STATICNestedComparator")
public class StaticNestedArrayComparator implements NestedArrayComparator {

	private static final long serialVersionUID = -3225288554501721435L;
	
	private static final DataAttribute VENDOR_MAPPINGS_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
	private static final DataAttribute VENDOR_DOMAIN_NAME_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainName", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS_ATTRIBUTE);
	private static final DataAttribute VENDOR_DOMAIN_SOURCE_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainSource", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS_ATTRIBUTE);
	private static final DataAttribute VENDOR_DOMAIN_VALUE_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainValue", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS_ATTRIBUTE);
	private static final DataAttribute VENDOR_DOMAIN_STATUS_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("status", DataLevel.DV_DOMAIN_MAP, VENDOR_MAPPINGS_ATTRIBUTE);

	/**
	 * Compares vendorMappings attribute of feed and DB.
	 * domainSource,domainValue and domainName are used for equality check 
	 */
	@Override
	public DataRow compare(NestedArrayComparatorInputPojo inputPojo) {
		DataAttribute arrayAttribute = inputPojo.getArrayAttribute();
		DataRow feed = inputPojo.getFeed();
		DataRowIterator dbIterator = inputPojo.getDbIterator();
		if(VENDOR_MAPPINGS_ATTRIBUTE.equals(arrayAttribute)){
			return getMatchingVendorMappingRow(feed,dbIterator);
		}
		return null;
	}

	/**
	 * @param feed
	 * @param dbIterator
	 * @return 
	 */
	private DataRow getMatchingVendorMappingRow(DataRow feed, DataRowIterator dbIterator) {
		DataRow matchingRow=null;
		String domainName=feed.getHighestPriorityAttributeValue(VENDOR_DOMAIN_NAME_ATTRIBUTE);
		DomainType domainValue=feed.getHighestPriorityAttributeValue(VENDOR_DOMAIN_VALUE_ATTRIBUTE);
		String domainSource=feed.getHighestPriorityAttributeValue(VENDOR_DOMAIN_SOURCE_ATTRIBUTE);
		
		while (dbIterator.hasNext()) {
			DataRow dbRow = dbIterator.next();
			if (domainName.equals(dbRow.getHighestPriorityAttributeValue(VENDOR_DOMAIN_NAME_ATTRIBUTE))
					&& domainValue.equals(dbRow.getHighestPriorityAttributeValue(VENDOR_DOMAIN_VALUE_ATTRIBUTE))
					&& domainSource.equals(dbRow.getHighestPriorityAttributeValue(VENDOR_DOMAIN_SOURCE_ATTRIBUTE))
					&& DomainStatus.ACTIVE
							.equals(dbRow.getHighestPriorityAttributeValue(VENDOR_DOMAIN_STATUS_ATTRIBUTE))) {
				matchingRow = dbRow;
				break;
			}
		}
		return matchingRow;
	}
}
