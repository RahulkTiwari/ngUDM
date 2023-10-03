/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergingDecorator.java
 * Author:	Jay Sangoi
 * Date:	24-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * Decorator class for DataContainerMerging. This used decorator design pattern
 * We will have multiple Decorator class which performs the operation related to specific merging
 * 
 * @author Jay Sangoi
 *
 */
public class DataContainerMergingDecorator implements DataContainerMerging{

	private final DataContainerMerging decorator;
	
	public DataContainerMergingDecorator(final DataContainerMerging decorator){
		this.decorator = decorator;
	}
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.DataContainerMerging#merge(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public void merge(DataContainer feedContainer, List<DataContainer> dbContainers)
			throws Exception {
		     decorator.merge(feedContainer, dbContainers);
	}

}
