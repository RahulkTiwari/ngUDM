/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ChildContainerComparator.java
 * Author: Shruti Arora
 * Date: 29-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

public interface ChildContainerComparator  {
	
	DataContainer compare(DataContainer  parent, DataContainer source, List<DataContainer> destination) throws Exception;
	
	boolean compare(DataContainer source, DataContainer destination);
	
	/**
	 * This method states whether the Child container should be added or no. 
	 * Assumption is there is no child container in db but a child container is populated in feed. By default it is the true.
	 * Override this method, if you want to perform some checks like inactivity etc
	 * @param dc - child data container
	 * @return
	 */
	default boolean shouldBeAdded(DataContainer dc, String datasource ){
		return true;
	}
}
