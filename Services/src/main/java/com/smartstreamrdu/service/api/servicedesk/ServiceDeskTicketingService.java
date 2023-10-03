/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ServiceDeskTicketingService.java
 * Author:	Divya Bharadwaj
 * Date:	27-Feb-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.api.servicedesk;

import java.util.concurrent.Future;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.exceptions.UdmExceptionDataHolder;

/**
 * @author Bharadwaj
 *
 */
public interface ServiceDeskTicketingService {
	 /**
	 * @param data
	 * @return
	 * @throws UdmTechnicalException
	 * 
	 * This method uses service desk jira client to raise issue and returns the incident id
	 */
	Future<BasicIssue> createIssue(UdmExceptionDataHolder data) throws UdmTechnicalException;
	
	Issue getIssue(String incidentId);
	
	void closeIssue(String incidentId);
}