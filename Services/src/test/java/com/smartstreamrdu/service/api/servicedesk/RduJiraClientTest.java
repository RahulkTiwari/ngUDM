/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: RduJiraClientTest.java
 * Author: Padgaonkar
 * Date: April 25, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.api.servicedesk;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RduJiraClientTest {
	
	@Autowired
	RduJiraClient jiraClient;

	@Test
	public void testJiraClient() throws URISyntaxException {

		JiraRestClient jiraRestClient = jiraClient.getJiraRestClient();

		Iterator<BasicProject> projectIterator = jiraRestClient.getProjectClient().getAllProjects().claim().iterator();

		while (projectIterator.hasNext()) {
			BasicProject basicEquityProject = projectIterator.next();

			String projectName = basicEquityProject.getName();

			if (projectName.equals("Equity")) {

				String key = basicEquityProject.getKey();
				URI uri = basicEquityProject.getSelf();
				Long projectId = basicEquityProject.getId();

				Assert.assertEquals("EQ", key);
				Assert.assertEquals(Long.valueOf("10010"), projectId);
				Assert.assertEquals(new URI("https://smartstreamrdu.atlassian.net/rest/api/2/project/10010"), uri);

			}
		}

	}
}
