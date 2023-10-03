/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MatchingGroupMicLookupService.java
 * Author:	Divya Bharadwaj
 * Date:	19-Oct-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.persistence.cache.repository.ExchangeRepository;

/**
 * @author Bharadwaj
 *
 */
@Component
public class MatchingGroupMicLookupServiceImpl implements MatchingGroupMicLookupService{
	
	@Autowired
	private ExchangeRepository exchangeRepository;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.openfigi.MatchingGroupMicLookupService#fetchFallbackMics(java.lang.String)
	 */
	@Override
	public List<String> fetchFallbackMics(String mic) throws Exception {
		List<String> result = null;
		if(isMicOperatingMic(mic)){
			result = exchangeRepository.getMicsByMatchingGroupMic(mic);
		} else {
			String resultMic = exchangeRepository.getMatchingGroupExchangeCodeByCode(mic);
			if (resultMic != null) {
				result = Arrays.asList(resultMic);
			}
		}
		if(!CollectionUtils.isEmpty(result)){
			Collections.sort(result);
		}
		return result;
	}

	/**
	 * @param mic
	 * @return
	 * @throws Exception 
	 */
	private boolean isMicOperatingMic(String mic) throws Exception {
		String operatingMic = exchangeRepository.getOpertingMicByCode(mic);
		return mic.equals(operatingMic);
	}
	
	
}
