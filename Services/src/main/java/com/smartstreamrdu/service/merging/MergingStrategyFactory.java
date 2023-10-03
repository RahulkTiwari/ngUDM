/*******************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    MergingStrategyFactory.java
 * Author:    Padgaonkar
 * Date:    05-Mar-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author Padgaonkar
 *<br>This facotry will give default merge strategy bean or feed specific bean to do feed specific task.
 *<br> for example. we have SnpXfrInActiveContainerMergingServiceImpl which is used to update only instrument status in db container from feed container.
 *get default strategy or snp strategy for delete
 *
 */
public interface MergingStrategyFactory extends Serializable {

	/**
	 * factory method to give default strategy or feed specific merge strategy 
	 * @see DataContainerMergingService
	 * @param feedContainer
	 * @param dbDataContainers
	 * @return DataContainerMergingService
	 */
	public DataContainerMergingService getMergingStrategy(DataContainer feedContainer,List<DataContainer> dbDataContainers );
}
