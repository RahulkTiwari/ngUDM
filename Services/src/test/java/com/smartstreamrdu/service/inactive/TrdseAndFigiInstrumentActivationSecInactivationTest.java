/**
 * 
 */
package com.smartstreamrdu.service.inactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.repository.FeedConfigurationRepository;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.service.persist.MergeAndPersistService;
import com.smartstreamrdu.util.DataSourceConstants;
import com.smartstreamrdu.util.SdDataAttributeConstant;

/**
 * test figi an      
 * 
 * @author RKaithwas
 *
 */
@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class TrdseAndFigiInstrumentActivationSecInactivationTest extends AbstractEmbeddedMongodbJunitParent {
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
	@InputCollectionsPath(paths = { "FigiInsActivationSecInactivation/dbContainer/sdData.json" })
	@ModifiedCollections(collections = { "sdData" })
	public void testFigiActiveInstrumentInactiveSecurity() throws Exception {

		List<DataContainer> db = bsonConverter.getListOfDataContainersFromFilePath(
				"FigiInsActivationSecInactivation/dbContainer/sdData.json", SdData.class);
		DataContainer feed = bsonConverter.getListOfDataContainersFromFilePath(
				"FigiInsActivationSecInactivation/feedContainer/sdData.json", SdData.class).get(0);
		service.mergeAndPersistSd(DataSourceConstants.FIGI_DS, feed, db,
				repo.findByDatasourceAndFileType("figi", "figi"));

		List<DataContainer> child = db.get(0).getAllChildDataContainers();
		DataContainer technicalSecurity = getTechnicalSecurity(child);

		assertNotNull(technicalSecurity);

		String technicalSecurityStatus = getSecurityStatus(technicalSecurity);
		assertEquals("A", technicalSecurityStatus);
		// There are two security in this test case. 1. technical 2. regular.
		DataContainer otherSecurity = child.stream().filter(ch -> !isTechnicalSecurity(ch)).findFirst().get();
		// this security is inactive
		assertEquals("0", getSecurityStatus(otherSecurity));

	}

	@Test
	@InputCollectionsPath(paths = { "TrdseInsActivationSecInactivation/dbContainer/sdData.json" })
	@ModifiedCollections(collections = { "sdData" })
	public void testTrdseActiveInstrumentInactiveSecurity() throws Exception {

		List<DataContainer> db = bsonConverter.getListOfDataContainersFromFilePath(
				"TrdseInsActivationSecInactivation/dbContainer/sdData.json", SdData.class);
		DataContainer feed = bsonConverter.getListOfDataContainersFromFilePath(
				"TrdseInsActivationSecInactivation/feedContainer/sdData.json", SdData.class).get(0);
		service.mergeAndPersistSd(DataSourceConstants.REUTERS_DS, feed, db,
				repo.findByDatasourceAndFileType("trdse", "XE"));

		List<DataContainer> child = db.get(0).getAllChildDataContainers();
		DataContainer technicalSecurity = getTechnicalSecurity(child);

		assertNotNull(technicalSecurity);

		String technicalSecurityStatus = getSecurityStatus(technicalSecurity);
		assertEquals("A", technicalSecurityStatus);
		// There are two security in this test case. 1. technical 2. regular.
		DataContainer otherSecurity = child.stream().filter(ch -> !isTechnicalSecurity(ch)).findFirst().get();
		// this security is inactive
		assertEquals("0", getSecurityStatus(otherSecurity));

	}

	/**
	 * Test reuters with 1 instrument having two securities. in db we have both ins and securites in active status.
	 * <br>in this case when the feed container consist of active instrument and only 1 inactive security then technical security should not get created.
	 * 
	 * @throws Exception
	 */
	@Test
	@InputCollectionsPath(paths = { "TrdseInsActivationSecInactivation/multi/dbContainer/sdData.json" })
	@ModifiedCollections(collections = { "sdData" })
	public void testTrdseActiveInstrumentInactiveMutlipleSecurity() throws Exception {

		List<DataContainer> db = bsonConverter.getListOfDataContainersFromFilePath(
				"TrdseInsActivationSecInactivation/multi/dbContainer/sdData.json", SdData.class);
		DataContainer feed = bsonConverter.getListOfDataContainersFromFilePath(
				"TrdseInsActivationSecInactivation/multi/feedContainer//sdData.json", SdData.class).get(0);
		service.mergeAndPersistSd(DataSourceConstants.REUTERS_DS, feed, db,
				repo.findByDatasourceAndFileType("trdse", "XE"));

		List<DataContainer> child = db.get(0).getAllChildDataContainers();
		DataContainer technicalSecurity = getTechnicalSecurity(child);

		assertNull(technicalSecurity);

	}

	private DataContainer getTechnicalSecurity(List<DataContainer> chlidContainers) {
		for (DataContainer container : chlidContainers) {
			if (isTechnicalSecurity(container)) {
				return container;
			}
		}
		return null;
	}

	private boolean isTechnicalSecurity(DataContainer container) {
		DomainType secType = (DomainType) container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		if (secType != null) {
			String secTypeVal = secType.getNormalizedValue();
			return StringUtils.isNotEmpty(secTypeVal) && secTypeVal.equals("Technical");
		}
		return false;
	}

	private String getSecurityStatus(DataContainer childContainer) {
		DomainType statusValue = (DomainType) childContainer
				.getHighestPriorityValue(SdDataAttributeConstant.SEC_STATUS);
		if (statusValue != null) {
			return statusValue.getVal() != null ? statusValue.getVal() : statusValue.getNormalizedValue();
		}

		return null;
	}
}
