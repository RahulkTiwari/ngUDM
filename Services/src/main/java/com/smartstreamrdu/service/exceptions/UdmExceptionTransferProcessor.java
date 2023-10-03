/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionTransferProcessor.java
 * Author:	Rushikesh Dedhia
 * Date:	25-Feb-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.api.servicedesk.ServiceDeskTicketingServiceImpl;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.util.UdmExceptionConstant;

/**
 * This processor is responsible pushing the exceptions that occur in the UDM
 * system to the ticketing engine.
 * 
 * @author Dedhia
 *
 */
@Component
public class UdmExceptionTransferProcessor implements Processor {

	private static final Logger _logger = LoggerFactory.getLogger(UdmExceptionTransferProcessor.class);

	@Autowired
	ServiceDeskTicketingServiceImpl service;
	
	@Autowired
	private PersistenceService persistenceService;
	
	@Autowired
	private UdmExceptionDbMatchService dbMatchService;

	@Override
	public void process(Exchange exchange) throws Exception {

		if (exchange.getIn() == null) {
			return;
		}
		
		UdmExceptionDataHolder exceptionDataHolder = getUdmExceptionDataHolderFromExchange(exchange);
		
		if (exceptionDataHolder == null) {
			return;
		}

		String status = exceptionDataHolder.getStatus();
		//The below logic is implemented for deDuplication of Exception
		Optional<DataContainer> dbContainerOptional = getExistingDbExceptionDataContainer(exceptionDataHolder);
		
		//It means exception is already raised in ServiceDesk so will not raise duplicate Exception.
		if(dbContainerOptional.isPresent()) {
			_logger.debug("Exception is already raised in service desk for following Criteria value :{}.",exceptionDataHolder.getStormCriteriaValue());
		}	
		
		if (UdmExceptionConstant.EXCEPTION_STATUS_ACTIVE.equals(status) && !dbContainerOptional.isPresent()) {
			raiseIssueInServiceDeskAndPersist(exceptionDataHolder);
		}
		
		else if (UdmExceptionConstant.EXCEPTION_STATUS_INACTIVE.equals(status) && dbContainerOptional.isPresent()) {
			inactivateExistingExceptionInServiceDeskAndPersist(exceptionDataHolder, dbContainerOptional.get());
		}
		
		
	}

