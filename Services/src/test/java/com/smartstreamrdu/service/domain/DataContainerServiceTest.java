/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DataContainerServiceTest.java
 * Author :SaJadhav
 * Date : 08-Sep-2020
 */
package com.smartstreamrdu.service.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.domain.SdRawData;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.util.Constant;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DataContainerServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	private static final DataAttribute ISIN_ATTR=DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
	private static final DataAttribute NAMELONG_ATTR=DataAttributeFactory.getAttributeByNameAndLevel("nameLong", DataLevel.INS);
	private static final DataAttribute TRQUOTEID_ATTR=DataAttributeFactory.getAttributeByNameAndLevel("trQuoteId", DataLevel.SEC);
	private static final DataAttribute LIPPERID_ATTR=DataAttributeFactory.getAttributeByNameAndLevel("lipperId", DataLevel.INS);
	private static final DataAttribute RIC_ATTR=DataAttributeFactory.getAttributeByNameAndLevel("ric", DataLevel.SEC);
	private static final DataAttribute INSTRUMENTID_ATTR =DataAttributeFactory.getAttributeByNameAndLevel("_instrumentId", DataLevel.INS);
	private static final DataAttribute SECURITYID_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("_securityId", DataLevel.SEC);
	
	@Autowired
	private BsonConverter bsonConverter;
	
	@Test
	@InputCollectionsPath(paths = {"DataContainerServiceTest/sdData.json"})
	@ModifiedCollections(collections = {"sdData"})
	public void testGetInsInsRelations() throws UdmTechnicalException, IOException {
		DataContainer container=bsonConverter.getListOfDataContainersFromFilePath("DataContainerServiceTest/sdData_overlying.json", SdData.class).get(0);
		List<DataAttribute> outputFields=Arrays.asList(ISIN_ATTR,NAMELONG_ATTR,TRQUOTEID_ATTR,RIC_ATTR,INSTRUMENTID_ATTR,SECURITYID_ATTR);
		DataContainer insInsRelation = DataContainerService.getInsInsRelation(RelationType.Underlying.name(), container,outputFields);
		assertNotNull(insInsRelation);
		
		String underlyingIsin = insInsRelation.getHighestPriorityValue(ISIN_ATTR);
		
		assertEquals("INF740KA1AE4", underlyingIsin);
		
		String underlyingNameLong=insInsRelation.getHighestPriorityValue(NAMELONG_ATTR);
		assertEquals("DSP FMP-Srs 204-37M-Dividend", underlyingNameLong);
		String lipperid=insInsRelation.getHighestPriorityValue(LIPPERID_ATTR);
		assertNull(lipperid);
		
		List<DataContainer> underlyingChildContainers = insInsRelation.getChildDataContainers(DataLevel.SEC);
		assertEquals(1, underlyingChildContainers.size());
		String underlyingRic = underlyingChildContainers.get(0).getHighestPriorityValue(TRQUOTEID_ATTR);
		assertEquals("0x00100b00144ab9f6", underlyingRic);
		
	}
	
	@Test
	@InputCollectionsPath(paths = {"DataContainerServiceTest/sdRawData.json"})
	@ModifiedCollections(collections = {"sdRawData"})
	public void testRawdata() throws UdmTechnicalException, IOException {
		DataContainer container=bsonConverter.getListOfDataContainersFromFilePath("DataContainerServiceTest/sdRawData.json", SdRawData.class).get(0);
		DataValue<?> jsonString = (DataValue<?>) container.getAttributeValue(DataAttributeFactory
				.getAttributeByNameAndLevel(Constant.SdAttributeNames.RAW_DATA_RECORD_ATTRIBUTE, DataLevel.SD_RAW_DATA));
		JSONObject obj = JsonConverterUtil.convertToSimpleJson(jsonString.getValue().toString());
		assertTrue(obj.containsKey("ASSET_ID"));
		assertTrue(obj.containsKey("Symbol_Cross_Reference"));
	}
	
	
	@Test
	@InputCollectionsPath(paths = {"DataContainerServiceTest/LookupLevelAttributeTest/sdData.json"})
	@ModifiedCollections(collections = {"sdData"})
	public void testGetInsInsRelations_withLookupLevel() throws UdmTechnicalException, IOException {
		DataContainer container=bsonConverter.getListOfDataContainersFromFilePath("DataContainerServiceTest/LookupLevelAttributeTest/sdData_overlying.json", SdData.class).get(0);
		List<DataAttribute> outputFields=Arrays.asList(ISIN_ATTR,NAMELONG_ATTR,TRQUOTEID_ATTR,RIC_ATTR,INSTRUMENTID_ATTR,SECURITYID_ATTR);
		DataContainer insInsRelation = DataContainerService.getInsInsRelation(RelationType.Underlying.name(), container,outputFields);
		assertNotNull(insInsRelation);
		
		String underlyingIsin = insInsRelation.getHighestPriorityValue(ISIN_ATTR);
		
		assertEquals("INF740KA1AE4", underlyingIsin);
		
		String underlyingNameLong=insInsRelation.getHighestPriorityValue(NAMELONG_ATTR);
		assertEquals("DSP FMP-Srs 204-37M-Dividend", underlyingNameLong);
		String lipperid=insInsRelation.getHighestPriorityValue(LIPPERID_ATTR);
		assertNull(lipperid);
		
		List<DataContainer> underlyingChildContainers = insInsRelation.getChildDataContainers(DataLevel.SEC);
		assertEquals(0, underlyingChildContainers.size());
	}

}
