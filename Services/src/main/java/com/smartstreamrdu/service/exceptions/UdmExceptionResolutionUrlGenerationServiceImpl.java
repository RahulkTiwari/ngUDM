/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionResolutionUrlBuilder.java
 * Author:	Divya Bharadwaj
 * Date:	07-May-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.exceptions;

import static com.smartstreamrdu.util.UdmExceptionConstant.CRITERIA_VALUE;
import static com.smartstreamrdu.util.UdmExceptionConstant.EXCEPTION_TYPE_ATTR;
import static com.smartstreamrdu.util.UdmSystemPropertiesConstant.UUI_HOST_NAME;
import static com.smartstreamrdu.util.UdmSystemPropertiesConstant.UUI_PORT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmConfigurationException;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.util.UdmExceptionConstant;

/**
 * @author Bharadwaj
 *
 */
@Component
public class UdmExceptionResolutionUrlGenerationServiceImpl implements UdmExceptionResolutionUrlGenerationService{

	/**
	 * 
	 */
	private static final String HTTP = "http";
	@Autowired
	private UdmSystemPropertiesCache systemCache;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.exceptions.UdmExceptionResolutionUrlGenerationService#populateResolutionUrl(java.lang.String, java.lang.String)
	 */
	@Override
	public String populateResolutionUrl(String stormCriteriaValue, String exceptionType) throws URISyntaxException {
		String hostName = getValueFromConfig(UUI_HOST_NAME);
		String port = getValueFromConfig(UUI_PORT);

		URIBuilder builder = new URIBuilder();
		builder.setScheme(HTTP);
		builder.setHost(hostName);
		builder.setPort(Integer.parseInt(port));
		builder.addParameter(EXCEPTION_TYPE_ATTR, exceptionType);
		builder.addParameter(CRITERIA_VALUE, stormCriteriaValue);
		URI uri = builder.build();
		
		return uri.toString().replaceAll(UdmExceptionConstant.PLUS_SPLITTER, UdmExceptionConstant.PLUS_ENCODED);
	}
	
	protected String getValueFromConfig(String propName){
		Optional<String> val = systemCache.getPropertiesValue(
				propName, DataLevel.UDM_SYSTEM_PROPERTIES);
		return val.orElseThrow(() -> new UdmConfigurationException("Configuration missing for required property: " + propName));
	}

}
