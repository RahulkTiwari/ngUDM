/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfChildContainerComparatorTest.java
 * Author: Shruti Arora
 * Date: 29-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.domain.DataContainer;

public class XrfChildContainerComparatorTest extends CrossRefDataContainers {

	@Test
	public void test_XRfChildCompartor() {
		
		DataContainer sourceDC= getChildDataContainerInput();
		DataContainer targetDC= getChildDataContainer1();
		DataContainer diffDC= getChildDataContainerUnmatched();
		DataContainer withoutLinks=getSecContainerWithoutLinks();
		DataContainer withoutRefId= getChildWithNoRefId();
		
		XrfChildContainerComparator secComparator = new XrfChildContainerComparator();
		Assert.assertTrue(secComparator.compare(sourceDC, targetDC));
		Assert.assertFalse(secComparator.compare(targetDC, diffDC));
		Assert.assertFalse(secComparator.compare(withoutLinks, targetDC));
		Assert.assertFalse(secComparator.compare(targetDC, withoutLinks));
		Assert.assertFalse(secComparator.compare(diffDC, withoutRefId));
		Assert.assertFalse(secComparator.compare(withoutRefId, diffDC));
				
	}
}
