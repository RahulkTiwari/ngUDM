/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfParentContainerComparator.java
 * Author: Shruti Arora
 * Date: 29-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.time.LocalDateTime;
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
import com.smartstreamrdu.util.Constant.CrossRefConstants;

@Component("XRFParentComparator")
public class XrfParentContainerComparator implements ParentContainerComparator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4334397764104626178L;

	@Override
	public DataContainer compareDataContainer(DataContainer feedContainer, List<DataContainer> dbContainers) {
		LocalDateTime oldestTime = LocalDateTime.MAX;
		DataContainer container = null;
		for (DataContainer dc : dbContainers) {
			LocalDateTime linkTime = getOldestDate(getLinkDataRows(dc));
			if (container == null || linkTime.isBefore(oldestTime)) {
				oldestTime = linkTime;
				container = dc;
			}
		}
		return container;
	}

	private ArrayList<DataRow> getLinkDataRows(DataContainer container) {
		DataAttribute links = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINKS,
				DataLevel.XRF_INS);
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

	private LocalDateTime getOldestDate(ArrayList<DataRow> rows) {
		LocalDateTime earliestTime = LocalDateTime.MAX;
		if(CollectionUtils.isEmpty(rows)) {
			return earliestTime;
		}
		DataAttribute startDate = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_INITIAL_DATE,
				DataLevel.XRF_INS);		
		for (DataRow row : rows) {
			LocalDateTime date = row.getAttributeValueAtLevel(LockLevel.RDU, startDate);
			if (date != null && earliestTime.isAfter(date)) {
					earliestTime = date;
			}
		}
		return earliestTime;
	}
	
	
	
}
