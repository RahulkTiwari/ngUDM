/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionDbMatchServiceImpl.java
 * Author:	Padgaonkar
 * Date:	17-April-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.util.UdmExceptionConstant;

@Component
public class UdmExceptionDbMatchServiceImpl implements UdmExceptionDbMatchService{
	
	@Autowired
	DataRetrievalService retrivalService;

	private static final DataAttribute stormCriteriaDataAttribute=DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_STORM_CRITERIA_VALUE_ATTR, DataLevel.UDM_EXCEPTION_DATA);
	private static final DataAttribute exceptionStatus =DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_STATUS_ATTR, DataLevel.UDM_EXCEPTION_DATA);
	private static final DataAttribute exceptionTypeAttribute=DataAttributeFactory.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_TYPE_ATTR, DataLevel.UDM_EXCEPTION_DATA);

	
	private static final Logger _logger = LoggerFactory.getLogger(UdmExceptionDbMatchServiceImpl.class);


	@Override
	public Optional<DataContainer> getExceptionDataContainer(UdmExceptionDataHolder udmExceptionDataHolder) throws UdmTechnicalException {
		
		Objects.requireNonNull(udmExceptionDataHolder);
		String stormCriteriaValue = udmExceptionDataHolder.getStormCriteriaValue();
		Objects.requireNonNull(stormCriteriaValue);
		
		String exceptionType = udmExceptionDataHolder.getExceptionType();
		Objects.requireNonNull(exceptionType);


		DataContainer container = null;
		List<DataContainer> exceptionDataHolersList = getDbDataContainerList(stormCriteriaValue,exceptionType);
	
		if(!exceptionDataHolersList.isEmpty()) {	
			container= validateDbList(exceptionDataHolersList,stormCriteriaValue);
		}
		return Optional.ofNullable(container);
	}
	
	public List<DataContainer> getDbDataContainerList(String stormCriteriaValue, String exceptionType) throws UdmTechnicalException {

		DataValue<String> dbCriteriaValue = new DataValue<>();
		dbCriteriaValue.setValue(LockLevel.RDU, stormCriteriaValue);
		
		DataValue<String> exceptionTypeValue = new DataValue<>();
		exceptionTypeValue.setValue(LockLevel.RDU, exceptionType);
		
		DataValue<String> status = new DataValue<>();
		status.setValue(LockLevel.RDU, UdmExceptionConstant.STATUS_ACTIVE);
		
		Criteria exceptionTypeCriteria = Criteria.where(exceptionTypeAttribute).is(exceptionTypeValue);
		Criteria statusCriteria = Criteria.where(exceptionStatus).is(status);
	
		Criteria criteria = Criteria.where(stormCriteriaDataAttribute).is(dbCriteriaValue);
		Criteria finalCriteria = criteria.andOperator(exceptionTypeCriteria,statusCriteria);
		
		 try {
			 DataRetrivalInput input=new DataRetrivalInput();
			 input.setCriteria(finalCriteria);
			return retrivalService.retrieve(Database.Mongodb,input);
		} catch (UdmTechnicalException e) {
			_logger.error("Exception occured while retriving container from databse for following criteriaKeyValue: {}", stormCriteriaValue);
			throw new UdmTechnicalException("Exception occured while retriving container from databse for following criteriaKeyValue: {}", e);
		}
	}

	private DataContainer validateDbList(List<DataContainer> exceptionDataContainerList, String stormCriteriaValue)
			throws UdmTechnicalException {

		if (exceptionDataContainerList.size() > 1) {
			_logger.error("Muliple Containers are retrived from databse for following criteriaKeyValue:{} ",stormCriteriaValue);
			throw new UdmTechnicalException("Muliple Containers are retrived from databse for following criteriaKeyValue: "+stormCriteriaValue, null);
		}	
		return exceptionDataContainerList.get(0);	

	}

}
