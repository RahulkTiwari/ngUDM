/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DenListenerFactoryTest.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.event.listener;
import java.io.Serializable;
import java.util.Date;
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
import com.smartstreamrdu.domain.ProcessingStatus;
import com.smartstreamrdu.domain.message.UdlMessage;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.ReloadIgniteCache;
import com.smartstreamrdu.persistence.domain.UdlMetrics;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.repository.service.UdlMetricsRepositoryService;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.service.event.process.listener.EventListenerFactory;
import com.smartstreamrdu.util.Constant.DomainStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DenListenerFactoryTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private DataRetrievalService retrieve;

	@Autowired
	private EventListenerFactory factory;
	
	@Autowired
	private UdlMetricsRepositoryService repo;
	
	@Value("${mongoDBUri}")
	private String mongoDbUri;
	
	@Autowired
	ReloadIgniteCache reloadCache;
	
	@Before
	public  void initialize(){
	 System.setProperty("spark.mongodb.input.uri", mongoDbUri + ".sdData");
	}

	@Test
	@InputCollectionsPath(paths = { "DenListenerFactoryTest/testIdcApex/sdData.json" })
	@ModifiedCollections(collections = {"sdData","udlMetrics"})
	public void testIdcApex() throws Exception {
		String cacheName = DataLevel.DV_DOMAIN_MAP.getCollectionName();
		reloadCache.reloadCache(cacheName);

		List<DenProcessListener> applicableListeners = factory.getApplicableListeners("idcApex","inactivationBasedOnFullLoad");

		addUdlMericsEntry();
		
		Map<String, Serializable> eventAttributes = new HashMap<String, Serializable>();
		eventAttributes.put("dataSource", "idcApex");
		
		EventMessage msg = new EventMessage();
		msg.setEventName("inactivationBasedOnFullLoad");
		msg.setEventAttributes(eventAttributes);
		
		applicableListeners.forEach(listn -> listn.onDenEventReceived(msg));

		DataContainer con = retrieveDbContainer();
		DataContainer secCon = con.getAllChildDataContainers().get(0);
		DomainType status = secCon.getHighestPriorityValue(SecurityAttrConstant.SECURITY_STATUS);

		Assert.assertNotNull(status.getNormalizedValue());
		Assert.assertEquals(DomainStatus.INACTIVE, status.getNormalizedValue());
	}

	private void addUdlMericsEntry() {
		UdlMetrics metrics = new UdlMetrics();
		
		UdlMessage msg = new UdlMessage();
		msg.setFileName("UDM-52840_init_1.xml");
		msg.setDataSource("idcApex");
		metrics.setMessage(msg);
		
		metrics.setProcessingStatus(ProcessingStatus.S);
	    Date date = new Date();
		metrics.setStartDate(date);
		
		repo.saveUdlMetrics(metrics);
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
