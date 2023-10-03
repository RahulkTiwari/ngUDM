/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduJiraClient.java
 * Author:	Divya Bharadwaj
 * Date:	26-Feb-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.api.servicedesk;

import static com.smartstreamrdu.util.UdmSystemPropertiesConstant.SERVICE_DESK_PROJECT_KEY;
import static com.smartstreamrdu.util.UdmSystemPropertiesConstant.SERVICE_DESK_PWORD;
import static com.smartstreamrdu.util.UdmSystemPropertiesConstant.SERVICE_DESK_URL;
import static com.smartstreamrdu.util.UdmSystemPropertiesConstant.SERVICE_DESK_USERNAME;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmConfigurationException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;

/**
 * This class is used to get rest client connection to service desk
 *
 */
@Component
public class RduJiraClient {

	private String jiraUrl;

	private JiraRestClient restClient;

	private String projectKey;

	public String getProjectKey() {
		return projectKey;
	}

	@Autowired
	private UdmSystemPropertiesCache systemCache;

	public void initializeRestClient() throws UdmConfigurationException {
		String serviceDeskPassword = getValueFromConfig(SERVICE_DESK_PWORD);
		String serviceDeskUsername = getValueFromConfig(SERVICE_DESK_USERNAME);
		projectKey = getValueFromConfig(SERVICE_DESK_PROJECT_KEY);
		jiraUrl = getValueFromConfig(SERVICE_DESK_URL);

		this.restClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(getJiraUri(),
				serviceDeskUsername, serviceDeskPassword);
	}

	protected String getValueFromConfig(String propName) throws UdmConfigurationException {
		Optional<String> val = systemCache.getPropertiesValue(
				propName, DataLevel.UDM_SYSTEM_PROPERTIES);
		return val.orElseThrow(() -> new UdmConfigurationException("Configuration missing for required property: " + propName));
	}


	private URI getJiraUri() {
		return URI.create(this.jiraUrl);
	}

	/**
	 * @return
	 * @throws UdmTechnicalException
	 */
	public JiraRestClient getJiraRestClient() throws UdmConfigurationException {

		if (restClient == null) {
			initializeRestClient();
		}
		return restClient;
	}
}