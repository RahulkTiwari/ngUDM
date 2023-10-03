/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ServiceDeskTicketingServiceImplTest.java
 * Author:	Divya Bharadwaj
 * Date:	26-Feb-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.api.servicedesk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.exceptions.UdmExceptionDataHolder;
import com.smartstreamrdu.service.exceptions.UdmExceptionDataHolder.Builder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class ServiceDeskTicketingServiceImplTest {
	@Autowired
	ServiceDeskTicketingService service;

	@Test
	public void testCreateIssue() throws UdmTechnicalException, InterruptedException, ExecutionException {
		Map<String, String> customFields = new HashMap<>();
		customFields.put("RDU Instrument ID", "RDUINS00125521");
		customFields.put("RDU Security ID", "RDUSEC00123123");

		Builder udmExceptionDataBuilder = UdmExceptionDataHolder.builder();
		udmExceptionDataBuilder.withCategory("Validation").withSubCategory("Value Mismatch")
				.withExceptionType("XRF Value Mismatch with SEDOL").withPriority("Low")
				.withStormCriteriaValue("RDUNSE000000000025|XRF Value Mismatch with SEDOL").withComponent("NG_XRF").withSummary("XRF Value Mismatch with SEDOL|2520159|trdse|2520158|bbgf")
				.withDescription("Value mismatch observed at security level in XRF for the given XRF Securities");
		UdmExceptionDataHolder data = udmExceptionDataBuilder.build();

		Future<BasicIssue> ticket = service.createIssue(data);
		String incidentId = ticket.get().getKey();
		Assert.assertNotNull(incidentId);
		service.closeIssue(incidentId);
	}

}