/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergingDataContainer.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataContainer;

/**
 * Service to merge the Feed Data Container with DB Data Container
 * @author Jay Sangoi
 *
 */
public interface MergingDataContainer extends Serializable{

	/**
	 * Merge feed and db contaner
	 * @param feedContainer - feed container
	 * @param dbContainer - db container
	 * @return DataContainer that needs to be stored in db
	 */
	DataContainer merge(DataContainer feedContainer, DataContainer dbContainer);
}
