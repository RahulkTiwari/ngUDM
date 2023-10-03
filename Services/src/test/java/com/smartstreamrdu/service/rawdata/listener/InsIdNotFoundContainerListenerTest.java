/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FilteredInactiveProcessListenerTest.java
 * Author:	Padgaonkar S
 * Date:	10-Jan-2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.event.inactive.filter.InsIdNotFoundFilteredInactiveContainerEvent;
import com.smartstreamrdu.service.event.inactive.filter.InsIdNotFoundInactiveContainerListenerInput;
import com.smartstreamrdu.service.event.inactive.filter.InsIdNotFoundInactiveContainerProcessListener;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant.DomainStatus;

@Profile("FilteredInactiveProcessListenerTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class InsIdNotFoundContainerListenerTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private ProducerFactory producerFactory;

	@Autowired
	private InsIdNotFoundInactiveContainerProcessListener listnr;

	private Message mesage;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks() {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				mesage = message;
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}

	@Test
	public void test_SecurityContainer() {
		List<DataContainer> secCons = new ArrayList<>();

		DataValue<DomainType> status = new DataValue<>();
		status.setValue(LockLevel.ENRICHED, new DomainType(null, null, DomainStatus.INACTIVE));
		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.SEC);
		DataValue<String> sourceUniqueId = new DataValue<>();
		sourceUniqueId.setValue(LockLevel.FEED, "sourceUniqueId");
		secContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC),
				sourceUniqueId);

		secContainer.addAttributeValue(SecurityAttrConstant.SECURITY_STATUS, status);

		secCons.add(secContainer);
		InsIdNotFoundInactiveContainerListenerInput input = InsIdNotFoundInactiveContainerListenerInput.builder()
				.dataSource("idcApex").securityDataContainers(secCons).build();
		EventConfiguration config = new EventConfiguration();
		listnr.setConfig(config);
		listnr.register(InsIdNotFoundFilteredInactiveContainerEvent.ON_INACTIVE_SECURITY_FILTER.name());
		listnr.onFilteredInactiveContainer(input);

		EventMessage actualMessage = JsonConverterUtil.convertFromJson(mesage.getData(), EventMessage.class);
		Assert.assertNotNull(actualMessage);
		Assert.assertEquals( "FilteredContainerInactivation",actualMessage.getEventName());
		Assert.assertEquals("RECORD_LEVEL_EVENT",actualMessage.getEventType().name());

	}

	@Test
	public void test_InsContainer() {
		List<DataContainer> insCons = new ArrayList<>();
		DataContainer insContainer = DataContainerTestUtil.getDataContainer(DataLevel.INS);

		insCons.add(insContainer);
		InsIdNotFoundInactiveContainerListenerInput input = InsIdNotFoundInactiveContainerListenerInput.builder()
				.dataSource("idcApex").securityDataContainers(insCons).build();
		EventConfiguration config = new EventConfiguration();
		listnr.setConfig(config);
		listnr.register(InsIdNotFoundFilteredInactiveContainerEvent.ON_INACTIVE_SECURITY_FILTER.name());
		listnr.onFilteredInactiveContainer(input);

		Assert.assertNull(mesage);

	}

}
