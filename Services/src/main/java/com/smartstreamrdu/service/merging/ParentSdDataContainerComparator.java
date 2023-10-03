/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ParentSdDataContainerComparator.java
 * Author: Rushikesh Dedhia
 * Date: May 31, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.List;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Dedhia
 *
 */
@Component("SDParentComparator")
public class ParentSdDataContainerComparator extends SdDataContainerComparator implements ParentContainerComparator{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8465221409661234851L;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.ParentContainerComparator#compareDataContainer(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainer compareDataContainer(DataContainer feedDContainer, List<DataContainer> dbContainers) throws UdmTechnicalException {
		
		return super.compareContainersAndReturn(null,feedDContainer, dbContainers);

	}


}
