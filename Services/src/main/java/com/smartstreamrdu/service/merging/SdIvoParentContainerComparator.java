/**
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdIvoParentContainerComparator.java
 * Author : SaJadhav
 * Date : 14-Feb-2019
 * 
 */
package com.smartstreamrdu.service.merging;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author SaJadhav
 *
 */
@Component("IVOParentComparator")
public class SdIvoParentContainerComparator implements ParentContainerComparator {

	private static final long serialVersionUID = -7623317362731840244L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.ParentContainerComparator#compareDataContainer(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainer compareDataContainer(DataContainer feedDContainer, List<DataContainer> dbContainers){

		
		if (feedDContainer == null || dbContainers == null) {
			throw new IllegalArgumentException("Exception while merging sd IVO data containers. Neither the feed container nor the database containers can be empty");
		}
		
		if (!CollectionUtils.isEmpty(dbContainers) && dbContainers.size() == 1) {
			return dbContainers.get(0);
		} else {
			throw new IllegalStateException("More than one SD IVO data containers fetched from database for merging");
		}
		
	
	}

}
