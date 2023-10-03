/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ChildContainerComparatorTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.domain.DataContainer;


/**
 * @author Jay Sangoi
 *
 */
public class ChildContainerComparatorTest {

	@Test
	public void test_shouldBeAdded(){
		
		ChildContainerComparator comaprator = new ChildContainerComparator() {
			
			@Override
			public boolean compare(DataContainer source, DataContainer destination) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public DataContainer compare(DataContainer parent, DataContainer source, List<DataContainer> destination)
					throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		Assert.assertTrue(comaprator.shouldBeAdded(null, null));
		
	}
	
}
