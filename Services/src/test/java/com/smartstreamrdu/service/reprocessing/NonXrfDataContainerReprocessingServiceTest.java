/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : NonXrfDataContainerReprocessingServiceTest.java
 * Author :SaJadhav
 * Date : 08-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.EnData;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class NonXrfDataContainerReprocessingServiceTest {
	
	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Autowired
	private NonXrfDataContainerReprocessingService nonXrfDataContainerReprocessingService;
	
	private Message kafkaMessage;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void initMocks() {
		kafkaMessage=null;
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				kafkaMessage = message;
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}
	
	@Test
	public void testReprocesDataContainer() throws IOException, UdmBaseException {
		List<DataContainer> listOfDataContainers = bsonConverter.getListOfDataContainersFromFilePath("NonXrfDataContainerReprocessingServiceTest/input/enData.json", EnData.class);
		DataContainer dataContainer=listOfDataContainers.get(0);
		LocalDateTime staticDataUpdateDate=LocalDateTime.of(2021, 06, 07, 12, 30);
		nonXrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, null, null);
		assertNotNull(kafkaMessage);
		ProformaMessage proformaMessage=JsonConverterUtil.convertFromJson(kafkaMessage.getData(), ProformaMessage.class);
		assertEquals("rduEns", proformaMessage.getDataSource());
		
		assertEquals("ABC.1.1234", proformaMessage.getUniqueAttributeVsValueMap().get("eventSourceUniqueId"));
	}
	
	@Test
	public void testReprocessDcForNonEnDs() throws UdmBaseException {
		DataContainer dataContainer=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		LocalDateTime staticDataUpdateDate=LocalDateTime.of(2021, 06, 07, 12, 30);
		nonXrfDataContainerReprocessingService.reprocesDataContainer(dataContainer, staticDataUpdateDate, null, null);
		assertNull(kafkaMessage);
	}

}
