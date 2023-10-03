/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoLockRemovalEventTest.java
* Author : VRamani
* Date : Mar 12, 2019
* 
*/
package com.smartstreamrdu.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.message.IvoLockRemovalMessage;
import com.smartstreamrdu.service.events.IvoLockRemovalEvent;
import com.smartstreamrdu.service.events.UpdateChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;

/**
* @author VRamani
*
*/
@RunWith(MockitoJUnitRunner.class)
public class IvoLockRemovalEventTest {
	
	@Mock
	private ProducerFactory producerFactory;
	
	@InjectMocks
	private IvoLockRemovalEvent ivoLockRemovalEvent;
	
	private Message ivoLockRemovalMessage;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks(){
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(
				new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				ivoLockRemovalMessage=message;
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

			
		});
	}
	
	@Test
	public void testIsEventApplicable(){
		assertTrue(ivoLockRemovalEvent.isEventApplicable(ListenerEvent.DataUpdate));
	}
	
	@Test
	public void testIsEventApplicableNegative(){
		assertTrue(!ivoLockRemovalEvent.isEventApplicable(ListenerEvent.MergeComplete));
	}
	
	@Test
	public void testCreateInput() {
		DataContainer postChangeDataContainer = createDataContainer();
		
		UpdateChangeEventListenerInputCreationContext input1 = new UpdateChangeEventListenerInputCreationContext();
		input1.setDbDataContainer(postChangeDataContainer);
		
		ChangeEventInputPojo input = ivoLockRemovalEvent.createInput(input1);
		
		assertEquals(postChangeDataContainer, input.getPostChangeContainer());
	}
	
	@Test
	public void testPropogateEvent() {
		ChangeEventInputPojo input = new ChangeEventInputPojo();
		input.setPostChangeContainer(createDataContainer());
		
		ivoLockRemovalEvent.propogateEvent(input);
		
		assertNotNull(ivoLockRemovalMessage);
		
		IvoLockRemovalMessage message = JsonConverterUtil.convertFromJson(ivoLockRemovalMessage.getData(), IvoLockRemovalMessage.class);
		
		assertEquals("trdse", message.getDataSource());
		assertEquals("exampleId", message.getDocumentId());
		assertEquals("instrumentId", message.getObjectId());
	}

	/**
	 * @return container
	 */
	private DataContainer createDataContainer() {
		DataContainer container = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		container.set_id("exampleId");
		DataValue<DomainType> dataSourceDataValue = new DataValue<>();
		DomainType dataSourceDomainType = new DomainType("trdse");
		dataSourceDataValue.setValue(LockLevel.FEED, dataSourceDomainType);
		container.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), dataSourceDataValue);
		
		DataValue<String> instrumentIdDataValue = new DataValue<>();
		instrumentIdDataValue.setValue(LockLevel.FEED, "instrumentId");
		container.setHasChanged(true);
		container.setNew(false);
		
		container.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.INS), instrumentIdDataValue);
		
		return container;
	}

}
