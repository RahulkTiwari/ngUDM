/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: AttributeMergeHandlerFactory.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import com.smartstreamrdu.domain.DataAttribute;

/**
 * @author Dedhia
 *
 */
public interface AttributeMergeHandlerFactory {
	
	AttributeMergeHandler getMergeHandler(DataAttribute dataAttribute);

}
