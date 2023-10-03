/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    DataContainerRejectionService.java
 * Author:  Padgaonkar
 * Date:    Feb 02, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * This is generic interface which rejects dataContainer before persistence
 * based on provided logic
 * @author Padgaonkar
 *
 */
public interface DataContainerRejectionService {

	/**
	 * This method validates dataContainer & removes it from list if its not applicable.
	 * @param dataContainers
	 */
	public void validateAndRemoveInvalidContainerFromList(List<DataContainer> dataContainers);
}
