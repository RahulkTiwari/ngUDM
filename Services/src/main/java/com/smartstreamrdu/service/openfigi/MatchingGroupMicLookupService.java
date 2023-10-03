/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MatchingGroupMicLookupService.java
 * Author:	Divya Bharadwaj
 * Date:	22-Oct-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import java.util.List;

/**
 * @author Bharadwaj
 *
 */
public interface MatchingGroupMicLookupService {
	List<String> fetchFallbackMics(String mic) throws Exception;
}
