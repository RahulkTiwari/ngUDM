/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FilterOutputTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import org.junit.Test;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.service.AbstractJavaBeanNonSerializableTest;

/**
 * @author Jay Sangoi
 *
 */
public class FilterOutputTest extends AbstractJavaBeanNonSerializableTest<FilterOutput>{
	
	@Test
	public void testFilterOutput() { // workaround to avoid SonarQube missing test issue
		new FilterOutput();
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.bean.test.AbstractJavaBeanNonSerializableTest#getBeanClass()
	 */
	@Override
	protected Class<FilterOutput> getBeanClass() {
		return FilterOutput.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Prafab<DataContainer> prefabValues() {
		return new Prafab<DataContainer>(DataContainer.class, new DataContainer(DataLevel.SEC,null), new DataContainer(DataLevel.INS,null));
	}

}
