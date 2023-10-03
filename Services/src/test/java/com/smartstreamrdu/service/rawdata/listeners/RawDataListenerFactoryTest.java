/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataListenerFactoryTest.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata.listeners;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdRawData;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.service.event.process.listener.EventListenerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RawDataListenerFactoryTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private BsonConverter bsonConverter;

	@Autowired
	private EventListenerFactory factory;
	
	@Autowired
	private DataRetrievalService retrieve;

	@Test
	@InputCollectionsPath(paths = { "RawDataListenerFactoryTest/testIdcApex/sdData.json" })
	@ModifiedCollections(collections = { "sdData" })
	public void testIdcApex() throws Exception {
		List<DataContainer> dataContainers = bsonConverter.getListOfDataContainersFromFilePath(
				"RawDataListenerFactoryTest/testIdcApex/rawData.json", SdRawData.class);

		List<RawDataProcessListener> applicableListeners = factory.getApplicableListeners("idcApex",RawDataEvent.ON_RAW_RECORD_FILTERD.name());

		DataContainer dataContainer = dataContainers.get(0);
		dataContainer.set_id("618121620d08ce70f411c369");

		RawDataListenerInput input = RawDataListenerInput.builder().dataSource("idcApex").dbDataContainer(dataContainer)
				.filePath("abc_init_apex.xml").build();
		applicableListeners.forEach(lstnr -> lstnr.onRawRecordFiltered(input));

		DataContainer con = retrieveDbContainer(dataContainer);
		
		DataContainer secCon = con.getAllChildDataContainers().get(0);
		LocalDateTime localTime = secCon.getHighestPriorityValue(SecurityAttrConstant.SEC_LAST_PROCESSED_DATE);

		Assert.assertNotNull(localTime);

	}

	private DataContainer retrieveDbContainer(DataContainer dbContainer) throws UdmTechnicalException {
		DataValue<String> val = new DataValue<String>();
		val.setValue(LockLevel.FEED, "81951192");
		Criteria cri = Criteria.where(InstrumentAttrConstant.INSTRUMENT_SOURCE_UNIQUE_ID).is(val);
		
		DataRetrivalInput input = new DataRetrivalInput();
		input.setCriteria(cri);
		
		List<DataContainer> cons = retrieve.retrieve(Database.Mongodb, input);

		return cons.get(0);
	}
}
