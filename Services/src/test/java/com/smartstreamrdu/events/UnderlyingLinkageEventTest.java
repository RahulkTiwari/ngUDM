/**
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: UnderlyingLinkageEventTest.java
 * Author : SaJadhav
 * Date : 28-Jan-2019
 * 
 */
package com.smartstreamrdu.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.LookupAttributes;
import com.smartstreamrdu.domain.message.LinkageMessage;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.events.UnderlyingLinkageEvent;
import com.smartstreamrdu.service.events.UpdateChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;

/**
 * @author SaJadhav
 *
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class UnderlyingLinkageEventTest extends AbstractEmbeddedMongodbJunitParent{
	
	private static DataAttribute dataSourceAttr=DataAttributeFactory.getDatasourceAttribute(DataLevel.INS);
	private static DataAttribute cfiCodeAttr =InstrumentAttrConstant.CFI_CODE;
	
	@Mock
	private ProducerFactory producerFactory;
	
	@Mock
	private CacheDataRetrieval cacheDataRetrieval;

	
	@InjectMocks
	private UnderlyingLinkageEvent underlyingLinkageEvent;
	
	private Message linkageMessage;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks(){
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(
				new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				linkageMessage=message;
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

			
		});
		
		Mockito.when(cacheDataRetrieval.getDataStorageFromDataSource("trdse")).thenReturn(DataStorageEnum.SD);
	}
	
	@Test
	public void test_isEventApplicable(){
		assertTrue(underlyingLinkageEvent.isEventApplicable(ListenerEvent.DataUpdate));
	}
	
	@Test
	public void test_isEventApplicable_negative(){
		assertTrue(!underlyingLinkageEvent.isEventApplicable(ListenerEvent.MergeComplete));
	}
	
	@Test
	public void test_createInput(){
		String cfiCode="2356";
		String dataSource="trdse";
		DataContainer preChangeContainer=createDataContainer(cfiCode,dataSource);
		DataContainer postChangeContainer=createDataContainer("6631", dataSource);
		FeedConfiguration feedConfig=new FeedConfiguration();
		feedConfig.setFeedName("trds");
		feedConfig.setDatasource(dataSource);
		Map<String, Serializable> eventContext=new HashMap<>();
		
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		input.setDbDataContainer(postChangeContainer);
		input.setFeedDataContaier(preChangeContainer);
		input.setFeedConfiguration(feedConfig);
		
		ChangeEventInputPojo changeEventPojo = underlyingLinkageEvent.createInput(input);
		
		
		assertEquals(preChangeContainer, changeEventPojo.getPreChangeContainer());
		assertEquals(postChangeContainer, changeEventPojo.getPostChangeContainer());
		assertEquals(feedConfig, changeEventPojo.getFeedConfiguration());
	}

	/**
	 * @param cfiCode
	 * @param dataSource
	 * @return
	 */
	private DataContainer createDataContainer(String cfiCode, String dataSource) {
		DataContainer container=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DataValue<DomainType> dataSourceVal=new DataValue<>();
		DomainType dataSourceDomain=new DomainType(dataSource);
		dataSourceVal.setValue(LockLevel.FEED, dataSourceDomain);
		container.addAttributeValue(dataSourceAttr, dataSourceVal);
		
		DataValue<String> cfiCodeVal=new DataValue<>();
		cfiCodeVal.setValue(LockLevel.FEED, cfiCode);
		container.addAttributeValue(cfiCodeAttr, cfiCodeVal);
		return container;
	}
	
	@Test
	public void test_propogateEvent(){
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		DataContainer postChangeContainer=getDataContainerWithSecs();
		changeEventPojo.setPostChangeContainer(postChangeContainer);
		FeedConfiguration feedConfiguration=new FeedConfiguration();
		List<LookupAttributes> lookupAttributesList=new ArrayList<>();
		LookupAttributes lookUpAttr=new LookupAttributes();
		lookUpAttr.setLevel("INS");
		List<List<String>> attributesList=new ArrayList<>();
		List<String> attributes=new ArrayList<>();
		attributes.add("trQuoteId");
		attributesList.add(attributes);
		lookUpAttr.setAttributes(attributesList);
		lookupAttributesList.add(lookUpAttr);
		feedConfiguration.setLookupAttributes(lookupAttributesList);
		changeEventPojo.setFeedConfiguration(feedConfiguration);
		underlyingLinkageEvent.propogateEvent(changeEventPojo);
		
		assertNotNull(linkageMessage);
		LinkageMessage json = JsonConverterUtil.convertFromJson(linkageMessage.getData(), LinkageMessage.class);
		assertEquals("trdse", json.getDataSource());
		assertEquals("12345678", json.getDocumentId());
		assertEquals("36952", json.getObjectId());
		List<Map<String,Serializable>> lookUpAttrList=new ArrayList<>();
		Map<String, Serializable> map=new HashMap<>();
		map.put("trQuoteId", "TR.2");
		lookUpAttrList.add(map);
		assertEquals(lookUpAttrList, json.getLookupAttributes());
	}
	
	@Test
	public void test_propogateEvent_1(){
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		DataContainer postChangeContainer=getDataContainerWithSecsNoAttributeValue();
		changeEventPojo.setPostChangeContainer(postChangeContainer);
		FeedConfiguration feedConfiguration=new FeedConfiguration();
		List<LookupAttributes> lookupAttributesList=new ArrayList<>();
		LookupAttributes lookUpAttr=new LookupAttributes();
		lookUpAttr.setLevel("INS");
		List<List<String>> attributesList=new ArrayList<>();
		List<String> attributes=new ArrayList<>();
		attributes.add("securitySourceUniqueId");
		attributesList.add(attributes);
		lookUpAttr.setAttributes(attributesList);
		lookupAttributesList.add(lookUpAttr);
		feedConfiguration.setLookupAttributes(lookupAttributesList);
		changeEventPojo.setFeedConfiguration(feedConfiguration);
		underlyingLinkageEvent.propogateEvent(changeEventPojo);
		
		assertNull(linkageMessage);
	}
	
	@Test
	public void test_propogateEvent_INS_Level(){
		ChangeEventInputPojo changeEventPojo=new ChangeEventInputPojo();
		DataContainer postChangeContainer=getDataContainerWithSecs();
		populateInstrumentSourceUniqueId(postChangeContainer);
		changeEventPojo.setPostChangeContainer(postChangeContainer);
		FeedConfiguration feedConfiguration=new FeedConfiguration();
		List<LookupAttributes> lookupAttributesList=new ArrayList<>();
		LookupAttributes lookUpAttr=new LookupAttributes();
		lookUpAttr.setLevel("INS");
		List<List<String>> attributesList=new ArrayList<>();
		List<String> attributes=new ArrayList<>();
		attributes.add("trQuoteId");
		attributes.add("instrumentSourceUniqueId");
		attributesList.add(attributes);
		lookUpAttr.setAttributes(attributesList);
		lookupAttributesList.add(lookUpAttr);
		
		feedConfiguration.setLookupAttributes(lookupAttributesList);
		changeEventPojo.setFeedConfiguration(feedConfiguration);
		underlyingLinkageEvent.propogateEvent(changeEventPojo);
		
		assertNotNull(linkageMessage);
		LinkageMessage json = JsonConverterUtil.convertFromJson(linkageMessage.getData(), LinkageMessage.class);
		assertEquals("trdse", json.getDataSource());
		assertEquals("12345678", json.getDocumentId());
		assertEquals("36952", json.getObjectId());
		List<Map<String,Serializable>> lookUpAttrList=new ArrayList<>();
		Map<String, Serializable> map=new HashMap<>();
		map.put("trQuoteId", "TR.2");
		map.put("instrumentSourceUniqueId", "12345");
		lookUpAttrList.add(map);
		assertEquals(lookUpAttrList, json.getLookupAttributes());
	}

	private void populateInstrumentSourceUniqueId(DataContainer postChangeContainer) {
		DataValue<String> dataValue = new DataValue<>();
		dataValue.setValue(LockLevel.FEED, "12345");
		postChangeContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), dataValue);
	}

	private DataContainer getDataContainerWithSecsNoAttributeValue() {
		DataContainer insContainer=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		insContainer.set_id("12345678");
		DataValue<DomainType> dataSourceVal=new DataValue<>();
		dataSourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		insContainer.addAttributeValue(dataSourceAttr, dataSourceVal);
		
		DataContainer secContainer=new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		secContainer.setNew(false);
		DataValue<String> securityIdVal=new DataValue<>();
		securityIdVal.setValue(LockLevel.FEED, "36952");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), securityIdVal);

		secContainer.setNew(true);
		
		insContainer.addDataContainer(secContainer, DataLevel.SEC);
		return insContainer;
	}

	/**
	 * @return
	 */
	private DataContainer getDataContainerWithSecs() {
		DataContainer insContainer=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		insContainer.set_id("12345678");
		DataValue<DomainType> dataSourceVal=new DataValue<>();
		dataSourceVal.setValue(LockLevel.FEED, new DomainType("trdse"));
		insContainer.addAttributeValue(dataSourceAttr, dataSourceVal);
		
		DataContainer secContainer=new DataContainer(DataLevel.SEC, DataContainerContext.builder().build());
		secContainer.setNew(false);
		DataValue<String> securityIdVal=new DataValue<>();
		securityIdVal.setValue(LockLevel.FEED, "36952");
		secContainer.addAttributeValue(DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC), securityIdVal);
		
		DataAttribute trQuoteIdAttr=DataAttributeFactory.getAttributeByNameAndLevel("trQuoteId", DataLevel.SEC);
		DataValue<String> trQuoteIdVal1=new DataValue<>();
		trQuoteIdVal1.setValue(LockLevel.FEED, "TR.1");
		trQuoteIdVal1.setValue(LockLevel.FEED, "TR.2");
		secContainer.addAttributeValue(trQuoteIdAttr,trQuoteIdVal1 );
		insContainer.addDataContainer(secContainer, DataLevel.SEC);
		return insContainer;
	}
	
	
}
