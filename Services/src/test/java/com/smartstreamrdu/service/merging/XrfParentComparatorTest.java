/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfParentComparatorTest.java
 * Author: Shruti Arora
 * Date: 31-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.domain.DataContainer;

public class XrfParentComparatorTest extends CrossRefDataContainers {

	@Test
	public void test_XRfChildCompartor() {
		DataContainer con1= getDataContainer1();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataContainer con2= getDataContainer1();
		
		XrfParentContainerComparator comparator= new XrfParentContainerComparator();
		DataContainer output=comparator.compareDataContainer(con1, Arrays.asList(con1,con2));
		Assert.assertEquals(true, con1.equals(output));
		Assert.assertEquals(false, con2.equals(output));
	}
}
