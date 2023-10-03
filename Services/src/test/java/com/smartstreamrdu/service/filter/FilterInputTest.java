/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FilterInputTest.java
 * Author:	Jay Sangoi
 * Date:	23-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;


/**
 * @author Jay Sangoi
 *
 */
public class FilterInputTest {

	@Test
	public void test_Object(){
		
		FilterInput input = new FilterInput();
		input.setDatasource("trdse");
		
		List<DataContainer> dbContainers = new ArrayList<>();
		dbContainers.add(new DataContainer(DataLevel.INS, DataContainerContext.builder().build() ));
		input.setDbContainers(dbContainers);

		input.setFeedContainer(new DataContainer(DataLevel.INS, DataContainerContext.builder().build() ));
		input.setFeedName("feed");
		
		Assert.assertNotNull(input);
		Assert.assertNotNull(input.getDatasource());
		Assert.assertNotNull(input.getFeedName());
		Assert.assertNotNull(input.getDbContainers());
		Assert.assertNotNull(input.getFeedContainer());
		
	}
	
}
