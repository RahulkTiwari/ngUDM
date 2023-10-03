/**
 * 
 */
package com.smartstreamrdu.service.inactive;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.repository.FeedConfigurationRepository;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.service.persist.MergeAndPersistService;
import com.smartstreamrdu.util.DataSourceConstants;

/**
 * test rdu inactivation date attribute is populated or not.
 * 
 * @author RKaithwas
 *
 */
@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class RduInactivationDateAttributePopulationTest extends AbstractEmbeddedMongodbJunitParent {
	@Autowired
	private MergeAndPersistService service;

	@Autowired
	BsonConverter bsonConverter;
	@Autowired
	FeedConfigurationRepository repo;

	@Autowired
	private ProducerFactory producerFactory;
	

	/*
	 * /**
	 * 
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() throws Exception {
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message message) throws Exception {
				return null;
			}

			@Override
			public Object sendMessageSync(Message message) throws Exception {
				return message;
			}

		});
	}

	@Test
	@InputCollectionsPath(paths = { "RduInactivationDateAttributePopulationTest/dbContainer/sdData.json" })
	@ModifiedCollections(collections = { "sdData" })
	public void testTrdseActiveInstrumentInactiveSecurity() throws Exception {
		List<DataContainer> db = bsonConverter.getListOfDataContainersFromFilePath(
				"RduInactivationDateAttributePopulationTest/dbContainer/sdData.json", SdData.class);
		DataContainer feed = bsonConverter.getListOfDataContainersFromFilePath(
				"RduInactivationDateAttributePopulationTest/feedContainer/sdData.json", SdData.class).get(0);
		service.mergeAndPersistSd(DataSourceConstants.REUTERS_DS, feed, db,
				repo.findByDatasourceAndFileType("trdse", "XE"));

		Serializable rduInactivationDate = db.get(0)
				.getAttributeValue(InstrumentAttrConstant.RDU_INACTIVATION_DATE);
		assertNotNull(rduInactivationDate);

	}


}
