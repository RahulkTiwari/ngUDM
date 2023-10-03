/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionDbMatchServiceTest.java
 * Author:	Padgaonkar
 * Date:	17-April-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.persistence.service.PersistenceEntityRepository;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.exceptions.UdmExceptionDataHolder.Builder;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.util.UdmExceptionConstant;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class UdmExceptionTransferProcessorTest extends AbstractEmbeddedMongodbJunitParent{

	private static final DataAttribute stormCriteriaDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(
			UdmExceptionConstant.EXCEPTION_STORM_CRITERIA_VALUE_ATTR, DataLevel.UDM_EXCEPTION_DATA);
	private static final DataAttribute exceptionStatus = DataAttributeFactory
			.getAttributeByNameAndLevel(UdmExceptionConstant.EXCEPTION_STATUS_ATTR, DataLevel.UDM_EXCEPTION_DATA);

	@Autowired
	private PersistenceService service;

	@Autowired
	private UdmExceptionTransferProcessor exceptionProcessor;

	@Autowired
	DataRetrievalService retrivalService;

	@Before
	public void init() {

		DataContainer udmExceptionData = getUdmExceptionData();
		service.persist(udmExceptionData);
	}


	@Test
	@ModifiedCollections(collections = { "udmExceptionData" })
	public void TestDbMatchServiceWithExisting() throws Exception {

		Builder udmExceptionDataHolderBuilder = UdmExceptionDataHolder.builder();
		String key = "RDUNIN000000000009|2520195+XNAS+Default|Source filter - Security";
		udmExceptionDataHolderBuilder.withStormCriteriaValue(key);
		udmExceptionDataHolderBuilder.withComponent("NG_XRF");
		udmExceptionDataHolderBuilder.withExceptionType("Source filter - Security");
		UdmExceptionDataHolder dataHolder = udmExceptionDataHolderBuilder.build();

		String jsonDataHolder = JsonConverterUtil.convertToJson(dataHolder);

		CamelContext camelContext = new DefaultCamelContext();
		Exchange exchange = new DefaultExchange(camelContext);
		exchange.getIn().setBody(jsonDataHolder);

		exceptionProcessor.process(exchange);
		List<DataContainer> exceptionDataContainer = getDataContainers("RDUNIN000000000009|2520195+XNAS+Default|Source filter - Security");
		validateDbContainer(exceptionDataContainer.get(0),"RDUNIN000000000009|2520195+XNAS+Default|Source filter - Security");

		// raising same Exception twice to ensure it will not create Duplicate.
		exceptionProcessor.process(exchange);
		List<DataContainer> exceptionDataContainer1 = getDataContainers(
				"RDUNIN000000000009|2520195+XNAS+Default|Source filter - Security");
		validateDbContainer(exceptionDataContainer1.get(0),"RDUNIN000000000009|2520195+XNAS+Default|Source filter - Security");

	}

	public List<DataContainer> getDataContainers(String stormCriteriaValue) throws UdmTechnicalException {

		DataValue<String> dbCriteriaValue = new DataValue<>();
		dbCriteriaValue.setValue(LockLevel.RDU, stormCriteriaValue);

		DataValue<String> status = new DataValue<>();
		status.setValue(LockLevel.RDU, UdmExceptionConstant.STATUS_ACTIVE);

		Criteria criteria = Criteria.where(stormCriteriaDataAttribute).is(dbCriteriaValue);
		Criteria finalCriteria = criteria.andOperator(Criteria.where(exceptionStatus).is(status));

		return retrivalService.retrieve(Database.Mongodb, finalCriteria, false, null);
	}

	private void validateDbContainer(DataContainer exceptionDataContainer,String criteriaValue) {

		Assert.assertNotNull(exceptionDataContainer);

		DataValue<String> criteriaValueDataValue = new DataValue<>();
		criteriaValueDataValue.setValue(LockLevel.RDU,criteriaValue);

		Serializable attributeValue = exceptionDataContainer.getAttributeValue(stormCriteriaDataAttribute);
		Assert.assertEquals(criteriaValueDataValue, attributeValue);
	}

	public DataContainer getUdmExceptionData() {
		DataContainer exceptionObjectDataContainer = new DataContainer(DataLevel.UDM_EXCEPTION_DATA, null);

		DataValue<String> exceptionTypeDataValue = new DataValue<>();
		exceptionTypeDataValue.setValue(LockLevel.RDU, "Source filter - Security");

		DataValue<String> exceptionStatusDataValue = new DataValue<>();
		exceptionStatusDataValue.setValue(LockLevel.RDU, "A");

		DataValue<String> criteriaValueDataValue = new DataValue<>();
		criteriaValueDataValue.setValue(LockLevel.RDU,
				"RDUNIN000000000009|2520195+XNAS+Default|Source filter - Security");

		DataValue<String> originatedFromValue = new DataValue<>();
		originatedFromValue.setValue(LockLevel.RDU, "NG_XRF");

		DataValue<String> incidentIdFromValue = new DataValue<>();
		incidentIdFromValue.setValue(LockLevel.RDU, "EQ-243");

		DataValue<LocalDateTime> insDateValue = new DataValue<>();
		insDateValue.setValue(LockLevel.RDU, LocalDateTime.now());

		exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(
				UdmExceptionConstant.EXCEPTION_TYPE_ATTR, DataLevel.UDM_EXCEPTION_DATA), exceptionTypeDataValue);
		exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(
				UdmExceptionConstant.EXCEPTION_STATUS_ATTR, DataLevel.UDM_EXCEPTION_DATA), exceptionStatusDataValue);
		exceptionObjectDataContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(
						UdmExceptionConstant.EXCEPTION_STORM_CRITERIA_VALUE_ATTR, DataLevel.UDM_EXCEPTION_DATA),
				criteriaValueDataValue);
		exceptionObjectDataContainer
				.addAttributeValue(
						DataAttributeFactory.getAttributeByNameAndLevel(
								UdmExceptionConstant.EXCEPTION_ORIGINATED_FROM_ATTR, DataLevel.UDM_EXCEPTION_DATA),
						originatedFromValue);
		exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(
				UdmExceptionConstant.EXCEPTION_INS_DATE_ATTR, DataLevel.UDM_EXCEPTION_DATA), insDateValue);
		exceptionObjectDataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(
				UdmExceptionConstant.EXCEPTION_INCIDENT_ID_ATTR, DataLevel.UDM_EXCEPTION_DATA), incidentIdFromValue);

		return exceptionObjectDataContainer;
	}

}
