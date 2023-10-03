/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FilterOutput.java
 * Author:	Jay Sangoi
 * Date:	17-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author Jay Sangoi
 *
 */
@Data
public class FilterOutput {
	private boolean persist;
	
	private String message;
	
	//This attribute to hold list of filtered inactive securities
	private List<DataContainer> filteredInactiveSecurities  = new ArrayList<>();

	
	/**
	 * 
	 * @param secContainers
	 */
	public void addFilteredInactiveSecurities(List<DataContainer> secContainers) {
		filteredInactiveSecurities.addAll(secContainers);
	}

}
