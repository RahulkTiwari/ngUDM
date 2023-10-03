/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : LegalEntityUpdateEventListenerTest.java
 * Author :SaJadhav
 * Date : 09-Jul-2020
 */
package com.smartstreamrdu.service.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.message.LeUpdateProformaProcessingMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant.Component;
import com.smartstreamrdu.util.Constant.Process;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes=ProducerFactoryMockConfig.class)
public class LegalEntityUpdateEventListenerTest extends AbstractEmbeddedMongodbJunitParent {
	
	private static final DataAttribute GICS_INDUSTRY_CODE_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("gicsIndustryCode", DataLevel.LE);

	private static final DataAttribute DATASOURCE = DataAttributeFactory.getDatasourceAttribute(DataLevel.LE);

	@Autowired
	private LegalEntityUpdateEventListener eventListener;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Autowired
	private ProducerFactory producerFactory;
	
	private Message leUpdateMessage;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks() {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				leUpdateMessage = message;
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
		assertTrue(eventListener.isEventApplicable(ListenerEvent.DataUpdate));
		assertFalse(eventListener.isEventApplicable(ListenerEvent.DataContainerMerging));
		assertFalse(eventListener.isEventApplicable(ListenerEvent.MergeComplete));
	}
	
	@Test
	public void testCreateInput() throws UdmTechnicalException, IOException{
		DataContainer dbContainer=getDbContainer();
		
		UpdateChangeEventListenerInputCreationContext inputCreationContext=new UpdateChangeEventListenerInputCreationContext();
		inputCreationContext.setDbDataContainer(dbContainer);
		ChangeEventInputPojo changeEventPojo = eventListener.createInput(inputCreationContext);
		assertNotNull(changeEventPojo.getPostChangeContainer());
		assertEquals("5eff066c2dd8674b19f58a89", changeEventPojo.getPostChangeContainer().get_id());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateInput_invalidInput() throws UdmTechnicalException, IOException{
		DataContainer dbContainer=getDbContainer();
		
		MergeCompleteChangeEventListenerInputCreationContext inputCreationContext=new MergeCompleteChangeEventListenerInputCreationContext();
		inputCreationContext.setDataContainer(dbContainer);;
		eventListener.createInput(inputCreationContext);
	}
	
	@Test
	public void testPropagateEvent_updatedContainer() throws UdmTechnicalException, IOException{
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		DataContainer postChangeContainer=getDbContainer();
		DataValue<String> gicsInsCodeDataValue=new DataValue<>();
		gicsInsCodeDataValue.setValue(LockLevel.FEED, "A2B1C1");
		postChangeContainer.addAttributeValue(GICS_INDUSTRY_CODE_ATTR, gicsInsCodeDataValue);
		
		changeEventPojo.setPostChangeContainer(postChangeContainer);
		eventListener.propogateEvent(changeEventPojo);
		assertNotNull(leUpdateMessage);
		assertEquals(Process.LE_UPDATE_PROFORMA_PROCESSING, leUpdateMessage.getProcess());
		assertEquals(Component.DATA_ENRICHMENT, leUpdateMessage.getTarget());
		String denMessage = leUpdateMessage.getData();
		LeUpdateProformaProcessingMessage convertFromJson = JsonConverterUtil.convertFromJson(denMessage, LeUpdateProformaProcessingMessage.class);
		assertEquals("trdse", convertFromJson.getDataSource());
		assertEquals("5eff066c2dd8674b19f58a89", convertFromJson.getDocumentId());
		assertEquals("64c3b3b0-27a3-4b73-acf4-44788744ea99", convertFromJson.getLegalEntityId());
	}
	
	@Test
	public void testPropagateEvent_unchangedContainer() throws UdmTechnicalException, IOException{
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		DataContainer postChangeContainer=getDbContainer();
		changeEventPojo.setPostChangeContainer(postChangeContainer);
		
		eventListener.propogateEvent(changeEventPojo);
		
		assertNull(leUpdateMessage);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testPropagateEvent_nullPostChangedContainer() throws UdmTechnicalException, IOException{
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		
		eventListener.propogateEvent(changeEventPojo);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testPropagateEvent_nullChangedEventPojo() throws UdmTechnicalException, IOException{
		
		eventListener.propogateEvent(null);
	}
	
	@Test
	public void testPropagateEvent_nonXrfDataSource() throws UdmTechnicalException, IOException{
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		DataContainer postChangeContainer=getDbContainer();
		DataValue<DomainType> dataSourceVal=new DataValue<>();
		dataSourceVal.setValue(LockLevel.FEED, new DomainType("gleifGc"));
		postChangeContainer.addAttributeValue(DATASOURCE, dataSourceVal);
		changeEventPojo.setPostChangeContainer(postChangeContainer);
		eventListener.propogateEvent(changeEventPojo);
		assertNull(leUpdateMessage);
	}
	
	@Test
	public void testPropagateEvent_nonLeDataContainer() throws UdmTechnicalException, IOException{
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		DataContainer postChangeContainer=new DataContainer(DataLevel.INS, null);
		DataValue<DomainType> dataSourceVal=new DataValue<>();
		dataSourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		postChangeContainer.addAttributeValue(DATASOURCE, dataSourceVal);
		changeEventPojo.setPostChangeContainer(postChangeContainer);
		eventListener.propogateEvent(changeEventPojo);
		assertNull(leUpdateMessage);
	}

	/**
	 * @return
	 * @throws IOException 
	 * @throws UdmTechnicalException 
	 */
	private DataContainer getDbContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> listOfDataContainersFromFilePath = bsonConverter
				.getListOfDataContainersFromFilePath("LegalEntityUpdateEventListenerTest/input/sdData.json", SdData.class);
		DataContainerUtil.populateNewFlagAndHasChanged(listOfDataContainersFromFilePath);
		DataContainer dataContainer = listOfDataContainersFromFilePath.get(0);
		return dataContainer;
	}
	
	@After
	public void cleanUp(){
		leUpdateMessage=null;
		
		
	}

}
