/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergeExceptionTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jay Sangoi
 *
 */
public class DataContainerMergeExceptionTest {

	@Test
	public void test_Exception() {

		DataContainerMergeException exception1 = new DataContainerMergeException("");

		Assert.assertNotNull(exception1);

		DataContainerMergeException exception2 = new DataContainerMergeException("Exception");
		Assert.assertNotNull(exception2);
		Assert.assertNotNull(exception2.getMessage());
		Assert.assertEquals("Exception", exception2.getMessage());

		
		DataContainerMergeException exception3 = new DataContainerMergeException("Exception", new RuntimeException());
		Assert.assertNotNull(exception3);
		Assert.assertNotNull(exception3.getMessage());
		Assert.assertEquals("Exception", exception3.getMessage());
		
		Assert.assertTrue(exception3.getCause() instanceof RuntimeException);
		
	}

}
