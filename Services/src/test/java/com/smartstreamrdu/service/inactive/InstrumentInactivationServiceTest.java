/**
 * 
 */
package com.smartstreamrdu.service.inactive;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.apache.kafka.clients.producer.internals.FutureRecordMetadata;
import org.apache.kafka.common.record.RecordBatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.service.kafka.KafkaProducer;
import com.smartstreamrdu.service.lookup.LookupService;
import com.smartstreamrdu.util.Constant;

/**
 * @author ViKumar
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { InstrumentInactivationServiceTestConfig.class })
public class InstrumentInactivationServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	
	@Autowired
	private LookupService lookupService;
	
	@Autowired
	private InstrumentInactivationService instrumentInactivationService;
	
	@Autowired
	@Qualifier("KafkaProducer")
	private KafkaProducer kafkaProducer;
	
	@Before
	public void init() throws Exception {
		Mockito.when(kafkaProducer.sendMessage(Mockito.any()))
				.thenReturn(new FutureRecordMetadata(null, 0, RecordBatch.NO_TIMESTAMP, 0L, 0, 0, null));
	}
	
	
	@Test
	@InputCollectionsPath(paths={"InstrumentInactivation/trdse/"})
	@ModifiedCollections(collections="sdData")
	public void testInactivateRecordsWithChildContainer() throws UdmBaseException {
		
		String expectedNormalizedValue = "I";
		DataContainer dataContainerFromDb = lookupService.getDbDataContainersByObjectId("8e2012e509a36d4855ae5db5", DataLevel.INS).get(0);
		instrumentInactivationService.inactivateInstruments(dataContainerFromDb, LockLevel.ENRICHED, Constant.Component.DATA_ENRICHMENT.name(), "trdse");
		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
		DataValue<Serializable> attributeValue = dataContainerFromDb.getDataRow().getAttributeValue(instrumentStatusAttribute);
		DomainType value = (DomainType) attributeValue.getValue(LockLevel.ENRICHED);
		String actualNormalizedValue = value.getNormalizedValue();
		assertEquals(expectedNormalizedValue, actualNormalizedValue);
		DataContainer childDataContainer = dataContainerFromDb.getChildDataContainers(DataLevel.SEC).get(0);
		DataAttribute securityStatus=DataAttributeFactory.getAttributeByNameAndLevel("securityStatus", DataLevel.SEC);
		DomainType highestPriorityValueAtSecStatus = (DomainType)childDataContainer.getHighestPriorityValue(securityStatus);
		assertEquals(expectedNormalizedValue, highestPriorityValueAtSecStatus.getNormalizedValue());
	}
	
	@Test
	@InputCollectionsPath(paths={"InstrumentInactivation/trdseWithoutChild/"})
	@ModifiedCollections(collections="sdData")
	public void testInactivateRecord() throws UdmBaseException {
		String expectedNormalizedValue = "I";
		DataContainer dataContainerFromDb = lookupService.getDbDataContainersByObjectId("8e2012a509a36d4855ae5d44",DataLevel.INS).get(0);
		instrumentInactivationService.inactivateInstruments(dataContainerFromDb, LockLevel.ENRICHED, Constant.Component.DATA_ENRICHMENT.name(), "asbIsin");
		DataAttribute instrumentStatusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
		DataValue<Serializable> attributeValue = dataContainerFromDb.getDataRow().getAttributeValue(instrumentStatusAttribute);
		DomainType value = (DomainType) attributeValue.getValue(LockLevel.ENRICHED);
		String actualNormalizedValue = value.getNormalizedValue();
		assertEquals(expectedNormalizedValue, actualNormalizedValue);
		
	}
	
}
