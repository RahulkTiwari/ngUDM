/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: VfsOpenFigiMessageGenerator.java
 * Author: Rushikesh Dedhia
 * Date: Jul 10, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.openfigi;

import java.io.Serializable;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.persistence.domain.OpenFigiRequestMetrics;

/**
 * @author Dedhia
 *
 */
public interface VfsOpenFigiMessageGenerator extends Serializable {

	/**
	 * Generates the VfsFigiRequestMessage from input childContainer and
	 * parentContainer
	 * 
	 * @param childContainer
	 * @param parentContainer
	 * @return
	 */
	VfsFigiRequestMessage generateVfsOpenFigiMessaage(DataContainer childContainer, DataContainer parentContainer, VfsFigiRequestTypeEnum requestType);

	/**
	 * This method generates VfsFigiRequestMessage from input request metrics ,
	 * requestType and retryCount. Also populate OpenFigiRequestRuleEnum based on
	 * last successful request.It populates the request parameters from
	 * OpenFigiRequestMetrics.originalRequest and
	 * OpenFigiRequestMetrics.primarySourceReferenceId.
	 * 
	 * @param metrics
	 * @param requestType
	 * @param retryCount
	 * @return
	 */
	VfsFigiRequestMessage generateVfsOpenFigiMessageFromRequestMetrics(OpenFigiRequestMetrics metrics,VfsFigiRequestTypeEnum requestType, int retryCount);

	/**
	 * This method generates VfsFigiRequestMessage from input request metrics ,
	 * requestType and retryCount. And populate OpenFigiRequestRuleEnum as Default.
	 * 
	 * @param metrics
	 * @param requestType
	 * @param retryCount
	 * @return
	 */
	VfsFigiRequestMessage generateDefaultOpenFigiMessageFromRequestMetrics(OpenFigiRequestMetrics metrics,VfsFigiRequestTypeEnum requestType, int retryCount);

}
