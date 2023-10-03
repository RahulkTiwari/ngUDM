/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduRuleListenerFactoryTest.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules.listeners;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.event.process.listener.EventListenerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RduRuleListenerFactoryTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private BsonConverter bsonConverter;

	@Autowired
	private EventListenerFactory factory;


	@Test
	@ModifiedCollections(collections = { "sdData" })
	public void testIdcApex() throws Exception {
		List<DataContainer> dataContainers = bsonConverter
				.getListOfDataContainersFromFilePath("RduRuleListenerFactoryTest/input/sdData.json", SdData.class);

		List<RduRuleProcessListener> applicableListeners = factory.getApplicableListeners("idcApex",RduRuleEvent.ON_RULE_EXECUTION.name());

		DataContainer dataContainer = dataContainers.get(0);

		RduRuleListenerInput input = RduRuleListenerInput.builder().feedContainer(dataContainer).build();
		applicableListeners.forEach(lstnr -> lstnr.onRuleExecution(input));

		DataContainer secCon = dataContainer.getAllChildDataContainers().get(0);
		LocalDateTime localTime = secCon.getHighestPriorityValue(SecurityAttrConstant.SEC_LAST_PROCESSED_DATE);

		Assert.assertNotNull(localTime);

	}
}
