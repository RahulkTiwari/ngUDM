/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: RawDataParentContainerComparator.java
 * Author: Divya Bharadwaj
 * Date: Jun 11, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author Bharadwaj
 *
 */
@Component("RAWParentComparator")
public class RawDataParentContainerComparator implements ParentContainerComparator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 676632450097762371L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.ParentContainerComparator#compareDataContainer(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainer compareDataContainer(DataContainer feedDContainer, List<DataContainer> dbContainers) {
		
		if (feedDContainer == null || dbContainers == null) {
			throw new IllegalArgumentException("Exception while merging raw data containers. Neither the feed container nor the database containers can be empty");
		}
		
		if (!CollectionUtils.isEmpty(dbContainers) && dbContainers.size() == 1) {
			return dbContainers.get(0);
		} else {
			throw new IllegalStateException("More than one raw data containers fetched from database for merging");
		}
		
	}

}
