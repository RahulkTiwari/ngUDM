/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfChildContainerComparator.java
 * Author: Shruti Arora
 * Date: 29-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;

/**
 * @author SArora
 *
 */
@Component("XRFChildComparator")
public class XrfChildContainerComparator implements ChildContainerComparator {

	
	@Override
	public DataContainer compare(DataContainer  parent, DataContainer source, List<DataContainer> destination) {
		if(CollectionUtils.isEmpty(destination)) {
			return null;
		}
		for(DataContainer dbContainer: destination) {
			if(checkSecContainerByRefId(source, dbContainer)) {
				return dbContainer;
			}
		}	
		return null;
	}
	
	@Override
	public boolean compare(DataContainer source, DataContainer destination) {
		return checkSecContainerByRefId(source, destination);

	}
	
	private boolean checkSecContainerByRefId(DataContainer xrChildContainer, DataContainer dbChildContainer) {
		List<DataRow> xrSecLinks = getLinkDataRows(xrChildContainer);
		List<DataRow> dbSecLinks = getLinkDataRows(dbChildContainer);
		if (xrSecLinks == null ||  dbSecLinks==null) {
			return false;
		}
		
		return (checkByLinks(dbSecLinks, xrSecLinks));
	}

	private ArrayList<DataRow> getLinkDataRows(DataContainer container) {
		String linkAttribute = "securityXrfLinks";
		DataAttribute links = DataAttributeFactory.getAttributeByNameAndLevel(linkAttribute, DataLevel.XRF_SEC);
		Serializable allLinks = container.getAttributeValue(links);
		ArrayList<DataRow> linksList = null;
		if (allLinks instanceof DataRow) {
			DataRow row = (DataRow) allLinks;
			if (row.getValue() != null && row.getValue().getValue(LockLevel.RDU) != null
					&& row.getValue().getValue(LockLevel.RDU) instanceof ArrayList) {
				linksList = row.getValue().getValue(LockLevel.RDU);
			}
		}
		return linksList;
	}

	private boolean checkByLinks(List<DataRow> dbRows, List<DataRow> xrRows) {
		for (DataRow dbRow : dbRows) {
			if (checkReferenceId(dbRow, xrRows)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkReferenceId(DataRow dbRow, List<DataRow> xrRows) {
		ReferenceId dbRefId = null;
		String refAttributeName = "_securityXrfRefId";
		dbRefId = getRefIdfromRow(refAttributeName, dbRow);
		if (dbRefId == null) {
			return false;
		}
		for (DataRow xrRow : xrRows) {
			ReferenceId xrRefId = getRefIdfromRow(refAttributeName, xrRow);
			if (xrRefId == null) {
				return false;
			}
			if (xrRefId.equals(dbRefId)) {
				return true;
			}
		}
		return false;
	}

	private ReferenceId getRefIdfromRow(String attributeName, DataRow row) {
		DataAttribute reference = DataAttributeFactory.getAttributeByNameAndLevel(attributeName, DataLevel.XRF_SEC);
		Serializable refVal = row.getAttributeValueAtLevel(LockLevel.RDU, reference);
		ReferenceId dbRefId = null;
		if (refVal != null) {
			dbRefId = (ReferenceId) refVal;
		}
		return dbRefId;
	}

	



}