	private void inactivateExistingExceptionInServiceDeskAndPersist(UdmExceptionDataHolder exceptionDataHolder, DataContainer dataContainer) throws UdmTechnicalException {
		Objects.requireNonNull(exceptionDataHolder);
		
		DataValue<String> statusValue = new DataValue<>();
		statusValue.setValue(LockLevel.RDU, exceptionDataHolder.getStatus());
		
		DataValue<String> updBy = new DataValue<>();
		updBy.setValue(LockLevel.RDU, exceptionDataHolder.getUpdateBy());
		
		dataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_STATUS_ATTR, DataLevel.UDM_EXCEPTION_DATA), statusValue);
		dataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_UPD_USER_ATTR, DataLevel.UDM_EXCEPTION_DATA), updBy);
		
		
		String incidentId = getIncidentIdForException(dataContainer);

		try {
			service.closeIssue(incidentId);
			_logger.info("Following issue is Closed in Service Desk :{} ",incidentId);		
		}
		catch(Exception e) {
			_logger.error("Exception occured while closing issue in Service Desk :{} for incident id : {} ",e,incidentId);
			throw new UdmTechnicalException("Exception occured while closing issue in Service Desk :{} for incident id :" + incidentId, e);
		}
		persistenceService.persist(dataContainer);
		
	}

	@SuppressWarnings("unchecked")
	private String getIncidentIdForException(DataContainer dataContainer) {
		Objects.requireNonNull(dataContainer);
		
		DataAttribute incidentIdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_INCIDENT_ID_ATTR, DataLevel.UDM_EXCEPTION_DATA);
		
		DataValue<String> incidentIdDataValaue = (DataValue<String>) dataContainer.getAttributeValue(incidentIdDataAttribute);
		
		return incidentIdDataValaue.getValue();
	}

	private UdmExceptionDataHolder getUdmExceptionDataHolderFromExchange(Exchange exchange) {
		Message message = exchange.getIn();
		Object data = message.getBody();
		return JsonConverterUtil.convertFromJson(data.toString(),UdmExceptionDataHolder.class);
	}

	private void raiseIssueInServiceDeskAndPersist(UdmExceptionDataHolder exceptionDataHolder) throws UdmTechnicalException, InterruptedException, ExecutionException {
		
		Future<BasicIssue> incidentId = service.createIssue(exceptionDataHolder);
		exceptionDataHolder.setIncidentId(incidentId.get().getKey());
		persistUdmExceptionData(exceptionDataHolder);
		
		_logger.info("Issue is raised in service desk with following incident id : {}.", incidentId.get().getKey());
		
	}


	/**
	 * This method is used to check if Exception is already raised in ServiceDesk using UdmExceptionData Table.If Entry
	 * is already exists in table we will not generate Duplicate ServiceDeskTicket For Same Exception.
	 * @param exceptionDataHolder
	 * @throws UdmTechnicalException 
	 */
	private Optional<DataContainer> getExistingDbExceptionDataContainer(UdmExceptionDataHolder exceptionDataHolder) throws UdmTechnicalException {	 
		
		 return dbMatchService.getExceptionDataContainer(exceptionDataHolder);
		 
	}

	/**
	 *  This method will invoke conversion of UdmExceptionDataHolder object to DataContainer and also invoke persistence.
	 * @param exceptionDataHolder
	 */
	private void persistUdmExceptionData(UdmExceptionDataHolder exceptionDataHolder) {
		
		if (exceptionDataHolder == null || exceptionDataHolder.getIncidentId() == null) {
			throw new IllegalStateException("Either the UdmExceptionDataHolder object or the incidentId was null.");
		}
		// Invoke conversion of UdmExceptionDataHolder to DataContainer
		DataContainer exceptionObjectDataContainer = convertUdmExceptionDataHolderToDataContainer(exceptionDataHolder);
		// Invoke persistence.
		persist(exceptionObjectDataContainer);
	}

	/**
	 *  This method uses the persistence service to persist the supplied UdmExceptionData container in the database.
	 * @param exceptionObjectDataContainer
	 */
	private void persist(DataContainer exceptionObjectDataContainer) {
		persistenceService.persist(exceptionObjectDataContainer);
	}

	/**
	 *  This method will convert the given UdmExceptionDataHolder object into its DataContainer equivalent.
	 * @param exceptionDataHolder
	 * @return
	 */
	private DataContainer convertUdmExceptionDataHolderToDataContainer(UdmExceptionDataHolder exceptionDataHolder) {
		
		if (exceptionDataHolder == null) {
			return null;
		}
		
		DataContainer exceptionObjectDataContainer = new DataContainer(DataLevel.UDM_EXCEPTION_DATA, null);
		
		try {
			DataValue<String> exceptionTypeDataValue = new DataValue<>();
			exceptionTypeDataValue.setValue(LockLevel.RDU, exceptionDataHolder.getExceptionType());
			
			DataValue<String> exceptionStatusDataValue = new DataValue<>();
			exceptionStatusDataValue.setValue(LockLevel.RDU, exceptionDataHolder.getStatus());
			
			DataValue<String> criteriaValueDataValue = new DataValue<>();
			criteriaValueDataValue.setValue(LockLevel.RDU, exceptionDataHolder.getStormCriteriaValue());
			
			DataValue<String> originatedFromValue = new DataValue<>();
			originatedFromValue.setValue(LockLevel.RDU, "NG_XRF");
			
			DataValue<String> incidentIdFromValue = new DataValue<>();
			incidentIdFromValue.setValue(LockLevel.RDU, exceptionDataHolder.getIncidentId());
			
			DataValue<LocalDateTime> insDateValue = new DataValue<>();
			insDateValue.setValue(LockLevel.RDU, LocalDateTime.now());
			
			DataValue<String> upUser = new DataValue<>();
			upUser.setValue(LockLevel.RDU, exceptionDataHolder.getUpdateBy());
			
			exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_TYPE_ATTR, DataLevel.UDM_EXCEPTION_DATA), exceptionTypeDataValue);
			exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_STATUS_ATTR, DataLevel.UDM_EXCEPTION_DATA), exceptionStatusDataValue);
			exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_STORM_CRITERIA_VALUE_ATTR, DataLevel.UDM_EXCEPTION_DATA), criteriaValueDataValue);
			exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_ORIGINATED_FROM_ATTR, DataLevel.UDM_EXCEPTION_DATA), originatedFromValue);
			exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_INS_DATE_ATTR, DataLevel.UDM_EXCEPTION_DATA), insDateValue);
			exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_INCIDENT_ID_ATTR, DataLevel.UDM_EXCEPTION_DATA), incidentIdFromValue);
			exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_UPD_USER_ATTR, DataLevel.UDM_EXCEPTION_DATA), upUser);

		} catch (Exception e) {
			_logger.error("Following error occured : ", e);
			throw e;
		}
		return exceptionObjectDataContainer;
	}
}