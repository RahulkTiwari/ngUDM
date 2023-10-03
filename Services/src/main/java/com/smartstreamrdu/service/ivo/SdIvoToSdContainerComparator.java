/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdIvoToSdContainerComparator.java
 * Author : SaJadhav
 * Date : 13-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author SaJadhav
 *
 */
public interface SdIvoToSdContainerComparator {
	
	DataContainer compareAndReturn(DataContainer sdContainer,List<DataContainer> sdIvoContainers,DataContainer xrDataContainer);

}
