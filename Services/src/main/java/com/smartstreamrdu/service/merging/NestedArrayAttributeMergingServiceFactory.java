/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    NestedArrayAttributeMergingServiceFactory.java
 * Author:  Dedhia
 * Date:    16-Jul-2020
 *
 ********************************************************************/
package com.smartstreamrdu.service.merging;

import com.smartstreamrdu.domain.DataContainer;

public interface NestedArrayAttributeMergingServiceFactory {
	
	/**
	 *  Returns the applicable NestedArrayAttributeMergingService based on the 
	 *  given data container object.
	 *  
	 * @param dataContainer
	 * @return
	 */
	NestedArrayAttributeMergingService getMergingService(DataContainer dataContainer);

}
