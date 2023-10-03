/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ChildSdDataContainerComparator.java
 * Author: Rushikesh Dedhia
 * Date: May 31, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.service.status.StatusService;

/**
 * @author Dedhia
 *
 */
@Component("SDChildComparator")
public class ChildSdDataContainerComparator extends SdDataContainerComparator implements ChildContainerComparator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1101410882730330871L;

	@Autowired
	private StatusService statusService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.merging.ChildContainerComparator#compare(com.
	 * smartstreamrdu.domain.DataContainer,
	 * com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer compare(DataContainer  parent, DataContainer source, List<DataContainer> destination) throws Exception {
		return super.compareContainersAndReturn(parent, source, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.merging.ChildContainerComparator#compare(com.
	 * smartstreamrdu.domain.DataContainer,
	 * com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public boolean compare(DataContainer source, DataContainer destination) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.merging.ChildContainerComparator#shouldBeAdded
	 * (com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public boolean shouldBeAdded(DataContainer dc, String datasource) {

			return !statusService.isStatusInactive(dc, datasource);

	}

}
