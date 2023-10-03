/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ServiceDeskTicketingServiceImpl.java
 * Author:	Divya Bharadwaj
 * Date:	26-Feb-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.api.servicedesk;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.exceptions.UdmExceptionDataHolder;
import com.smartstreamrdu.util.ServiceDeskConstant;
import io.atlassian.util.concurrent.Promise;

/**
 * @author Bharadwaj
 *
 */
@Component
public class ServiceDeskTicketingServiceImpl implements ServiceDeskTicketingService {

	private static final Logger _logger = LoggerFactory.getLogger(ServiceDeskTicketingServiceImpl.class);

	@Autowired
	private RduJiraClient jiraClient;

	private Iterable<CimProject> metadata;
	
	private Iterable<Priority> priorityMetadata;

	public Iterable<Priority> getPriorityMetadata() {
		if(priorityMetadata ==  null){
			priorityMetadata = jiraClient.getJiraRestClient().getMetadataClient().getPriorities().claim();
		}
		return priorityMetadata;
	}

	public Iterable<CimProject> getMetadata() {
		if(metadata == null){
			GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder()
					.withProjectKeys(jiraClient.getProjectKey()).withExpandedIssueTypesFields().build();
			metadata = jiraClient.getJiraRestClient().getIssueClient().getCreateIssueMetadata(options).claim();
		}
		return metadata;
	}

	@Override
	public Future<BasicIssue> createIssue(UdmExceptionDataHolder data) throws UdmTechnicalException {
		_logger.info("Creating issue in service desk for udm exception {}",data);
		try {
			IssueRestClient issueClient = jiraClient.getJiraRestClient().getIssueClient();
			Optional<Long> issueTypeId = getIssueTypeId(ServiceDeskConstant.ISSUETYPE_VALIDATION);
			if (issueTypeId.isPresent()) {
				IssueInputBuilder builder = new IssueInputBuilder(jiraClient.getProjectKey(), issueTypeId.get());
				setDefaultFieldValues(builder, data);
				setCustomFieldValues(builder, data);

				IssueInput newIssue = builder.build();
				return issueClient.createIssue(newIssue);

			}
		} catch (Exception e) {
			_logger.error("Error while creating issue in service desk {}",e);
			throw new UdmTechnicalException("Exception while creating issue in service desk", e);
		}
		return null;
	}

	/**
	 * @param builder
	 * @param data
	 * @throws UdmTechnicalException 
	 */
	private void setDefaultFieldValues(IssueInputBuilder builder, UdmExceptionDataHolder data) throws UdmTechnicalException {
		try{
			builder.setFieldValue(getFieldId(ServiceDeskConstant.DESCRIPTION_FOR_EQUITY), data.getDescription())
			.setPriorityId(getPriorityId(data.getPriority()))
			.setFieldValue(getFieldId(ServiceDeskConstant.EXCEPTION_TYPE), data.getExceptionType())
			.setFieldValue(getFieldId(ServiceDeskConstant.CATEGORY_FOR_EQUITY),
					ComplexIssueInputFieldValue.with(ServiceDeskConstant.VALUE, data.getCategory()))
			.setFieldValue(getFieldId(ServiceDeskConstant.SUB_CATEGORY),
					ComplexIssueInputFieldValue.with(ServiceDeskConstant.VALUE, data.getSubCategory()))
			.setFieldValue(getFieldId(ServiceDeskConstant.SUMMARY),data.getSummary());
		}catch(Exception e){
			throw new UdmTechnicalException("Unable to build IssueInput for service desk",e);
		}
	}

	/**
	 * @param builder
	 * @param data
	 * @throws UdmTechnicalException 
	 */
	private void setCustomFieldValues(IssueInputBuilder builder, UdmExceptionDataHolder data) throws UdmTechnicalException {
		
		builder.setFieldValue(getFieldId(ServiceDeskConstant.COMPONENT),
				ComplexIssueInputFieldValue.with(ServiceDeskConstant.VALUE, data.getComponent()));
		try{
			Map<String, String> customFieldValues = data.getCustomFieldValues();
			if(customFieldValues!= null && !customFieldValues.isEmpty()){
				customFieldValues.forEach((fieldName,fieldValue) -> builder.setFieldValue(getFieldId(fieldName), fieldValue));
			}
		}catch(Exception e){
			throw new UdmTechnicalException("Unable to build IssueInput for service desk",e);
		}
	}

	@Override
	public void closeIssue(String incidentId) {
		Promise<Iterable<Transition>> ptransitions = jiraClient.getJiraRestClient().getIssueClient()
				.getTransitions(getIssue(incidentId));
		Iterable<Transition> transitions = ptransitions.claim();
		int closeId = 0;
		for (Transition t : transitions) {
			if (t.getName().equals("Close Issue")) {
				closeId = t.getId();
				break;
			}
		}
		IssueRestClient issueClient = jiraClient.getJiraRestClient().getIssueClient();
		TransitionInput tinput = new TransitionInput(closeId);

		issueClient.transition(getIssue(incidentId), tinput).claim();
	}

	@Override
	public Issue getIssue(String incidentId) {
		return jiraClient.getJiraRestClient().getIssueClient().getIssue(incidentId).claim();
	}

	private String getFieldId(String fieldName) {
		CimProject cim = getMetadata().iterator().next();
		for (CimFieldInfo info : cim.getIssueTypes().iterator().next().getFields().values()) {
			if (info.getName().equals(fieldName)) {
				return info.getId();
			}
		}
		return StringUtils.EMPTY;
	}

	private Optional<Long> getIssueTypeId(String issueType) {
		CimProject cim = getMetadata().iterator().next();
		for (CimIssueType info : cim.getIssueTypes()) {
			if (info.getName().equals(issueType)) {
				return Optional.of(info.getId());
			}
		}
		return Optional.empty();
	}

	private Long getPriorityId(String priorityName) {
		for (Priority priority : getPriorityMetadata()) {
			if (priority.getName().equals(priorityName)) {
				return priority.getId();
			}
		}
		return Long.MAX_VALUE;
	}
}
