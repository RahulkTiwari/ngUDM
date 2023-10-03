/**
* Copyright (c) 2009-2023 The SmartStream Reference Data Utility
* All rights reserved.
*
* File : FigiPrimarySourceRequestTypeMessageSender.java
* Author :AGupta
* Date : Feb 28, 2023
*/
package com.smartstreamrdu.service.openfigi;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.persistence.domain.OpenFigiRequestMetrics;

/**
 * Send message to FigiRequestQueue based on VfsFigiRequestTypeEnum
 * @author AGupta
 *
 */
public interface FigiPrimarySourceRequestTypeMessageSender {

	/**
	 * @param metrics
	 * @param requestTypeEnum
	 */
	void sendMessageToFigiRequestQueue(OpenFigiRequestMetrics metrics, VfsFigiRequestTypeEnum requestTypeEnum);
}
