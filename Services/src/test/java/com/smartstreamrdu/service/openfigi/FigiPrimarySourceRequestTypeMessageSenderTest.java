package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.commons.openfigi.VfsFigiRequestTypeEnum;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.domain.OpenFigiRequestMetrics;
import com.smartstreamrdu.persistence.domain.VfsOriginalRequest;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;

@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class FigiPrimarySourceRequestTypeMessageSenderTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private FigiPrimarySourceRequestTypeMessageSender figiPrimarySourceRequestTypeMessageSender;
	
	private Message kafkaMessage;
	
	private ObjectMapper objectMapper;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

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
	public void testSendMsgToFigiReqQueueForSuccessRetry() throws JsonMappingException, JsonProcessingException {
		OpenFigiRequestMetrics figiRequestMetrics = new OpenFigiRequestMetrics();
		figiRequestMetrics.setDataSource("trdse");
		figiRequestMetrics.setOpenFigiIds(Arrays.asList("BBG0123"));
		ReferenceId id = new ReferenceId("1", "5fde33a04fcab955e533fc6a");
		figiRequestMetrics.setPrimarySourceReferenceId(id);
		figiRequestMetrics.setStatus("S");
		figiRequestMetrics.setRetriesAttempted(1L);
		figiRequestMetrics.setInsDate(LocalDateTime.now().minusDays(1));
		figiRequestMetrics.setUpdDate(LocalDateTime.now());
		VfsOriginalRequest originalRequest = new VfsOriginalRequest();
		originalRequest.setIsin("ISIN01");
		figiRequestMetrics.setOriginalRequest(originalRequest);
		figiRequestMetrics.setSuccessfulRequest("{\"idType\":\"ID_ISIN\",\"idValue\":\"KYG644742010\"}");
		figiPrimarySourceRequestTypeMessageSender.sendMessageToFigiRequestQueue(figiRequestMetrics, VfsFigiRequestTypeEnum.SUCCESS_RETRY);
		assertNotNull(kafkaMessage);
		VfsFigiRequestMessage figiMsg = objectMapper.readValue(kafkaMessage.getData(), VfsFigiRequestMessage.class);
		assertEquals(1, figiMsg.getRetryCount());
	}
	

	@Test
	public void testSendMsgToFigiReqQueueForInactivationAfterNoReply() throws JsonMappingException, JsonProcessingException {
		OpenFigiRequestMetrics figiRequestMetrics = new OpenFigiRequestMetrics();
		figiRequestMetrics.setDataSource("trdse");
		figiRequestMetrics.setOpenFigiIds(Arrays.asList("BBG0123"));
		ReferenceId id = new ReferenceId("1", "5fde33a04fcab955e533fc6a");
		figiRequestMetrics.setPrimarySourceReferenceId(id);
		figiRequestMetrics.setStatus("F");
		figiRequestMetrics.setInsDate(LocalDateTime.now().minusDays(1));
		figiRequestMetrics.setUpdDate(LocalDateTime.now());
		figiRequestMetrics.setRetriesAttempted(3L);
		VfsOriginalRequest originalRequest = new VfsOriginalRequest();
		originalRequest.setIsin("ISIN01");
		figiRequestMetrics.setOriginalRequest(originalRequest);
		figiRequestMetrics.setSuccessfulRequest("{\"idType\":\"ID_ISIN\",\"idValue\":\"KYG644742010\"}");
		figiPrimarySourceRequestTypeMessageSender.sendMessageToFigiRequestQueue(figiRequestMetrics, VfsFigiRequestTypeEnum.INACTIVATION_AFTER_NO_REPLY);
		assertNotNull(kafkaMessage);
		VfsFigiRequestMessage figiMsg = objectMapper.readValue(kafkaMessage.getData(), VfsFigiRequestMessage.class);
		assertEquals(2, figiMsg.getRetryCount());
	}

}
