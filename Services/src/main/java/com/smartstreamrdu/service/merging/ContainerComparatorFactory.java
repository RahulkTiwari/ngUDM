/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ParentContainerComparatorFactory.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author Dedhia
 *
 */
public interface ContainerComparatorFactory extends Serializable {

	ParentContainerComparator getParentContainerComparator(DataContainer dataContainer);
	
	ChildContainerComparator getChildContainerComparator(DataContainer dataContainer);
	
	NestedArrayComparator getNestedArrayComparator(DataContainer dataContainer);
	
}
