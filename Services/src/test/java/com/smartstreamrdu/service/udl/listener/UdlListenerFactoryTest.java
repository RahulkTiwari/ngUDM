/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdlListenerFactoryTest.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.udl.listener;

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
public class UdlListenerFactoryTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private BsonConverter bsonConverter;

	@Autowired
	private EventListenerFactory factory;

	@Test
	public void testIdcApex() throws Exception {
		List<DataContainer> dataContainers = bsonConverter
				.getListOfDataContainersFromFilePath("UdlListenerFactoryTest/input/sdData.json", SdData.class);

		List<UdlMergeListener> applicableListeners = factory.getApplicableListeners("idcApex",UdlMergeEvent.POST_MERGING_CONTAINER.name());

		DataContainer dataContainer = dataContainers.get(0);

		UdlProcessListenerInput input = UdlProcessListenerInput.builder().container(dataContainer).build();

		applicableListeners.forEach(lstnr -> lstnr.postMergingContainer(input));

		DataContainer secCon = dataContainer.getAllChildDataContainers().get(0);
		LocalDateTime localTime = secCon.getHighestPriorityValue(SecurityAttrConstant.SEC_LAST_PROCESSED_DATE);

		Assert.assertNotNull(localTime);

	}
}
