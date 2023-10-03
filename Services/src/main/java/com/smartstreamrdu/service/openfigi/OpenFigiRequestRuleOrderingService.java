/**
 * Copyright (c) 2009-2023 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : OpenFigiRequestRuleOrderingService.java
 * Author :SaJadhav
 * Date : 30-Jan-2023
 */
package com.smartstreamrdu.service.openfigi;

import java.util.Optional;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestOrderTypeEnum;
import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;

/**
 * This service gives the open figi rule based on the current rule and 
 * request order type
 * @author SaJadhav
 *
 */
public interface OpenFigiRequestRuleOrderingService {
	
	/**
	 * It returns the next request order based on input currentRequestRule and requestOrderType
	 * 
	 * @param currentRequestRule
	 * @param requestOrderType
	 * @return
	 */
	public Optional<OpenFigiRequestRuleEnum> getNextRequestRule(OpenFigiRequestRuleEnum currentRequestRule,
			OpenFigiRequestOrderTypeEnum requestOrderType);
	
	/**
	 * It returns the request order type for input MIC value.
	 * 
	 * @param mic
	 * @return
	 */
	public OpenFigiRequestOrderTypeEnum getRequestOrderTypeForMic(String mic);

}
