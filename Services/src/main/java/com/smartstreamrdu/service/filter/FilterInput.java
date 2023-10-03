/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FilterInput.java
 * Author:	Jay Sangoi
 * Date:	17-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

import lombok.Data;

/**
 * @author Jay Sangoi
 *
 */
@Data
public class FilterInput {
	/**
	 * Feed container
	 */
	private DataContainer feedContainer; 
	
	/**
	 * Db Containeer
	 */
	private List<DataContainer> dbContainers;
	
	/**
	 * Feed Name
	 */
	private String feedName;
	
	private String datasource;
	
}
