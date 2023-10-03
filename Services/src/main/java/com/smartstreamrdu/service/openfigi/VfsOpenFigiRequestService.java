/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : VfsOpenFigiRequestService.java
 * Author :SaJadhav
 * Date : 30-Nov-2021
 */
package com.smartstreamrdu.service.openfigi;

import com.smartstreamrdu.domain.DataContainer;

/**
 * Service to send request to VfsRequestQueue
 * 
 * @author SaJadhav
 *
 */
public interface VfsOpenFigiRequestService {

	/**
	 * If the input {@code DataContainer } is valid for making openfigi request and it contains changes for
	 * configured xrf or non-xrf attributes then sends a request to VfsRequestQueue 
	 * 
	 * @param dataContainer
	 */
	public void sendRequestToVfsOpenFigi(DataContainer dataContainer);
}
