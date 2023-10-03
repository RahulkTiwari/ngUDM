/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InsIdNotFoundFilteredInactivationListenerTest.java
 * Author:	Padgaonkar S
 * Date:	10-Jan-2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.event.listener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.service.event.process.listener.EventListenerFactory;
import com.smartstreamrdu.util.Constant.DomainStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class InsIdNotFoundFilteredInactivationListenerTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private DataRetrievalService retrieve;

	@Autowired
	private EventListenerFactory factory;
	
	@Autowired
	ReloadIgniteCache reloadCache;
	
	@Value("${mongoDBUri}")
	private String mongoDbUri;
	
	@Before
	public  void initialize(){
	 System.setProperty("spark.mongodb.input.uri", mongoDbUri + ".sdData");
		
	}

	@Test
	@InputCollectionsPath(paths = {"DenListenerFactoryTest/testIdcApex/sdData.json"})
	@ModifiedCollections(collections = {"sdData"})
	public void testIdcApex() throws Exception {
		String cacheName = DataLevel.DV_DOMAIN_MAP.getCollectionName();
		reloadCache.reloadCache(cacheName);

		List<DenProcessListener> applicableListeners = factory.getApplicableListeners("idcApex","FilteredContainerInactivation");
		
		Map<String, Serializable> eventAttributes = new HashMap<String, Serializable>();
		eventAttributes.put(SecurityAttrConstant.COL_SECURITY_SOURCE_UNIQUE_ID,"81958185");
		eventAttributes.put("dataSource","idcApex");
		
		EventMessage msg = new EventMessage();
		msg.setEventName("FilteredContainerInactivation");
		msg.setEventAttributes(eventAttributes);
		
		applicableListeners.forEach(listn -> listn.onDenEventReceived(msg));

		DataContainer con = retrieveDbContainer();
		DataContainer secCon = con.getAllChildDataContainers().get(0);
		DomainType status = secCon.getHighestPriorityValue(SecurityAttrConstant.SECURITY_STATUS);

		Assert.assertNotNull("Inactivation not set by listener", status.getNormalizedValue());
		Assert.assertEquals(DomainStatus.INACTIVE, status.getNormalizedValue());
	}

	
	private DataContainer retrieveDbContainer() throws UdmTechnicalException {
		DataValue<String> val = new DataValue<String>();
		val.setValue(LockLevel.FEED, "81951192");
		Criteria cri = Criteria.where(InstrumentAttrConstant.INSTRUMENT_SOURCE_UNIQUE_ID).is(val);
		
		DataRetrivalInput input = new DataRetrivalInput();
		input.setCriteria(cri);
		
		List<DataContainer> cons = retrieve.retrieve(Database.Mongodb, input);

		return cons.get(0);
	}
}
