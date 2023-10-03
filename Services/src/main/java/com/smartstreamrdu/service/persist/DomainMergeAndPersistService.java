/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainMergeAndPersistService.java
 * Author : SaJadhav
 * Date : 16-Aug-2019
 * 
 */
package com.smartstreamrdu.service.persist;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.service.merging.DataContainerMergeException;

/**
 * @author SaJadhav
 *
 */
public interface DomainMergeAndPersistService {
	
	/**
	 * Merges dataContainer with dbDataContainer.
	 * 
	 * @param dataContainer
	 * @param dbDataContainer
	 * @throws DataContainerMergeException 
	 */
	public void merge(DataContainer dataContainer,DataContainer dbDataContainer) throws DataContainerMergeException;

}
