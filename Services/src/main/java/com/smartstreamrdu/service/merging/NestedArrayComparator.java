/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	NestedArrayComparator.java
 * Author:	Jay Sangoi
 * Date:	07-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;

import com.smartstreamrdu.domain.DataRow;

/**
 * Compares the Nested array structure and return the correct element in array
 * @author Jay Sangoi
 *
 */
public interface NestedArrayComparator extends Serializable {
	
	/**
	 * Compares the data row with db data rows and return the correct data row
	 * @param feed
	 * @param dbIterator
	 * @return
	 */
	DataRow compare(NestedArrayComparatorInputPojo inputPojo);
	
}
