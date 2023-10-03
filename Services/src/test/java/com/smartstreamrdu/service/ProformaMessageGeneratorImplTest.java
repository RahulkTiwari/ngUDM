package com.smartstreamrdu.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.domain.ServiceTestConfiguration;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.service.xrf.messaging.ProformaMessageGenerator;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.DomainStatus;


/**
 * @author Dedhia
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfiguration.class })
public class ProformaMessageGeneratorImplTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private ProformaMessageGenerator proformaMessageGenerator;
	

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;
	
	private static final DataAttribute dataSourceAttribute = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
	private static final DataAttribute updateDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.updateDate, DataLevel.Document);
	private static final DataAttribute isinAttribute = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
	private static final DataAttribute leiAttribute = DataAttributeFactory.getAttributeByNameAndLevel("lei", DataLevel.LE);
	private static final DataAttribute LEGAL_ENTITY_STATUS = DataAttributeFactory.getAttributeByNameAndLevel("legalEntityStatus", DataLevel.LE);

	private DataContainer dataContainer = null;
	private DataContainer dataContainerLE = null;
	
	
	@Before
	public void setUp() throws Exception {
		testDataINS();
		testDataLE();
		DomainType activeDt = new DomainType("ACTIVE");
		activeDt.setDomain("entityStatusMap");
		List<DomainType> activeStatusDomain = new ArrayList<>();
		activeStatusDomain.add(activeDt);

		Mockito.when(cacheDataRetrieval.getVendorDomainValuesFromCache("statuses", "gleif", DomainStatus.ACTIVE)).thenReturn(activeStatusDomain);
		Mockito.when(cacheDataRetrieval.getDataStorageFromDataSource("gleifGc")).thenReturn(DataStorageEnum.SD);
		Mockito.when(cacheDataRetrieval.getDataStorageFromDataSource("esmaFirds")).thenReturn(DataStorageEnum.SD);
		Mockito.when(cacheDataRetrieval.getDataStorageFromDataSource("rduEns")).thenReturn(DataStorageEnum.EN);
		
		
	}
	
	
	private void testDataINS() {
		dataContainer = new DataContainer(DataLevel.INS, null);
		dataContainer.set_id("5d5ead25fd03c713883bdedd");
		
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		DomainType domainValue = new DomainType();
		domainValue.setVal("esmaFirds");
		
		dataSourceValue.setValue(LockLevel.RDU, domainValue);
		dataContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);
		
		DataValue<LocalDateTime> updateDateValue = new DataValue<>();
		updateDateValue.setValue(LockLevel.RDU, LocalDateTime.of(2019, 8, 14, 12, 30));
		dataContainer.addAttributeValue(updateDateAttribute, updateDateValue);
		
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "XS0129477047");
		dataContainer.addAttributeValue(isinAttribute, isinValue);

	}
		
	private void testDataLE() {
		dataContainerLE = new DataContainer(DataLevel.LE, null);
		dataContainerLE.set_id("5d5ead25fd03c713883bdedd");
		
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		DomainType domainValue = new DomainType();
		domainValue.setVal("gleifGc");
		
		dataSourceValue.setValue(LockLevel.RDU, domainValue);
		dataContainerLE.addAttributeValue(dataSourceAttribute, dataSourceValue);
		
		DataValue<LocalDateTime> updateDateValue = new DataValue<>();
		updateDateValue.setValue(LockLevel.RDU, LocalDateTime.of(2019, 8, 14, 12, 30));
		dataContainerLE.addAttributeValue(updateDateAttribute, updateDateValue);
		
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "C7J4FOV6ELAVE39B7M83");
		dataContainerLE.addAttributeValue(leiAttribute, isinValue);
		
		DataValue<DomainType> leStatusValue = new DataValue<>();
		DomainType domainleStatus = new DomainType();
		domainleStatus.setDomain("entityStatusMap");
		domainleStatus.setVal("ACTIVE");
		leStatusValue.setValue(LockLevel.RDU, domainleStatus);
		dataContainerLE.addAttributeValue(LEGAL_ENTITY_STATUS,leStatusValue);
	}

	@Test
	public void testGenerateMessage() {
		
		ChangeEventInputPojo pojo = new ChangeEventInputPojo();
		pojo.setPostChangeContainer(dataContainer);
		
		List<ProformaMessage> message = proformaMessageGenerator.generateMessage(pojo);
		
		
		Assert.assertNotNull(message);
		
		Assert.assertEquals("5d5ead25fd03c713883bdedd", message.get(0).getDocumentId());
		
		String jsonMessage = JsonConverterUtil.convertToJson(message.get(0));
		
		
		Assert.assertNotNull(jsonMessage);
		
		ProformaMessage jsonRecreatedMessage = JsonConverterUtil.convertFromJson(jsonMessage, ProformaMessage.class);
		
		
		Assert.assertNotNull(jsonRecreatedMessage);
	}
	
	
	@Test
	@InputCollectionsPath(paths={"ProformaMessageGeneratorImplTest/input"})
	@ModifiedCollections(collections={"sdData"})
	public void testGenerateMessageLE() {
		
		ChangeEventInputPojo pojo = new ChangeEventInputPojo();
		pojo.setPostChangeContainer(dataContainerLE);
		
		List<ProformaMessage> message = proformaMessageGenerator.generateMessage(pojo);
		
		Assert.assertNotNull(message);
		Assert.assertEquals("5d5ead25fd03c713883bdedd", message.get(0).getDocumentId());
		
		String jsonMessage = JsonConverterUtil.convertToJson(message.get(0));
		
		Assert.assertNotNull(jsonMessage);
		
		ProformaMessage jsonRecreatedMessage = JsonConverterUtil.convertFromJson(jsonMessage, ProformaMessage.class);

		Assert.assertNotNull(jsonRecreatedMessage);
		
		//child container message testing
		String childLEI = JsonConverterUtil.convertToJson(message.get(1));
		
		Assert.assertNotNull(childLEI);
		
		ProformaMessage jsonRecreatedMessage1 = JsonConverterUtil.convertFromJson(childLEI, ProformaMessage.class);
		
		
		Assert.assertNotNull(jsonRecreatedMessage1);
	}

}
