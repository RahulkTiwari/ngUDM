/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    NestedArrayAttributeMergingService.java
 * Author:  Dedhia
 * Date:    16-Jul-2020
 *
 ********************************************************************/
package com.smartstreamrdu.service.merging;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;

public interface NestedArrayAttributeMergingService {

	/**
	 * Merges the value for the given nested array data attribute between the given
	 * feed data container and the database data container.
	 * 
	 * @param feedContainer
	 * @param dbContainer
	 * @param dataAttribute
	 */
	void merge(DataContainer feedContainer, DataContainer dbContainer, DataAttribute dataAttribute);

}
