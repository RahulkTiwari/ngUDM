/**
* Copyright (c) 2009-2023 The SmartStream Reference Data Utility
* All rights reserved.
*
* File : FigiPrimarySourceRequestTypeMessageSenderImpl.java
* Author :AGupta
* Date : Feb 28, 2023
*/
package com.smartstreamrdu.service.openfigi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.persistence.domain.OpenFigiRequestMetrics;
import com.smartstreamrdu.util.Constant.MessagingConstant;
import com.smartstreamrdu.util.Constant.Process;
import com.smartstreamrdu.util.Constant.VfsConstants;
import com.smartstreamrdu.util.VfsSystemPropertiesConstant;

/**
 * This class should be used when we want to retry with DEFAULT
 * openFigiRequestRuleEnum and retryCount is populated from DB
 * 
 * @author AGupta
 *
 */
@Component
public class FigiPrimarySourceRequestTypeMessageSenderImpl implements FigiPrimarySourceRequestTypeMessageSender {

    @Autowired
    private VfsMessageSender vfsMessageSender;
    
    @Autowired
    private UdmSystemPropertiesCache systemCache;
    
    @Autowired
    private VfsOpenFigiMessageGeneratorImpl vfsOpenFigiMessageGeneratorImpl;

	@Override
    public void sendMessageToFigiRequestQueue(OpenFigiRequestMetrics metrics,VfsFigiRequestTypeEnum requestTypeEnum) {
		
        int retryCount = 0;
        if(requestTypeEnum.equals(VfsFigiRequestTypeEnum.INACTIVATION_AFTER_NO_REPLY)) {
        	retryCount= getRetryCountForInactivation();
        }else {
        	retryCount = getRetryCount(metrics);
        }
              
        VfsFigiRequestMessage enrichedRequest = vfsOpenFigiMessageGeneratorImpl.generateDefaultOpenFigiMessageFromRequestMetrics(metrics, requestTypeEnum, retryCount); 
        vfsMessageSender.sendMessage(enrichedRequest, Process.FigiRequest, MessagingConstant.PRODUCER_PARTITION_1);
    }

    /**
     * @param metrics 
     * @return
     */
    private int getRetryCount(OpenFigiRequestMetrics metrics) {
        Optional<String> successRetryLimit = systemCache.getPropertiesValue(VfsConstants.OPENFIGI_SUCCESS_RETRY_LIMIT, VfsSystemPropertiesConstant.COMPONENT_VFS, DataLevel.VFS_SYSTEM_PROPERTIES);
        try {
            return successRetryLimit.isPresent() && metrics.getRetriesAttempted().intValue()>=Integer.valueOf(successRetryLimit.get()) ? Integer.valueOf(successRetryLimit.get()) -1
                    :metrics.getRetriesAttempted().intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Property 'openfigiSuccessRetryLimit' specified. Value : " + successRetryLimit);
        }
    }
    
    /**
     * @param metrics
     * @return
     */
    private int getRetryCountForInactivation() {
        Optional<String> inactivationRetryLimit = systemCache.getPropertiesValue(VfsConstants.FIGI_FAILED_RETRY_INACTIVATION_LIMIT, VfsSystemPropertiesConstant.COMPONENT_VFS, DataLevel.VFS_SYSTEM_PROPERTIES);
        try {
			if (inactivationRetryLimit.isPresent()) {
				return Integer.valueOf(inactivationRetryLimit.get()) - 1;
			}
            return VfsConstants.DEFAULT_FIGI_FAILED_RETRY_INACTIVATION_LIMIT - 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Property 'openfigiSuccessRetryLimit' specified. Value : " + inactivationRetryLimit);
        }
    }

}
