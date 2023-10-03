/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	CrossReferenceDataSourceEvaluator.java
 * Author:	Ashok Thanage
 * Date:	06-Jan-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.xrf.XrfProcessMode;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.EventListenerConstants;
import com.smartstreamrdu.util.FeedConfigMessagePropagationType;

/**
 * @author Ashok Thanage
 *
 */

@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class XrfChangeEventListenerTest {


	private DataContainer dataContainer = null;
	private static final DataAttribute dataSourceAttribute = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
	private static final DataAttribute updateDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.updateDate, DataLevel.Document);
	private static final DataAttribute isinAttribute = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
	
	@Autowired
	private XrfChangeEventListener listener;
	
	@Autowired
	private ProducerFactory producerFactory;
	
	protected Message sendEvent ;
	
	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void init() throws Exception {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Future sendMessage(Message message) throws Exception {
				sendEvent = message;
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});

		
		DataContainerContext dataContainerContext = DataContainerContext.builder().withComment("first insert")
				.withProgram("NG-XRF").withUpdateBy("athanage").build();
		
		dataContainer = new DataContainer(DataLevel.INS, dataContainerContext);
		dataContainer.set_id("5d5ead25fd03c713883bdedd");
		
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		DomainType domainValue = new DomainType();
		domainValue.setVal("trdse");
		
		dataSourceValue.setValue(LockLevel.FEED, domainValue);
		dataContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);
		
		DataValue<LocalDateTime> updateDateValue = new DataValue<>();
		updateDateValue.setValue(LockLevel.RDU, LocalDateTime.of(2019, 8, 14, 12, 30));
		dataContainer.addAttributeValue(updateDateAttribute, updateDateValue);
		
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "XS0129477047");
		dataContainer.addAttributeValue(isinAttribute, isinValue);
		
		DataAttribute insStatusAttribute = InstrumentAttrConstant.INSTRUMENT_STATUS;
		DataValue<DomainType> insStatusAttributeValue = new DataValue<>();
		DomainType insStatusAttrdomainValue = new DomainType();
		insStatusAttrdomainValue.setVal("1");
		insStatusAttrdomainValue.setNormalizedValue("A");
		insStatusAttributeValue.setValue(LockLevel.RDU, insStatusAttrdomainValue);
		dataContainer.addAttributeValue(insStatusAttribute, insStatusAttributeValue);
		// Set security data container
		DataContainer childDataContainer = new DataContainer(DataLevel.SEC, null);

		DataAttribute secStatusAttribute = SecurityAttrConstant.SECURITY_STATUS;
		DataValue<DomainType> secStatusAttributeValue = new DataValue<>();
		DomainType secStatusAttrdomainValue = new DomainType();
		secStatusAttrdomainValue.setVal("1");
		secStatusAttrdomainValue.setNormalizedValue("A");
		secStatusAttributeValue.setValue(LockLevel.RDU, secStatusAttrdomainValue);
		childDataContainer.addAttributeValue(secStatusAttribute, secStatusAttributeValue);
		
		dataContainer.addDataContainer(childDataContainer, DataLevel.SEC);
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.events.XrfChangeEventListener#propogateEvent(com.smartstreamrdu.events.ChangeEventInputPojo)}.
	 */
	@Test
	public void testPropogateEvent() {
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		input.setDbDataContainer(dataContainer);
		listener.propogateEvent(listener.createInput(input));
		assertEquals("{\"action\":\"xrfDefault\",\"variableAttributeValue\":\"5d5ead25fd03c713883bdedd\"}", sendEvent.getKey());
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.events.XrfChangeEventListener#isEventApplicable(com.smartstreamrdu.service.listener.ListenerEvent)}.
	 */
	@Test
	public void testIsEventApplicable() {
		assertTrue(listener.isEventApplicable(ListenerEvent.DataUpdate));
	}
	
	@Test
	public void testIsEventApplicableNegative() {
		assertFalse(listener.isEventApplicable(ListenerEvent.DataContainerMerging));
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.events.XrfChangeEventListener#createInput(com.smartstreamrdu.service.events.ChangeEventListenerInputCreationContext)}.
	 */
	@Test
	public void testCreateInput() {
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		input.setDbDataContainer(dataContainer);
		ChangeEventInputPojo inputPojo = listener.createInput(input);
		assertNotNull(inputPojo);
		assertNotNull(inputPojo.getPostChangeContainer());
	}
	
	@Test
	public void testCreateInputWithXrfProcessMode() {
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		input.setDbDataContainer(dataContainer);
		input.setXrfProcessMode(XrfProcessMode.REEVALUATE);
		ChangeEventInputPojo inputPojo = listener.createInput(input);
		assertNotNull(inputPojo);
		assertNotNull(inputPojo.getPostChangeContainer());
		assertEquals(XrfProcessMode.REEVALUATE, inputPojo.getFromMessageContext(EventListenerConstants.XRF_PROCESS_MODE));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateInputNegative() {
		MergeCompleteChangeEventListenerInputCreationContext input = new MergeCompleteChangeEventListenerInputCreationContext();
		input.setDataContainer(dataContainer);
		listener.createInput(input);
	}
	
	@Test
	public void testPropogateEventRAW() {
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		DataContainer container = SerializationUtils.clone(dataContainer);
		input.setDbDataContainer(container);
		FeedConfiguration feedConfiguration = new FeedConfiguration();
		feedConfiguration.setMessagePropagationType(FeedConfigMessagePropagationType.RAW);
		input.setFeedConfiguration(feedConfiguration);
		container.setNew(false);
		container.setHasChanged(false);
		container.getChildDataContainers(DataLevel.SEC).forEach(sec -> sec.setHasChanged(false));
		
		listener.propogateEvent(listener.createInput(input));
		assertEquals("{\"action\":\"xrfDefault\",\"variableAttributeValue\":\"5d5ead25fd03c713883bdedd\"}", sendEvent.getKey());
		
		container.setNew(true);
		container.setHasChanged(true);
	}

	@Test
	public void testPropogateEventNONE() {
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		DataContainer container = SerializationUtils.clone(dataContainer);
		input.setDbDataContainer(container);
		FeedConfiguration feedConfiguration = new FeedConfiguration();
		feedConfiguration.setMessagePropagationType(FeedConfigMessagePropagationType.NONE);
		input.setFeedConfiguration(feedConfiguration);
		container.setNew(false);
		container.setHasChanged(false);
		container.getChildDataContainers(DataLevel.SEC).forEach(sec -> sec.setHasChanged(false));
		
		listener.propogateEvent(listener.createInput(input));
		assertNull(sendEvent);
		
		container.setNew(true);
		container.setHasChanged(true);
	}
}
