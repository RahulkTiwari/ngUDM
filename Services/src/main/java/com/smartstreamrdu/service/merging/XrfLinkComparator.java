/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfLinkComparator.java
 * Author: Shruti Arora
 * Date: 31-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.util.Constant.CrossRefConstants;

@Component("XRFNestedComparator")
public class XrfLinkComparator implements NestedArrayComparator {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 336160566745254382L;

	public boolean compareLinks(DataRow link1, DataRow link2, DataLevel level) {
		DataAttribute referenceAttr= null;
		if(DataLevel.XRF_INS.equals(level)) {
			referenceAttr= DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_REF_ID, level);
		}
		else {
			referenceAttr=DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_REF_ID, level);
		}
		return checkReferenceId(link1, link2, referenceAttr);
	}
	
	private boolean checkReferenceId(DataRow link1, DataRow link2, DataAttribute referenceAttribute) {
		ReferenceId link1RefId = getRefIdfromRow(referenceAttribute, link1);
		ReferenceId link2RefId = getRefIdfromRow(referenceAttribute, link2);
		if (link1RefId == null) {
			return false;
		}
		if (link2RefId == null) {
			return false;
		}
		return link1RefId.equals(link2RefId);
	}
	
	private ReferenceId getRefIdfromRow(DataAttribute reference, DataRow row) {
		Serializable refVal = row.getAttributeValueAtLevel(LockLevel.RDU, reference);
		ReferenceId refId = null;
		if (refVal != null) {
			refId = (ReferenceId) refVal;
		}
		return refId;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.NestedArrayComparator#comprator(com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataRowIterator)
	 */
	@Override
	public DataRow compare(NestedArrayComparatorInputPojo inputPojo) {
		DataAttribute arrayAttribute = inputPojo.getArrayAttribute();
		DataRow feed = inputPojo.getFeed();
		DataRowIterator dbIterator = inputPojo.getDbIterator();
		if(arrayAttribute == null || feed == null || dbIterator == null){
			return null;
		}
		DataRow row = null;
		while(dbIterator.hasNext()){
			DataRow dbRow = dbIterator.next();
			boolean compareLinks = compareLinks(feed, dbRow, arrayAttribute.getAttributeLevel());
			if(compareLinks){
				row = dbRow;
				break;
			}
		}
		
		return row;
	}

}
